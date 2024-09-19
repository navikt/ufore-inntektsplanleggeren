#!/usr/bin/env node
import k8s from "@kubernetes/client-node";
const kc = new k8s.KubeConfig();
import autocomplete from 'inquirer-autocomplete-standalone';
import { select } from 'inquirer-select-pro';
import fs from 'fs/promises';
import { stringify } from "envfile";
import { spawn } from "child_process";

kc.loadFromDefault();

const inputNamespace = process.argv[2]
const inputDeployment = process.argv[3]
const inputSecretsToFetch = process.argv[4]?.split(',')

const k8sCore = kc.makeApiClient(k8s.CoreV1Api);
const k8sApi = kc.makeApiClient(k8s.AppsV1Api);

const chosenNamespace: string = inputNamespace ?? await k8sCore.listNamespace().then((res) => {
  const namespaces = res.body.items.map((namespace) => namespace.metadata?.name) as string[]
  return autocomplete({
    default: "pensjonselvbetjening",
    pageSize: 20,
    message: 'Choose namespace',
    source: async (input = '') => {
      return new Promise((resolve) => {
        const allNamespaces = namespaces.filter((namespace) => namespace.includes(input)).map((namespace) => {
          return {
            name: namespace,
            value: namespace,
            description: namespace,
            disabled: false
          }
        })
        resolve(allNamespaces)
      })
    }
  });
})


const chosenDeployment: string = inputDeployment ?? await k8sApi.listNamespacedDeployment(chosenNamespace).then((res) => {
  const deployments = res.body.items.map((namespace) => namespace.metadata?.name) as string[]
  return autocomplete({
    pageSize: 20,
    message: 'Choose deployment',
    source: async (input = '') => {
      return new Promise((resolve) => {
        const allDeployments = deployments.filter((namespace) => namespace.includes(input)).map((namespace) => {
          return {
            name: namespace,
            value: namespace,
            description: namespace,
            disabled: false
          }
        })
        resolve(allDeployments)
      })
    }
  });
})

// Ask for secrets to get
const secretsToFetch = inputSecretsToFetch ?? await select({
  message: 'Secrets to set to environment',
  multiple: true,
  defaultValue: ['azure', 'tokenx'],
  options: [
    { name: 'Azure', value: "azure" },
    { name: 'TokenX', value: "tokenx" },
  ]
})

const parsedSecrets = await k8sCore.listNamespacedSecret(chosenNamespace).then((res) => {
  const secrets = res.body.items;

  return secretsToFetch.reduce((acc, inputSecret) => {
    const secretsToParse = secrets.find((secret) => 
      secret.metadata?.name?.includes(`${inputSecret}-${chosenDeployment}`)
    )

    if(!secretsToParse?.data) {
      throw Error(`No secrets found for ${inputSecret}-${chosenDeployment}`)
    }

    const newParsedSecrets = Object.entries(secretsToParse.data).reduce((parsedSecrets, [key, value]) => {
      const decodedSecret = Buffer.from(value, "base64").toString("ascii")
      return ({
        ...parsedSecrets,
        [key]: decodedSecret
      })
    }, {})

    return {...acc, ...newParsedSecrets}
  }, {});
})

// TODO: Kanskje gjøre det mulig å lage env. fil i stedet

// const TEMP_FILE_PATH = '/tmp/nav-env-file'
// Save env-file to /tmp folder
// await fs.writeFile(TEMP_FILE_PATH, stringify(parsedSecrets)).then(() => {
//   console.log(`Env-file saved to ${TEMP_FILE_PATH}`)
// })
const shellCommand = process.env.SHELL
if(!shellCommand) {
  throw Error('No shell command found')
}
spawn(shellCommand, [], {stdio: 'inherit', env: {...process.env, ...parsedSecrets}})
