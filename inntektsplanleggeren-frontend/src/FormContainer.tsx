import { useContext } from "react";
import { Link as RouterLink, Outlet } from "react-router-dom";
import { Heading, FormProgress, VStack, Link } from "@navikt/ds-react";
import { FormStateContext } from "@/context/FormData";
import { ArrowLeftIcon } from "@navikt/aksel-icons";
import { DataContext } from "@/DataContextProvider";
import { ErrorView } from "@/components/common/Error";
import { getPidQueryParamString } from "@/components/utils/UrlUtil";

export const DESKTOP_WIDTH = 768

export const FormContainer = () => {
  const { formStep, previousYear } = useContext(FormStateContext);
  const { errorMessage } = useContext(DataContext)

  if (errorMessage) {
    return <ErrorView message={errorMessage} />
  }

  return (
    <VStack gap="8">
      {(formStep !== null) &&
        <VStack gap="6">
          <div>
            <Link as={RouterLink} to={(previousYear === null && formStep === 1) ? getPreviousPage(0) : getPreviousPage(formStep)} ><ArrowLeftIcon aria-hidden /> Tilbake</Link>
          </div>
          <Heading level="2" size="large">{getPageName(formStep)}</Heading>
        </VStack>}
      {formStep && <FormProgress totalSteps={3} activeStep={formStep}>
        <FormProgress.Step
          as={RouterLink}
          to={getFullPathForPage(PageLinks.FORVENTET_INNTEKT)}
          completed={isStepCompleted(formStep, 1)}
          interactive={isStepEnabled(formStep, 1)}>
          {getPageName(1)}
        </FormProgress.Step>
        <FormProgress.Step
          as={RouterLink}
          to={getFullPathForPage(PageLinks.BEREGNING)}
          completed={isStepCompleted(formStep, 2)}
          interactive={isStepEnabled(formStep, 2)}>
          {getPageName(2)}
        </FormProgress.Step>
        <FormProgress.Step
          as={RouterLink}
          to={getFullPathForPage(PageLinks.OPPSUMMERING)}
          completed={isStepCompleted(formStep, 3)}
          interactive={isStepEnabled(formStep, 3)}>
          {getPageName(3)}
        </FormProgress.Step>
      </FormProgress>}
      <Outlet />
    </VStack>
  );
};

export enum PageNames {
  FORRIGE_INNTEKTER = "Forrige inntekter",
  FORVENTEDE_INNTEKTER = "Forventede inntekter",
  BEREGNING = "Beregning",
  OPPSUMMERING = "Oppsummering - se over før du sender inn",
  KVIITTERING = "Kvittering"
}

const isStepEnabled = (currentStepIndex: number, stepToCheckIndex: number) => {
  return currentStepIndex >= stepToCheckIndex
}

const isStepCompleted = (currentStepIndex: number, stepToCheckIndex: number) => {
  return currentStepIndex > stepToCheckIndex
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
  FORRIGE_INNTEKTER = "/forrige-inntekter",
  FORVENTET_INNTEKT = "/forventet-inntekt",
  BEREGNING = "/beregning",
  OPPSUMMERING = "/oppsummering",
  KVITTERING = "/kvittering",

  KONTAKT = "https://www.nav.no/kontaktoss"
}

export const PAGE_LINKS = {
  0: "/",
  1: "/forrige-inntekter",
  2: "/forventet-inntekt",
  3: "/beregning",
  4: "/oppsummering",
  5: "/kvittering"
};

export const getFullPathForPage = (pageLink: PageLinks) => {
  return pageLink + getPidQueryParamString()
}

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
    case 5:
      return PAGE_LINKS[5];
    default:
      return PAGE_LINKS[0];
  }
}

const getPreviousPage = (index: number): string => getPage(index) + getPidQueryParamString();
