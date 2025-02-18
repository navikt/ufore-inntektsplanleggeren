import {Alert, Bleed, BodyLong, BodyShort, Heading, VStack} from "@navikt/ds-react";
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
            { !statusResponse || statusResponse.status === StatusCodes.TIL_BEHANDLING &&
                <VStack gap="5">
                    <Bleed marginInline="0 20">
                        <Alert variant="info">
                            <VStack gap="3">
                                <Heading size="small">Nav har mottatt opplysninger om inntekten din</Heading>
                                <BodyShort>Din registrerte inntekt i {selectedYear}: <strong><FormatKroner value={registeredInntekt}/> (før skatt)</strong></BodyShort>
                                { epsRegisteredInntekt != null && <BodyShort>Annen forelders registrerte inntekt i {selectedYear}: <strong><FormatKroner value={epsRegisteredInntekt}/> (før skatt)</strong></BodyShort> }
                            </VStack>
                        </Alert>
                    </Bleed>
                        <BodyShort>Mottatt av Nav: <FormatDateTime value={statusResponse.registeringsTidspunktEndring}/></BodyShort>  {/*    todo display date in nice format*/}
                        <BodyShort>Referansenummer: {statusResponse.sakId}</BodyShort>

                </VStack>
            }


            { statusResponse.status === StatusCodes.BEHANDLET_MEDFOERER_ENDRING &&
                <VStack gap="5">
                    <Bleed marginInline="0 20">
                        <Alert variant="success">
                            <VStack gap="3">
                                <Heading size="small">Ny inntekt er mottatt av oss og saken er behandlet</Heading>
                                <BodyShort>Din registrerte inntekt i {selectedYear}: <strong><FormatKroner value={registeredInntekt}/> (før skatt)</strong></BodyShort>
                                { epsRegisteredInntekt != null && <BodyShort>Annen forelders registrerte inntekt i {selectedYear}: <strong><FormatKroner value={epsRegisteredInntekt}/> (før skatt)</strong></BodyShort> }
                                { statusResponse.maandedligeUtbetalinger != null && <BodyShort>Din nye månedlige utbetaling fra <FormatDate value={statusResponse.maandedligeUtbetalinger.fom}/>: <strong><FormatKroner
                                    value={statusResponse.maandedligeUtbetalinger?.beloep}/> (før skatt)</strong></BodyShort> }                {/*    todo display date in nice format*/}
                            </VStack>
                        </Alert>
                    </Bleed>
                    <VStack>
                        <BodyShort>Mottatt av Nav: <FormatDateTime value={statusResponse.registeringsTidspunktEndring}/></BodyShort>  {/*    todo display date in nice format*/}
                        <BodyShort>Referansenummer: {statusResponse.sakId}</BodyShort>
                    </VStack>
                    <BodyLong>
                        Vi har behandlet saken din og du vil snart motta et vedtaksbrev i <Link target="_blank" to={import.meta.env.VITE_NAV_INNBOKS_URL}>Din innboks (åpnes i ny fane)</Link>.
                    </BodyLong>
                </VStack> }

            { statusResponse.status === StatusCodes.BEHANDLET_MEDFOERER_INGEN_ENDRING &&
                <VStack gap="5">
                    <Bleed marginInline="0 20">
                        <Alert variant="success">
                            <VStack gap="3">
                                <Heading size="small">Nye inntekter er registrert, og de påvirker ikke utbetalingen din</Heading>
                                <BodyShort>Din registrerte inntekt i {selectedYear}: <strong><FormatKroner value={registeredInntekt}/> (før skatt)</strong></BodyShort>
                                { epsRegisteredInntekt != null ? <BodyShort>Annen forelders registrerte inntekt i {selectedYear}: <strong><FormatKroner
                                    value={epsRegisteredInntekt}/> (før skatt)</strong></BodyShort> : null}
                                { statusResponse.maandedligeUtbetalinger != null ? <BodyShort>Du får månedlig utbetalt: <strong><FormatKroner value={statusResponse.maandedligeUtbetalinger?.beloep}/> (før skatt)</strong></BodyShort> : null}
                            </VStack>
                        </Alert>
                    </Bleed>
                    <VStack>
                        <BodyShort>Mottatt av Nav: <FormatDateTime value={statusResponse.registeringsTidspunktEndring}/></BodyShort>  {/*    todo display date in nice format*/}
                        <BodyShort>Referansenummer: {statusResponse.sakId}</BodyShort>
                    </VStack>
                    <BodyLong>
                        Inntekten du har sendt inn endrer ikke utbetalingen din. Du får derfor ikke et nytt vedtaksbrev fra oss.
                    </BodyLong>
                </VStack>
            }


        </VStack>
    )

}