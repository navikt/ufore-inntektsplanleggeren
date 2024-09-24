import {Accordion, BodyLong, Box, Button, Heading, VStack, Link as NavLink, Alert} from "@navikt/ds-react";
import {InntektsgrenseCard} from "@/components/dinInntektsgrenseCard/DinInntektsgrenseCard";
import {Link} from "react-router-dom";
import React, {useContext, useState} from "react";
import {YearView} from "@/components/YearView";
import "./InitialView.css"
import {DataContext} from "@/DataContextProvider";
import {FormStateContext} from "@/SelectedYear/SelectedYear";

export function InitialView() {
    const {displayData, warningMessage} = useContext(DataContext)
    const {selectedYear, setSelectedYear} = useContext(FormStateContext)
    const [errorMessage, setErrorMessage] = useState<string | null>(null)

    // const handleButtonClick = () => {
    //     if (!selectedYear) {
    //         setErrorMessage("Du må velge et år før du kan starte inntektsplanleggeren.")
    //     } else {
    //
    //
    //     }
    // }

    return (
        <div>
            <VStack gap={{ xs: "4", sm: "6", md: "8", lg: "10", xl: "12" }}>
                {warningMessage.length > 0 ?
                    <Alert variant="warning">{warningMessage[0].details}</Alert> :
                    <Box  borderRadius="xlarge" padding="4" borderWidth="1" className="top-box">
                        <VStack>
                            <Heading size={"small"} level={"2"}>Nåværende registrert forventet inntekt i tillegg til uføretrygd</Heading>
                            <BodyLong> Dette tallet kan komme fra en tidligere registrering eller være basert på fjorårets inntekt.</BodyLong>
                            <BodyLong> Din forventede inntekt: {displayData.forventetInntekt}kr </BodyLong>
                            {displayData.forventetInntektAnnenForelder ?
                                <BodyLong> Annen forelder du bor med sin forventede inntekt: {displayData.forventetInntektAnnenForelder}kr</BodyLong> : <></>
                            }
                        </VStack>
                    </Box>
                }

                <InntektsgrenseCard displayData={displayData}/>

                <Heading size={"medium"} level={"2"}>I inntektsplanleggeren kan du</Heading>
                <BodyLong>
                    <li>se hvor mye du vil få i uføretrygd ved siden av inntekt</li>
                    <li>melde inn forventet inntekt til oss</li>
                    Du kan melde inn flere ganger hvis du ser at inntekten blir høyere eller lavere enn først forventet.
                    <NavLink href="#">Å kombinere arbeid og uføretrygd.</NavLink>
                </BodyLong>

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

                <Heading size={"medium"} level={"2"}>Har du (bostøtte eller) andre ytelser i tillegg til uføretrygd? </Heading>
                <BodyLong>
                    Inntektsplanleggeren påvirker kun uføretrygd, samt barnetillegg og gjenlevendetillegg på uføretrygden dersom du har det. Du ser ikke hvordan ny inntekt påvirker eventuelle andre ytelser du har fra oss, eller eventuelle ytelser du har fra andre ordninger enn NAV.
                </BodyLong>
                <BodyLong>
                    <b> Vær obs på at enkelte ytelser, for eksempel bostøtte, kan ha egne grenser for hvor mye man kan tjene før disse bortfaller. Hvis du har andre ytelser enn uføretrygd og eventuelt barnetillegg eller gjenlevendetillegg, er det viktig at du undersøker hvordan inntekt vil påvirke dem. </b>
                </BodyLong>
                <NavLink href="#">Har du spørsmål?  Kontakt oss</NavLink>

                <YearView availableYears={displayData.aktuelleAar} setYear={setSelectedYear} infoType={1}></YearView>

                {errorMessage && <Alert variant="error">{errorMessage}</Alert>}

                <VStack>
                    {/*<Button onClick={handleButtonClick} variant="primary">*/}
                    {/*    Start inntektsplanlegger*/}
                    {/*</Button>*/}
                    <Button as={Link} to="/forventede-inntekter" variant="primary">
                        Start inntektsplanlegger
                    </Button>
                </VStack>
            </VStack>
        </div>
    )
}