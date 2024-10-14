import { ExternalLinkIcon } from "@navikt/aksel-icons";
import { BodyLong, ExpansionCard, Label, Link, Table } from "@navikt/ds-react";
import "./DinInntektTable.css";
import {InntektDetaljer} from "@/api/model/ApiRequests";
import {Month} from "@/common/MonthEnum";
import {belopSum, numberFormat} from "@/common/Utils"; // Import the CSS file

interface DinInntektTableProps {
    data: InntektDetaljer[];
    children: React.ReactNode;
}

export const DinInntektTable = ({ data, children }: DinInntektTableProps) => (
    <div className="grid gap-6">
        {/*todo style/colors when open or selected?*/}
        <ExpansionCard size="small" aria-label="Small-variant med description" className="expansion-card-gray">
            <ExpansionCard.Header>
                <ExpansionCard.Description>
                    {children}
                </ExpansionCard.Description>
            </ExpansionCard.Header>
            <ExpansionCard.Content>
                <Innhold data={data} />
            </ExpansionCard.Content>
        </ExpansionCard>
    </div>
);

const Innhold = (props: { data: InntektDetaljer[] }) => {
    return (
        <Table>
            <Table.Header>
                <Table.Row>
                    <Table.HeaderCell scope="col">Måned</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Beløp per måned</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Arbeidsgiver</Table.HeaderCell>
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {props.data.map(({ maned, belop, inntektsgivere }, i) => (
                    <Table.Row key={i}>
                        <Table.HeaderCell scope="row">{Month[maned]}</Table.HeaderCell>
                        <Table.DataCell>{numberFormat(belop)} kr</Table.DataCell>
                        <Table.DataCell>{inntektsgivere.join(", ")}</Table.DataCell>
                    </Table.Row>
                ))}
                <Table.Row>
                    <Table.DataCell colSpan={4}>
                        <Table.HeaderCell scope="row">Sum hittil i år</Table.HeaderCell>
                        <Table.DataCell>{numberFormat(belopSum(props.data))} kr</Table.DataCell>
                    </Table.DataCell>
                </Table.Row>
            </Table.Body>
        </Table>
    );
};
