import { BodyLong, BodyShort, Box, Button, Heading, HStack, List, Loader, VStack } from '@navikt/ds-react'
import { useContext, useEffect, useState } from 'react'
import { FormStateContext } from '@/context/FormData'
import { DataContext } from '@/context/DataContextProvider'
import { Link } from 'react-router-dom'
import { KvitteringStatusBox } from '@/components/kvittering/KvitteringStatusBox'
import { getStatus } from '@/api/apiFetching'
import { StatusCodes } from '@/api/model/StatusCodes'
import { ErrorCode, ErrorResponse, ErrorView } from '@/components/common/Error'
import { getPidQueryParamString } from '@/components/utils/UrlUtil'

export const KvitteringPage = () => {
    const [isWaiting, setIsWaiting] = useState(true)
    const { setFormStep, getBrukerinntektSum, getAnnenForelderInntektSum, selectedYear } = useContext(FormStateContext)
    const { statusResponse, setStatusResponse, sendResponse } = useContext(DataContext)
    const [systemErrorMessage, setSystemErrorMessage] = useState<ErrorCode | null>(null)

    useEffect(() => {
        setFormStep(null)
    }, [setFormStep])

    useEffect(() => {
        let attempts = 0
        setIsWaiting(true)
        const intervalId = setInterval(() => {
            if (attempts >= 13) {
                setIsWaiting(false)
                clearInterval(intervalId)
                return
            }

            if (selectedYear && sendResponse?.innsendingsTidspunkt && attempts > 3) {
                getStatus(selectedYear, sendResponse.innsendingsTidspunkt)
                    .then((result) => {
                        if (result instanceof ErrorResponse) {
                            setSystemErrorMessage(result.message)
                        } else {
                            setSystemErrorMessage(null)
                            setStatusResponse(result)
                            if (result.status === 'BEHANDLET_MEDFOERER_ENDRING' || result.status === 'BEHANDLET_MEDFOERER_INGEN_ENDRING') {
                                setIsWaiting(false)
                                clearInterval(intervalId)
                            }
                        }
                    })
                    .catch(() => {
                        setSystemErrorMessage(ErrorCode.STATUS_ERROR)
                    })
            }
            attempts++
        }, 1_000)

        return () => {
            clearInterval(intervalId)
        }
    }, [])

    if (isWaiting) {
        return (
            <Box.New background="neutral-soft" padding="space-64" borderRadius="large">
                <VStack className="form-container" align="center" gap="space-80">
                    <Heading level="2" size="large" align="center">
                        Vent mens vi sender inn
                    </Heading>
                    <Loader size="3xlarge" />
                    <BodyShort align="center">Dette kan ta opptil ett minutt.</BodyShort>
                </VStack>
            </Box.New>
        );
    }

    return (
        <VStack className="form-container">
            <section aria-label={'Kvittering'}>
                <Heading level="2" size="large">
                    Kvittering
                </Heading>
                <ErrorView message={systemErrorMessage} />
                {statusResponse ? (
                    <KvitteringStatusBox
                        statusResponse={statusResponse}
                        registeredInntekt={getBrukerinntektSum()}
                        epsRegisteredInntekt={getAnnenForelderInntektSum()}
                    />
                ) : null}
            </section>
            {statusResponse?.status === StatusCodes.TIL_BEHANDLING && (
                <section aria-label={'Hva skjer videre?'}>
                    <Heading level="2" size={'large'}>
                        Hva skjer videre?
                    </Heading>
                    <List as="ul">
                        <List.Item>Endringen er sendt til behandling. I de fleste tilfeller vil saken være ferdig behandlet i løpet av 14 dager. </List.Item>
                        <List.Item>Din nye inntekt vil ikke vises i inntektsplanleggeren før vi har behandlet saken.</List.Item>
                        <List.Item>
                            Når saken er ferdig behandlet vil du finne vedtaksbrevet på{' '}
                            <Link to={import.meta.env.VITE_DIN_UFORETRYGD_URL + getPidQueryParamString()} target="_blank">
                                Din uføretrygd (åpnes i ny fane).
                            </Link>
                        </List.Item>
                    </List>
                </section>
            )}
            <section aria-label={'Etteroppgjør'}>
                <Heading className="header" level="2" size={'large'}>
                    Etteroppgjør
                </Heading>
                <BodyLong>
                    Hver høst sjekker vi om du har fått utbetalt riktig beløp. Det gjør vi ved å hente dine inntektsopplysninger fra forrige år, fra blant annet
                    Skatteetaten. Har du fått utbetalt for mye, må du betale tilbake. Har du fått utbetalt for lite, betaler vi deg tilbake. Dette kalles
                    etteroppgjør.{' '}
                    <Link to={import.meta.env.VITE_NAV_UFORETRYGD_INFO_URL + '#etteroppgjor'} target="_blank">
                        Les mer om etteroppgjøret (åpnes i ny fane).
                    </Link>
                </BodyLong>
            </section>
            <section aria-label={'Hvis inntekten din endrer seg'}>
                <Heading className="header" level="2" size={'large'}>
                    Hvis inntekten din endrer seg
                </Heading>
                <BodyLong>
                    Ser du at inntekten din blir annerledes enn det du meldte inn her, bør du melde inn ny inntekt så fort som mulig. Det gir mindre risiko for
                    stor tilbakebetaling i etteroppgjøret. Du kan melde ny endring i inntektsplanleggeren så mange ganger du trenger i løpet av året.
                </BodyLong>
            </section>
            <section aria-label={'Husk å oppdatere skattekortet'}>
                <Heading className="header" level="2" size={'large'}>
                    Husk å oppdatere skattekortet
                </Heading>
                <BodyLong>
                    Hvis du har fått endret inntekt, kan det være at skattekortet ditt må oppdateres.{' '}
                    <Link to={import.meta.env.VITE_SKATTEETATEN_SKATTEKORT_URL} target="_blank">
                        Les om skattekort og endre det hos Skatteetaten (åpnes i ny fane).
                    </Link>
                </BodyLong>
            </section>
            <section aria-label={'Må du melde fra til flere?'}>
                <Heading className="header" level="2" size={'large'}>
                    Må du melde fra til flere?
                </Heading>
                <BodyLong>
                    Får du andre utbetalinger fra Nav eller pengestøtter fra andre, kan det hende at du må melde om endring i inntekt til disse også. Det kan
                    for eksempel gjelde
                    <List>
                        <List.Item>økonomisk sosialhjelp</List.Item>
                        <List.Item>uførepensjon fra en pensjonskasse eller forsikringsordning</List.Item>
                        <List.Item>bostøtte fra Husbanken</List.Item>
                    </List>
                </BodyLong>
            </section>
            <HStack gap="space-16">
                <Button as={Link} to={import.meta.env.VITE_DIN_UFORETRYGD_URL} variant="primary" className="button-container">
                    Din uføretrygd
                </Button>
            </HStack>
        </VStack>
    );
}
