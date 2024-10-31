import {MessageCodes} from "@/api/model/MessageCodes";

export type Message = {
    messageCode: MessageCodes;
    details: string;
    type: string;
    metadata: Record<string, unknown>;
};

export interface GetInntektsgrenseResponse {
    messages: Message[]
    data: InitiateData
}

export interface StringDictionary {
    [key: string]: never;
}

export interface BaseInitiateData {
    forventetInntekt: number;
    forventetInntektAnnenForelder: number | null;
    inntektsgrense: number;
    kompensasjonsgrad: number;
    grenseStoppAvUfoeretrygd: number;
    aktuelleAar: number[];
    hasVarigTilrettelagtArbeid: boolean;
    hasGjenlevendeTillegg: boolean;
}

export interface WithBarneTilleggFellesBarn extends BaseInitiateData {
    hasBarneTilleggFellesbarn: true;
    grenseStoppAvBarnetilleggFellesbarn: number;
    fribelopBarnetilleggFellesbarn: number;
}

export interface WithoutBarneTilleggFellesBarn extends BaseInitiateData {
    hasBarneTilleggFellesbarn: false;
    grenseStoppAvBarnetilleggFellesbarn: null;
    fribelopBarnetilleggFellesbarn: null;
}

export interface WithBarnetilleggSaerkullsbarn extends BaseInitiateData {
    hasBarnetilleggSaerkullsbarn: true,
    grenseStoppAvBarnetilleggSaerkullsbarn: number,
    fribelopBarnetilleggSaerkullsbarn: number,
}

export interface WithoutBarnetilleggSaerkullsbarn extends BaseInitiateData {
    hasBarnetilleggSaerkullsbarn: false,
    grenseStoppAvBarnetilleggSaerkullsbarn: null,
    fribelopBarnetilleggSaerkullsbarn: null,
}

export type InitiateData = (WithBarneTilleggFellesBarn | WithoutBarneTilleggFellesBarn) & (WithBarnetilleggSaerkullsbarn | WithoutBarnetilleggSaerkullsbarn);

export interface InntektInnfylling {
    brukerinntekt: ForventedeInntekter
    annenForelderInntekt: ForventedeInntekter
}

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


