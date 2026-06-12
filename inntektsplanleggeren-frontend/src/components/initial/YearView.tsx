import { ArrowRightIcon } from '@navikt/aksel-icons'
import { Alert, Button, ErrorSummary, Heading, HStack, List, Radio, RadioGroup, ReadMore, VStack } from '@navikt/ds-react'
import { useContext, useRef, useState } from 'react'
import { FormStateContext } from '@/context/FormData'

interface Props {
    availableYears: number[]
    anotherAvalableYear: number | null
    handleSubmit: (year: number, previousYear: number | null) => void
    isLoading: boolean
}

export function YearView({ availableYears, anotherAvalableYear, handleSubmit, isLoading }: Props) {
    const { setPreviousYear } = useContext(FormStateContext)
    const [firstYear, secondYear] = availableYears
    const [year, setYear] = useState<number | undefined>(undefined)
    const [errors, setErrors] = useState({
        year: '',
    })
    const errorSummaryRef = useRef<HTMLDivElement>(null)

    if (availableYears.length === 0) {
        return null
    }

    if (firstYear !== undefined && secondYear === undefined && !year) {
        setYear(firstYear)
    }

    function onSubmitStart(event: React.FormEvent) {
        event.preventDefault()
        const newErrors = {
            year: year ? '' : 'Du må velge året du vil registrere inntekt for.',
        }
        setErrors(newErrors)

        if (year && !Object.values(newErrors).some(Boolean)) {
            setPreviousYear(null)
            handleSubmit(year, null)
        }
    }

    function onSubmitSeePreviousYear(event: React.FormEvent) {
        event.preventDefault()

        if (year) {
            setPreviousYear(anotherAvalableYear)
            handleSubmit(year, anotherAvalableYear)
        }
    }

    return (
        <section aria-label={'Start inntektsplanleggeren'}>
            <form onSubmit={onSubmitStart}>
                <VStack>
                    {availableYears.length === 1 ? (
                        <Heading size={'medium'} level={'2'} spacing>
                            Du kan registrere inntekt for {firstYear}
                        </Heading>
                    ) : (
                        <Heading size="medium" level="2">
                            Du kan registrere inntekt for {firstYear} og {secondYear}
                        </Heading>
                    )}

                    <VStack gap="space-28">
                        <ReadMore header="Tidspunkt for å registrere inntekt">
                            <List>
                                <List.Item>I perioden 1. januar - 30. september kan du bare legge inn inntekt for dette året.</List.Item>
                                <List.Item>Fra 1. oktober - 30. november kan du både legge inn inntekt for dette året og neste år.</List.Item>
                                <List.Item>
                                    Fra 1. til 31. desember kan du bare registrere inntekt for neste år, fordi endringen ikke vil påvirke utbetalingen din før
                                    til neste år.
                                </List.Item>
                            </List>
                        </ReadMore>

                        {anotherAvalableYear !== null || (firstYear !== undefined && secondYear !== undefined) ? (
                            <Alert variant="info">
                                Hvis du ikke sender inn ny forventet inntekt for neste år, lager vi en forventet inntekt for deg. Den vil være litt høyere enn
                                den forventede inntekten din for året vi er i nå.{' '}
                            </Alert>
                        ) : null}

                        {anotherAvalableYear == null && firstYear !== undefined && secondYear !== undefined ? (
                            <RadioGroup
                                id="year"
                                error={errors.year}
                                legend="Hvilket år ønsker du å registrere inntekt for?"
                                value={year}
                                onChange={(newValue) => {
                                    setYear(newValue)
                                    setErrors({ ...errors, year: '' })
                                }}
                            >
                                {availableYears.map((year) => (
                                    <Radio key={year} value={year}>
                                        {year.toString(10)}
                                    </Radio>
                                ))}
                            </RadioGroup>
                        ) : null}

                        {Object.values(errors).some(Boolean) && (
                            <ErrorSummary ref={errorSummaryRef} heading="Du må rette disse feilene før du kan fortsette">
                                {Object.entries(errors)
                                    .filter(([, error]) => error)
                                    .map(([key, error]) => (
                                        <ErrorSummary.Item href={`#${key}`} key={key}>
                                            {error}
                                        </ErrorSummary.Item>
                                    ))}
                            </ErrorSummary>
                        )}

                        {anotherAvalableYear == null ? (
                            <HStack>
                                <Button
                                    type="submit"
                                    onClick={onSubmitStart}
                                    variant="primary"
                                    loading={isLoading}
                                    iconPosition="right"
                                    icon={<ArrowRightIcon aria-hidden />}
                                >
                                    Start inntektsplanlegger
                                </Button>
                            </HStack>
                        ) : (
                            <VStack gap="space-24">
                                <HStack>
                                    <Button
                                        type="submit"
                                        onClick={onSubmitStart}
                                        variant="primary"
                                        loading={isLoading}
                                        iconPosition="right"
                                        icon={<ArrowRightIcon aria-hidden />}
                                    >
                                        Registrer inntekt for {year}
                                    </Button>
                                </HStack>
                                <HStack>
                                    <Button
                                        variant="secondary"
                                        onClick={onSubmitSeePreviousYear}
                                        loading={isLoading}
                                        iconPosition="right"
                                        icon={<ArrowRightIcon aria-hidden />}
                                    >
                                        Se tall for {anotherAvalableYear}
                                    </Button>
                                </HStack>
                            </VStack>
                        )}
                    </VStack>
                </VStack>
            </form>
        </section>
    )
}
