import {
  Alert,
  Bleed,
  BodyLong,
  Box,
  Button,
  ErrorSummary,
  Heading,
  HStack,
  List,
  Loader,
  VStack,
} from '@navikt/ds-react'
import React, { FormEvent, MouseEvent, useContext, useEffect, useState } from 'react'
import './innfylling.css'
import {Link as RouterLink, useLocation, useNavigate} from 'react-router-dom'
import { FormStateContext } from '@/context/FormData'
import { FormFieldsUser } from './FormFieldsUser'
import { simulate } from '@/api/apiFetching'
import { DinInntektTable } from '@/components/common/DinInntektTable'
import { belopSum } from '@/common/Utils'
import { DataContext } from '@/DataContextProvider'
import { PersonInntekter, SimulationResponse } from '@/api/model/ApiRequests'
import { FormatKroner } from '@/components/utils/FormatKroner'
import { ArrowLeftIcon, ArrowRightIcon } from '@navikt/aksel-icons'
import { getFullPathForPage, PageLinks } from '@/FormContainer'
import { FormFieldsEps } from '@/components/innfylling/FormFieldsEps'
import { CancelConfirmationModal } from '@/components/common/CancelConfirmationModal'
import { MessageCodes } from '@/api/model/MessageCodes'
import { ErrorCode, ErrorResponse } from '@/components/common/Error'

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

  const brukerFormRef = React.useRef<HTMLDivElement>(null)
  const epsFormRef = React.useRef<HTMLDivElement>(null)
  const location = useLocation();

  useEffect(() => {
    if (location.hash === '#bruker-inntekt') {
      brukerFormRef.current?.scrollIntoView(true)
      } else if (location.hash === '#eps-inntekt') {
      epsFormRef.current?.scrollIntoView(true)
    }
  }, []);

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



  const handleSubmit = async (e: MouseEvent | FormEvent) => {
    e.preventDefault()
    setFormDirty(true)

    try {
      setIsLoading(true)
      if (checkForFieldErrors()) {
        setIsLoading(false)
        return
      }
      const result = await simulate(brukerinntekt, annenForelderInntekt, selectedYear)
      if (result instanceof ErrorResponse) {
        setErrorMessage(result.message)
        setIsLoading(false)
      } else {
          setIsLoading(false)
          setSimulationResponse(result)
          navigate(getFullPathForPage(PageLinks.BEREGNING))
      }
    } catch {
      setIsLoading(false)
      setErrorMessage(ErrorCode.GENERIC_ERROR)
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
          <ErrorSummary.Item
            key={'andrePensjonsgivendeYtelser_' + suffix}
            href={'#andrePensjonsgivendeYtelser_' + suffix}
          >
            {errors.andrePensjonsgivendeYtelser}
          </ErrorSummary.Item>
        )}
      </>
    )
  }

  return (
    <VStack className="form-container">
      {(inntekterResponse.pensjonFraAndreHittilIAar?.length > 0 ||
        inntekterResponse.arbeidsinntektOgYtelserHittilIAar?.length > 0) && (
        <VStack>
          <Heading level="3" size="medium" spacing>
            {inntekterResponse.uforeHeleAaret ? `Din inntekt hittil i ${selectedYear}` : `Din inntekt samtidig med uføretrygd i ${selectedYear}`}
          </Heading>
          <BodyLong>
            {' '}
            Under kan du se hvilken inntekt som er registrert via A-meldingen. Det er likevel viktig at du sender inn
            forventet inntekt for
            {inntekterResponse.uforeHeleAaret ? ' hele året til oss. ' : ' den delen av året du får uføretrygd. '}
            Når vi får registrert riktig inntekt, kan vi gjøre en riktig beregning av din utbetaling.
          </BodyLong>
        </VStack>
      )}

      {inntekterResponse.arbeidsinntektOgYtelserHittilIAar?.length > 0 && (
        <DinInntektTable data={inntekterResponse.arbeidsinntektOgYtelserHittilIAar} type="arbeidsgiver">
          <Heading level="4" size={'xsmall'}>
            Arbeidsinntekt og pengestøtter
          </Heading>
          <BodyLong>
            Vi har registrert at du har fått{' '}
            <strong>
              <FormatKroner value={belopSum(inntekterResponse.arbeidsinntektOgYtelserHittilIAar)} />
            </strong>{' '}
            i arbeidsinntekt og pengestøtter
            {inntekterResponse.uforeHeleAaret ? ' hittil i år.' : ' i perioden du har hatt uføretrygd.'}{' '}
          </BodyLong>
        </DinInntektTable>
      )}
      {inntekterResponse.pensjonFraAndreHittilIAar?.length > 0 && (
        <DinInntektTable data={inntekterResponse.pensjonFraAndreHittilIAar} type="pensjonsordning">
          <Heading level="4" size={'xsmall'}>
            Pensjoner fra andre enn folketrygden
          </Heading>
          <BodyLong>
            Vi har registrert at du har fått{' '}
            <strong>
              <FormatKroner value={belopSum(inntekterResponse.pensjonFraAndreHittilIAar)} />
            </strong>{' '}
            i pensjoner fra andre enn folketrygden
            {inntekterResponse.uforeHeleAaret ? ' hittil i år.' : ' i perioden du har hatt uføretrygd.'}{' '}
          </BodyLong>
        </DinInntektTable>
      )}

      <div>
        <Heading level="3" size="medium">
          Slik skal du oppgi inntekten
        </Heading>
        <List>
          <List.Item>skriv inntekten du forventer å få utbetalt før skatt</List.Item>
          <List.Item>alltid i norske kroner</List.Item>
        </List>
      </div>

      <form onSubmit={handleSubmit}>
        <VStack gap="4">
          <VStack gap="4">
            <div ref={brukerFormRef}>
                <Bleed marginInline={{md: '0 20'}} asChild>
                  <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding={{xs: '6', md: '10'}}>
                    <VStack gap="6">
                      <Heading size="medium" level="3" spacing>
                        Din inntekt {selectedYear}
                      </Heading>
                      <BodyLong>
                        Du må endre inntektsopplysningene nedenfor hvis de ikke er riktige. Opplysninger som er feil kan gi
                        deg feil utbetaling av uføretrygd. Du kan sende inn ny inntekt så mange ganger du trenger i løpet av året.{' '}
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
                          setInntekt={(field, belop) => setBrukerinntekt((b) => ({...b, [field]: belop}))}
                          forventedeInntekter={brukerinntekt}
                          inntektSum={getBrukerinntektSum()}
                      />
                    </VStack>
                  </Box>
                </Bleed>
            </div>

              {inntekterResponse?.forventedeInntekter.eps ? (
                  <div ref={epsFormRef}>
                    <Bleed marginInline={{md: '0 20'}}>
                      <Box borderColor="border-default" borderWidth="1" borderRadius="large"
                           padding={{xs: '6', md: '10'}}>
                        <VStack gap="6">
                          <Heading level="3" size="medium" spacing>
                            Annen forelders inntekt {selectedYear}
                          </Heading>
                          <BodyLong>
                            Fordi du mottar barnetillegg til uføretrygden, må du også registrere den forventede inntekten til forelderen som du bor sammen med.
                          </BodyLong>
                          <BodyLong>
                            <strong>Du skal oppgi inntekten til forelder med
                              fødselsnummer {inntekterResponse.epsPid}</strong>
                          </BodyLong>
                          <BodyLong>
                            Du må endre inntektsopplysningene nedenfor hvis de ikke er riktige. Inntekten til den andre forelderen har bare betydning for størrelsen på barnetillegget ditt. Du kan sende inn ny
                            inntekt så mange ganger du trenger i løpet av året.{' '}
                          </BodyLong>

                          {!inntekterResponse.uforeHeleAaret ? (
                              <Alert inline variant="info">
                                Du har ikke uføretrygd hele året. Du skal kun legge inn den andre forelderens inntekt
                                for den
                                perioden du har uføretrygd.
                              </Alert>
                          ) : null}
                          <FormFieldsEps
                              year={selectedYear}
                              errors={epsErrors}
                              setErrors={setEpsErrors}
                              setInntekt={(field, belop) =>
                                  setAnnenForelderInntekt((b) => (b ? {...b, [field]: belop} : null))
                              }
                              forventedeInntekter={annenForelderInntekt || ({} as PersonInntekter)}
                              inntektSum={getAnnenForelderInntektSum() || 0}
                          />
                        </VStack>
                      </Box>
                    </Bleed>
                  </div>
              ) : null}
          </VStack>
          {checkForFieldErrors() && formDiry ? (
            <ErrorSummary
              headingTag="h3"
              heading="Du må rette disse feilene før du kan fortsette:"
              className="button-container"
            >
              {errorSummary(brukerErrors, 'bruker')}
              {errorSummary(epsErrors, 'eps')}
            </ErrorSummary>
          ) : null}

          <VStack gap="3" className="button-container">
            <HStack gap="4">
              <Button
                as={RouterLink}
                to={
                  previousYear != null
                    ? getFullPathForPage(PageLinks.FORRIGE_INNTEKTER)
                    : getFullPathForPage(PageLinks.INDEX)
                }
                iconPosition="left"
                icon={<ArrowLeftIcon aria-hidden />}
                variant="secondary"
              >
                Tilbake
              </Button>
              <Button
                type="button"
                variant="primary"
                iconPosition="right"
                icon={<ArrowRightIcon aria-hidden />}
                onClick={handleSubmit}
                loading={isLoading}
              >
                Gå videre og se resultat
              </Button>
            </HStack>
            <CancelConfirmationModal />
          </VStack>
        </VStack>
      </form>
    </VStack>
  )
}
