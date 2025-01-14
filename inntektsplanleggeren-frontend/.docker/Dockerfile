FROM gcr.io/distroless/nodejs20-debian12
ENV NODE_ENV=production

WORKDIR /app

USER nonroot

COPY ./inntektsplanleggeren-frontend/package.json .
COPY ./inntektsplanleggeren-frontend/package-lock.json .
COPY ./inntektsplanleggeren-frontend/node_modules/ node_modules/
COPY ./inntektsplanleggeren-frontend/dist/ dist/

ENTRYPOINT ["/nodejs/bin/node", "./dist/server/server.js"]
