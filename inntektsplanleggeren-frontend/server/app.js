import express from "express";
import tokenx from "./tokenx.js";
import azure from "./azureAd.js";
import dotenv from "dotenv"
import path from "path";
import {fileURLToPath} from "url";

export const basePath = "/pensjon/selvbetjening/inntektsplanleggeren";

const app = express();
app.use(express.json())
app.use(express.urlencoded({extended: true}));

const PORT = process.env.PORT || 8080;

dotenv.config()

let client = process.env.MODE === "borger" ? await tokenx.client() : await azure.client();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const buildPath = path.resolve(__dirname, "../dist")
app.use(basePath, express.static(buildPath));

app.get(
    basePath + '/api/initiate',
    async (req, res) => {

        const idToken = req.headers['authorization'].replace('Bearer', '').trim();
        let accessToken = await getTokenValue(idToken);
        let newHeaders = req.headers;
        newHeaders['authorization'] = 'Bearer ' + accessToken; // Override authorization header with new token

        const response = await fetch(process.env.INNTEKTSPLANLEGGEREN_BACKEND_URL + "/api/initiate", {
            method: req.method,
            headers: newHeaders
        });

        const body = await response.json();

        const statuskode = response.status
        res.status(statuskode).send(body)
    }
);

app.get(
    basePath + '/forventede-inntekter/api/inntekter',
    async (req, res) => {

        const idToken = req.headers['authorization'].replace('Bearer', '').trim();
        let accessToken = await getTokenValue(idToken);
        let newHeaders = req.headers;
        newHeaders['authorization'] = 'Bearer ' + accessToken; // Override authorization header with new token


        console.log(req.query.simuleringsaar)
        const response = await fetch(process.env.INNTEKTSPLANLEGGEREN_BACKEND_URL + `/api/inntekter?simuleringsaar=${req.query.simuleringsaar}`, {
            method: req.method,
            headers: newHeaders
        });

        const body = await response.json();

        const statuskode = response.status
        res.status(statuskode).send(body)
    }
);




app.put(basePath + '/api/samboer/:periodeId', async (req, res) => {

        const idToken = req.headers['authorization'].replace('Bearer', '').trim();
        let accessToken = await getTokenValue(idToken)
        let newHeaders = req.headers
        newHeaders['authorization'] = 'Bearer ' + accessToken; // Override authorization header with new token

        const periodeId = parseInt(req.params.periodeId)
        const params = {
            method: req.method,
            headers: newHeaders,
            body: JSON.stringify(req.body)
        }

        const response = await fetch(process.env.FAMILIEFORHOLD_BACKEND_URL + "/api/samboer/" + periodeId, params)
        if (response.status === 400) {
            const body = await response.json();
            res.status(response.status).send(body)
        }

        res.status(response.status).send()
    }
);

app.delete(basePath + '/api/samboer/:periodeId', async (req, res) => {

        const idToken = req.headers['authorization'].replace('Bearer', '').trim();
        let accessToken = await getTokenValue(idToken)
        let newHeaders = req.headers
        newHeaders['authorization'] = 'Bearer ' + accessToken; // Override authorization header with new token

        const periodeId = parseInt(req.params.periodeId)

        const params = {
            method: req.method,
            headers: newHeaders,
        }

        const response = await fetch(process.env.FAMILIEFORHOLD_BACKEND_URL + "/api/samboer/" + periodeId, params)
        if (response.status === 400) {
            const body = await response.json();
            res.status(response.status).send(body)
        }

        res.status(response.status).send()
    }
);

app.all(
    basePath + '/api/samboer/**',
    async (req, res) => {

        const idToken = req.headers['authorization'].replace('Bearer', '').trim();
        let accessToken = await getTokenValue(idToken)
        let newHeaders = req.headers
        newHeaders['authorization'] = 'Bearer ' + accessToken; // Override authorization header with new token

        const params = {
            method: req.method,
            headers: newHeaders,
        }

        if (req.body) {
            params["body"] = JSON.stringify(req.body)
        }

        console.log(req.params)
        console.log(req.body)
        console.log(JSON.stringify(req.body))

        const response = await fetch(process.env.FAMILIEFORHOLD_BACKEND_URL + req.path, params)
        const statuskode = response.status
        const body = await response.text()
        res.status(statuskode).send(body)
    }
);

async function getTokenValue(idToken) {
    if (process.env.MODE === "veileder") {

        const tokenEndpoint = await azure.azureTokenEndpoint()

        return await azure.getOnBehalfOfAccessToken(
            client,
            idToken,
            process.env.INNTEKTSPLANLEGGEREN_BACKEND_SCOPE,
            tokenEndpoint
        );

    } else if (process.env.MODE === "borger") {
        return await tokenx.getTokenExchangeAccessToken(
            client,
            idToken,
            process.env.INNTEKTSPLANLEGGEREN_BACKEND_AUDIENCE
        );
    }
}

app.get('/internal/health/liveness', (req, res) => {
    res.send({
        "status": "UP"
    });
});

app.get('/internal/health/readiness', (req, res) => {
    res.send({
        "status": "UP"
    });
});

app.get('*', (req, res) => {
    res.sendFile(path.resolve(__dirname, '../dist', 'index.html'));
});

app.listen(PORT, () => console.log("Server started"));
