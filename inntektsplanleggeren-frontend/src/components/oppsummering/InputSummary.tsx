import {PersonInntekter} from "@/api/model/ApiRequests";
import {BodyShort, VStack} from "@navikt/ds-react";
import {FormatKroner} from "@/components/utils/FormatKroner";

export const InputSummary = (props: { inntekter: PersonInntekter }) => {

    return (
<VStack gap="1">
        <BodyShort>Arbeidsinntekt og pensjonsgivende ytelser: <FormatKroner value={props.inntekter.arbeidsinntekt ?? 0}/></BodyShort>
        <BodyShort>Næringsinntekt: <FormatKroner value={props.inntekter.naeringsinntekt ?? 0}/></BodyShort>
        <BodyShort>Inntekt fra utlandet: <FormatKroner value={props.inntekter.inntektUtland ?? 0}/></BodyShort>
        <BodyShort>Pensjoner og uførepensjon fra andre enn Folketrygden: <FormatKroner value={props.inntekter.andrePensjonsgivendeYtelser ?? 0}/></BodyShort>
        <BodyShort>Pensjoner fra utlandet: <FormatKroner value={props.inntekter.pensjonUtland ?? 0}/></BodyShort>
    <BodyShort><strong>Sum forventede inntekt: <FormatKroner value={(props.inntekter.arbeidsinntekt ?? 0) + (props.inntekter.naeringsinntekt ?? 0) + (props.inntekter.inntektUtland ?? 0) + (props.inntekter.andrePensjonsgivendeYtelser ?? 0) + (props.inntekter.pensjonUtland ?? 0)}/></strong></BodyShort>
</VStack>

    );
};