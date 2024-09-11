
import {Accordion, BodyLong, Box, Button, Heading, VStack, Link as NavLink} from "@navikt/ds-react";
import {UforetrydgInBarnetilleggOgGjenlevendetillegg, gradertUforetrygCard, varigTilrettlagtArbeid} from "@/components/dinInntektsgrenseCard/DinInntektsgrenseCard";
import {Link} from "react-router-dom";
import React, {useContext} from "react";
import {YearView} from "@/components/YearView";
import "./InitialView.css"
import Body from "@navikt/ds-react/esm/table/Body";
import {DisplayData} from "@/api/apiFetching";
import {DataContext} from "@/DataContextProvider";



// eslint-disable-next-line @typescript-eslint/no-unused-vars
export function InitialView(props: {
    setCounter: (value: number) => void
    // displayData: DisplayData
    // aktivSamboer: boolean
    // setCounter: (value: number) => void
    // aktuelleAar: number[]
    //
    // forventetInntekt: number
    // forventetInntektAnnenForelder: number | null
    // inntektsgrense: number,
    // kompensasjonsgrad: number,
    // grenseStoppAvUfoeretrygd: number,
    // harVarigTilrettelagtArbeid: boolean,
    // harBarneTillegg: boolean,
    // harGjenlevendeTillegg: boolean
}) {
    const {displayData} = useContext(DataContext)

    return (
        <div>
            <VStack>
                <Box  borderRadius="xlarge" padding="4" borderWidth="1" className="top-box">
                    <VStack>
                        <Heading size={"small"} level={"2"}>Nåværende registrert forventet inntekt i tillegg til uføretrygd</Heading>
                            <BodyLong> Dette tallet kan komme fra en tidligere registrering eller være basert på fjorårets inntekt.</BodyLong>
                            {/*<BodyLong> Din forventede inntekt: {forventetInntekt} kr </BodyLong>*/}
                            <BodyLong> Annen forelder du bor med sin forventede inntekt: 550 000 kr</BodyLong>

                    </VStack>
                </Box>

                {UforetrydgInBarnetilleggOgGjenlevendetillegg("a", "b", "c")}
                {gradertUforetrygCard("a", "b", "c")}
                {varigTilrettlagtArbeid("a", "b", "c")}

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

                {/*{ console.debugger("A") }*/}
                <YearView availableYears={displayData.aktuelleAar} infoType={1}></YearView>

                <VStack>
                    <Button as={Link} to="/forventede-inntekter" variant="primary">
                        Start inntektsplanlegger
                    </Button>
                </VStack>
            </VStack>
        </div>
    )
}