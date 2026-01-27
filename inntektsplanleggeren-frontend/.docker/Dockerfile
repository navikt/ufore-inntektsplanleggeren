FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/node:22
ENV NODE_ENV=production

WORKDIR /app

COPY ./inntektsplanleggeren-frontend/package.json .
COPY ./inntektsplanleggeren-frontend/package-lock.json .
COPY ./inntektsplanleggeren-frontend/node_modules/ node_modules/
COPY ./inntektsplanleggeren-frontend/dist/ dist/

CMD ["./dist/server/server.js"]
