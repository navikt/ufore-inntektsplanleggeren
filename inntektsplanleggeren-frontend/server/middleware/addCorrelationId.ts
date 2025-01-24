import { NextFunction } from "express";

const addCorrelationIdMiddleware = (req: Request, next: NextFunction) => {
  if (!req.headers.get("x_correlation-id")) {
    req.headers.set("x_correlation-id", crypto.randomUUID());
  }
  next();
};

export default addCorrelationIdMiddleware;
