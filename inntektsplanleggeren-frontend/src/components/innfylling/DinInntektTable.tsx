import { ExternalLinkIcon } from "@navikt/aksel-icons";
import { BodyLong, ExpansionCard, Label, Link, Table } from "@navikt/ds-react";
import "./DinInntektTable.css"; // Import the CSS file

export const DinInntektTable = () => {
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
                    <Innhold />
                </ExpansionCard.Content>
            </ExpansionCard>
        </div>
    );
};

const Innhold = () => {
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
                {data.map(({ name, fnr, start }, i) => (
                    <Table.Row key={i + fnr}>
                        <Table.HeaderCell scope="row">{name}</Table.HeaderCell>
                        <Table.DataCell>{fnr}</Table.DataCell>
                        <Table.DataCell>{start}</Table.DataCell>
                    </Table.Row>
                ))}
            </Table.Body>
        </Table>
    );
};

const format = (date: Date) => {
    const y = date.getFullYear();
    const m = (date.getMonth() + 1).toString().padStart(2, "0");
    const d = date.getDate().toString().padStart(2, "0");
    return `${d}.${m}.${y}`;
};

const data = [
    { name: "Januar", fnr: "21 280 kr", start: "Milano restaurant, NAV " },
    { name: "Februar", fnr: "21 280 kr", start: "Milano restaurant, NAV " },
    { name: "Mars", fnr: "21 280 kr", start: "Milano restaurant" },
    { name: "April", fnr: "21 280 kr", start: "Milano restaurant" },
    { name: "Mai", fnr: "21 280 kr", start: "Milano restaurant" }
];