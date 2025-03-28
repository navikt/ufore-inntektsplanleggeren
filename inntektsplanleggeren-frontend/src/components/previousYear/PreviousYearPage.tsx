import { BodyLong, Button, Heading, HStack, Loader, VStack } from '@navikt/ds-react'
import { useContext, useState } from 'react'
import './PreviousYearPage.css'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { FormStateContext } from '@/context/FormData'
import { getInntekter } from '@/api/apiFetching'
import { DinInntektTable } from '@/components/common/DinInntektTable'
import { belopSum } from '@/common/Utils'
import { DataContext } from '@/DataContextProvider'
import { FormatKroner } from '@/components/utils/FormatKroner'
import { ArrowLeftIcon, ArrowRightIcon } from '@navikt/aksel-icons'
import { getFullPathForPage, PageLinks } from '@/FormContainer'
import { ErrorCode, ErrorResponse } from '@/components/common/Error'
import { PreviousExpectedIncomeTable } from '@/components/previousYear/PreviousExpectedIncomeTable'

export const PreviousYearPage = () => {
  const navigate = useNavigate()
  const { previousYearInntekterResponse, setInntekterResponse, setErrorMessage } = useContext(DataContext)
  const { selectedYear, previousYear, setBrukerinntekt, setAnnenForelderInntekt } = useContext(FormStateContext)
  const [isLoading, setIsLoading] = useState<boolean>(false)

  const onClickButton = async () => {
    try {
      const data = await getInntekter(selectedYear)
      if (data instanceof ErrorResponse) {
        setErrorMessage(data.message)
      } else {
        setInntekterResponse(data)
        setBrukerinntekt(data.forventedeInntekter.bruker)
        setAnnenForelderInntekt(data.forventedeInntekter.eps)
      }
    } catch {
      setErrorMessage(ErrorCode.GENERIC_ERROR)
    }
    setIsLoading(false)
    navigate(getFullPathForPage(PageLinks.FORVENTET_INNTEKT))
  }

  if (previousYearInntekterResponse === null) {
    return <Loader />
  }

  return (
    <VStack className="page">
      {(previousYearInntekterResponse.pensjonFraAndreHittilIAar?.length > 0 ||
        previousYearInntekterResponse.pensjonFraAndreHittilIAar?.length > 0) && (
        <VStack>
          <Heading level="2" size="medium">
            Din inntekt hittil i {previousYear}
          </Heading>
          <BodyLong>
            Under kan du se hvilken inntekt som er registrert via A-meldingen
            {previousYearInntekterResponse.uforeHeleAaret ? '.' : ' den delen av året du får uføretrygd.'}
          </BodyLong>
        </VStack>
      )}

      {previousYearInntekterResponse.arbeidsinntektOgYtelserHittilIAar?.length > 0 && (
        <DinInntektTable data={previousYearInntekterResponse.arbeidsinntektOgYtelserHittilIAar} type="arbeidsgiver">
          <Heading size={'xsmall'}>Arbeidsinntekt og pengestøtter</Heading>
          <BodyLong>
            Vi har registrert at du har fått{' '}
            <strong>
              <FormatKroner value={belopSum(previousYearInntekterResponse.arbeidsinntektOgYtelserHittilIAar)} />
            </strong>{' '}
            i arbeidsinntekt og pengestøtter
            {previousYearInntekterResponse.uforeHeleAaret
              ? ' hittil i år.'
              : ' i perioden du har hatt uføretrygd.'}{' '}
          </BodyLong>
        </DinInntektTable>
      )}
      {previousYearInntekterResponse.pensjonFraAndreHittilIAar?.length > 0 && (
        <DinInntektTable data={previousYearInntekterResponse.pensjonFraAndreHittilIAar} type="pensjonsordning">
          <Heading size={'xsmall'}>Pensjoner fra andre enn folketrygdene</Heading>
          <BodyLong>
            Vi har registrert at du har fått{' '}
            <strong>
              <FormatKroner value={belopSum(previousYearInntekterResponse.pensjonFraAndreHittilIAar)} />
            </strong>{' '}
            i pensjoner fra andre enn folketrygden
            {previousYearInntekterResponse.uforeHeleAaret
              ? ' hittil i år.'
              : ' i perioden du har hatt uføretrygd.'}{' '}
          </BodyLong>
        </DinInntektTable>
      )}

      <Heading level="2" size="medium">
        Registrert forventet inntekt for {previousYear}
      </Heading>
      <VStack gap={'10'}>
        {previousYearInntekterResponse.forventedeInntekter.bruker !== null && previousYear !== null && (
          <PreviousExpectedIncomeTable
            personInntekter={previousYearInntekterResponse.forventedeInntekter.bruker}
            eps={false}
            year={previousYear}
          />
        )}
        {previousYearInntekterResponse.forventedeInntekter.eps !== null && previousYear !== null && (
          <PreviousExpectedIncomeTable
            personInntekter={previousYearInntekterResponse.forventedeInntekter.eps}
            eps={true}
            year={previousYear}
          />
        )}
      </VStack>

      <HStack gap="4">
        <Button
          as={RouterLink}
          to={getFullPathForPage(PageLinks.INDEX)}
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
          onClick={onClickButton}
          loading={isLoading}
        >
          Registrer inntekt for {selectedYear}
        </Button>
      </HStack>
    </VStack>
  )
}
