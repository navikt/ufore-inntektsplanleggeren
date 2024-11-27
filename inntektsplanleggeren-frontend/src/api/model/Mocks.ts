import {InitiateData, InntekterResponse, SendApplicationResponse, StatusResponse} from "@/api/model/ApiRequests";
import {MessageCodes, MessageTypes} from "@/api/model/MessageCodes";
import {StatusCodes} from "@/api/model/StatusCodes";

export const mockInitiateResponse = {
    "messages":[
        // {
        //     "messageCode": MessageCodes.EPS_INNTEKT_CHANGED,
        //     "details": "Bruker har endret en av EPS sine inntekter sammenlignet med det som tidligere var benyttet som EPS sin inntekt.",
        //     "type": MessageTypes.WARNING,
        //     "metadata": {}
        // },
        // {
        //     "messageCode": "USER_HAS_NO_UFORE",
        //     "details": "Bruker har endret en av EPS sine inntekter sammenlignet med det som tidligere var benyttet som EPS sin inntekt.",
        //     "type": "WARNING",
        //     "metadata": {}
        // }
        // {
        //     "messageCode": "USER_HAS_NO_LOPENDE_VEDTAK_YET",
        //     "details": "",
        //     "type": "WARNING",
        //     "metadata": {}
        // }
        ],

    "data": {
        "forventetInntekt": 120000,
        "forventetInntektAnnenForelder": 800000,
        "inntektsgrense": 49611,
        "kompensasjonsgrad": 49.7,
        "grenseStoppAvUfoeretrygd": 460060,
        "aktuelleAar": [
            2024,
            2025
        ],
        "hasVarigTilrettelagtArbeid": true,
        "hasBarneTilleggFellesbarn": true,
        "grenseStoppAvBarnetilleggFellesbarn": 669751,
        "fribelopBarnetilleggFellesbarn": 570529,
        "hasBarnetilleggSaerkullsbarn": true,
        "grenseStoppAvBarnetilleggSaerkullsbarn": 827,
        "fribelopBarnetilleggSaerkullsbarn": 242,
        "hasGjenlevendeTillegg": true
    } satisfies InitiateData
}

