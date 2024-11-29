import {Button, HStack, VStack} from "@navikt/ds-react";
import {Link as RouterLink, useNavigate} from "react-router-dom";
import {PageLinks} from "@/form-container";
import {ArrowLeftIcon, ArrowRightIcon} from "@navikt/aksel-icons";
import {CancelConfirmationModal} from "@/components/common/CancelConfirmationModal";
import React, {FormEvent, MouseEvent, useContext, useEffect, useState} from "react";
import {send} from "@/api/apiFetching";
import {FormStateContext} from "@/context/FormData";
import {SelectedYearContext} from "@/context/SelectedYear";
import {DataContext} from "@/DataContextProvider";

export const OppsummeringPage = () => {
    const navigate = useNavigate();
    const { setFormStep, brukerinntekt, annenForelderInntekt } = useContext(FormStateContext);
    const { setSendResponse } = useContext(DataContext);
    const { selectedYear } = useContext(SelectedYearContext);
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        setFormStep(2)
    }, [setFormStep]);

    const handleSubmit = async (e: MouseEvent | FormEvent) => {
        e.preventDefault();

        try {
            setIsLoading(true);
            const result = await send(brukerinntekt, annenForelderInntekt, selectedYear);
            setSendResponse(result);
            navigate(PageLinks.KVITTERING);
        } catch (error) {
            console.error("Error submitting income simulation:", error);
        }

        navigate(PageLinks.KVITTERING);
    }

    return (
        <VStack gap="4">
            <HStack gap="4">
                <Button as={RouterLink} to={PageLinks.FORVENTEDE_INNTEKTER} iconPosition="left" icon={<ArrowLeftIcon aria-hidden />} variant="secondary">
                    Endre beløp i beregning
                </Button>
                <Button variant="primary" iconPosition="right" icon={<ArrowRightIcon aria-hidden/>}onClick={handleSubmit} loading={isLoading}>
                    Gå til innsending
                </Button>
            </HStack>
            <HStack>
                <CancelConfirmationModal/>
            </HStack>
        </VStack>
    )

}