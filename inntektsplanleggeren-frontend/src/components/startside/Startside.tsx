import { Accordion, Alert, BodyShort, GuidePanel, Heading, List, VStack } from '@navikt/ds-react'
import { useContext, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getInntekter, getInntekterForSimulering } from '@/api/apiFetching'
import { hentStartsideData, type StartsideData } from '@/api/hentStartsideData'
import { MessageCodes, MessageTypes } from '@/api/model/MessageCodes'
import { ErrorView } from '@/components/common/Error'
import ForventetInntekt from '@/components/startside/ForventetInntekt'
import { LoadingBox } from '@/components/startside/LoadingBox'
import { YearView } from '@/components/startside/YearView'
import { DataContext } from '@/context/DataContextProvider'
import { FormStateContext } from '@/context/FormData'
import { getFullPathForPage, PageLinks } from '@/FormContainer'

export function Startside() {
    const { setInntekterResponse, setPreviousYearInntekterResponse, errorMessage, setErrorMessage } = useContext(DataContext)
    const { setSelectedYear, setBrukerinntekt, setAnnenForelderInntekt } = useContext(FormStateContext)
    const navigate = useNavigate()
    const [isLoading, setIsLoading] = useState<boolean>(false)
    const [data, setData] = useState<StartsideData | null>(null)

    useEffect(() => {
        const hentData = async () => {
            const result = await hentStartsideData()

            if (!result.ok) {
                setErrorMessage(result.error)
                return
            }
            setData(result.data)
        }
        void hentData()
    }, [setErrorMessage])

    const handleButtonClick = async (year: number, previousYear: number | null) => {
        setIsLoading(true)
        setSelectedYear(year)
        if (previousYear !== null) {
            const inntekterResultat = await getInntekter(previousYear)
            if (!inntekterResultat.ok) {
                setErrorMessage(inntekterResultat.error)
            } else {
                setPreviousYearInntekterResponse(inntekterResultat.data)
            }
            navigate(getFullPathForPage(PageLinks.FORRIGE_INNTEKTER))
        } else {
            const inntekterResultat = await getInntekterForSimulering(year)
            if (!inntekterResultat.ok) {
                setErrorMessage(inntekterResultat.error)
            } else {
                setInntekterResponse(inntekterResultat.data)
                setBrukerinntekt(inntekterResultat.data.forventedeInntekter.bruker)
                setAnnenForelderInntekt(inntekterResultat.data.forventedeInntekter.eps)
                setIsLoading(false)
            }
            navigate(getFullPathForPage(PageLinks.FORVENTET_INNTEKT))
        }
        setIsLoading(false)
    }

    if (errorMessage) {
        return <ErrorView message={errorMessage} />
    }

    if (!data) {
        return <LoadingBox />
    }

    if (data.messages.some((message) => message.messageCode === MessageCodes.USER_HAS_NO_UFORE)) {
        return <Alert variant="warning">Du har ikke uføretrygd. Derfor kan du ikke bruke inntektsplanleggeren.</Alert>
    } else if (data.messages.some((message) => message.messageCode === MessageCodes.USER_HAS_NO_LOPENDE_VEDTAK_YET)) {
        return (
            <Alert variant="warning">
                Du kan ikke bruke inntektsplanleggeren ennå. Din inntekt kan registreres her fra måneden før din første utbetaling av uføretrygd.
            </Alert>
        )
    } else if (data.messages.some((message) => message.type === MessageTypes.ERROR)) {
        return <ErrorView message={errorMessage} />
    }

    return (
        <VStack className="form-container">
            <section aria-label={'Greit å vite'}>
                <GuidePanel poster>
                    <Heading size="medium" level="2" spacing>
                        Greit å vite
                    </Heading>
                    <BodyShort spacing>
                        I inntektsplanleggeren kan du se hvordan inntekt påvirker uføretrygden din, og melde inn inntekt til oss. Vi bruker inntekten du melder
                        inn til å beregne riktig utbetaling av uføretrygd og eventuelle tillegg.
                    </BodyShort>
                    <BodyShort spacing>Du kan melde fra om inntekt så ofte du trenger.</BodyShort>
                    <BodyShort>
                        Dine opplysninger lagres dessverre ikke hvis du logger ut av inntektsplanleggeren, eller tar en lang pause. Vi beklager for dette.
                    </BodyShort>
                </GuidePanel>
            </section>
            {data.uforetrygd?.aarKanRegistrereInntekt?.length > 0 && (
                <YearView
                    availableYears={data.uforetrygd.aarKanRegistrereInntekt}
                    anotherAvalableYear={data.uforetrygd.seTallForAar}
                    handleSubmit={handleButtonClick}
                    isLoading={isLoading}
                />
            )}
            <section aria-label={'Dine tall'}>
                <ForventetInntekt data={data.uforetrygd} />
            </section>
            <Accordion>
                <Accordion.Item>
                    <Accordion.Header>Dette bør du melde fra om</Accordion.Header>
                    <Accordion.Content>
                        <List>
                            <List.Item>Hvis du har barnetillegg og bor sammen med barnets andre forelder, skal du også oppgi forelderens inntekt.</List.Item>
                            <List.Item>Du kan melde fra om forventet inntekt for neste år fra oktober i år.</List.Item>
                            <List.Item>Har du ingen endring i inntekten din til neste år, bør du likevel melde inn forventet inntekt.</List.Item>
                            <List.Item>
                                Melder du ikke fra om forventet inntekt til neste år, vil vi bruke inntekten du har oppgitt i år og justere den ved årsskiftet.
                            </List.Item>
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
                                Ser du at inntekten din blir annerledes enn det du tidligere har meldt inn, bør du melde fra til oss så fort som mulig. Du kan
                                bruke inntektsplanleggeren så ofte du trenger.
                            </BodyShort>
                        </VStack>
                    </Accordion.Content>
                </Accordion.Item>
                <Accordion.Item>
                    <Accordion.Header>Tidspunkt for å registrere inntekt</Accordion.Header>
                    <Accordion.Content>
                        <List>
                            <List.Item>I perioden 1. januar - 30. september kan du bare legge inn inntekt for dette året.</List.Item>
                            <List.Item>Fra 1. oktober - 30. november kan du både legge inn inntekt for dette året og neste år.</List.Item>
                            <List.Item>
                                Fra 1. til 31. desember kan du bare registrere inntekt for neste år, fordi endringen ikke vil påvirke utbetalingen din før til
                                neste år.
                            </List.Item>
                        </List>
                    </Accordion.Content>
                </Accordion.Item>
            </Accordion>
        </VStack>
    )
}
