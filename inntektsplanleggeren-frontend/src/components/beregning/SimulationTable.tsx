import { SimulationResult } from "@/api/model/ApiRequests";
import {BodyShort, Table, VStack} from "@navikt/ds-react";
import {FormatKroner} from "@/components/utils/FormatKroner";
import React, {useEffect, useState} from "react";

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
                            <Table.DataCell scope="row">{gjenlevendetillegg ? "Uføretrygd inkludert gjenlevendetillegg" : "Uføretrygd"}</Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={uforetrygd.yearly.before + (gjenlevendetillegg?.yearly.before ?? 0)}/></Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={uforetrygd.yearly.after + (gjenlevendetillegg?.yearly.after ?? 0)}/></Table.DataCell>
                        </Table.Row>
                        { (barnetilleggSaerkullsbarn || barnetilleggFellesbarn) &&
                            <Table.Row>
                                <Table.DataCell scope="row">Barnetillegg</Table.DataCell>
                                <Table.DataCell align="right"><FormatKroner value={(barnetilleggFellesbarn?.yearly.before ?? 0) + (barnetilleggSaerkullsbarn?.yearly.before ?? 0)}/></Table.DataCell>
                                <Table.DataCell align="right"><FormatKroner value={(barnetilleggFellesbarn?.yearly.after ?? 0) + (barnetilleggSaerkullsbarn?.yearly.after ?? 0)}/></Table.DataCell>
                            </Table.Row> }
                        <Table.Row>
                            <Table.DataCell scope="row">Forventet inntekt</Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={forventetInntekt.yearly.before}/></Table.DataCell>
                            <Table.DataCell align="right"><FormatKroner value={forventetInntekt.yearly.after}/></Table.DataCell>
                        </Table.Row>
                    </Table.Body>
                     <Table.Row style={{ backgroundColor: "var(--a-bg-subtle)" }}>
                         <Table.HeaderCell scope="row">Sum årlig</Table.HeaderCell>
                         <Table.DataCell align="right"><strong><FormatKroner value={sum.yearly.before}/></strong></Table.DataCell>
                         <Table.DataCell align="right"><strong><FormatKroner value={sum.yearly.after}/></strong></Table.DataCell>
                     </Table.Row>
                </Table>
             </VStack>

            : <Table>
                <Table.Body>
                        <Table.Row>
                            <Table.DataCell>
                            <VStack gap="1">
                                <BodyShort><strong>Uføretrygd inkludert gjenlevendetillegg</strong></BodyShort>
                                <BodyShort>I dag: <FormatKroner value={uforetrygd.yearly.before + (gjenlevendetillegg?.yearly.before ?? 0)}/></BodyShort>
                                <BodyShort>Med dine endringer: <FormatKroner value={uforetrygd.yearly.after + (gjenlevendetillegg?.yearly.after ?? 0)}/></BodyShort>
                            </VStack>
                            </Table.DataCell>
                        </Table.Row>
                        <Table.Row>
                            <Table.DataCell>
                            <VStack gap="1">
                                <BodyShort><strong>Forventet Inntekt</strong></BodyShort>
                                <BodyShort>I dag: <FormatKroner value={forventetInntekt.yearly.before}/></BodyShort>
                                <BodyShort>Med dine endringer: <FormatKroner value={forventetInntekt.yearly.after}/></BodyShort>
                            </VStack>
                            </Table.DataCell>
                        </Table.Row>
                        {barnetilleggSaerkullsbarn || barnetilleggFellesbarn ?
                            <Table.Row>
                                <Table.DataCell>
                                    <VStack gap="1">
                                        <BodyShort><strong>Barnetillegg</strong></BodyShort>
                                        <BodyShort>I dag: <FormatKroner value={(barnetilleggFellesbarn?.yearly.before ?? 0) + (barnetilleggSaerkullsbarn?.yearly.before ?? 0)}/></BodyShort>
                                        <BodyShort>Med dine endringer: <FormatKroner value={(barnetilleggFellesbarn?.yearly.after ?? 0) + (barnetilleggSaerkullsbarn?.yearly.after ?? 0)}/></BodyShort>
                                    </VStack>
                                </Table.DataCell>
                            </Table.Row> : null }
                        <Table.Row>
                            <Table.DataCell style={{ backgroundColor: "var(--a-bg-subtle)" }}>
                                <VStack gap="1">
                                    <BodyShort><strong>Sum årlig</strong></BodyShort>
                                    <BodyShort>I dag: <FormatKroner value={sum.yearly.before}/></BodyShort>
                                    <BodyShort>Med dine endringer: <FormatKroner value={sum.yearly.after}/></BodyShort>
                                </VStack>
                            </Table.DataCell>
                        </Table.Row>
                </Table.Body>
            </Table>
    );
};