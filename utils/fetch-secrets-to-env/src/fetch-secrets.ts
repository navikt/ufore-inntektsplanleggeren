#!/usr/bin/env node
import * as k8s from "@kubernetes/client-node";
const kc = new k8s.KubeConfig();
import autocomplete from "inquirer-autocomplete-standalone";
import { select } from "inquirer-select-pro";
import fs from "fs/promises";
import { stringify } from "envfile";
import { spawn } from "child_process";

kc.loadFromDefault();

const inputNamespace = process.argv[2];
const inputDeployment = process.argv[3];
const inputSecretsToFetch = process.argv[4]?.split(",");
const envFileName = process.argv[5];

const k8sCore = kc.makeApiClient(k8s.CoreV1Api);
const k8sApi = kc.makeApiClient(k8s.AppsV1Api);

const chosenNamespace =
  inputNamespace ??
  (await k8sCore.listNamespace().then((res) => {
    const namespaces = res.items.map(
      (namespace) => namespace.metadata?.name,
    ) as string[];
    return autocomplete({
      default: "pensjonselvbetjening",
      pageSize: 20,
      message: "Choose namespace",
      source: async (input = "") => {
        return new Promise((resolve) => {
          const allNamespaces = namespaces
            .filter((namespace) => namespace.includes(input))
            .map((namespace) => {
              return {
                name: namespace,
                value: namespace,
                description: namespace,
                disabled: false,
              };
            });
          resolve(allNamespaces);
        });
      },
    });
  }));

const chosenDeployment =
  inputDeployment ??
  (await k8sApi
    .listNamespacedDeployment({ namespace: chosenNamespace })
    .then((res) => {
      const deployments = res.items.map(
        (namespace) => namespace.metadata?.name,
      ) as string[];
      return autocomplete({
        pageSize: 20,
        message: "Choose deployment",
        source: async (input = "") => {
          return new Promise((resolve) => {
            const allDeployments = deployments
              .filter((namespace) => namespace.includes(input))
              .map((namespace) => {
                return {
                  name: namespace,
                  value: namespace,
                  description: namespace,
                  disabled: false,
                };
              });
            resolve(allDeployments);
          });
        },
      });
    }));

const azureSecretMount = "/var/run/secrets/nais.io/azure";

interface ISecretOption {
  name: "azure" | "tokenx";
  value: string;
}
const possibleSecrets: ISecretOption[] = await k8sApi
  .readNamespacedDeployment({
    name: chosenDeployment,
    namespace: chosenNamespace,
  })
  .then((res) => {
    const tokenXSecret = res.spec?.template.spec?.containers
      .find((container) => container.name === chosenDeployment)
      ?.envFrom?.find((envFrom) => envFrom.secretRef?.name?.includes("tokenx"))
      ?.secretRef?.name;
    const azureSecret = res.spec?.template.spec?.containers
      .find((container) => container.name === chosenDeployment)
      ?.volumeMounts?.find(
        (volumeMount) => volumeMount.mountPath === azureSecretMount,
      )?.name;
    return [
      { name: "TokenX", value: tokenXSecret },
      { name: "Azure", value: azureSecret },
    ].filter((secret) => secret.value) as ISecretOption[];
  });

// Ask for secrets to get
const secretsStep = async (): Promise<string[]> => {
  if (inputSecretsToFetch) {
    const filteredSecrets = possibleSecrets
      .filter((secret) =>
        inputSecretsToFetch.find((inputSecret) =>
          secret.value.includes(inputSecret),
        ),
      )
      .map((secret) => secret.value);
    if (filteredSecrets.length === 0) {
      throw Error(`No secrets found of: ${inputSecretsToFetch.toString()}`);
    } else {
      return filteredSecrets;
    }
  } else if (possibleSecrets.every((secret) => !secret.value)) {
    throw Error("No secrets found");
  } else if (possibleSecrets.length === 1) {
    return [possibleSecrets[0].value];
  } else {
    return await select({
      message: "Secrets to set to environment",
      multiple: true,
      defaultValue: possibleSecrets.map((secret) => secret.value),
      options: possibleSecrets,
    });
  }
};

const secretsToFetch = await secretsStep();

console.log("Secrets to fetch", secretsToFetch);

const parsedSecrets = await k8sCore
  .listNamespacedSecret({ namespace: chosenNamespace })
  .then((res) => {
    const secrets = res.items;
    return secretsToFetch.reduce((acc, inputSecret) => {
      const secretsToParse = secrets.find(
        (secret) => secret.metadata?.name === inputSecret,
      );

      console.log("Found secret", secretsToParse?.metadata?.name);

      if (!secretsToParse?.data) {
        throw Error(`No secrets ${inputSecret} found`);
      }

      const newParsedSecrets = Object.entries(secretsToParse.data).reduce(
        (parsedSecrets, [key, value]) => {
          const decodedSecret = Buffer.from(value, "base64").toString("ascii");
          return {
            ...parsedSecrets,
            [key]: decodedSecret,
          };
        },
        {},
      );

      return { ...acc, ...newParsedSecrets };
    }, {});
  });

if (envFileName) {
  const tmpFilePath = `${process.cwd()}/${envFileName}`;
  await fs.writeFile(tmpFilePath, stringify(parsedSecrets)).then(() => {
    console.log(`Env-file saved to ${tmpFilePath}`);
  });
} else {
  const shellCommand = process.env.SHELL;
  if (!shellCommand) {
    throw Error("No shell command found");
  }
  spawn(shellCommand, [], {
    stdio: "inherit",
    env: { ...process.env, ...parsedSecrets },
  });
}
