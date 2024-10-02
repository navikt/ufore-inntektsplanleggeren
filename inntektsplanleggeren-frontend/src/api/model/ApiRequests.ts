import {Familierelasjon, InntektInnfylling} from "@/api/apiFetching";

export type RelasjonPersondata = {
    navn: Navn | null,
    tilgangsbegrensning: string | null,
    foedselsdato: string | null,
    doedsdato: string | null
}

export type Navn = {
    etternavn: string,
    mellomnavn: string | null,
    fornavn: string
}

export type HenteFamilierelasjonsResponse = {
    familierelasjoner: Familierelasjon[],
    samboerforhold: Samboerforhold[]
}

export type HentSamboerforholdResponse = {
    samboerforhold: Samboerforhold[]
}

export type Samboerforhold = {
    pid: string
    fom: string
    tom: string | null //endre til Dato?
    periodeId: number
}

export type OpprettSamboerforholdRequest = {
    pidSamboer: string
    fom: string
}

export type EndreSamboerforholdRequest = {
    fom: string
    tom: string | null
}

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