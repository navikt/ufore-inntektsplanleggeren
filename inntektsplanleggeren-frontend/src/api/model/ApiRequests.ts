import {InntektInnfylling} from "@/api/apiFetching";


export type SubmitInntektRequest = {
    inntekt: InntektInnfylling
}

export type InntektDetaljer = {
    maned: number;
    belop: number;
    inntektsgivere: string[];
};

export type ForventetInntekt = {
    belop: number;
};

export type ForventedeInntekter = {
    arbeidsinntekt: ForventetInntekt;
    andrePensjonsgivendeYtelser: ForventetInntekt;
    naeringsinntekt: ForventetInntekt;
    inntektUtland: ForventetInntekt;
    pensjonUtland: ForventetInntekt;
};

export type ForventedeInntekterResponse = {
    bruker: ForventedeInntekter;
    eps: ForventedeInntekter;
};

export type InntekterResponse = {
    arbeidsinntektOgYtelserHittilIAar: InntektDetaljer[];
    pensjonFraAndreHittilIAar: InntektDetaljer[];
    forventedeInntekter: ForventedeInntekterResponse;
    uforeHeleAaret: boolean;
};

export type SubmitInntektRequest = {
    inntekt: InntektInnfylling
}

export type SubmitInntektResponse = {
    a: string
}
