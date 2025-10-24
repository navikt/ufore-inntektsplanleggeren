import { Alert, VStack } from '@navikt/ds-react'
import { useNavigate } from 'react-router-dom'
import { getFullPathForPage, PageLinks } from '@/FormContainer'
import { FormEvent, MouseEvent, useContext, useEffect, useState } from 'react'
import { send } from '@/api/apiFetching'
import { FormStateContext } from '@/context/FormData'
import { DataContext } from '@/context/DataContextProvider'
import { ErrorCode, ErrorResponse } from '@/components/common/Error'
import { InntektSummary } from '@/components/oppsummering/InntektSummary'
import Knapperad from '@/components/common/Knapperad'

export const OppsummeringPage = () => {
    const navigate = useNavigate()
    const { selectedYear, setFormStep, brukerinntekt, annenForelderInntekt, getBrukerinntektSum, getAnnenForelderInntektSum } = useContext(FormStateContext)
    const { simulationResponse, setSendResponse, setErrorMessage } = useContext(DataContext)
    const [isLoading, setIsLoading] = useState(false)

    useEffect(() => {
        setFormStep(3)
    }, [setFormStep])

    const handleSubmit = async (e: MouseEvent | FormEvent) => {
        e.preventDefault()

        try {
            setIsLoading(true)
            const result = await send(brukerinntekt, annenForelderInntekt, selectedYear)
            if (result instanceof ErrorResponse) {
                setErrorMessage(result.message)
                setIsLoading(false)
            } else {
                setSendResponse(result)
                navigate(getFullPathForPage(PageLinks.KVITTERING))
            }
        } catch {
            setErrorMessage(ErrorCode.GENERIC_ERROR)
            setIsLoading(false)
        }

        navigate(getFullPathForPage(PageLinks.KVITTERING))
    }

    return (
        <VStack gap="12">
            <InntektSummary inntekt={brukerinntekt} inntektSum={getBrukerinntektSum()} type="bruker" />
            {annenForelderInntekt && <InntektSummary inntekt={annenForelderInntekt} inntektSum={getAnnenForelderInntektSum()!} type="eps" />}

            {simulationResponse?.messages.some((message) => message.messageCode === 'EPS_INNTEKT_CHANGED') && (
                <Alert variant="info">
                    Husk at inntektene du melder inn for annen forelder bare brukes for å beregne barnetillegget til uføretrygden din. Hvis den andre forelderen
                    har utbetalinger fra oss, må hen selv også melde fra om ny inntekt til oss.
                </Alert>
            )}

            <Knapperad handleSubmit={handleSubmit} tilbakePageLink={PageLinks.BEREGNING} gåVidereTekst="Send inn" laster={isLoading} />
        </VStack>
    )
}
