import { Alert, Button, FormSummary, HStack, VStack } from "@navikt/ds-react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { getFullPathForPage, PageLinks } from "@/FormContainer";
import { ArrowLeftIcon, ArrowRightIcon } from "@navikt/aksel-icons";
import { CancelConfirmationModal } from "@/components/common/CancelConfirmationModal";
import React, { FormEvent, MouseEvent, useContext, useEffect, useState } from "react";
import { send } from "@/api/apiFetching";
import { FormStateContext } from "@/context/FormData";
import { DataContext } from "@/DataContextProvider";
import { FormatKroner } from "@/components/utils/FormatKroner";
import { ErrorCode, ErrorResponse } from "@/components/common/Error";

export const OppsummeringPage = () => {
  const navigate = useNavigate();
  const { selectedYear, setFormStep, brukerinntekt, annenForelderInntekt, getBrukerinntektSum, getAnnenForelderInntektSum } = useContext(FormStateContext);
  const { simulationResponse, setSendResponse, setErrorMessage } = useContext(DataContext);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    setFormStep(3)
  }, [setFormStep]);

  const handleSubmit = async (e: MouseEvent | FormEvent) => {
    e.preventDefault();

    try {
      setIsLoading(true);
      const result = await send(brukerinntekt, annenForelderInntekt, selectedYear);
      if (result instanceof ErrorResponse) {
        setErrorMessage(result.message)
        setIsLoading(false);
      } else {
        setSendResponse(result);
        navigate(getFullPathForPage(PageLinks.KVITTERING));
      }
    } catch (error) {
      setErrorMessage(ErrorCode.GENERIC_ERROR)
      setIsLoading(false);
    }

    navigate(getFullPathForPage(PageLinks.KVITTERING));
  }

  return (
    <VStack gap="12">
      <FormSummary>
        <FormSummary.Header>
          <FormSummary.Heading level="3">Opplysningene du sender inn</FormSummary.Heading>
          <FormSummary.EditLink as={RouterLink} to={getFullPathForPage(PageLinks.FORVENTET_INNTEKT)} />
        </FormSummary.Header>
        <FormSummary.Answers>
          <FormSummary.Answer>
            <FormSummary.Label>Din forventede inntekt i {selectedYear}</FormSummary.Label>
            <FormSummary.Value><FormatKroner value={getBrukerinntektSum()} /></FormSummary.Value>
          </FormSummary.Answer>
          {annenForelderInntekt &&
            <FormSummary.Answer>
              <FormSummary.Label>Annen forelders forventede inntekt i {selectedYear}</FormSummary.Label>
              <FormSummary.Value><FormatKroner value={getAnnenForelderInntektSum() ?? 0} /></FormSummary.Value>
            </FormSummary.Answer>}
        </FormSummary.Answers>
      </FormSummary>

      {simulationResponse?.messages.some(message => message.messageCode === "EPS_INNTEKT_CHANGED") &&
        <Alert variant="info">
          Husk at inntektene du melder inn for annen forelder bare brukes for å beregne barnetillegget til uføretrygden din. Hvis den andre forelderen har utbetalinger fra oss, må hen selv også melde fra om ny inntekt til oss.
        </Alert>
      }

      <VStack gap="3">
        <HStack gap="4">
          <Button as={RouterLink} to={getFullPathForPage(PageLinks.BEREGNING)} iconPosition="left" icon={<ArrowLeftIcon aria-hidden />} variant="secondary">
            Tilbake
          </Button>
          <Button variant="primary" iconPosition="right" icon={<ArrowRightIcon aria-hidden />} onClick={handleSubmit} loading={isLoading}>
            Send inn
          </Button>
        </HStack>
        <CancelConfirmationModal />
      </VStack>
    </VStack>
  )

}
