import React from "react";
import {Box, VStack, BodyLong, Button, HStack} from "@navikt/ds-react";
import { numberFormatWithKr } from "@/common/Utils";
import {ChevronDownIcon, ChevronUpIcon} from "@navikt/aksel-icons";

interface ExpectedIncomeBoxProps {
    forventetInntekt: number;
    forventetInntektAnnenForelder?: number | null | undefined;
}

export const ExpectedIncomeBox: React.FC<ExpectedIncomeBoxProps> = ({ forventetInntekt, forventetInntektAnnenForelder }) => {
    const [isOpen, setIsOpen] = React.useState(false)
    const [buttonText, setButtonText] = React.useState("Vis forklaring")

    const handleButton = () => {
        setIsOpen(!isOpen)
        setButtonText(isOpen ? "Vis forklaring" : "Skjul forklaring")
    }

    return(
        <Box borderRadius="xlarge" padding="4" className="top-box">
            <VStack gap="1">
                <VStack gap="6">
                    <section>
                        <BodyLong> Din forventede inntekt: <b>{numberFormatWithKr(forventetInntekt)}</b></BodyLong>
                        {forventetInntektAnnenForelder !== null && forventetInntektAnnenForelder !== undefined ?
                            <BodyLong> Annen forelder du bor med sin forventede inntekt: <b>{numberFormatWithKr(forventetInntektAnnenForelder)}</b></BodyLong> : <></>
                        }
                    </section>

                    {isOpen &&
                        <BodyLong>
                            Forventet inntekt kan komme fra dine tidligere registreringer, eller i noen tilfeller fra opplysninger vi har hentet. Har du nylig meldt inn ny inntekt, vil den ikke vises her før den har blitt behandlet hos oss.
                        </BodyLong>
                    }
                    <HStack justify="center">
                        <Button onClick={handleButton} variant="secondary-neutral" iconPosition="right" icon={isOpen ? <ChevronUpIcon aria-hidden /> : <ChevronDownIcon aria-hidden />}>{buttonText}</Button>
                    </HStack>
                </VStack>
            </VStack>
        </Box>
    );
}
