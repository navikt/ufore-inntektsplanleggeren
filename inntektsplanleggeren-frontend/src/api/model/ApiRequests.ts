import {InntektInnfylling} from "@/api/apiFetching";


export type SubmitInntektRequest = {
    inntekt: InntektInnfylling
    year: string
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

export type SubmitInntektSimulationResponse = {
    messages: Message[];
    result: SimulationResult;
};

export type Message = {
    messageCode: string;
    details: string;
    type: string;
    metadata: Record<string, unknown>;
};

export type ResultDetails = {
    before: number;
    after: number;
};

export type SimulationResult = {
    uforetrygd: ResultDetails;
    forventetInntekt: ResultDetails;
    barnetilleggFellesbarn: ResultDetails;
    barnetilleggSaerkullsbarn: ResultDetails;
    gjenlevendetillegg: ResultDetails;
    sum: ResultDetails;
};


