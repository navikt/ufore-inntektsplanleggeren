import {Heading, HStack, VStack, Alert, BodyLong} from "@navikt/ds-react";
import React, {useContext, useState, useEffect} from "react";
import {Link, useNavigate, useSearchParams} from "react-router-dom";


export const Kvittering = () => {


    return (
        <VStack className="form-container">
            <Heading level="2" size="small">Kvittering</Heading>
            <Alert variant="success">
                <Heading spacing size="small" level="3">        Viktig informasjon      </Heading>
                blabla
                {/*<BodyLong>Something</BodyLong>*/}
                {/*<BodyLong>Something</BodyLong>*/}
            </Alert>

        </VStack>
    );
};