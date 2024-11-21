import { SimulationResult } from "@/api/model/ApiRequests";
import {BodyShort, Table, VStack} from "@navikt/ds-react";
import {FormatKroner} from "@/components/utils/FormatKroner";
import React, {useEffect, useState} from "react";
import {Month} from "@/common/MonthEnum";

export const SimulationTable = (props: { simulationResult: SimulationResult }) => {
    const { uforetrygd, forventetInntekt, barnetilleggFellesbarn, barnetilleggSaerkullsbarn, gjenlevendetillegg, sum } = props.simulationResult;
    const [width, setWidth] = useState<number>(window.innerWidth);

    const handleWindowSize = () => setWidth(window.innerWidth);

    useEffect(() => {
        window.addEventListener('resize', handleWindowSize);
        return () => window.removeEventListener('resize', handleWindowSize);
    });

    const isDesktop = width > 768;



    return ( isDesktop ?
             <VStack gap="6">
                 <Table>
                    <Table.Header>
                        <Table.Row>
                            <Table.HeaderCell scope="col"></Table.HeaderCell>
                            <Table.HeaderCell scope="col" align="right">I dag</Table.HeaderCell>
                            <Table.HeaderCell scope="col" align="right">Med dine endringer</Table.HeaderCell>
                        </Table.Row>
                    </Table.Header>
                    <Table.Body>
                        <Table.Row>
                            <Table.HeaderCell scope="row">Uføretrygd inkludert gjenlevendetillegg</Table.HeaderCell>
                            <Table.DataCell align="right"><FormatKroner value={uforetrygd.yearly.before + (gjenlevendetillegg?.yearly.before ?? 0)}/></Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={uforetrygd.yearly.after + (gjenlevendetillegg?.yearly.after ?? 0)}/></Table.DataCell>
                        </Table.Row>
                        <Table.Row>
                            <Table.HeaderCell scope="row">Forventet Inntekt</Table.HeaderCell>
                            <Table.DataCell align="right"><FormatKroner value={forventetInntekt.yearly.before}/></Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={forventetInntekt.yearly.after}/></Table.DataCell>
                        </Table.Row>
                        {barnetilleggSaerkullsbarn || barnetilleggFellesbarn ?
                            <Table.Row>
                                <Table.HeaderCell scope="row">Barnetillegg</Table.HeaderCell>
                                <Table.DataCell align="right"><FormatKroner value={(barnetilleggFellesbarn?.yearly.before ?? 0) + (barnetilleggSaerkullsbarn?.yearly.before ?? 0)}/></Table.DataCell>
                                <Table.DataCell align="right"><FormatKroner value={(barnetilleggFellesbarn?.yearly.after ?? 0) + (barnetilleggSaerkullsbarn?.yearly.after ?? 0)}/></Table.DataCell>
                            </Table.Row>: null }
                        <Table.Row>
                            <Table.HeaderCell scope="row">Sum årlig</Table.HeaderCell>
                            <Table.DataCell align="right"><FormatKroner value={sum.yearly.before}/></Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={sum.yearly.after}/></Table.DataCell>
                        </Table.Row>
                    </Table.Body>
                </Table>
             </VStack>

            : <Table>
                <Table.Body>
                        <Table.Row>
                            <Table.DataCell>
                            <VStack gap="1">
                                <BodyShort><strong>Uføretrygd inkludert gjenlevendetillegg</strong></BodyShort>
                                <BodyShort>I dag: <FormatKroner value={uforetrygd.yearly.before + (gjenlevendetillegg?.yearly.before ?? 0)}/></BodyShort>
                                <BodyShort>Med dine engringer: <FormatKroner value={uforetrygd.yearly.after + (gjenlevendetillegg?.yearly.after ?? 0)}/></BodyShort>
                            </VStack>
                            </Table.DataCell>
                        </Table.Row>
                        <Table.Row>
                            <Table.DataCell>
                            <VStack gap="1">
                                <BodyShort><strong>Forventet Inntekt</strong></BodyShort>
                                <BodyShort>I dag: <FormatKroner value={forventetInntekt.yearly.before}/></BodyShort>
                                <BodyShort>Med dine engringer: <FormatKroner value={forventetInntekt.yearly.after}/></BodyShort>
                            </VStack>
                            </Table.DataCell>
                        </Table.Row>
                        {barnetilleggSaerkullsbarn || barnetilleggFellesbarn ?
                            <Table.Row>
                                <Table.DataCell>
                                    <VStack gap="1">
                                        <BodyShort><strong>Barnetillegg</strong></BodyShort>
                                        <BodyShort>I dag: <FormatKroner value={(barnetilleggFellesbarn?.yearly.before ?? 0) + (barnetilleggSaerkullsbarn?.yearly.before ?? 0)}/></BodyShort>
                                        <BodyShort>Med dine engringer: <FormatKroner value={(barnetilleggFellesbarn?.yearly.after ?? 0) + (barnetilleggSaerkullsbarn?.yearly.after ?? 0)}/></BodyShort>
                                    </VStack>
                                </Table.DataCell>
                            </Table.Row> : null }
                        <Table.Row>
                            <Table.DataCell>
                                <VStack gap="1">
                                    <BodyShort><strong>Sum årlig</strong></BodyShort>
                                    <BodyShort>I dag: <FormatKroner value={sum.yearly.before}/></BodyShort>
                                    <BodyShort>Med dine engringer: <FormatKroner value={sum.yearly.after}/></BodyShort>
                                </VStack>
                            </Table.DataCell>
                        </Table.Row>
                </Table.Body>
            </Table>

        // : null }
    );
};