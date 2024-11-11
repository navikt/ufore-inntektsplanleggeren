import {Alert, BodyLong, BodyShort, Heading, VStack} from "@navikt/ds-react";
import {StatusCodes} from "@/api/model/StatusCodes";
import {StatusResponse} from "@/api/model/ApiRequests";
import {FormatKroner} from "@/components/utils/FormatKroner";
import {useContext} from "react";
import {FormStateContext} from "@/context/FormData";

interface Props {
    statusResponse: StatusResponse

}

export const KvitteringStatusBox = ({ statusResponse }: Props) => {
    const {selectedYear} = useContext(FormStateContext)


    return (
        <VStack>
            { statusResponse.status === StatusCodes.TIL_BEHANDLING ?
                <Alert variant="info">
                    <Heading size="small">Nav har mottatt opplysninger om inntekten din</Heading>
                    {statusResponse.maandedligeUtbetalinger ?
                        <BodyLong>Din registrerte inntekt i {selectedYear} </BodyLong> : null}
                </Alert> : null }

            { statusResponse.status === StatusCodes.BEHANDLET_MEDFOERER_ENDRING ?
                <Alert variant="success">
                    <Heading size="small">Ny inntekt er mottatt av oss og saken er behandlet</Heading>
                    {statusResponse.maandedligeUtbetalinger ?
                        <BodyLong>Din registrerte inntekt i {selectedYear}: <FormatKroner
                            value={statusResponse.maandedligeUtbetalinger?.beloep}/></BodyLong> : null}
                </Alert> : null }

            { statusResponse.status === StatusCodes.BEHANDLET_MEDFOERER_INGEN_ENDRING ?
                <Alert variant="success">Saken din er ferdig behandlet</Alert> : null
            }


            <BodyShort>Mottatt av Nav {statusResponse.registeringsTidspunktEndring}</BodyShort>
            <BodyShort>Referansenummer {statusResponse.sakId}</BodyShort>
        </VStack>
    )

}