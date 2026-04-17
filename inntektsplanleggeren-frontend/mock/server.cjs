const express = require('express')
const fs = require("fs");
const app = express()
const port = 3000
var cors = require('cors')

const mockInitiateResponse = JSON.parse(fs.readFileSync('mock/initiateMockResponse.json', 'utf8'));
const mockInitiateResponseError = JSON.parse(fs.readFileSync('mock/initiateMockResponseError.json', 'utf8'));
const mockForbiddenResponse = JSON.parse(fs.readFileSync('mock/mockForbiddenResponse.json', 'utf8'));
const mockInntekterResponse = JSON.parse(fs.readFileSync('mock/inntekterMockResponse.json', 'utf8'));
const mockSimulationResponse = JSON.parse(fs.readFileSync('mock/simulateMockResponse.json', 'utf8'));
const mockSimulationResponseError = JSON.parse(fs.readFileSync('mock/simulateMockResponseError.json', 'utf8'));
const mockSimulationResponseWarnings = JSON.parse(fs.readFileSync('mock/simulateMockResponseWarnings.json', 'utf8'));
const mockSendResponse = JSON.parse(fs.readFileSync('mock/sendMockResponse.json', 'utf8'));
const mockStatusResponse = JSON.parse(fs.readFileSync('mock/statusMockResponse.json', 'utf8'));

app.use(cors({
    credentials: true,
    origin: function (origin, callback) {
      return callback(null, true)
    }
}));

app.get('/uforetrygd/selvbetjening/inntektsplanleggeren/api/initiate', (req, res) => {
    console.log("GET - /api/initiate")
    //403 response
    //res.status(403).send(mockForbiddenResponse)

    //200 response
    res.status(200).send(mockInitiateResponse)

    //200 response
    // res.status(200).send(mockInitiateResponseError)
})

app.get('/uforetrygd/selvbetjening/inntektsplanleggeren/api/inntekter', (req, res) => {
    console.log("GET - /api/inntekter")
    //403 response
    //res.status(403).send(mockForbiddenResponse)

    //200 response
    res.status(200).send(mockInntekterResponse)
})

app.post('/uforetrygd/selvbetjening/inntektsplanleggeren/api/simuler', (req, res) => {
    console.log("POST - /api/simuler")
    //403 response
    //res.status(403).send(mockForbiddenResponse)

    //200 response
    res.status(200).send(mockSimulationResponse)

    //200 response with warnings
    // res.status(200).send(mockSendResponseWarnings)

    //500 response
    // res.status(200).send(mockSimulationResponseError)
})

app.post('/uforetrygd/selvbetjening/inntektsplanleggeren/api/send', (req, res) => {
    console.log("POST - /api/send")
    //403 response
    //res.status(403).send(mockForbiddenResponse)

    //200 response
    res.status(200).send(mockSendResponse)
})

app.get('/uforetrygd/selvbetjening/inntektsplanleggeren/api/status', (req, res) => {
    console.log("GET - /api/status")
    //403 response
    //res.status(403).send(mockForbiddenResponse)

    //200 response
    res.status(200).send(mockStatusResponse)
})

app.listen(port, () => {
    console.log(`inntektsplanleggeren-backend mock lytter på port ${port}`)
})
