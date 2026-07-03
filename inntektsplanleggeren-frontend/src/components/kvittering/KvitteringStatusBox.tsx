import { Alert, Bleed, BodyLong, BodyShort, Heading, VStack } from '@navikt/ds-react'
import { useContext } from 'react'
import { Link } from 'react-router-dom'
import type { StatusResponse } from '@/api/model/ApiRequests'
import { StatusCodes } from '@/api/model/StatusCodes'
import { FormatDateLong, FormatDateTime } from '@/components/utils/FormatDate'
import { FormatKroner } from '@/components/utils/FormatKroner'
import { getPidQueryParamString } from '@/components/utils/UrlUtil'
import { FormStateContext } from '@/context/FormData'

interface Props {
    statusResponse: StatusResponse
    registeredInntekt: number
    epsRegisteredInntekt: number | null
}

export const KvitteringStatusBox = ({ statusResponse, registeredInntekt, epsRegisteredInntekt }: Props) => {
    const { selectedYear } = useContext(FormStateContext)

    return (
        <VStack>
            {!statusResponse ||
                (statusResponse.status === StatusCodes.TIL_BEHANDLING && (
                    <VStack gap="space-20">
                        <Bleed marginInline={{ md: 'space-0 space-80' }}>
                            <Alert variant="info">
                                <VStack gap="space-12">
                                    <Heading level="3" size="small">
                                        Nav har mottatt opplysninger om inntekten din
                                    </Heading>
                                    <BodyShort>
                                        Din forventede inntekt i {selectedYear}:{' '}
                                        <strong>
                                            <FormatKroner value={registeredInntekt} /> (før skatt)
                                        </strong>
                                    </BodyShort>
                                    {epsRegisteredInntekt != null && (
                                        <BodyShort>
                                            Annen forelders forventede inntekt i {selectedYear}:{' '}
                                            <strong>
                                                <FormatKroner value={epsRegisteredInntekt} /> (før skatt)
                                            </strong>
                                        </BodyShort>
                                    )}
                                </VStack>
                            </Alert>
                        </Bleed>
                        <BodyShort>
                            Mottatt av Nav: <FormatDateTime value={statusResponse.registeringsTidspunktEndring} />
                        </BodyShort>
                        <BodyShort>Referansenummer: {statusResponse.sakId}</BodyShort>
                    </VStack>
                ))}
            {statusResponse.status === StatusCodes.BEHANDLET_MEDFOERER_ENDRING && (
                <VStack gap="space-20">
                    <Bleed marginInline={{ md: 'space-0 space-80' }}>
                        <Alert variant="success">
                            <VStack gap="space-12">
                                <Heading level="3" size="small">
                                    Ny inntekt er mottatt av oss og saken er behandlet
                                </Heading>
                                <BodyShort>
                                    Din forventede inntekt i {selectedYear}:{' '}
                                    <strong>
                                        <FormatKroner value={registeredInntekt} /> (før skatt)
                                    </strong>
                                </BodyShort>
                                {epsRegisteredInntekt != null && (
                                    <BodyShort>
                                        Annen forelders forventede inntekt i {selectedYear}:{' '}
                                        <strong>
                                            <FormatKroner value={epsRegisteredInntekt} /> (før skatt)
                                        </strong>
                                    </BodyShort>
                                )}
                                {statusResponse.maandedligeUtbetalinger != null && (
                                    <BodyShort>
                                        Din månedlige utbetaling fra <FormatDateLong value={statusResponse.maandedligeUtbetalinger.fom} />:{' '}
                                        <strong>
                                            <FormatKroner value={statusResponse.maandedligeUtbetalinger?.beloep} /> (før skatt)
                                        </strong>
                                    </BodyShort>
                                )}
                            </VStack>
                        </Alert>
                    </Bleed>
                    <VStack>
                        <BodyShort>
                            Mottatt av Nav: <FormatDateTime value={statusResponse.registeringsTidspunktEndring} />
                        </BodyShort>
                        <BodyShort>Referansenummer: {statusResponse.sakId}</BodyShort>
                    </VStack>
                    <BodyLong>
                        Vi har behandlet saken din og du vil snart motta et vedtaksbrev på{' '}
                        <Link to={import.meta.env.VITE_DIN_UFORETRYGD_URL + getPidQueryParamString()} target="_blank">
                            Din uføretrygd (åpnes i ny fane)
                        </Link>
                        .
                    </BodyLong>
                </VStack>
            )}
            {statusResponse.status === StatusCodes.BEHANDLET_MEDFOERER_INGEN_ENDRING && (
                <VStack gap="space-20">
                    <Bleed marginInline={{ md: 'space-0 space-80' }}>
                        <Alert variant="success">
                            <VStack gap="space-12">
                                <Heading level="3" size="small">
                                    Ny inntekt er registrert, og den påvirker ikke utbetalingen din
                                </Heading>
                                <BodyShort>
                                    Din forventede inntekt i {selectedYear}:{' '}
                                    <strong>
                                        <FormatKroner value={registeredInntekt} /> (før skatt)
                                    </strong>
                                </BodyShort>
                                {epsRegisteredInntekt != null ? (
                                    <BodyShort>
                                        Annen forelders forventede inntekt i {selectedYear}:{' '}
                                        <strong>
                                            <FormatKroner value={epsRegisteredInntekt} /> (før skatt)
                                        </strong>
                                    </BodyShort>
                                ) : null}
                                {statusResponse.maandedligeUtbetalinger != null ? (
                                    <BodyShort>
                                        Din månedlige utbetaling:{' '}
                                        <strong>
                                            <FormatKroner value={statusResponse.maandedligeUtbetalinger?.beloep} /> (før skatt)
                                        </strong>
                                    </BodyShort>
                                ) : null}
                            </VStack>
                        </Alert>
                    </Bleed>
                    <VStack>
                        <BodyShort>
                            Mottatt av Nav: <FormatDateTime value={statusResponse.registeringsTidspunktEndring} />
                        </BodyShort>{' '}
                        {/*    todo display date in nice format*/}
                        <BodyShort>Referansenummer: {statusResponse.sakId}</BodyShort>
                    </VStack>
                    <BodyLong>Inntekt du har sendt inn endrer ikke utbetalingen din. Du får derfor ikke et nytt vedtaksbrev fra oss.</BodyLong>
                </VStack>
            )}
        </VStack>
    )
}
