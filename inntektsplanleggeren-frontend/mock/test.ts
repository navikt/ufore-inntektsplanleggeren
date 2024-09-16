import { MockMethod} from 'vite-plugin-mock'
import hentFamilieforholdResponse from './hentFamilieforholdResponseMock.json'
import hentSamboerforholdResponse from './hentSamboerResponseMock.json'
import opprettSamboerforholdFeilmelding from './opprettSamboerforholdFeilmelding.json'

const endreFeilrespons = {
    "timestamp": "2024-05-13T12:44:19.578+00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "FINNES_IKKE",
    "path": "/api/samboer/1002048"
}
export default [
    {
        url: '/api/familieforhold',
        method: 'get',
        statusCode: 200,
        response: () => {
            return hentFamilieforholdResponse
        },
    },
    {
        url: '/api/samboer',
        method: 'get',
        statusCode: 200,
        response: () => {
            return hentSamboerforholdResponse
        },
    },
    {
        url: '/api/samboer',
        method: 'post',
        statusCode: 400,
        timeout: 2000,
        response: opprettSamboerforholdFeilmelding ,
    },
    {
        url: '/api/samboer',
        method: 'put',
        statusCode: 200,
        timeout: 2000,
        response: "" ,
    },{
        url: '/api/samboer/1001860',
        method: 'delete',
        statusCode: 200,
        response: "" ,
    },
    {
        url: '/api/samboer/1001874',
        method: 'delete',
        statusCode: 200,
        response: "" ,
    },
    {
        url: '/api/samboer/1001874',
        method: 'put',
        statusCode: 200,
        response: () => {
            return hentFamilieforholdResponse
        },
    },
    {
        url: '/api/samboer/1001860',
        method: 'put',
        statusCode: 400,
        response: endreFeilrespons ,
    },
    {
        url: '/api/samboer/1001861',
        method: 'put',
        statusCode: 400,
        response: endreFeilrespons ,
    },
    {
        url: '/api/samboer/1001862',
        method: 'put',
        statusCode: 400,
        response: endreFeilrespons ,
    },
] as MockMethod[]
