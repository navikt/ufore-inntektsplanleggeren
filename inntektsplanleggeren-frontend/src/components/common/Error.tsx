import {Alert, BodyShort, VStack} from "@navikt/ds-react";
import React from "react";


export const ErrorView = (props: { message: ErrorCode | null }) => {
    const getErrorMessage = () => {
        switch (props.message) {
            case ErrorCode.LOGIN_LEVEL_TOO_LOW:
                return (
                    <>
                        <BodyShort>Du må logge inn med et høyere sikkerhetsnivå for å få tilgang til denne siden.</BodyShort>
                        <BodyShort>Du kan for eksempel bruke BankID.</BodyShort>
                    </>
                )
            case ErrorCode.VEILEDER_UNAUTHORIZED:
            case ErrorCode.NO_FULLMAKT_PRESENT:
                return "Du har ikke tilgang til denne siden."
            case ErrorCode.GENERIC_ERROR:
                return "Det har skjedd en teknisk feil. Hvis du har registrert informasjon, har den dessverre ikke blitt lagret. " +
                    "Vi beklager for dette. Du kan prøve igjen senere. Ta gjerne kontakt med oss hvis problemet fortsetter."
            case ErrorCode.STATUS_ERROR:
                return "Det har skjedd en teknisk feil. Vi klarte ikke å sjekke status på behandlingen av inntektsendringen din. Ta gjerne kontakt med oss hvis problemet fortsetter."
        }
    }

    return <>{props.message!==null && <Alert variant="error" role="alert">{getErrorMessage()}</Alert>}</>
}

export class ErrorResponse {
    message: ErrorCode

    constructor(data: ErrorResponse) {
        this.message = data.message
    }
}

export enum ErrorCode {
    LOGIN_LEVEL_TOO_LOW = "LOGIN_LEVEL_TOO_LOW",
    VEILEDER_UNAUTHORIZED = "VEILEDER_UNAUTHORIZED",
    NO_FULLMAKT_PRESENT = "NO_FULLMAKT_PRESENT",
    GENERIC_ERROR = "GENERIC_ERROR",
    STATUS_ERROR = "STATUS_ERROR",
}