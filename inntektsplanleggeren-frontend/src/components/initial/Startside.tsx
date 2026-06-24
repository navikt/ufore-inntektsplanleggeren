import { Alert, VStack } from '@navikt/ds-react'
import { useContext, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getInntekter, getInntekterForSimulering } from '@/api/apiFetching'
import { MessageCodes, MessageTypes } from '@/api/model/MessageCodes'
import { ErrorCode, ErrorResponse, ErrorView } from '@/components/common/Error'
import { LoadingBox } from '@/components/initial/LoadingBox'
import StartsideInnhold from '@/components/initial/StartsideInnhold'
import StartsideInnholdGammel from '@/components/initial/StartsideInnholdGammel'
import { YearView } from '@/components/initial/YearView'
import { DataContext } from '@/context/DataContextProvider'
import { FormStateContext } from '@/context/FormData'
import { getFullPathForPage, PageLinks } from '@/FormContainer'
import { useToggle } from '@/hooks/useToggle'

export function Startside() {
    const { initiateResponse, setInntekterResponse, setPreviousYearInntekterResponse, errorMessage, setErrorMessage } = useContext(DataContext)
    const { setSelectedYear, setBrukerinntekt, setAnnenForelderInntekt } = useContext(FormStateContext)
    const navigate = useNavigate()
    const [isLoading, setIsLoading] = useState<boolean>(false)
    const regelverksendringer2026 = useToggle('inntektsplanleggeren.regelverksendringer.tekst')

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
            {regelverksendringer2026 ? (
                <StartsideInnhold data={initiateResponse.data} handleButtonClick={handleButtonClick} isLoading={isLoading} />
            ) : (
                <StartsideInnholdGammel data={initiateResponse.data} />
            )}

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
