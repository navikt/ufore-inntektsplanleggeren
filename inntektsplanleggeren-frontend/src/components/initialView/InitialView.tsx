
import {Accordion, BodyLong, Box, Heading, Link} from "@navikt/ds-react";
import {UforetrydgInBarnetilleggOgGjenlevendetillegg, gradertUforetrygCard, varigTilrettlagtArbeid} from "@/components/dinInntektsgrenseCard/DinInntektsgrenseCard";



export function InitialView(props: {
    aktivSamboer: boolean
}) {
    return (
        <div>
            {/*todo: extract this into a textbox*/}
            <Box background="surface-info-subtle" borderRadius="xlarge" padding="6" className="melde-fra-box">
                <Heading size={"small"} level={"2"}>Nåværende registrert forventet inntekt i tillegg til uføretrygd</Heading>
                <BodyLong>
                    Dette tallet kan komme fra en tidligere registrering eller være basert på fjorårets inntekt.
                    Din forventede inntekt: 330 000 kr
                    Annen forelder du bor med sin forventede inntekt: 550 000 kr
                </BodyLong>
            </Box>

            {UforetrydgInBarnetilleggOgGjenlevendetillegg("a", "b", "c")}
            {gradertUforetrygCard("a", "b", "c")}
            {varigTilrettlagtArbeid("a", "b", "c")}

            <Heading size={"medium"} level={"2"}>I inntektsplanleggeren kan du</Heading>
            <BodyLong>
                <li>se hvor mye du vil få i uføretrygd ved siden av inntekt</li>
                <li>melde inn forventet inntekt til oss</li>
                Du kan melde inn flere ganger hvis du ser at inntekten blir høyere eller lavere enn først forventet.
                <Link href="#">Å kombinere arbeid og uføretrygd.</Link>
            </BodyLong>


            <Accordion>
                <Accordion.Item>
                    <Accordion.Header>Accordion Item 1</Accordion.Header>
                    <Accordion.Content>
                        Text 1 Text 1 Text 1
                    </Accordion.Content>
                </Accordion.Item>
                <Accordion.Item>
                    <Accordion.Header>Accordion Item 2</Accordion.Header>
                    <Accordion.Content>
                        Text 2 Text 2 Text 2
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
            <Link href="#">Har du spørsmål?  Kontakt oss</Link>
        </div>
    )
}