export const mockInntekterResponse : InntekterResponse = {
    "arbeidsinntektOgYtelserHittilIAar": [
        {
            "maned": 5,
            "belop": 53426.0,
            "inntektsgivere": [
                "Veterinær AS",
                "Grønnsakssuppekjøkkenet AS"
            ]
        },
        {
            "maned": 6,
            "belop": 0.0,
            "inntektsgivere": [
                // "Isbilen AS"
            ]
        },
        {
            "maned": 7,
            "belop": 54001.0,
            "inntektsgivere": [
                "Veterinær AS"
            ]
        },
        {
            "maned": 8,
            "belop": 7641.0,
            "inntektsgivere": [
                "Veterinær AS"
            ]
        }
    ],
    "pensjonFraAndreHittilIAar": [
        {
            "maned": 5,
            "belop": 10,
            "inntektsgivere": [
                "Isbilen AS",
                "Veterinær AS"
            ]
        },
        {
            "maned": 6,
            "belop": 34543.0,
            "inntektsgivere": [
                "Grønnsakssuppekjøkkenet AS"
            ]
        },
        {
            "maned": 7,
            "belop": 54001.0,
            "inntektsgivere": [
                "Isbilen AS"
            ]
        },
        {
            "maned": 8,
            "belop": 7641.0,
            "inntektsgivere": [
                "Isbilen AS"
            ]
        }
    ],
    "forventedeInntekter": {
        "bruker": {
            "arbeidsinntekt": 1,
            "andrePensjonsgivendeYtelser": 2,
            "naeringsinntekt": 3,
            "inntektUtland": 4,
            "pensjonUtland": 5
        },
        "eps": {
            "arbeidsinntekt": 6,
            "andrePensjonsgivendeYtelser": 7,
            "naeringsinntekt": 8,
            "inntektUtland": 9,
            "pensjonUtland": 10,
        }
    },
    "uforeHeleAaret": false,
    "epsPid": "1234"
};
// export const mockInntekterResponse : InntekterResponse ={
//     "arbeidsinntektOgYtelserHittilIAar": [
//     {
//         "maned": 1,
//         "belop": 4500,
//         "inntektsgivere": [
//             "BESK KAFFE"
//         ]
//     },
//     {
//         "maned": 2,
//         "belop": 4500,
//         "inntektsgivere": [
//             "BESK KAFFE"
//         ]
//     },
//     {
//         "maned": 3,
//         "belop": 4500,
//         "inntektsgivere": [
//             "BESK KAFFE"
//         ]
//     },
//     {
//         "maned": 4,
//         "belop": 4500,
//         "inntektsgivere": [
//             "BESK KAFFE"
//         ]
//     },
//     {
//         "maned": 5,
//         "belop": 4500,
//         "inntektsgivere": [
//             "BESK KAFFE"
//         ]
//     },
//     {
//         "maned": 6,
//         "belop": 4500,
//         "inntektsgivere": [
//             "BESK KAFFE"
//         ]
//     },
//     {
//         "maned": 7,
//         "belop": 4500,
//         "inntektsgivere": [
//             "BESK KAFFE"
//         ]
//     },
//     {
//         "maned": 8,
//         "belop": 4500,
//         "inntektsgivere": [
//             "BESK KAFFE"
//         ]
//     }
// ],
//     "pensjonFraAndreHittilIAar": [],
//     "forventedeInntekter": {
//     "bruker": {
//         "arbeidsinntekt": 75000,
//             "andrePensjonsgivendeYtelser": 0,
//             "naeringsinntekt": 0,
//             "inntektUtland": 0,
//             "pensjonUtland": 0
//     },
//     "eps": {
//         "arbeidsinntekt": 0,
//             "andrePensjonsgivendeYtelser": 0,
//             "naeringsinntekt": 0,
//             "inntektUtland": 0,
//             "pensjonUtland": 0
//     }
// },
//     "uforeHeleAaret": true
// }

export const mockSimulationResponse = {
    "messages": [],
    "result": {
        "uforetrygd": {
            "monthly": {
                "before": 30000,
                "after": 31000
            },
            "yearly": {
                "before": 353000,
                "after": 354000
            }
        },
        "forventetInntekt": {
            "monthly": {
                "before": 30000,
                "after": 31000
            },
            "yearly": {
                "before": 300000,
                "after": 310000
            }
        },
        "barnetilleggFellesbarn": {
            "monthly": {
                "before": 1000,
                "after": 500
            },
            "yearly": {
                "before": 500000,
                "after": 6000
            }
        },
        "barnetilleggSaerkullsbarn": null,
        "gjenlevendetillegg": {
            "monthly": {
                "before": 0,
                "after": 0
            },
            "yearly": {
                "before": 0,
                "after": 0
            }
        },
        "sum": {
            "monthly": {
                "before": 30000,
                "after": 25984
            },
            "yearly": {
                "before": 404534,
                "after": 390238
            }
        }
    }
}

export const mockSendApplicationResponse: SendApplicationResponse = {
    "messages": [
    {
        "messageCode": MessageCodes.USER_HAS_NO_UFORE,
        "details": "string",
        "type": MessageTypes.ERROR,
        "metadata": {
            "additionalProp1": {},
            "additionalProp2": {},
            "additionalProp3": {}
        }
    }
],
    "status": "AUTOMATISK_BEHANDLING",
    "innsendingsTidspunkt":"2024-11-27 11:50:44"
}

export const mockStatusResponse : StatusResponse = {
    "registeringsTidspunktEndring": new Date("2024-06-02T09:06:38.971Z"),
    "status": StatusCodes.TIL_BEHANDLING,
    "sakId": 2112,
    "maandedligeUtbetalinger": {
        "fom": new Date("2024-06-01"),
        "beloep": 8
    },
    "mottarBarnetilleggForFellesBarn": true,
    "forventetAarligInntekt": 1,
    "forventetAarligInntektEps": 2
}