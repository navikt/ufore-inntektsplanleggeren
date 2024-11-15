import express from "express";
import tokenx from "./tokenx.js";
import azure from "./azureAd.js";
import dotenv from "dotenv"
import path from "path";
import {fileURLToPath} from "url";
import {getToken, validateToken, parseIdportenToken} from "@navikt/oasis";
import {initRedis, isRedisReady, redisClient} from "./redis.js";

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

/**
 * @param {object|null} data
 * @returns {boolean}
 */
const isInntekterPayload = (data) => {
    if (data === null || typeof data !== 'object') {
        return false;
    }

    if (!Object.hasOwn(data, 'year') || !Object.hasOwn(data, 'brukerInntekter') || !Object.hasOwn(data, 'epsInntekter')) {
        return false;
    }

    return isInntektObject(data.brukerInntekter) && isInntektObject(data.epsInntekter);
};

/**
 * @param {object|null} data
 * @returns {boolean}
 */
const isInntektObject = (data) => data !== null &&
    hasInntektValue(data, 'arbeidsinntekt') &&
    hasInntektValue(data, 'andrePensjonsgivendeYtelser') &&
    hasInntektValue(data, 'naeringsinntekt') &&
    hasInntektValue(data, 'inntektUtland') &&
    hasInntektValue(data, 'pensjonUtland');

/**
 * @param {Object} data
 * @param {string} key
 * @returns {boolean}
 */
const hasInntektValue = (data, key) => Object.hasOwn(data, key) && isInntektValue(data.inntekter[key]);

/**
 * @param {Object} data
 * @returns {boolean}
 */
const isInntektValue = (data) => data === null || (typeof data === 'number' && data >= 0);

/**
 * @param {express.Request} req
 * @param {express.Response} res
 * @param {express.NextFunction} next
 * @returns {Promise<void>}
 */
const authMiddleware = async (req, res, next) => {
    const token = getToken(req);

    if (token === undefined) {
        res.status(401).send('Token mangler');
        return;
    }

    if (!await validateToken(token)) {
        res.status(403).send('Token er ugyldig');
        return;
    }

    next();
};

/**
 * @param {string} pid
 */
const getRedisKey = (pid) => pid;

app.post(basePath + '/persistence', authMiddleware, async (req, res) => {
    const token = getToken(req);
    const parsed = await parseIdportenToken(token);

    if (!parsed.ok) {
        res.status(403).send('Token er ugyldig');
        return;
    }

    const data = req.body;

    if (!isInntekterPayload(data)) {
        res.status(400).send('Ugyldig data');
        return;
    }

    try {
        await redisClient.set(getRedisKey(parsed.pid), JSON.stringify(data));

        res.status(200).send();
    } catch (e) {
        res.status(500).send('Noe gikk galt');
    }
});

app.delete(basePath + '/persistence', authMiddleware, async (req, res) => {
    const token = getToken(req);
    const parsed = await parseIdportenToken(token);

    if (!parsed.ok) {
        res.status(403).send('Token er ugyldig');
        return;
    }

    try {
        await redisClient.del(getRedisKey(parsed.pid));

        res.status(200).send();
    } catch (e) {
        res.status(500).send('Noe gikk galt');
    }
});

app.get(basePath + '/persistence', authMiddleware, async (req, res) => {
    const token = getToken(req);
    const parsed = await parseIdportenToken(token);

    if (!parsed.ok) {
        res.status(403).send('Token er ugyldig');
        return;
    }

    try {
        const data = await redisClient.get(getRedisKey(parsed.pid));

        console.log("data", data)

        if (data === null) {
            res.status(404).send('Data ikke funnet');
            return;
        }

        res.status(200).contentType("application/json").send(data);
    } catch (e) {
        res.status(500).send('Noe gikk galt');
    }
});

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
    basePath + '/api/inntekter',
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


app.post(
    basePath + '/api/simuler',
    async (req, res) => {

        const idToken = req.headers['authorization'].replace('Bearer', '').trim();
        let accessToken = await getTokenValue(idToken);
        let newHeaders = req.headers;
        newHeaders['authorization'] = 'Bearer ' + accessToken; // Override authorization header with new token

        console.log(req.query.simuleringsaar)
        const response = await fetch(process.env.INNTEKTSPLANLEGGEREN_BACKEND_URL + `/api/simuler?simuleringsaar=${req.query.simuleringsaar}`, {
            method: req.method,
            headers: newHeaders,
            body: JSON.stringify(req.body)
        });

        const body = await response.json();

        const statuskode = response.status
        res.status(statuskode).send(body)
    }
);

app.post(
    basePath + '/api/send',
    async (req, res) => {

        const idToken = req.headers['authorization'].replace('Bearer', '').trim();
        let accessToken = await getTokenValue(idToken);
        let newHeaders = req.headers;
        newHeaders['authorization'] = 'Bearer ' + accessToken; // Override authorization header with new token

        console.log(req.query.simuleringsaar)
        const response = await fetch(process.env.INNTEKTSPLANLEGGEREN_BACKEND_URL + `/api/send?simuleringsaar=${req.query.simuleringsaar}`, {
            method: req.method,
            headers: newHeaders,
            body: JSON.stringify(req.body)
        });

        const body = await response.json();

        const statuskode = response.status
        res.status(statuskode).send(body)
    }
);

app.post(
    basePath + '/api/status',
    async (req, res) => {

        const idToken = req.headers['authorization'].replace('Bearer', '').trim();
        let accessToken = await getTokenValue(idToken);
        let newHeaders = req.headers;
        newHeaders['authorization'] = 'Bearer ' + accessToken; // Override authorization header with new token

        console.log(req.query.simuleringsaar)
        const response = await fetch(process.env.INNTEKTSPLANLEGGEREN_BACKEND_URL + `/api/status?innsendingstidspunkt=${req.query.innsendingstidspunkt}?`, {
            method: req.method,
            headers: newHeaders,
            body: JSON.stringify(req.body)
        });

        const body = await response.json();

        const statuskode = response.status
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
    if (!isRedisReady()) {
        res.status(418).send({
            "status": "NOT_READY"
        });
    }

    res.send({
        "status": "UP"
    });
});

app.get('*', (req, res) => {
    res.sendFile(path.resolve(__dirname, '../dist', 'index.html'));
});

app.listen(PORT, () => {
    console.log("Server started");
    initRedis();
});
