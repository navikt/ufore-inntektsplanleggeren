import { SimulationResult } from "@/api/model/ApiRequests";
import { Table } from "@navikt/ds-react";
import { numberFormatWithKr } from "@/common/Utils";

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
                    <Table.DataCell>{numberFormatWithKr(uforetrygd.yearly.before)}</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(uforetrygd.yearly.after)}</Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Forventet Inntekt</Table.HeaderCell>
                    <Table.DataCell>{numberFormatWithKr(forventetInntekt.yearly.before)}</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(forventetInntekt.yearly.after)}</Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Barnetillegg Fellesbarn</Table.HeaderCell>
                    <Table.DataCell>{numberFormatWithKr(barnetilleggFellesbarn.yearly.before)}</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(barnetilleggFellesbarn.yearly.after)}</Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Barnetillegg Særkullsbarn</Table.HeaderCell>
                    <Table.DataCell>{numberFormatWithKr(barnetilleggSaerkullsbarn.yearly.before)}</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(barnetilleggSaerkullsbarn.yearly.after)}</Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Gjenlevendetillegg</Table.HeaderCell>
                    <Table.DataCell>{numberFormatWithKr(gjenlevendetillegg.yearly.before)}</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(gjenlevendetillegg.yearly.after)}</Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Sum</Table.HeaderCell>
                    <Table.DataCell>{numberFormatWithKr(sum.yearly.before)}</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(sum.yearly.after)}</Table.DataCell>
                </Table.Row>
            </Table.Body>
        </Table>
    );
};