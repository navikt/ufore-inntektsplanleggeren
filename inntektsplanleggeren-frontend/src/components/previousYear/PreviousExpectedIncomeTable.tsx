import {PersonInntekter} from "@/api/model/ApiRequests";
import {BodyShort, Table, VStack} from "@navikt/ds-react";
import {FormatKroner} from "@/components/utils/FormatKroner";
import React, {useEffect, useState} from "react";
import {DESKTOP_WIDTH} from "@/FormContainer";


export const PreviousExpectedIncomeTable = (props: {personInntekter: PersonInntekter, year: number, eps: boolean}) => {
    const { arbeidsinntekt, andrePensjonsgivendeYtelser, naeringsinntekt, inntektUtland, pensjonUtland } = props.personInntekter;
    const [width, setWidth] = useState<number>(window.innerWidth);


    const handleWindowSize = () => setWidth(window.innerWidth);

    useEffect(() => {
        window.addEventListener('resize', handleWindowSize);
        return () => window.removeEventListener('resize', handleWindowSize);
    });

    const isDesktop = width > DESKTOP_WIDTH;

    return ( isDesktop ?
             <VStack gap="6">
                 <Table>
                    <Table.Header>
                        <Table.Row>
                            <Table.HeaderCell scope="col">{props.eps ? "Annen forelders" : "Din"} forventede inntekt i {props.year}</Table.HeaderCell>
                            <Table.HeaderCell scope="col" align="right">Beløp før skatt</Table.HeaderCell>
                        </Table.Row>
                    </Table.Header>
                    <Table.Body>
                        <Table.Row>
                            <Table.DataCell scope="row">Lønn og pensjonsgivende ytelser</Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={arbeidsinntekt || 0}/></Table.DataCell>
                        </Table.Row>
                        <Table.Row>
                            <Table.DataCell scope="row">Næringsinntekt</Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={naeringsinntekt || 0}/></Table.DataCell>
                        </Table.Row>
                        <Table.Row>
                            <Table.DataCell scope="row">Inntekt fra utlandet</Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={inntektUtland || 0}/></Table.DataCell>
                        </Table.Row>
                        <Table.Row>
                            <Table.DataCell scope="row">Uførepensjon og pensjoner fra andre enn Nav</Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={andrePensjonsgivendeYtelser || 0}/></Table.DataCell>
                        </Table.Row>
                        <Table.Row>
                            <Table.DataCell scope="row">Pensjoner fra utlandet</Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={pensjonUtland || 0}/></Table.DataCell>
                        </Table.Row>
                        <Table.Row  style={{ backgroundColor: "var(--a-bg-subtle)" }}>
                            <Table.DataCell scope="row"><strong>Sum {props.eps ? "annen forelders" : "din"} forventede inntekt</strong></Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={(arbeidsinntekt || 0) + (naeringsinntekt || 0) + (inntektUtland || 0) + (andrePensjonsgivendeYtelser || 0) + (pensjonUtland || 0)}/></Table.DataCell>
                        </Table.Row>
                    </Table.Body>
                </Table>
             </VStack>

            : <Table>
                <Table.Header>
                    <BodyShort size={"large"}><strong>{props.eps ? "Annen forelders" : "Din"}  forventede inntekt i {props.year}</strong></BodyShort>
                </Table.Header>
                <Table.Body>
                        <Table.Row>
                            <Table.DataCell>
                            <VStack gap="1">
                                <BodyShort><strong>{"Lønn og pensjonsgivende ytelser"}</strong></BodyShort>
                                <BodyShort>Beløp før skatt: <FormatKroner value={arbeidsinntekt || 0}/></BodyShort>
                            </VStack>
                            </Table.DataCell>
                        </Table.Row>
                    <Table.Row>
                        <Table.DataCell>
                            <VStack gap="1">
                                <BodyShort><strong>{"Næringsinntek"}</strong></BodyShort>
                                <BodyShort>Beløp før skatt: <FormatKroner value={naeringsinntekt || 0}/></BodyShort>
                            </VStack>
                        </Table.DataCell>
                    </Table.Row>
                    <Table.Row>
                        <Table.DataCell>
                            <VStack gap="1">
                                <BodyShort><strong>{"Inntekt fra utlandet"}</strong></BodyShort>
                                <BodyShort>Beløp før skatt: <FormatKroner value={inntektUtland || 0}/></BodyShort>
                            </VStack>
                        </Table.DataCell>
                    </Table.Row>
                    <Table.Row>
                        <Table.DataCell>
                            <VStack gap="1">
                                <BodyShort><strong>{"Uførepensjon og pensjoner fra andre enn Nav"}</strong></BodyShort>
                                <BodyShort>Beløp før skatt: <FormatKroner value={andrePensjonsgivendeYtelser || 0}/></BodyShort>
                            </VStack>
                        </Table.DataCell>
                    </Table.Row>
                    <Table.Row>
                        <Table.DataCell>
                            <VStack gap="1">
                                <BodyShort><strong>{"Pensjoner fra utlandet"}</strong></BodyShort>
                                <BodyShort>Beløp før skatt: <FormatKroner value={pensjonUtland || 0}/></BodyShort>
                            </VStack>
                        </Table.DataCell>
                    </Table.Row>
                    <Table.Row>
                        <Table.DataCell style={{ backgroundColor: "var(--a-bg-subtle)" }}>
                            <VStack gap="1">
                                <BodyShort><strong>Sum {props.eps ? "annen forelders" : "din"} forventede inntekt</strong></BodyShort>
                                <BodyShort>Beløp før skatt: <FormatKroner value={(arbeidsinntekt || 0) + (naeringsinntekt || 0) + (inntektUtland || 0) + (andrePensjonsgivendeYtelser || 0) + (pensjonUtland || 0)}/></BodyShort>
                            </VStack>
                        </Table.DataCell>
                    </Table.Row>
                </Table.Body>
            </Table>
    );
};