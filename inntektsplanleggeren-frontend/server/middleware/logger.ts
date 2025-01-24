import { RequestHandler } from "express";
import winston from "winston";

const loggerMiddleware =
  (logger: winston.Logger): RequestHandler =>
  (req, res, next) => {
    const start = Date.now();
    res.on("finish", () => {
      const duration = Date.now() - start;
      const logMetadata = {
        url: req.originalUrl,
        method: req.method,
        duration,
        statusCode: res.statusCode,
        "x_correlation-id": req.headers["x_correlation-id"],
      };

      const logMessage = `${req.method} ${req.path} ${res.statusCode}`;
      if (res.statusCode >= 400) {
        logger.error(logMessage, logMetadata);
      } else {
        logger.info(logMessage, logMetadata);
      }
    });
    next();
  };

export default loggerMiddleware;
