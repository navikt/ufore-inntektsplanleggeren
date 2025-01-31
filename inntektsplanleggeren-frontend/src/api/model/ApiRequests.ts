import {MessageCodes, MessageTypes} from "@/api/model/MessageCodes";
import {StatusCodes} from "@/api/model/StatusCodes";


export type Message = {
    messageCode: MessageCodes;
    details: string;
    type: MessageTypes;
    metadata: Record<string, unknown>;
};

export interface InitiateResponse {
    messages: Message[];
    data: InitiateData;
}

export interface GetInntektsgrenseResponse {
    messages: Message[]
    data: InitiateData
}

export interface BaseInitiateData {
    forventetInntekt: Record<number, number>;
    forventetInntektAnnenForelder: Record<number, number | null>;
    inntektsgrense: number;
    kompensasjonsgrad: number;
    grenseStoppAvUfoeretrygd: number;
    aktuelleAar: number[];
    annetRelevantAar: number | null;
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

export type InntektDetaljer = {
    maned: number;
    belop: number;
    inntektsgivere: string[];
};

export type PersonInntekter = {
    arbeidsinntekt: number | null;
    andrePensjonsgivendeYtelser: number | null;
    naeringsinntekt: number | null;
    inntektUtland: number | null;
    pensjonUtland: number | null;
};

export type ForventedeInntekterResponse = {
    bruker: PersonInntekter;
    eps: PersonInntekter | null;
};

export type InntekterResponse = {
    arbeidsinntektOgYtelserHittilIAar: InntektDetaljer[];
    pensjonFraAndreHittilIAar: InntektDetaljer[];
    forventedeInntekter: ForventedeInntekterResponse;
    uforeHeleAaret: boolean;
    epsPid: string | null;
};

export type SubmitInntekterRequest = {
    bruker: PersonInntekter;
    eps: PersonInntekter | null;
};

export type SimulationResponse = {
    messages: Message[];
    result: SimulationResult;
};

export type SimulationResult = {
    uforetrygd: SimulationDetail;
    forventetInntekt: SimulationDetail;
    barnetilleggFellesbarn: SimulationDetail | null;
    barnetilleggSaerkullsbarn: SimulationDetail | null;
    gjenlevendetillegg: SimulationDetail | null;
    sum: SimulationDetail;
};

export type SendApplicationResponse = {
    messages: Message[];
    status: string;
    innsendingsTidspunkt: string
};

export type SimulationDetail = {
    monthly: PayDetail;
    yearly: PayDetail;
};

export type PayDetail = {
    before: number | null;
    after: number;
};

export type StatusResponse = {
    "registeringsTidspunktEndring": Date,
    "status": StatusCodes,
    "sakId": number | null,
    "maandedligeUtbetalinger": {
    "fom": Date,
        "beloep": number
} | null,
    "mottarBarnetilleggForFellesBarn": boolean,
    "forventetAarligInntekt": number | null,
    "forventetAarligInntektEps": number | null
}

export type FormState = {
    year : number | null;
    brukerInntekter: PersonInntekter | null;
    epsInntekter: PersonInntekter | null;
}


