FROM gcr.io/distroless/nodejs20-debian12
ENV NODE_ENV=production

WORKDIR /app

USER nonroot

COPY ./inntektsplannlegeren-frontend/package.json .
COPY ./inntektsplannlegeren-frontend/package-lock.json .
COPY ./inntektsplannlegeren-frontend/node_modules/ node_modules/
COPY ./inntektsplannlegeren-frontend/server server/
COPY ./inntektsplannlegeren-frontend/dist/ dist/

ENTRYPOINT ["/nodejs/bin/node", "./server/app.js"]
