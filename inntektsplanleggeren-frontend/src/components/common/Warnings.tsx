import {Message} from "@/api/model/ApiRequests";
import {MessageCodes} from "@/api/model/MessageCodes";
import {Alert, VStack} from "@navikt/ds-react";
import React from "react";


export function Warnings(props: {
    messages: Message[]
}) {
    return (
        <VStack>
            { props.messages.some(message => message.messageCode === MessageCodes.USER_HAS_NO_UFORE) &&
                <Alert variant="warning">
                    Du har ikke uføretrygd. Derfor kan du ikke bruke inntektsplanleggeren.
                </Alert> }
            { props.messages.some(message => message.messageCode === MessageCodes.USER_HAS_NO_LOPENDE_VEDTAK_YET) &&
                <Alert variant="warning">
                    Du kan ikke bruke inntektsplanleggeren ennå. Din inntekt kan registreres her fra måneden før din første utbetaling av uføretrygd.
                </Alert> }
        </VStack>
    )
}