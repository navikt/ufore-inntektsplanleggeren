import React, {useContext} from "react";
import {Link, Outlet} from "react-router-dom";
import {Heading, FormProgress, VStack, Button, HStack} from "@navikt/ds-react";
import {FormStateContext} from "@/context/FormData";
import {ArrowLeftIcon} from "@navikt/aksel-icons";
import {BASE_PATH} from "@/routes";



export const FormContainer = () => {


    const { formStep } = useContext(FormStateContext);

    return (
        <VStack gap="5">
            <HStack>
                <Button as={Link} to={PageLinks[formStep-1]} variant="tertiary"  iconPosition="left" icon={<ArrowLeftIcon aria-hidden />}>Tilbake</Button>
            </HStack>
            <Heading level="1" size="large">{PageNames[formStep]}</Heading>
            <FormProgress totalSteps={3} activeStep={formStep} interactiveSteps={false}>
                <FormProgress.Step href={BASE_PATH + PageLinks[1]} completed>{PageNames[1]}</FormProgress.Step>
                <FormProgress.Step href={BASE_PATH + PageLinks[2]}>{PageNames[2]}</FormProgress.Step>
                <FormProgress.Step href={BASE_PATH + PageLinks[3]}>{PageNames[3]}</FormProgress.Step>
            </FormProgress>
            <Outlet/>
        </VStack>
    );
};

export enum PageNames {
    "Forventede inntekter" = 1,
    "Oppsummering før innsending" = 2,
    "Beregning" = 3
}

export enum PageLinks {
    "/" = 0,
    "/forventede-inntekter" = 1,
    "/oppsummering" = 2,
    "/beregning" = 3,
}

