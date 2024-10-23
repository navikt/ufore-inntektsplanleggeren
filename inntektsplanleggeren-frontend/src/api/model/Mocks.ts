import {InntekterResponse} from "@/api/model/ApiRequests";

export const mockInitiateResponse = {
    "messages": [],
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
        "hasVarigTilrettelagtArbeid": false,
        "hasBarneTilleggFellesbarn": true,
        "grenseStoppAvBarnetilleggFellesbarn": 669751,
        "fribelopBarnetilleggFellesbarn": 570529,
        "hasBarnetilleggSaerkullsbarn": false,
        "grenseStoppAvBarnetilleggSaerkullsbarn": null,
        "fribelopBarnetilleggSaerkullsbarn": null,
        "hasGjenlevendeTillegg": false
    }
}

// export const mockInntekterResponse : InntekterResponse = {
//     "arbeidsinntektOgYtelserHittilIAar": [
//         {
//             "maned": 5,
//             "belop": 53426.0,
//             "inntektsgivere": [
//                 "Veterinær AS",
//                 "Grønnsakssuppekjøkkenet AS"
//             ]
//         },
//         {
//             "maned": 6,
//             "belop": 0.0,
//             "inntektsgivere": [
//                 // "Isbilen AS"
//             ]
//         },
//         {
//             "maned": 7,
//             "belop": 54001.0,
//             "inntektsgivere": [
//                 "Veterinær AS"
//             ]
//         },
//         {
//             "maned": 8,
//             "belop": 7641.0,
//             "inntektsgivere": [
//                 "Veterinær AS"
//             ]
//         }
//     ],
//     "pensjonFraAndreHittilIAar": [
//         {
//             "maned": 5,
//             "belop": 10,
//             "inntektsgivere": [
//                 "Isbilen AS",
//                 "Veterinær AS"
//             ]
//         },
//         {
//             "maned": 6,
//             "belop": 34543.0,
//             "inntektsgivere": [
//                 "Grønnsakssuppekjøkkenet AS"
//             ]
//         },
//         {
//             "maned": 7,
//             "belop": 54001.0,
//             "inntektsgivere": [
//                 "Isbilen AS"
//             ]
//         },
//         {
//             "maned": 8,
//             "belop": 7641.0,
//             "inntektsgivere": [
//                 "Isbilen AS"
//             ]
//         }
//     ],
//     "forventedeInntekter": {
//         "bruker": {
//             "arbeidsinntekt": 32456,
//             "andrePensjonsgivendeYtelser": 22144,
//             "naeringsinntekt": 23543,
//             "inntektUtland": 1009,
//             "pensjonUtland": 9342
//         },
//         "eps": {
//             "arbeidsinntekt": 10,
//             "andrePensjonsgivendeYtelser": 10,
//             "naeringsinntekt": 2341024,
//             "inntektUtland": 4553,
//             "pensjonUtland": 3323,
//         }
//     },
//     "uforeHeleAaret": false
// };

export const mockInntekterResponse : InntekterResponse ={
    "arbeidsinntektOgYtelserHittilIAar": [
    {
        "maned": 1,
        "belop": 4500,
        "inntektsgivere": [
            "BESK KAFFE"
        ]
    },
    {
        "maned": 2,
        "belop": 4500,
        "inntektsgivere": [
            "BESK KAFFE"
        ]
    },
    {
        "maned": 3,
        "belop": 4500,
        "inntektsgivere": [
            "BESK KAFFE"
        ]
    },
    {
        "maned": 4,
        "belop": 4500,
        "inntektsgivere": [
            "BESK KAFFE"
        ]
    },
    {
        "maned": 5,
        "belop": 4500,
        "inntektsgivere": [
            "BESK KAFFE"
        ]
    },
    {
        "maned": 6,
        "belop": 4500,
        "inntektsgivere": [
            "BESK KAFFE"
        ]
    },
    {
        "maned": 7,
        "belop": 4500,
        "inntektsgivere": [
            "BESK KAFFE"
        ]
    },
    {
        "maned": 8,
        "belop": 4500,
        "inntektsgivere": [
            "BESK KAFFE"
        ]
    }
],
    "pensjonFraAndreHittilIAar": [],
    "forventedeInntekter": {
    "bruker": {
        "arbeidsinntekt": 75000,
            "andrePensjonsgivendeYtelser": 0,
            "naeringsinntekt": 0,
            "inntektUtland": 0,
            "pensjonUtland": 0
    },
    "eps": {
        "arbeidsinntekt": 0,
            "andrePensjonsgivendeYtelser": 0,
            "naeringsinntekt": 0,
            "inntektUtland": 0,
            "pensjonUtland": 0
    }
},
    "uforeHeleAaret": true
}
