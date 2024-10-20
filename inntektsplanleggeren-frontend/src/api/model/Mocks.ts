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

export const inntektData : InntekterResponse = {
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
            "arbeidsinntekt": {
                "belop": 32456
            },
            "andrePensjonsgivendeYtelser": {
                "belop": 22144
            },
            "naeringsinntekt": {
                "belop": 23543
            },
            "inntektUtland": {
                "belop": 1009
            },
            "pensjonUtland": {
                "belop": 9342
            }
        },
        "eps": {
            "arbeidsinntekt": {
                "belop": 10
            },
            "andrePensjonsgivendeYtelser": {
                "belop": 10
            },
            "naeringsinntekt": {
                "belop": 2341024
            },
            "inntektUtland": {
                "belop": 4553
            },
            "pensjonUtland": {
                "belop": 3323,
            }
        }
    },
    "uforeHeleAaret": false
};
