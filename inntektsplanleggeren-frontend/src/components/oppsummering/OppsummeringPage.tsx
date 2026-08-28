import { Alert, VStack } from '@navikt/ds-react'
import { type FormEvent, type MouseEvent, useContext, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { send } from '@/api/apiFetching'
import Knapperad from '@/components/common/Knapperad'
import { InntektSummary } from '@/components/oppsummering/InntektSummary'
import { DataContext } from '@/context/DataContextProvider'
import { FormStateContext } from '@/context/FormData'
import { getFullPathForPage, PageLinks } from '@/FormContainer'

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

        setIsLoading(true)
        const result = await send(brukerinntekt, annenForelderInntekt, selectedYear)
        if (!result.ok) {
            setErrorMessage(result.error)
            setIsLoading(false)
        } else {
            setSendResponse(result.data)
            navigate(getFullPathForPage(PageLinks.KVITTERING))
        }

        navigate(getFullPathForPage(PageLinks.KVITTERING))
    }

    return (
        <VStack gap="space-48">
            <section aria-label={'Din inntekt'}>
                <InntektSummary inntekt={brukerinntekt} inntektSum={getBrukerinntektSum()} type="bruker" />
            </section>
            {annenForelderInntekt && (
                <section aria-label={'Annen forelders forventede inntekt'}>
                    <InntektSummary inntekt={annenForelderInntekt} inntektSum={getAnnenForelderInntektSum()!} type="eps" />
                </section>
            )}
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
