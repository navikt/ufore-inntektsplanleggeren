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
import LonnFordelerOgPengestotter from '../common/LonnFordelerOgPengestotter'
import PensjonFraAndreEnnNav from '../common/PensjonFraAndreEnnNav'

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
            <VStack className="page">
                <VStack gap="4">
                    {(previousYearInntekterResponse.pensjonFraAndreHittilIAar?.length > 0 ||
                        previousYearInntekterResponse.pensjonFraAndreHittilIAar?.length > 0) && (
                        <VStack>
                            <Heading level="2" size="medium">
                                {previousYearInntekterResponse.uforeHeleAaret
                                    ? `Din inntekt hittil i ${previousYear}`
                                    : `Din inntekt samtidig med uføretrygd i ${previousYear}`}
                            </Heading>
                            <BodyLong>
                                Under kan du se hvilken inntekt som er registrert hos Skatteetaten
                                {previousYearInntekterResponse.uforeHeleAaret ? '.' : ' den delen av året du har hatt uføretrygd.'}
                            </BodyLong>
                        </VStack>
                    )}

                    {previousYearInntekterResponse.arbeidsinntektOgYtelserHittilIAar?.length > 0 && (
                        <LonnFordelerOgPengestotter
                            uforeHeleAaret={previousYearInntekterResponse.uforeHeleAaret}
                            inntekter={previousYearInntekterResponse.arbeidsinntektOgYtelserHittilIAar}
                        />
                    )}
                    {previousYearInntekterResponse.pensjonFraAndreHittilIAar?.length > 0 && (
                        <PensjonFraAndreEnnNav
                            pensjonFraAndre={previousYearInntekterResponse.pensjonFraAndreHittilIAar}
                            uforeHeleAaret={previousYearInntekterResponse.uforeHeleAaret}
                        />
                    )}
                </VStack>

                <VStack gap="4">
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
                </VStack>
            </VStack>

            <HStack gap="4">
                <Button as={RouterLink} to={getFullPathForPage(PageLinks.INDEX)} iconPosition="left" icon={<ArrowLeftIcon aria-hidden />} variant="secondary">
                    Tilbake
                </Button>
                <Button type="button" variant="primary" iconPosition="right" icon={<ArrowRightIcon aria-hidden />} onClick={onClickButton} loading={isLoading}>
                    Registrer inntekt for {selectedYear}
                </Button>
            </HStack>
        </VStack>
    )
}
