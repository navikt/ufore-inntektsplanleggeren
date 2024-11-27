import {Alert, BodyLong, BodyShort, Heading, VStack} from "@navikt/ds-react";
import {StatusCodes} from "@/api/model/StatusCodes";
import {StatusResponse} from "@/api/model/ApiRequests";
import {FormatKroner} from "@/components/utils/FormatKroner";
import {useContext} from "react";
import {FormStateContext} from "@/context/FormData";
import {Link} from "react-router-dom";
import {FormatDate, FormatDateTime} from "@/components/utils/FormatDate";

interface Props {
    statusResponse: StatusResponse
    registeredInntekt: number
    epsRegisteredInntekt: number | null
}

export const KvitteringStatusBox = ({ statusResponse, registeredInntekt, epsRegisteredInntekt }: Props) => {
    const {selectedYear} = useContext(FormStateContext)


    return (
        <VStack>


            { statusResponse.status === StatusCodes.BEHANDLET_MEDFOERER_ENDRING ?
                <VStack gap="5">
                    <Alert variant="success">
                        <VStack gap="3">
                            <Heading size="small">Ny inntekt er mottatt av oss og saken er behandlet</Heading>
                            <BodyShort>Din registrerte inntekt i {selectedYear}: <strong><FormatKroner value={registeredInntekt}/> (før skatt)</strong></BodyShort>
                            { epsRegisteredInntekt && <BodyShort>Annen forelders registrerte inntekt i {selectedYear}: <strong><FormatKroner value={epsRegisteredInntekt}/> (før skatt)</strong></BodyShort> }
                            { statusResponse.maandedligeUtbetalinger && <BodyShort>Din nye månedlige utbetaling fra <FormatDate value={statusResponse.maandedligeUtbetalinger.fom}/>: <strong><FormatKroner
                                value={statusResponse.maandedligeUtbetalinger?.beloep}/> (før skatt)</strong></BodyShort> }                {/*    todo display date in nice format*/}
                        </VStack>
                    </Alert>
                    <VStack>
                        <BodyShort>Mottatt av Nav: <FormatDateTime value={statusResponse.registeringsTidspunktEndring}/></BodyShort>  {/*    todo display date in nice format*/}
                        <BodyShort>Referansenummer: {statusResponse.sakId}</BodyShort>
                    </VStack>
                    <BodyLong>
                        Vi har behandlet saken din og du vil snart motta et vedtaksbrev i <Link to="/">Din innboks (åpnes i ny fane)</Link>.  {/* TODO open in new tab!   */}
                    </BodyLong>
                </VStack>: null }

            { statusResponse.status === StatusCodes.BEHANDLET_MEDFOERER_INGEN_ENDRING ?
                <VStack gap="5">
                    <Alert variant="success">
                        <VStack gap="3">
                            <Heading size="small">Nye inntekter er registrert, og de påvirker ikke utbetalingen din</Heading>
                            <BodyShort>Din registrerte inntekt i {selectedYear}: <strong><FormatKroner value={registeredInntekt}/> (før skatt)</strong></BodyShort>
                            { epsRegisteredInntekt && <BodyShort>Annen forelders registrerte inntekt i {selectedYear}: <strong><FormatKroner
                                value={epsRegisteredInntekt}/> (før skatt)</strong></BodyShort> }
                            { statusResponse.maandedligeUtbetalinger && <BodyShort>Du får månedlig utbetalt: <strong><FormatKroner value={statusResponse.maandedligeUtbetalinger?.beloep}/> (før skatt)</strong></BodyShort> }
                        </VStack>
                    </Alert>
                    <VStack>
                        <BodyShort>Mottatt av Nav: <FormatDateTime value={statusResponse.registeringsTidspunktEndring}/></BodyShort>  {/*    todo display date in nice format*/}
                        <BodyShort>Referansenummer: {statusResponse.sakId}</BodyShort>
                    </VStack>
                    <BodyLong>
                        Inntekten du har sendt inn endrer ikke utbetalingen din. Du får derfor ikke et nytt vedtaksbrev fra oss.
                    </BodyLong>
                </VStack>: null
            }

            { statusResponse.status === StatusCodes.TIL_BEHANDLING ?
                <VStack gap="5">
                    <Alert variant="info">
                        <Heading size="small">Nav har mottatt opplysninger om inntekten din</Heading>
                        <BodyShort>Din registrerte inntekt i {selectedYear}: <strong><FormatKroner value={registeredInntekt}/> (før skatt)</strong></BodyShort>
                        { epsRegisteredInntekt && <BodyShort>Annen forelders registrerte inntekt i {selectedYear}: <strong><FormatKroner
                            value={epsRegisteredInntekt}/> (før skatt)</strong></BodyShort> }
                    </Alert>
                    <VStack>
                        <BodyShort>Mottatt av Nav: <FormatDateTime value={statusResponse.registeringsTidspunktEndring}/></BodyShort>  {/*    todo display date in nice format*/}
                        <BodyShort>Referansenummer: {statusResponse.sakId}</BodyShort>
                    </VStack>
                </VStack>: null
            }
        </VStack>
    )

}