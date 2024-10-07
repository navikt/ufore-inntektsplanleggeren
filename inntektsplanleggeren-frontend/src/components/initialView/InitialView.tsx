import {
    Accordion,
    BodyLong,
    Box,
    Button,
    Heading,
    VStack,
    Link as NavLink,
    Alert,
    List,
    BodyShort
} from "@navikt/ds-react";
import {InntektsgrenseCard} from "@/components/initialView/DinInntektsgrenseCard";
import { useNavigate } from "react-router-dom";
import React, {useContext, useState} from "react";
import {YearView} from "@/components/YearView";
import "./InitialView.css"
import {DataContext} from "@/DataContextProvider";
import {FormStateContext} from "@/context/FormData";

export function InitialView() {
    const {initialViewData, warningMessage} = useContext(DataContext)
    const {selectedYear} = useContext(FormStateContext)
    const [errorMessage, setErrorMessage] = useState<string | null>(null)
    const navigate = useNavigate()

    const handleButtonClick = () => {
        if (!selectedYear) {
            setErrorMessage("Du må velge et år før du kan starte inntektsplanleggeren.")
        } else {
            const params = new URLSearchParams({ year: selectedYear.toString() })
            navigate(`/forventede-inntekter?${params.toString()}`)
        }
    }

    return (
        <VStack gap="4">
            {warningMessage.length > 0 ?
                <Alert variant="warning">{warningMessage[0].details}</Alert> :

                <Box borderRadius="xlarge" padding="4" borderWidth="1" className="top-box">
                    <VStack>
                        <Heading size={"small"} level={"2"}>Nåværende registrert forventet inntekt i tillegg til uføretrygd</Heading>
                        <BodyLong> Dette tallet kan komme fra en tidligere registrering eller være basert på fjorårets inntekt.</BodyLong>
                        <BodyLong> Din forventede inntekt: {initialViewData.forventetInntekt}kr </BodyLong>
                        {initialViewData.forventetInntektAnnenForelder ?
                            <BodyLong> Annen forelder du bor med sin forventede inntekt: {initialViewData.forventetInntektAnnenForelder}kr</BodyLong> : <></>
                        }
                    </VStack>
                </Box>
            }

            <InntektsgrenseCard displayData={initialViewData}/>

            <section>
                <List title="I inntektsplanleggeren kan du">
                    <List.Item>se hvor mye du vil få i uføretrygd ved siden av inntekt</List.Item>
                    <List.Item>melde inn forventet inntekt til oss</List.Item>
                </List>

                <BodyShort spacing>
                    Du kan melde inn flere ganger hvis du ser at inntekten blir høyere eller lavere enn først forventet.
                </BodyShort>

                <BodyShort spacing>
                    <NavLink href="#">Å kombinere arbeid og uføretrygd.</NavLink>
                </BodyShort>
            </section>

            <Accordion>
                <Accordion.Item>
                    <Accordion.Header>Slik fungerer inntektsplanleggeren</Accordion.Header>
                    <Accordion.Content>
                        Text 1 Text 1 Text 1
                    </Accordion.Content>
                </Accordion.Item>
                <Accordion.Item>
                    <Accordion.Header>Om inntekten har endret seg/varierer i løpet av året</Accordion.Header>
                    <Accordion.Content>
                        Text 2 Text 2 Text 2
                    </Accordion.Content>
                </Accordion.Item>
                <Accordion.Item>
                    <Accordion.Header>Dette skjer etter innsending av inntektsmeldingen</Accordion.Header>
                    <Accordion.Content>
                        Text 3 Text 3 Text 3
                    </Accordion.Content>
                </Accordion.Item>
            </Accordion>

            <section>
                <Heading size="medium" level="2" spacing>Har du (bostøtte eller) andre ytelser i tillegg til uføretrygd? </Heading>

                <BodyLong spacing>
                    Inntektsplanleggeren påvirker kun uføretrygd, samt barnetillegg og gjenlevendetillegg på uføretrygden dersom du har det. Du ser ikke hvordan ny inntekt påvirker eventuelle andre ytelser du har fra oss, eller eventuelle ytelser du har fra andre ordninger enn NAV.
                </BodyLong>

                <BodyLong spacing>
                    <b>Vær obs på at enkelte ytelser, for eksempel bostøtte, kan ha egne grenser for hvor mye man kan tjene før disse bortfaller. Hvis du har andre ytelser enn uføretrygd og eventuelt barnetillegg eller gjenlevendetillegg, er det viktig at du undersøker hvordan inntekt vil påvirke dem.</b>
                </BodyLong>

                <BodyShort spacing>
                    <NavLink href="#">Har du spørsmål?  Kontakt oss</NavLink>
                </BodyShort>
            </section>

            <YearView availableYears={initialViewData.aktuelleAar} infoType={1}></YearView>

            {errorMessage && <Alert variant="error">{errorMessage}</Alert>}

            <VStack>
                <Button onClick={handleButtonClick} variant="primary">
                    Start inntektsplanlegger
                </Button>
                {/*/!*<Button as={Link} to="/forventede-inntekter" variant="primary">*!/*/}
                {/*    Start inntektsplanlegger*/}
                {/*</Button>*/}
            </VStack>
        </VStack>
    )
}