import {
    Accordion,
    BodyLong,
    Button,
    Heading,
    VStack,
    Link as NavLink,
    Alert,
    List,
    BodyShort, GuidePanel, HStack
} from "@navikt/ds-react";
import { ArrowRightIcon } from '@navikt/aksel-icons';
import {InntektsgrenseCard} from "@/components/initialView/DinInntektsgrenseCard";
import { useNavigate } from "react-router-dom";
import React, {useContext, useEffect, useState} from "react";
import {YearView} from "@/components/initialView/YearView";
import "./InitialView.css"
import {DataContext} from "@/DataContextProvider";
import {FormStateContext} from "@/context/FormData";
import {deleteState, getInntekter} from "@/api/apiFetching";
import {ExpectedIncomeBox} from "@/components/initialView/ExpectedIncomeBox";
import {MessageCodes} from "@/api/model/MessageCodes";

export function InitialView() {
    const {initialViewData, messages, setInntekterResponse} = useContext(DataContext)
    const {selectedYear, setBrukerinntekt, setAnnenForelderInntekt} = useContext(FormStateContext)
    const [errorMessage, setErrorMessage] = useState<string | null>(null)
    const navigate = useNavigate()
    const [isLoading, setIsLoading] = useState<boolean>(false)

    useEffect(() => {
        deleteState()
    }, []);
    
    useEffect(() => {
        if (selectedYear !== null) {
            setErrorMessage(null)
        }
    }, [selectedYear]);

    const handleButtonClick = async () => {
        if (!selectedYear) {
            setErrorMessage("Du må velge et år før du kan starte inntektsplanleggeren.")
        } else {
            setIsLoading(true)
            const data = await getInntekter(selectedYear)
            setInntekterResponse(data);
            setBrukerinntekt(data.forventedeInntekter.bruker)
            setAnnenForelderInntekt(data.forventedeInntekter.eps)
            sessionStorage.setItem("selectedYear", selectedYear)
            navigate('/forventede-inntekter')
        }
    }


    if (messages.some(message => message.messageCode === MessageCodes.USER_HAS_NO_UFORE)) {
        return (
            <Alert variant="warning">
                Du har ikke uføretrygd. Derfor kan du ikke bruke inntektsplanleggeren.
            </Alert>
        );
    }


    return (
        <VStack gap="10">
            {/* warningMessage !== null && warningMessage.length > 0 ?*/}
            {/* <Alert variant="warning">{warningMessage[0].details}</Alert> :*/}

            <GuidePanel poster>
                <Heading size="medium" level="2" spacing>Greit å vite</Heading>
                    <BodyShort spacing>Uføretrygd skal sikre deg inntekt når du ikke kan forsørge deg selv på grunn av sykdom eller skade.</BodyShort>
                    <BodyShort spacing>For at vi skal beregne riktig utbetaling av uføretrygden din, må du oppgi hvor mye du forventer å
                    tjene samtidig som du får uføretrygd.</BodyShort>
                    <BodyShort>Dine opplysninger lagres dessverre ikke hvis du logger ut av innteksplanleggeren, eller tar en lang
                        pause. Vi beklager for dette.</BodyShort>
            </GuidePanel>

            <section>
                <List title="I inntektsplanleggeren kan du" size="medium">
                    <List.Item>se hvor mye du vil få i uføretrygd ved siden av inntekt</List.Item>
                    <List.Item>melde inn forventet inntekt til oss</List.Item>
                </List>
                <BodyShort spacing>
                    <NavLink href="https://www.nav.no/uforetrygd#kombinere">Her finner du mer informasjon om å jobbe samtidig som du har uføretrygd.</NavLink>
                </BodyShort>
            </section>

            { initialViewData !== null &&
            <ExpectedIncomeBox forventetInntekt={initialViewData.forventetInntekt}
                               forventetInntektAnnenForelder={initialViewData.forventetInntektAnnenForelder}/>
            }

            <section>
                <BodyLong>
                    Det er viktig at du melder fra hvis inntekten din blir annerledes enn det du har meldt inn tidligere.
                    Det gir mindre risiko for stor tilbakebetaling i etteroppgjøret.
                </BodyLong>
            </section>

            <InntektsgrenseCard displayData={initialViewData}/>


            <Accordion>
                <Accordion.Item>
                    <Accordion.Header>Slik fungerer inntektsplanleggeren</Accordion.Header>
                    <Accordion.Content>
                        <List>
                            <List.Item>Du skal bare oppgi inntekt du har samtidig med uføretrygd.</List.Item>
                            <List.Item>Du oppgir hva du forventer å tjene, så beregner vi riktig utbetaling av uføretrygden din.</List.Item>
                            <List.Item>Får du for mye utbetalt, må du betale tilbake. Får du for lite utbetalt, betaler Nav penger tilbake til deg. Dette kalles etteroppgjør.</List.Item>
                            <List.Item>Du skal oppgi årlig inntekt før skatt (brutto). </List.Item>
                            <List.Item>Når du melder inn en endring i inntekten din, lager vi en ny beregning. Alle de kommende utbetalingene dette året blir like.</List.Item>
                            <List.Item>Vi melder ikke inn dine inntekter til Skatteetaten.</List.Item>
                            <List.Item>Husk å undersøke om du skal inkludere feriepenger og andre utbetalinger.</List.Item>
                            <List.Item>Du kan melde fra om forventet inntekt for neste år fra oktober i år. </List.Item>
                            <List.Item>Har du ingen endring i inntekten din til neste år, bør du likevel melde inn forventet inntekt.</List.Item>
                            <List.Item>Melder du ikke fra om forventet inntekt til neste år, vil vi bruke inntekten du har oppgitt i år og justere den ved årsskiftet.</List.Item>
                        </List>
                    </Accordion.Content>
                </Accordion.Item>
                <Accordion.Item>
                    <Accordion.Header>Usikker på hva du kommer til å tjene?</Accordion.Header>
                    <Accordion.Content>
                        <List>
                            <List.Item>Har du variabel inntekt, kan det være vanskelig å vite hva du kommer til å tjene fremover. Vi stoler på at du melder fra til oss så godt du kan.</List.Item>
                            <List.Item>Ser du at inntekten din blir annerledes enn det du tidligere har meldt inn, bør du melde fra til oss så fort som mulig.</List.Item>
                            <List.Item>Får du endring i inntekt, skal du ikke legge inn den nye årslønnen din, men regne ut antall måneder med gammel årslønn og legge sammen med antall måneder med ny årslønn dette året. </List.Item>
                        </List>
                    </Accordion.Content>
                </Accordion.Item>
                <Accordion.Item>
                    <Accordion.Header>Har du andre pengestøtter i tillegg til uføretrygd fra Nav?</Accordion.Header>
                    <Accordion.Content>
                        Inntektsplanleggeren viser bare hvordan inntekt påvirker uføretrygden. Innsending via inntektsplanleggeren påvirker kun din uføretrygd, og eventuelt barnetillegg og gjenlevendetillegg hvis du har det.
                        Har du andre pengestøtter fra NAV, må du si i fra om ny inntekt til disse. Endring i din inntekt kan påvirke pengestøtter fra andre enn NAV,  og kan det være at du må melde fra om endring i inntekt til disse også.
                    </Accordion.Content>
                </Accordion.Item>
            </Accordion>

            {(initialViewData?.aktuelleAar && initialViewData.aktuelleAar.length > 0) &&
                <VStack gap="10">
                    <YearView error={errorMessage} availableYears={initialViewData.aktuelleAar} infoType={1}></YearView>

                    <HStack>
                        <Button onClick={handleButtonClick} variant="primary" loading={isLoading} iconPosition="right" icon={<ArrowRightIcon aria-hidden />}>
                            Start inntektsplanlegger
                        </Button>
                    </HStack>
                </VStack>
            }
        </VStack>
    )
}

// const delay = (ms: number) => new Promise(res => setTimeout(res, ms)); // TODO: remove after demo.