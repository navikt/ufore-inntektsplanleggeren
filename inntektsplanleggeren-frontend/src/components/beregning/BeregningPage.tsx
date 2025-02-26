import { Alert, BodyLong, Button, Heading, HStack, ReadMore, VStack, Link } from "@navikt/ds-react";
import { FormEvent, MouseEvent, useContext, useEffect } from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { FormStateContext } from "@/context/FormData";
import { SimulationTable } from "@/components/beregning/SimulationTable";
import { DataContext } from "@/DataContextProvider";
import { getFullPathForPage, PageLinks } from "@/FormContainer";
import { MessageCodes } from "@/api/model/MessageCodes";
import { Graph } from "@/components/beregning/Graph";
import { InputSummary } from "@/components/beregning/InputSummary";
import { ArrowLeftIcon, ArrowRightIcon } from "@navikt/aksel-icons";
import { FormatKroner } from "@/components/utils/FormatKroner";
import { CancelConfirmationModal } from "@/components/common/CancelConfirmationModal";
import { BeregningWarnings } from "@/components/beregning/BeregningWarnings";
import { ErrorView } from "@/components/common/Error";

export const BeregningPage = () => {
  const { selectedYear, setFormStep, brukerinntekt, annenForelderInntekt } = useContext(FormStateContext);
  const { simulationResponse, errorMessage } = useContext(DataContext);
  const navigate = useNavigate();

  useEffect(() => {
    setFormStep(2)
  }, [setFormStep]);

  const handleSubmit = async (e: MouseEvent | FormEvent) => {
    e.preventDefault();
    navigate(getFullPathForPage(PageLinks.OPPSUMMERING));
  }

  const showSimulering = !(simulationResponse?.messages.some(message => message.messageCode === MessageCodes.FAKTOROMREGNET_ELLER_MANUELT_OVERSTYRT
    || message.messageCode === MessageCodes.SIMULERING_CONTAINS_MOTREGNING))

  if (errorMessage) {
    return <ErrorView message={errorMessage} />
  }

  if (simulationResponse?.result) return (
    <VStack gap="8">
      {simulationResponse.messages.some(message => message.messageCode === MessageCodes.USER_HAS_NO_LOPENDE_VEDTAK_YET) ?
        <Alert variant="warning">
          Du kan ikke bruke inntektsplanleggeren ennå. Din inntekt kan registreres her fra måneden før din første utbetaling av uføretrygd.
        </Alert> : null
      }
      <section>
        <Heading level="3" size="medium" style={{ marginBottom: "20px" }}>Din inntekt og uføretrygd før skatt i {selectedYear}</Heading>
        <ReadMore header="Inntekt du har lagt inn">
          <VStack gap="7">
            <VStack>
              <Heading level="4" size="small">Din forventede inntekt i {selectedYear}</Heading>
              <InputSummary inntekter={brukerinntekt}></InputSummary>
            </VStack>
            {annenForelderInntekt ?
              <VStack>
                <Heading level="4" size="small">Annen forelders forventede inntekt i {selectedYear}</Heading>
                <InputSummary inntekter={annenForelderInntekt} sumOverrideText="Sum annen forelders inntekt"></InputSummary>
              </VStack> : null}
          </VStack>
        </ReadMore>
      </section>

      <BeregningWarnings messages={simulationResponse.messages} />

      {showSimulering ?
        <>
          <section>
            <Heading level="3" size="medium">Oversikt i graf</Heading>
            <div style={{ marginTop: "10px" }}>
              <Graph simulationResult={simulationResponse?.result} aria-hidden={true} />
            </div>
          </section>

          <section>
            <Heading level="3" size="medium">Oversikt i tabell</Heading>
            {simulationResponse?.result &&
              <SimulationTable simulationResult={simulationResponse.result}></SimulationTable>}
          </section>
          <section>
            <BodyLong><strong>Månedlig utbetaling av uføretrygd med dine endringer, før
              skatt: <FormatKroner
                value={simulationResponse?.result.sum.monthly.after ?? 0} /></strong></BodyLong>
            <ReadMore header="Månedsbeløp spesifisert">
              <BodyLong>{simulationResponse?.result?.gjenlevendetillegg ? "Uføretrygd inkludert gjenlevendetillegg: " : "Uføretrygd: "}
                <FormatKroner
                  value={(simulationResponse?.result?.uforetrygd.monthly.after ?? 0) + (simulationResponse?.result.gjenlevendetillegg?.monthly.after ?? 0)} /></BodyLong>
              {simulationResponse?.result.barnetilleggFellesbarn !== null ?
                <BodyLong>Barnetillegg for fellesbarn: <FormatKroner
                  value={(simulationResponse?.result.barnetilleggFellesbarn.monthly.after ?? 0)} /></BodyLong> : null}
              {simulationResponse?.result.barnetilleggSaerkullsbarn !== null ?
                <BodyLong>Barnetillegg for særkullsbarn: <FormatKroner
                  value={(simulationResponse?.result.barnetilleggSaerkullsbarn.monthly.after ?? 0)} /></BodyLong> : null}
              {simulationResponse?.result.sum.monthly.after !== null ?
                <BodyLong><strong>Totalt per måned: <FormatKroner
                  value={simulationResponse?.result.sum.monthly.after} /></strong></BodyLong> : null}
            </ReadMore>
          </section>
        </>
        :
        <Alert variant="warning">
          Vi kan dessverre ikke vise hvordan den nye inntekten din vil påvirke utbetalingen din av uføretrygd.
          Du kan likevel sende inn din inntektsendring.
        </Alert>
      }

      <BodyLong><strong>Har du spørsmål? <Link href={PageLinks.KONTAKT} target="_blank">Kontakt oss (åpnes i ny fane)</Link></strong></BodyLong>

      <VStack gap="3">
        <HStack gap="4">
          <Button as={RouterLink} to={getFullPathForPage(PageLinks.FORVENTET_INNTEKT)} iconPosition="left" icon={<ArrowLeftIcon aria-hidden />} variant="secondary">
            Endre beløp i beregning
          </Button>
          <Button variant="primary" iconPosition="right" icon={<ArrowRightIcon aria-hidden />} onClick={handleSubmit}>
            Gå til innsending
          </Button>
        </HStack>
        <CancelConfirmationModal />
      </VStack>
    </VStack>
  );
};
