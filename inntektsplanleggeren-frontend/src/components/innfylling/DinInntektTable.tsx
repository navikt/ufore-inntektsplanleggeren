import { ExternalLinkIcon } from "@navikt/aksel-icons";
import { BodyLong, ExpansionCard, Label, Link, Table } from "@navikt/ds-react";
import "./DinInntektTable.css";
import {InntektDetaljer} from "@/api/model/ApiRequests"; // Import the CSS file

export const DinInntektTable = (props : {data: InntektDetaljer[]}) => {
    return (
        <div className="grid gap-6">
            <ExpansionCard size="small" aria-label="Small-variant med description" className="expansion-card-gray">
                <ExpansionCard.Header>
                    <ExpansionCard.Title>Utbetaling av sykepenger</ExpansionCard.Title>
                    <ExpansionCard.Description>
                        Du er registerert som mottaker av sykepenger fra NAV
                    </ExpansionCard.Description>
                </ExpansionCard.Header>
                <ExpansionCard.Content>
                    <Innhold data={props.data} />
                </ExpansionCard.Content>
            </ExpansionCard>
        </div>
    );
};


const Innhold = (props : {data: InntektDetaljer[]}) => {
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
                        <Table.HeaderCell scope="row">{maned}</Table.HeaderCell>
                        <Table.DataCell>{belop} kr</Table.DataCell>
                        <Table.DataCell>{inntektsgivere.join(", ")}</Table.DataCell>
                    </Table.Row>
                ))}
            </Table.Body>
        </Table>
    );
};