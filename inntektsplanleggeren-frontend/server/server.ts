import express, { NextFunction, Request, Response } from "express";
import winston from "winston";
import { createProxyMiddleware } from "http-proxy-middleware";
import { getToken, requestOboToken, validateToken } from "@navikt/oasis";
import promBundle from "express-prom-bundle";
import path from "path";
import loggerMiddleware from "./middleware/logger.js";
import dotenv from "dotenv";
import ensureEnv from "./ensureEnv.js";

const BASE_PATH = "/uforetrygd/selvbetjening/inntektsplanleggeren";
const PORT = process.env.PORT || 8080;

const metricsMiddleware = promBundle({ includeMethod: true });

const app = express();
const __dirname = process.cwd();

dotenv.config();

const logger = winston.createLogger({
  transports: [new winston.transports.Console()],
});

app.get("/internal/health/liveness", (req, res) => {
  res.send({
    status: "UP",
  });
});

app.get("/internal/health/readiness", (req, res) => {
  res.send({
    status: "UP",
  });
});

app.use(metricsMiddleware);
app.use(loggerMiddleware(logger));

const AUTH_PROVIDER = (() => {
  const tokenx: boolean = !!process.env.TOKEN_X_ISSUER;
  const azure: boolean = !!process.env.AZURE_OPENID_CONFIG_ISSUER;
  if (tokenx && azure) {
    throw new Error(
      "Both TOKEN_X_ISSUER and AZURE_OPENID_CONFIG_ISSUER are set. Only one of these can be set.",
    );
  }

  if (!tokenx && !azure) {
    throw new Error("No auth provider is set");
  }

  if (tokenx) {
    return "tokenx";
  }

  if (azure) {
    return "azure";
  }
})() as "tokenx" | "azure";

// Sett miljøvariabler for veileder eller borger
const env =
  AUTH_PROVIDER === "tokenx"
    ? ensureEnv({
        oboAudience: "INNTEKTSPLANLEGGEREN_BACKEND_AUDIENCE",
        inntektsplanleggerenBackendUrl: "INNTEKTSPLANLEGGEREN_BACKEND_URL",
      })
    : ensureEnv({
        oboAudience: "INNTEKTSPLANLEGGEREN_BACKEND_SCOPE",
        inntektsplanleggerenBackendUrl: "INNTEKTSPLANLEGGEREN_BACKEND_URL",
      });

const getOboToken = async (req: Request) => {
  const token = getToken(req);
  if (!token) {
    logger.info("No token found in request", {
      "x_correlation-id": req.headers["x_correlation-id"],
    });
    throw new Error("403");
  }

  const validationResult = await validateToken(token);
  if (!validationResult.ok) {
    logger.error("Failed to validate token", {
      error: validationResult.error.message,
      errorType: validationResult.errorType,
      "x_correlation-id": req.headers["x_correlation-id"],
    });
    throw new Error("401");
  }

  const obo = await requestOboToken(token, env.oboAudience);
  if (!obo.ok) {
    logger.error("Failed to get OBO token", {
      error: obo.error.message,
      "x_correlation-id": req.headers["x_correlation-id"],
    });
    throw new Error("401");
  }
  return obo.token;
};

app.use(
  `${BASE_PATH}/api`,
  async (req: Request, res: Response, next: NextFunction) => {
    let oboToken;
    try {
      oboToken = getOboToken(req);
    } catch {
      return res.sendStatus(401);
    }

    return createProxyMiddleware({
      target: `${env.inntektsplanleggerenBackendUrl}/api`,
      headers: {
        Authorization: `Bearer ${oboToken}`,
      },
      logger: logger,
    })(req, res, next);
  },
);

app.get("*", (req, res) => {
  if (AUTH_PROVIDER === "azure") {
    res.sendFile(path.resolve(__dirname, "./dist", "index-veileder.html"));
  } else {
    res.sendFile(path.resolve(__dirname, "./dist", "index.html"));
  }
});

app.listen(PORT, () => {
  logger.info(
    `Started server with AUTH_PROVIDER ${AUTH_PROVIDER} on port ${PORT}`,
  );
});
