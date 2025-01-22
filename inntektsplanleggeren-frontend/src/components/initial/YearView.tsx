import {
    Alert,
    Button,
    ErrorSummary,
    Heading,
    HStack,
    List,
    Radio,
    RadioGroup,
    ReadMore,
    VStack
} from "@navikt/ds-react";
import React, {useContext, useEffect, useState} from "react";
import {FormStateContext} from "@/context/FormData";
import {ArrowRightIcon} from "@navikt/aksel-icons";

interface Props {
    availableYears: number[],
    handleSubmit: (year: number) => void;
    isLoading: boolean;
}

export function YearView({ availableYears, handleSubmit, isLoading }: Props) {
    const [firstYear, secondYear] = availableYears;
    const [year, setYear] = React.useState<number | undefined>(undefined);
    const [errors, setErrors] = React.useState({
        year: ""
    });
    const errorSummaryRef = React.useRef<HTMLDivElement>(null);

    if (availableYears.length === 0) {
        return null;
    }

    if (firstYear !== undefined && secondYear === undefined) {
        setYear(firstYear);
    }

    function onSubmit(event: React.FormEvent) {
        event.preventDefault();
        const newErrors = {
            year: year ? "" : "Du må velge et år før du kan starte inntektsplanleggeren.",
        };
        setErrors(newErrors);

        if (year && !Object.values(newErrors).some(Boolean)) {
            handleSubmit(year);
        }
    }


    return (
            <form onSubmit={onSubmit}>
                <VStack>
                    {availableYears.length === 1 ?
                        <Heading size={"medium"} level={"2"} spacing>Du kan registrere inntekter for {firstYear}</Heading> :
                        <Heading size="medium" level="2">Du kan registrere inntekter
                            for {firstYear} og {secondYear}</Heading>
                    }

                    <VStack gap="7">
                        <ReadMore header="Tidspunkt for å registrere inntekt">
                            <List>
                                <List.Item>I perioden 1. januar - 30. september kan du bare legge inn inntekter for dette
                                    året.</List.Item>
                                <List.Item>Fra 1. oktober - 30. november kan du både legge inn inntekter for dette året og
                                    neste år.</List.Item>
                                <List.Item>Fra 1. til 31. desember kan du bare registrere inntekter for neste år, fordi
                                    endringen ikke vil påvirke utbetalingen din før til neste år.</List.Item>
                            </List>
                        </ReadMore>

                        {firstYear !== undefined && secondYear !== undefined ?
                            <Alert variant="info">Hvis du ikke sender inn ny forventet inntekt for neste år, lager vi en
                                forventet inntekt for deg. Den vil være litt høyere enn den forventede inntekten din for
                                året vi er i nå. </Alert> : null
                        }

                        {firstYear !== undefined && secondYear !== undefined ?
                            <RadioGroup id="year"
                                    error={errors.year}
                                    legend="Hvilket år ønsker du å registrere inntekter for?"
                                    value={year}
                                    onChange={(newValue) => {
                                        setYear(newValue);
                                        setErrors({ ...errors, year: "" });
                                    }}>
                                {availableYears.map(year => <Radio key={year} value={year}>{year.toString(10)}</Radio>)}
                            </RadioGroup> : null
                        }

                        {Object.values(errors).some(Boolean) && (
                            <ErrorSummary ref={errorSummaryRef} heading="Du må rette disse feilene før du kan fortsette:">
                                {Object.entries(errors)
                                    .filter(([, error]) => error)
                                    .map(([key, error]) => (
                                        <ErrorSummary.Item href={`#${key}`} key={key}>
                                            {error}
                                        </ErrorSummary.Item>
                                    ))}
                            </ErrorSummary>
                        )}

                        <HStack>
                            <Button type="submit" onClick={onSubmit} variant="primary" loading={isLoading}
                                    iconPosition="right" icon={<ArrowRightIcon aria-hidden/>}>
                                Start inntektsplanlegger
                            </Button>
                        </HStack>
                    </VStack>
                </VStack>
            </form>
    )
}



