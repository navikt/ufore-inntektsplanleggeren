import {Heading, HStack, VStack, Alert, BodyLong} from "@navikt/ds-react";
import React, {useContext, useState, useEffect} from "react";
import {Link, useNavigate, useSearchParams} from "react-router-dom";


export const SuccessView = () => {


    return (
        <VStack className="form-container">
            <Heading level="2" size="small">Kvittering</Heading>
            <Alert variant="success">
            <Heading spacing size="small" level="3">Ny inntekt er mottatt av oss og saken er behandlet</Heading>
                Din registrerte forventede inntekt i 2024: 100 000 kr
                Din nye månedlige utbetaling er 11 000 kr fra 01.09.2024.
            </Alert>
    </VStack>
);
};