import { Request, Response, NextFunction } from 'express'
import { randomUUID } from 'crypto'

export default function correlationIdMiddleware(req: Request, res: Response, next: NextFunction) {
    const headerName = 'Nav-Call-Id'
    if (!req.header(headerName)) {
        const id = randomUUID()
        req.headers[headerName] = id
        res.setHeader(headerName, id)
    }
    next()
}
