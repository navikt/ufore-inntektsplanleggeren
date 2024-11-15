import { SimulationResult } from "@/api/model/ApiRequests";
import { Table } from "@navikt/ds-react";
import {FormatKroner} from "@/components/utils/FormatKroner";

export const SimulationTable = (props: { simulationResult: SimulationResult }) => {
    const { uforetrygd, forventetInntekt, barnetilleggFellesbarn, barnetilleggSaerkullsbarn, gjenlevendetillegg, sum } = props.simulationResult;


    return (
        <Table>
            <Table.Header>
                <Table.Row>
                    <Table.HeaderCell scope="col"></Table.HeaderCell>
                    <Table.HeaderCell scope="col">I dag (kr)</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Med dine endringer (kr)</Table.HeaderCell>
                </Table.Row>
            </Table.Header>
            <Table.Body>
                <Table.Row>
                    <Table.HeaderCell scope="row">Uføretrygd</Table.HeaderCell>
                    <Table.DataCell><FormatKroner value={uforetrygd.yearly.before}/></Table.DataCell>
                    <Table.DataCell><FormatKroner value={uforetrygd.yearly.after}/></Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Forventet Inntekt</Table.HeaderCell>
                    <Table.DataCell><FormatKroner value={forventetInntekt.yearly.before}/></Table.DataCell>
                    <Table.DataCell><FormatKroner value={forventetInntekt.yearly.after}/></Table.DataCell>
                </Table.Row>
                {barnetilleggSaerkullsbarn || barnetilleggFellesbarn ?
                    <Table.Row>
                        <Table.HeaderCell scope="row">Barnetillegg</Table.HeaderCell>
                        <Table.DataCell><FormatKroner value={(barnetilleggFellesbarn?.yearly.before ?? 0) + (barnetilleggSaerkullsbarn?.yearly.before ?? 0)}/></Table.DataCell>
                        <Table.DataCell><FormatKroner value={(barnetilleggFellesbarn?.yearly.after ?? 0) + (barnetilleggSaerkullsbarn?.yearly.after ?? 0)}/></Table.DataCell>
                    </Table.Row>: null }
                {gjenlevendetillegg ?
                    <Table.Row>
                        <Table.HeaderCell scope="row">Gjenlevendetillegg</Table.HeaderCell>
                        <Table.DataCell><FormatKroner value={gjenlevendetillegg?.yearly.before ?? 0}/></Table.DataCell>
                        <Table.DataCell><FormatKroner value={gjenlevendetillegg?.yearly.after ?? 0}/></Table.DataCell>
                    </Table.Row> : null }
                <Table.Row>
                    <Table.HeaderCell scope="row">Sum</Table.HeaderCell>
                    <Table.DataCell><FormatKroner value={sum.yearly.before}/></Table.DataCell>
                    <Table.DataCell><FormatKroner value={sum.yearly.after}/></Table.DataCell>
                </Table.Row>
            </Table.Body>
        </Table>
    );
};