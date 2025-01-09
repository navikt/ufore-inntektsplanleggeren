import React from "react";
import {Box, VStack, BodyLong, Button, HStack, Heading} from "@navikt/ds-react";
import {ChevronDownIcon, ChevronUpIcon} from "@navikt/aksel-icons";
import {FormatKroner} from "@/components/utils/FormatKroner";

interface ExpectedIncomeBoxProps {
    forventetInntekt: Record<number, number | null>
    forventetInntektAnnenForelder: Record<number, number | null>
    hasBarnetilleggFellesbarn: boolean
    hasBarnetilleggSaerkullsbarn: boolean

}

export const ExpectedIncomeBox: React.FC<ExpectedIncomeBoxProps> = ({ forventetInntekt, forventetInntektAnnenForelder, hasBarnetilleggFellesbarn, hasBarnetilleggSaerkullsbarn }) => {
    const [isOpen, setIsOpen] = React.useState(false)
    const [buttonText, setButtonText] = React.useState("Vis forklaring")
    const expectedIncomeMap = new Map(Object.entries(forventetInntekt))
    const expectedIncomeAnnenForelderMap = new Map(Object.entries(forventetInntektAnnenForelder))

    const handleButton = () => {
        setIsOpen(!isOpen)
        setButtonText(isOpen ? "Vis forklaring" : "Skjul forklaring")
    }

    const ExpectedIncome = ({year}: {year:string}) => {
        const expectedIncome = expectedIncomeMap.get(year)
        const expectedIncomeAnnenForelder = expectedIncomeAnnenForelderMap.get(year)
        return (
            <section>
                <Heading size="small">Registrert forventet inntekt for {year}</Heading>
                <BodyLong> {"Din forventede inntekt: "}
                    {expectedIncome !== null && expectedIncome !== undefined ?
                        <b><FormatKroner value={expectedIncome}/></b>
                        : <b>Ingen registrert inntekt funnet</b>}
                </BodyLong>
                {hasBarnetilleggFellesbarn || hasBarnetilleggSaerkullsbarn ?
                        <BodyLong> {"Annen forelder du bor med sin forventede inntekt: "}
                            {expectedIncomeAnnenForelder !== null && expectedIncomeAnnenForelder !== undefined ?
                                <b><FormatKroner value={expectedIncomeAnnenForelder}/></b>
                                : <b>Ingen registrert inntekt funnet</b>}
                        </BodyLong>
                    : <></>
                }
            </section>)
    }

    return (
        <Box borderRadius="xlarge" padding="4" className="top-box">
            <VStack gap="7">
                {Array.from(expectedIncomeMap.keys()).map((year) => (
                    <ExpectedIncome year={year}/>
                ))}
                {isOpen &&
                    <BodyLong>
                        Forventet inntekt kan komme fra dine tidligere registreringer, eller i noen tilfeller fra
                        opplysninger vi har hentet. Har du nylig meldt inn ny inntekt, vil den ikke vises her før den
                        har blitt behandlet hos oss.
                    </BodyLong>
                }
                <HStack justify="center">
                    <Button onClick={handleButton} variant="secondary-neutral" iconPosition="right"
                            icon={isOpen ? <ChevronUpIcon aria-hidden/> :
                                <ChevronDownIcon aria-hidden/>}>{buttonText}</Button>
                </HStack>
            </VStack>
        </Box>
    );
}
