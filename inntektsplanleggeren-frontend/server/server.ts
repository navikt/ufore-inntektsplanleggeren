import express, { NextFunction, Request, Response } from 'express'
import winston, { format } from 'winston'
import { createProxyMiddleware } from 'http-proxy-middleware'
import { getToken, requestOboToken, validateToken } from '@navikt/oasis'
import promBundle from 'express-prom-bundle'
import path from 'path'
import loggerMiddleware from './middleware/logger.js'
import dotenv from 'dotenv'
import ensureEnv from './ensureEnv.js'
import { initialize } from 'unleash-client'
import { stengForReguleringMiddleware } from '@navikt/steng-for-regulering/express'
import correlationIdMiddleware from './middleware/correlationId.js'

const BASE_PATH = '/uforetrygd/selvbetjening/inntektsplanleggeren'
const PORT = process.env.PORT || 8080

const metricsMiddleware = promBundle({ includeMethod: true })

const app = express()
const __dirname = process.cwd()

const isDevelopment = process.env.NODE_ENV === 'development' || process.env.NODE_ENV === 'development-local'

dotenv.config()

if (process.env.NODE_ENV === 'development') {
    dotenv.config({ path: ['.env.development', '.env.local'], override: true })
} else if (process.env.NODE_ENV === 'development-local') {
    dotenv.config({ path: ['.env.development', '.env.development-local', '.env.local'], override: true })
}

const logger = winston.createLogger({
    format: isDevelopment ? format.simple() : undefined,
    transports: [new winston.transports.Console()],
})

const AUTH_PROVIDER = (() => {
    const tokenx: boolean = !!process.env.TOKEN_X_ISSUER
    const azure: boolean = !!process.env.AZURE_OPENID_CONFIG_ISSUER
    if (tokenx && azure) {
        throw new Error('Both TOKEN_X_ISSUER and AZURE_OPENID_CONFIG_ISSUER are set. Only one of these can be set.')
    }

    if (!tokenx && !azure) {
        throw new Error('No auth provider is set')
    }

    if (tokenx) {
        return 'tokenx'
    }

    if (azure) {
        return 'azure'
    }
})() as 'tokenx' | 'azure'

const unleashUrl = process.env.UNLEASH_SERVER_API_URL
const unleashToken = process.env.UNLEASH_SERVER_API_TOKEN
const unleashEnv = process.env.UNLEASH_SERVER_API_ENV

// Sett miljøvariabler for veileder eller borger
const env =
    AUTH_PROVIDER === 'tokenx'
        ? ensureEnv({
              oboAudience: 'INNTEKTSPLANLEGGEREN_BACKEND_AUDIENCE',
              inntektsplanleggerenBackendUrl: 'INNTEKTSPLANLEGGEREN_BACKEND_URL',
              pselvUrl: 'URL_PSELV_INNTEKTSPLANLEGGER',
          })
        : ensureEnv({
              oboAudience: 'INNTEKTSPLANLEGGEREN_BACKEND_SCOPE',
              inntektsplanleggerenBackendUrl: 'INNTEKTSPLANLEGGEREN_BACKEND_URL',
              pselvUrl: 'URL_PSELV_INNTEKTSPLANLEGGER',
          })

const unleash = initialize({
    disableAutoStart: !(unleashToken && unleashUrl && unleashEnv),
    url: `${unleashUrl}/api`,
    appName: 'inntektsplanleggeren-frontend',
    environment: unleashEnv,
    customHeaders: {
        Authorization: unleashToken ?? '',
    },
})

unleash.on('synchronized', () => {
    logger.info('Unleash synchronized')
})

const getOboToken = async (req: Request) => {
    if (isDevelopment && process.env.ACCESS_TOKEN) {
        logger.error('Returning mock ACCESS_TOKEN from enviroment variable')
        return process.env.ACCESS_TOKEN
    }
    const token = getToken(req)
    if (!token) {
        logger.info('No token found in request', {
            'x_correlation-id': req.headers['x_correlation-id'],
        })
        throw new Error('403')
    }

    const validationResult = await validateToken(token)
    if (!validationResult.ok) {
        logger.error('Failed to validate token', {
            error: validationResult.error.message,
            errorType: validationResult.errorType,
            'x_correlation-id': req.headers['x_correlation-id'],
        })
        throw new Error('401')
    }

    const obo = await requestOboToken(token, env.oboAudience)
    if (!obo.ok) {
        logger.error('Failed to get OBO token', {
            error: obo.error.message,
            'x_correlation-id': req.headers['x_correlation-id'],
        })
        throw new Error('401')
    }
    return obo.token
}

app.get('/internal/health/liveness', (req, res) => {
    res.send({
        status: 'UP',
    })
})

app.get('/internal/health/readiness', (req, res) => {
    res.send({
        status: 'UP',
    })
})
app.use(stengForReguleringMiddleware({ env: isDevelopment ? 'dev' : 'prod', unleashClient: unleash }))
app.use(metricsMiddleware)
app.use(correlationIdMiddleware)
app.use(loggerMiddleware(logger))

app.use(`${BASE_PATH}/assets`, (req: Request, res: Response, next: NextFunction) => {
    const assetFolder = path.join(__dirname, './dist', 'assets')
    return express.static(assetFolder)(req, res, next)
})

app.use(`${BASE_PATH}/api`, (req: Request, res: Response, next: NextFunction) => {
    getOboToken(req)
        .then((oboToken) => {
            createProxyMiddleware({
                target: `${env.inntektsplanleggerenBackendUrl}/api`,
                changeOrigin: true,
                headers: {
                    Authorization: `Bearer ${oboToken}`,
                },
                logger: logger,
            })(req, res, next)
        })
        .catch(() => {
            res.sendStatus(401)
        })
})

app.get('/*splat', async (req, res) => {
    if (AUTH_PROVIDER === 'azure') {
        res.sendFile(path.resolve(__dirname, './dist', 'index-veileder.html'))
    } else {
        res.sendFile(path.resolve(__dirname, './dist', 'index-borger.html'))
    }
})

app.listen(PORT, () => {
    logger.info(`Started server with AUTH_PROVIDER ${AUTH_PROVIDER} on port ${PORT}`)
})
