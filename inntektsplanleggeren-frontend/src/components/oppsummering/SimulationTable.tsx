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
                    <Table.DataCell>{numberFormatWithKr(uforetrygd.before)} kr</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(uforetrygd.after)} kr</Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Forventet Inntekt</Table.HeaderCell>
                    <Table.DataCell>{numberFormatWithKr(forventetInntekt.before)} kr</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(forventetInntekt.after)} kr</Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Barnetillegg Fellesbarn</Table.HeaderCell>
                    <Table.DataCell>{numberFormatWithKr(barnetilleggFellesbarn.before)} kr</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(barnetilleggFellesbarn.after)} kr</Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Barnetillegg Særkullsbarn</Table.HeaderCell>
                    <Table.DataCell>{numberFormatWithKr(barnetilleggSaerkullsbarn.before)} kr</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(barnetilleggSaerkullsbarn.after)} kr</Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Gjenlevendetillegg</Table.HeaderCell>
                    <Table.DataCell>{numberFormatWithKr(gjenlevendetillegg.before)} kr</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(gjenlevendetillegg.after)} kr</Table.DataCell>
                </Table.Row>
                <Table.Row>
                    <Table.HeaderCell scope="row">Sum</Table.HeaderCell>
                    <Table.DataCell>{numberFormatWithKr(sum.before)} kr</Table.DataCell>
                    <Table.DataCell>{numberFormatWithKr(sum.after)} kr</Table.DataCell>
                </Table.Row>
            </Table.Body>
        </Table>
    );
};