import {InntektInnfylling} from "@/api/apiFetching";

export type InntektDetaljer = {
    maned: number;
    belop: number;
    inntektsgivere: string[];
};

export type ForventedeInntekter = {
    arbeidsinntekt: number;
    andrePensjonsgivendeYtelser: number;
    naeringsinntekt: number;
    inntektUtland: number;
    pensjonUtland: number;
};

export type ForventedeInntekterResponse = {
    bruker: ForventedeInntekter;
    eps: ForventedeInntekter | null;
};

export type InntekterResponse = {
    arbeidsinntektOgYtelserHittilIAar: InntektDetaljer[];
    pensjonFraAndreHittilIAar: InntektDetaljer[];
    forventedeInntekter: ForventedeInntekterResponse;
    uforeHeleAaret: boolean;
};

export type SimulationRequest = {
    bruker: ForventedeInntekter;
    eps: ForventedeInntekter | null;
};

export type SimulationResponse = {
    messages: Message[];
    result: SimulationResult;
};

export type Message = {
    messageCode: string;
    details: string;
    type: string;
    metadata: Record<string, unknown>;
};

export type SimulationResult = {
    uforetrygd: SimulationDetail;
    forventetInntekt: SimulationDetail;
    barnetilleggFellesbarn: SimulationDetail;
    barnetilleggSaerkullsbarn: SimulationDetail;
    gjenlevendetillegg: SimulationDetail;
    sum: SimulationDetail;
};

export type SimulationDetail = {
    monthly: PayDetail;
    yearly: PayDetail;
};

export type PayDetail = {
    before: number;
    after: number;
};


