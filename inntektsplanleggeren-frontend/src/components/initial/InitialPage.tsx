import { Accordion, Alert, BodyLong, BodyShort, GuidePanel, Heading, List, VStack } from '@navikt/ds-react'
import { InntektsgrenseCard } from '@/components/initial/DinInntektsgrenseCard'
import { useNavigate } from 'react-router-dom'
import { useContext, useState } from 'react'
import { YearView } from '@/components/initial/YearView'
import { DataContext } from '@/context/DataContextProvider'
import { FormStateContext } from '@/context/FormData'
import { getInntekter, getInntekterForSimulering } from '@/api/apiFetching'
import { ExpectedIncomeBox } from '@/components/initial/ExpectedIncomeBox'
import { MessageCodes, MessageTypes } from '@/api/model/MessageCodes'
import { getFullPathForPage, PageLinks } from '@/FormContainer'
import { LoadingBox } from '@/components/initial/LoadingBox'
import { ErrorCode, ErrorResponse, ErrorView } from '@/components/common/Error'

export function InitialPage() {
    const { initiateResponse, setInntekterResponse, setPreviousYearInntekterResponse, errorMessage, setErrorMessage } = useContext(DataContext)
    const { setSelectedYear, setBrukerinntekt, setAnnenForelderInntekt } = useContext(FormStateContext)
    const navigate = useNavigate()
    const [isLoading, setIsLoading] = useState<boolean>(false)

    const handleButtonClick = async (year: number, previousYear: number | null) => {
        setIsLoading(true)
        setSelectedYear(year)
        if (previousYear !== null) {
            try {
                const data = await getInntekter(previousYear)
                if (data instanceof ErrorResponse) {
                    setErrorMessage(data.message)
                } else {
                    setPreviousYearInntekterResponse(data)
                }
            } catch {
                setErrorMessage(ErrorCode.GENERIC_ERROR)
            }
            navigate(getFullPathForPage(PageLinks.FORRIGE_INNTEKTER))
        } else {
            try {
                const data = await getInntekterForSimulering(year)
                if (data instanceof ErrorResponse) {
                    setErrorMessage(data.message)
                } else {
                    setInntekterResponse(data)
                    setBrukerinntekt(data.forventedeInntekter.bruker)
                    setAnnenForelderInntekt(data.forventedeInntekter.eps)
                    setIsLoading(false)
                }
            } catch {
                setErrorMessage(ErrorCode.GENERIC_ERROR)
            }
            navigate(getFullPathForPage(PageLinks.FORVENTET_INNTEKT))
        }
        setIsLoading(false)
    }

    if (errorMessage) {
        return <ErrorView message={errorMessage} />
    }

    if (!initiateResponse) {
        return <LoadingBox />
    }

    if (initiateResponse.messages.some((message) => message.messageCode === MessageCodes.USER_HAS_NO_UFORE)) {
        return <Alert variant="warning">Du har ikke uføretrygd. Derfor kan du ikke bruke inntektsplanleggeren.</Alert>
    } else if (initiateResponse.messages.some((message) => message.messageCode === MessageCodes.USER_HAS_NO_LOPENDE_VEDTAK_YET)) {
        return (
            <Alert variant="warning">
                Du kan ikke bruke inntektsplanleggeren ennå. Din inntekt kan registreres her fra måneden før din første utbetaling av uføretrygd.
            </Alert>
        )
    } else if (initiateResponse.messages.some((message) => message.type === MessageTypes.ERROR)) {
        return <ErrorView message={errorMessage} />
    }

    return (
        <VStack className="form-container">
            <section aria-label={'Greit å vite'}>
                <GuidePanel poster>
                    <Heading size="medium" level="2" spacing>
                        Greit å vite
                    </Heading>
                    <BodyShort spacing>Uføretrygd skal sikre deg inntekt når du ikke kan forsørge deg selv på grunn av sykdom eller skade.</BodyShort>
                    <BodyShort spacing>
                        For at vi skal beregne riktig utbetaling av uføretrygden din, må du oppgi hvor mye du forventer å tjene samtidig som du får uføretrygd.
                    </BodyShort>
                    <BodyShort>
                        Dine opplysninger lagres dessverre ikke hvis du logger ut av innteksplanleggeren, eller tar en lang pause. Vi beklager for dette.
                    </BodyShort>
                </GuidePanel>
            </section>
            <section aria-label={'I inntektsplanleggeren kan du'}>
                <Heading level="2" size="small">
                    I inntektsplanleggeren kan du
                </Heading>
                <List style={{ margin: '1rem 0' }} size="medium">
                    <List.Item>se hvor mye du vil få i uføretrygd ved siden av inntekt</List.Item>
                    <List.Item>melde inn forventet inntekt til oss</List.Item>
                </List>
            </section>
            {initiateResponse.data !== null && (
                <section aria-label={'Registrert forventet inntekt'}>
                    <ExpectedIncomeBox
                        forventetInntekt={initiateResponse.data.forventetInntekt}
                        forventetInntektAnnenForelder={initiateResponse.data.forventetInntektAnnenForelder}
                        hasBarnetilleggFellesbarn={initiateResponse.data.hasBarneTilleggFellesbarn}
                    />
                </section>
            )}
            <section aria-label={'Meld fra'}>
                <BodyLong>
                    Det er viktig at du melder fra hvis inntekten din blir annerledes enn det du har meldt inn tidligere. Det gir mindre risiko for stor
                    tilbakebetaling i etteroppgjøret.
                </BodyLong>
            </section>
            {initiateResponse.data !== null && (
                <section aria-label="Inntektsgrense og trekkprosent">
                    <InntektsgrenseCard displayData={initiateResponse.data} />
                </section>
            )}
            <Accordion>
                <Accordion.Item>
                    <Accordion.Header>Slik fungerer inntektsplanleggeren</Accordion.Header>
                    <Accordion.Content>
                        <List>
                            <List.Item>
                                Du oppgir hva du forventer å tjene, så beregner vi riktig utbetaling av uføretrygden din. Hvis du har barnetillegg og bor sammen
                                med barnets andre forelder, skal du også oppgi forelderens inntekt.
                            </List.Item>
                            <List.Item>
                                Når du melder inn en endring i inntekten din, lager vi en ny beregning. Hvis inntekten du melder inn påvirker uføreutbetalingene
                                dine, vil alle dine månedlige utbetalinger bli justert.
                            </List.Item>
                            <List.Item>
                                Får du for mye utbetalt, må du betale tilbake. Får du for lite utbetalt, betaler Nav penger tilbake til deg. Dette kalles
                                etteroppgjør.
                            </List.Item>
                            <List.Item>Du kan melde fra om forventet inntekt for neste år fra oktober i år.</List.Item>
                            <List.Item>Har du ingen endring i inntekten din til neste år, bør du likevel melde inn forventet inntekt.</List.Item>
                            <List.Item>
                                Melder du ikke fra om forventet inntekt til neste år, vil vi bruke inntekten du har oppgitt i år og justere den ved årsskiftet.
                            </List.Item>
                            <List.Item>Vi melder ikke inn din inntekt til Skatteetaten.</List.Item>
                        </List>
                    </Accordion.Content>
                </Accordion.Item>
                <Accordion.Item>
                    <Accordion.Header>Usikker på hva du kommer til å tjene?</Accordion.Header>
                    <Accordion.Content>
                        <VStack gap="space-32">
                            <BodyShort>
                                Har du variabel inntekt, kan det være vanskelig å vite hva du kommer til å tjene fremover. Vi stoler på at du melder fra til oss
                                så godt du kan.
                            </BodyShort>
                            <BodyShort>
                                Ser du at inntekten din blir annerledes enn det du tidligere har meldt inn, bør du melde fra til oss så fort som mulig.
                            </BodyShort>
                        </VStack>
                    </Accordion.Content>
                </Accordion.Item>
                <Accordion.Item>
                    <Accordion.Header>Har du andre pengestøtter i tillegg til uføretrygd fra Nav?</Accordion.Header>
                    <Accordion.Content>
                        Inntektsplanleggeren viser bare hvordan inntekt påvirker uføretrygden. Innsending via inntektsplanleggeren påvirker kun din uføretrygd,
                        og eventuelt barnetillegg og gjenlevendetillegg hvis du har det. Har du andre pengestøtter fra Nav, må du si i fra om ny inntekt til
                        disse. Endring i din inntekt kan påvirke pengestøtter fra andre enn Nav, og kan det være at du må melde fra om endring i inntekt til
                        disse også.
                    </Accordion.Content>
                </Accordion.Item>
            </Accordion>
            {initiateResponse?.data?.aktuelleAar?.length > 0 && (
                <YearView
                    availableYears={initiateResponse.data.aktuelleAar}
                    anotherAvalableYear={initiateResponse.data.annetRelevantAar}
                    handleSubmit={handleButtonClick}
                    isLoading={isLoading}
                ></YearView>
            )}
        </VStack>
    )
}
