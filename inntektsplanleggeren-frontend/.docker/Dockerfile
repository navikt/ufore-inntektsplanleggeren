FROM navikt/node-express:18

WORKDIR /app
COPY ./inntektsplannlegeren-frontend/package.json .
COPY ./inntektsplannlegeren-frontend/package-lock.json .
COPY ./inntektsplannlegeren-frontend/node_modules/ node_modules/
COPY ./inntektsplannlegeren-frontend/server server/
COPY ./inntektsplannlegeren-frontend/dist/ dist/

ENTRYPOINT ["sh", "-c"]
CMD ["node ./server/app.js"]
