import { Alert, Bleed, BodyLong, Box, ErrorSummary, Heading, List, Loader, VStack } from '@navikt/ds-react'
import { type FormEvent, type MouseEvent, useContext, useEffect, useState } from 'react'
import './innfylling.css'
import { useLocation, useNavigate } from 'react-router-dom'
import { simulate } from '@/api/apiFetching'
import type { PersonInntekter, SimulationResponse } from '@/api/model/ApiRequests'
import { MessageCodes } from '@/api/model/MessageCodes'
import { numberFormatWithKr } from '@/common/Utils'
import Knapperad from '@/components/common/Knapperad'
import { FormFieldsEps } from '@/components/innfylling/FormFieldsEps'
import { DataContext } from '@/context/DataContextProvider'
import { FormStateContext } from '@/context/FormData'
import { getFullPathForPage, PageLinks } from '@/FormContainer'
import LonnFordelerOgPengestotter from '../common/LonnFordelerOgPengestotter'
import PensjonFraAndreEnnNav from '../common/PensjonFraAndreEnnNav'
import { FormFieldsUser } from './FormFieldsUser'

export const InnfyllingPage = () => {
    const navigate = useNavigate()
    const {
        selectedYear,
        previousYear,
        brukerinntekt,
        setBrukerinntekt,
        annenForelderInntekt,
        setAnnenForelderInntekt,
        getBrukerinntektSum,
        getAnnenForelderInntektSum,
        setFormStep,
    } = useContext(FormStateContext)
    const { inntekterResponse, setSimulationResponse, setErrorMessage } = useContext(DataContext)
    const [brukerErrors, setBrukerErrors] = useState<Partial<Record<keyof PersonInntekter, string>>>({})
    const [epsErrors, setEpsErrors] = useState<Partial<Record<keyof PersonInntekter, string>>>({})
    const [isLoading, setIsLoading] = useState<boolean>(false)
    const [formDiry, setFormDirty] = useState<boolean>(false)
    const location = useLocation()

    useEffect(() => {
        if (location.hash) {
            const element = document.getElementById(location.hash.substring(1))
            if (element) {
                element.scrollIntoView()
            }
        }
    }, [location.hash])

    //scroll to element with id like location.hash

    useEffect(() => {
        setFormStep(1)
    }, [setFormStep])

    const checkForFieldErrors = (): boolean => {
        const errorMessages = Object.values(brukerErrors)
            .concat(Object.values(epsErrors))
            .filter((message) => message !== undefined)
        const hasErrors = errorMessages.length > 0
        return hasErrors
    }

    const checkForSendingErrors = (response: SimulationResponse): boolean => {
        setFormDirty(true)
        const bErrors: Partial<Record<keyof PersonInntekter, string>> = {}
        const eErrors: Partial<Record<keyof PersonInntekter, string>> = {}
        let isError = false

        for (const message of response.messages) {
            if (message.messageCode === MessageCodes.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR) {
                isError = true
                if (message.metadata['AFFECTED_FIELD'] === 'ARBEIDSINNTEKT_BRUKER') {
                    bErrors['arbeidsinntekt'] =
                        `Beløpet kan ikke være mindre enn ${numberFormatWithKr(Number(message.metadata['SUM_HITTIL_I_AAR']))}, fordi du allerede har fått dette i lønn og pengestøtte. Kontakt oss hvis du har fått inntekt som ikke skal føre til lavere utbetaling av uføretrygden.`
                } else if (message.metadata['AFFECTED_FIELD'] === 'ARBEIDSINNTEKT_EPS') {
                    eErrors['arbeidsinntekt'] =
                        `Beløpet må være høyere enn det den andre forelderen har fått i lønn og pengestøtte allerede. Kontakt oss hvis den andre forelderen har fått inntekt som ikke skal føre til lavere utbetaling av barnetilleget.`
                }
            } else if (message.messageCode === MessageCodes.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR) {
                isError = true
                if (message.metadata['AFFECTED_FIELD'] === 'ANDRE_YTELSER_BRUKER') {
                    bErrors['andrePensjonsgivendeYtelser'] =
                        `Beløpet kan ikke være mindre enn ${numberFormatWithKr(Number(message.metadata['SUM_HITTIL_I_AAR']))}, fordi du allerede har fått dette i pensjon. Kontakt oss hvis du har mottatt pensjon som ikke skal føre til lavere utbetaling av barnetillegget.`
                } else if (message.metadata['AFFECTED_FIELD'] === 'ANDRE_YTELSER_EPS') {
                    eErrors['andrePensjonsgivendeYtelser'] =
                        `Beløpet må være høyere enn det den andre forelderen har fått i pensjon allerede. Kontakt oss hvis den andre forelderen har mottatt pensjon som ikke skal føre til lavere utbetaling av barnetillegget.`
                }
            }
        }

        setBrukerErrors(bErrors)
        setEpsErrors(eErrors)
        if (isError) {
            document.getElementById('error-summary')?.scrollIntoView()
        }
        return isError
    }

    const handleSubmit = async (e: MouseEvent | FormEvent) => {
        e.preventDefault()
        setFormDirty(true)

        setIsLoading(true)
        if (checkForFieldErrors()) {
            setIsLoading(false)
            return
        }
        const result = await simulate(brukerinntekt, annenForelderInntekt, selectedYear)
        if (!result.ok) {
            setErrorMessage(result.error)
            setIsLoading(false)
        } else {
            if (checkForSendingErrors(result.data)) {
                setIsLoading(false)
            } else {
                setIsLoading(false)
                setSimulationResponse(result.data)
                navigate(getFullPathForPage(PageLinks.BEREGNING))
            }
        }
    }

    if (inntekterResponse === null) {
        return <Loader />
    }

    const errorSummary = (errors: Partial<Record<keyof PersonInntekter, string>>, suffix: string) => {
        return (
            <>
                {errors.arbeidsinntekt && (
                    <ErrorSummary.Item key={'arbeidsinntekt_' + suffix} href={'#arbeidsinntekt_' + suffix}>
                        {errors.arbeidsinntekt}
                    </ErrorSummary.Item>
                )}
                {errors.naeringsinntekt && (
                    <ErrorSummary.Item key={'naeringsinntekt_' + suffix} href={'#naeringsinntekt_' + suffix}>
                        {errors.naeringsinntekt}
                    </ErrorSummary.Item>
                )}
                {errors.inntektUtland && (
                    <ErrorSummary.Item key={'inntektUtland_' + suffix} href={'#inntektUtland_' + suffix}>
                        {errors.inntektUtland}
                    </ErrorSummary.Item>
                )}
                {errors.pensjonUtland && (
                    <ErrorSummary.Item key={'pensjonUtland_' + suffix} href={'#pensjonUtland_' + suffix}>
                        {errors.pensjonUtland}
                    </ErrorSummary.Item>
                )}
                {errors.andrePensjonsgivendeYtelser && (
                    <ErrorSummary.Item key={'andrePensjonsgivendeYtelser_' + suffix} href={'#andrePensjonsgivendeYtelser_' + suffix}>
                        {errors.andrePensjonsgivendeYtelser}
                    </ErrorSummary.Item>
                )}
            </>
        )
    }

    return (
        <>
            <section aria-label={'Registrert inntekt i ' + selectedYear}>
                <VStack className="form-container">
                    {(inntekterResponse.pensjonFraAndreHittilIAar?.length > 0 || inntekterResponse.arbeidsinntektOgYtelserHittilIAar?.length > 0) && (
                        <VStack>
                            <Heading level="3" size="medium" spacing>
                                {inntekterResponse.uforeHeleAaret
                                    ? `Din inntekt hittil i ${selectedYear}`
                                    : `Din inntekt samtidig med uføretrygd i ${selectedYear}`}
                            </Heading>
                            <BodyLong>
                                Under kan du se hvilken inntekt som er registrert hos Skatteetaten. Det er likevel viktig at du sender inn forventet inntekt for
                                {inntekterResponse.uforeHeleAaret ? ' hele året til oss. ' : ' den delen av året du har hatt uføretrygd. '}
                                Når vi får registrert riktig inntekt, kan vi gjøre en riktig beregning av din utbetaling.
                            </BodyLong>
                        </VStack>
                    )}

                    {inntekterResponse.arbeidsinntektOgYtelserHittilIAar?.length > 0 && (
                        <LonnFordelerOgPengestotter
                            uforeHeleAaret={inntekterResponse.uforeHeleAaret}
                            inntekter={inntekterResponse.arbeidsinntektOgYtelserHittilIAar}
                        />
                    )}
                    {inntekterResponse.pensjonFraAndreHittilIAar?.length > 0 && (
                        <PensjonFraAndreEnnNav
                            pensjonFraAndre={inntekterResponse.pensjonFraAndreHittilIAar}
                            uforeHeleAaret={inntekterResponse.uforeHeleAaret}
                        />
                    )}
                </VStack>
            </section>
            <section aria-label="Slik skal du oppgi inntekten">
                <VStack>
                    <Heading level="3" size="medium">
                        Slik skal du oppgi inntekten
                    </Heading>
                    <List style={{ margin: '1rem 0' }}>
                        {inntekterResponse.uforeHeleAaret && <List.Item>årlig beløp</List.Item>}
                        {!inntekterResponse.uforeHeleAaret && <List.Item>kun inntekt for den perioden du har uføretrygd</List.Item>}
                        <List.Item>forventet inntekt</List.Item>
                        <List.Item>før skatt</List.Item>
                        <List.Item>norske kroner</List.Item>
                    </List>
                </VStack>
            </section>
            <VStack className="form-container">
                <form onSubmit={handleSubmit}>
                    <VStack gap="space-16">
                        <VStack gap="space-16">
                            <section aria-label={'Registrer din inntekt'}>
                                <Bleed marginInline={{ md: 'space-0 space-80' }} asChild>
                                    <Box
                                        borderWidth="1"
                                        borderRadius="8"
                                        padding={{ xs: 'space-24', md: 'space-40' }}
                                        id={'bruker-inntekt'}
                                        aria-label="Din inntekt"
                                    >
                                        <VStack gap="space-24">
                                            <Heading size="medium" level="3" spacing>
                                                Registrer din inntekt for {selectedYear}
                                            </Heading>
                                            <BodyLong>
                                                Du må endre inntektsopplysningene nedenfor hvis de ikke er riktige. Opplysninger som er feil kan gi deg feil
                                                utbetaling av uføretrygd. Du kan sende inn ny inntekt så mange ganger du trenger i løpet av året.{' '}
                                            </BodyLong>
                                            {!inntekterResponse.uforeHeleAaret ? (
                                                <Alert inline variant="info">
                                                    Du har ikke uføretrygd hele året. Du skal kun legge inn inntekt for den perioden du har uføretrygd.
                                                </Alert>
                                            ) : null}
                                            <FormFieldsUser
                                                year={selectedYear}
                                                errors={brukerErrors}
                                                setErrors={setBrukerErrors}
                                                setInntekt={(field, belop) =>
                                                    setBrukerinntekt((b) => ({
                                                        ...b,
                                                        [field]: belop,
                                                    }))
                                                }
                                                forventedeInntekter={brukerinntekt}
                                                inntektSum={getBrukerinntektSum()}
                                            />
                                        </VStack>
                                    </Box>
                                </Bleed>
                            </section>

                            {inntekterResponse?.forventedeInntekter.eps ? (
                                <section aria-label={'Registrer annen forelders inntekt'}>
                                    <Bleed marginInline={{ md: 'space-0 space-80' }}>
                                        <Box
                                            borderWidth="1"
                                            borderRadius="8"
                                            padding={{ xs: 'space-24', md: 'space-40' }}
                                            id={'eps-inntekt'}
                                            aria-label="Registrer annen forelders inntekt"
                                        >
                                            <VStack gap="space-24">
                                                <Heading level="3" size="medium" spacing>
                                                    Annen forelders inntekt {selectedYear}
                                                </Heading>
                                                <BodyLong>
                                                    Fordi du mottar barnetillegg til uføretrygden, må du også registrere den forventede inntekten til forelderen
                                                    som du bor sammen med.
                                                </BodyLong>
                                                <BodyLong>
                                                    <strong>Du skal oppgi inntekten til forelder med fødselsnummer {inntekterResponse.epsPid}</strong>
                                                </BodyLong>
                                                <BodyLong>
                                                    Du må endre inntektsopplysningene nedenfor hvis de ikke er riktige. Inntekten til den andre forelderen har
                                                    bare betydning for størrelsen på barnetillegget ditt. Du kan sende inn ny inntekt så mange ganger du trenger
                                                    i løpet av året.{' '}
                                                </BodyLong>

                                                {!inntekterResponse.uforeHeleAaret ? (
                                                    <Alert inline variant="info">
                                                        Du har ikke uføretrygd hele året. Du skal kun legge inn den andre forelderens inntekt for den perioden
                                                        du har uføretrygd.
                                                    </Alert>
                                                ) : null}
                                                <FormFieldsEps
                                                    year={selectedYear}
                                                    errors={epsErrors}
                                                    setErrors={setEpsErrors}
                                                    setInntekt={(field, belop) =>
                                                        setAnnenForelderInntekt((b) =>
                                                            b
                                                                ? {
                                                                      ...b,
                                                                      [field]: belop,
                                                                  }
                                                                : null
                                                        )
                                                    }
                                                    forventedeInntekter={annenForelderInntekt || ({} as PersonInntekter)}
                                                    inntektSum={getAnnenForelderInntektSum() || 0}
                                                />
                                            </VStack>
                                        </Box>
                                    </Bleed>
                                </section>
                            ) : null}
                        </VStack>
                        {checkForFieldErrors() && formDiry ? (
                            <div ref={(errorSummaryDiv) => errorSummaryDiv?.scrollIntoView()}>
                                <ErrorSummary
                                    id="error-summary"
                                    headingTag="h3"
                                    heading="Du må rette disse feilene før du kan fortsette:"
                                    className="button-container"
                                >
                                    {errorSummary(brukerErrors, 'bruker')}
                                    {errorSummary(epsErrors, 'eps')}
                                </ErrorSummary>
                            </div>
                        ) : null}

                        <span className="button-container">
                            <Knapperad
                                handleSubmit={handleSubmit}
                                tilbakePageLink={previousYear != null ? PageLinks.FORRIGE_INNTEKTER : PageLinks.INDEX}
                                laster={isLoading}
                            />
                        </span>
                    </VStack>
                </form>
            </VStack>
        </>
    )
}
