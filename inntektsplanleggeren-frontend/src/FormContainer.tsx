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
            { (formStep !== null) ?
                <VStack>
                    <HStack>
                        <Button as={Link} to={getPreviousPage(formStep)} variant="tertiary"  iconPosition="left" icon={<ArrowLeftIcon aria-hidden />}>Tilbake</Button>
                    </HStack>
                    <Heading level="1" size="large">{getPageName(formStep)}</Heading>
                </VStack> : null }
            { formStep ? <FormProgress totalSteps={3} activeStep={formStep} interactiveSteps={false}>
                <FormProgress.Step href={BASE_PATH + PageLinks.FORVENTEDE_INNTEKTER} completed>{getPageName(1)}</FormProgress.Step>
                <FormProgress.Step href={BASE_PATH + PageLinks.BEREGNING}>{getPageName(2)}</FormProgress.Step>
                <FormProgress.Step href={BASE_PATH + PageLinks.OPPSUMMERING}>{getPageName(3)}</FormProgress.Step>
            </FormProgress> : null }
            <Outlet/>
        </VStack>
    );
};

export enum PageNames {
    FORVENTEDE_INNTEKTER = "Forventede inntekter",
    BEREGNING = "Beregning",
    OPPSUMMERING = "Oppsummering - se over før du sender inn",
    KVIITTERING = "Kvittering"
}

const getPageName = (index: number): string => {
    switch (index) {
        case 1:
            return PageNames.FORVENTEDE_INNTEKTER;
        case 2:
            return PageNames.BEREGNING;
        case 3:
            return PageNames.OPPSUMMERING;
        case 4:
            return PageNames.KVIITTERING;
        default:
            return PageNames.FORVENTEDE_INNTEKTER;
    }
}

export enum PageLinks {
    INDEX = "/",
    FORVENTEDE_INNTEKTER = "/forventede-inntekter",
    BEREGNING = "/beregning",
    OPPSUMMERING = "/oppsummering",
    KVITTERING = "/kvittering",

    KONTAKT = "https://www.nav.no/kontaktoss",
    DIN_UFORE = "#" //TODO actual link?
}

export const PAGE_LINKS = {
    0: "/",
    1: "/forventede-inntekter",
    2: "/beregning",
    3: "/oppsummering",
    4: "/kvittering"
};

const getPage = (index: number): string => {
    switch (index) {
        case 1:
            return PAGE_LINKS[1];
        case 2:
            return PAGE_LINKS[2];
        case 3:
            return PAGE_LINKS[3];
        case 4:
            return PAGE_LINKS[4];
        default:
            return PAGE_LINKS[0];
    }
}

const getPreviousPage = (index: number): string => getPage(index - 1);
