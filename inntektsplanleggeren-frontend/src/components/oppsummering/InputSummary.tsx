import {PersonInntekter} from "@/api/model/ApiRequests";
import {BodyShort, VStack} from "@navikt/ds-react";
import {FormatKroner} from "@/components/utils/FormatKroner";

export const InputSummary = (props: { inntekter: PersonInntekter }) => {

    return (
<VStack gap="1">
    {props.inntekter.arbeidsinntekt !== null && (
        <BodyShort>Arbeidsinntekt og pensjonsgivende ytelser: <FormatKroner value={props.inntekter.arbeidsinntekt}/></BodyShort>
    )}
    {props.inntekter.naeringsinntekt !== null && (
        <BodyShort>Næringsinntekt: <FormatKroner value={props.inntekter.naeringsinntekt}/></BodyShort>
    )}
    {props.inntekter.inntektUtland !== null && (
        <BodyShort>Inntekt fra utlandet: <FormatKroner value={props.inntekter.inntektUtland}/></BodyShort>
    )}
    {props.inntekter.andrePensjonsgivendeYtelser !== null && (
        <BodyShort>Pensjoner og uførepensjon fra andre enn Folketrygden: <FormatKroner value={props.inntekter.andrePensjonsgivendeYtelser}/></BodyShort>
    )}
    {props.inntekter.pensjonUtland !== null && (
        <BodyShort>Pensjoner fra utlandet: <FormatKroner value={props.inntekter.pensjonUtland}/></BodyShort>
    )}
    <BodyShort><strong>Sum forventede inntekt: <FormatKroner value={(props.inntekter.arbeidsinntekt ?? 0) + (props.inntekter.naeringsinntekt ?? 0) + (props.inntekter.inntektUtland ?? 0) + (props.inntekter.andrePensjonsgivendeYtelser ?? 0) + (props.inntekter.pensjonUtland ?? 0)}/></strong></BodyShort>
</VStack>

    );
};