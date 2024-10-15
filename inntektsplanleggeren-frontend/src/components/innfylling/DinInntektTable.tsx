import { ExternalLinkIcon } from "@navikt/aksel-icons";
import { BodyLong, ExpansionCard, Label, Link, Table } from "@navikt/ds-react";
import "./DinInntektTable.css";
import {InntektDetaljer} from "@/api/model/ApiRequests";
import {Month} from "@/common/MonthEnum";
import {belopSum, numberFormatWithKr} from "@/common/Utils";
import {useEffect, useState} from "react"; // Import the CSS file

interface DinInntektTableProps {
    data: InntektDetaljer[];
    children: React.ReactNode;
}

export const DinInntektTable = ({ data, children }: DinInntektTableProps) => { //todo button to open/close?
    const [width, setWidth] = useState<number>(window.innerWidth);

    const handleWindowSize = () => setWidth(window.innerWidth);

    useEffect(() => {
        window.addEventListener('resize', handleWindowSize);
        return () => window.removeEventListener('resize', handleWindowSize);
    });

    const isDesktop = width > 768;
    const tableTextSize = isDesktop ? "medium" : "small"

    return    <div className="grid gap-6">
            {/*todo style/colors when open or selected?*/}
            <ExpansionCard size="small" aria-label="Small-variant med description" className="expansion-card-gray">
                <ExpansionCard.Header>
                    <ExpansionCard.Description>
                        {children}
                    </ExpansionCard.Description>
                </ExpansionCard.Header>
                <ExpansionCard.Content>
                    { isDesktop ? <Innhold data={data}   /> : <InnholdMobile data={data}/> }
                </ExpansionCard.Content>
            </ExpansionCard>
        </div>
}

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
            <Table.Row key={i} className="table-row">
                <Table.DataCell scope="row">{Month[maned]}</Table.DataCell>
                <Table.DataCell>{ belop > 0 ? numberFormatWithKr(belop) : "Ikke mottatt"} </Table.DataCell>
                <Table.DataCell>{inntektsgivere.join(", ")}</Table.DataCell>
            </Table.Row>
            ))}
            <Table.Row>
                    <Table.HeaderCell scope="row">Sum hittil i år</Table.HeaderCell>
                    <Table.DataCell><b>{numberFormatWithKr(belopSum(props.data))}</b></Table.DataCell>
                    <Table.DataCell></Table.DataCell>
            </Table.Row>
            </Table.Body>
        </Table>
    );
};

const InnholdMobile = (props: { data: InntektDetaljer[] }) => {
    return (
        <Table>
            <Table.Header>
                <Table.Row>
                    <Table.HeaderCell scope="col">Detaljer</Table.HeaderCell>
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {props.data.map(({ maned, belop, inntektsgivere }, i) => (
                    <Table.Row key={i}>
                        <Table.DataCell>
                            <div>
                                <b>{Month[maned]}</b>
                                <div>Beløp per måned: {numberFormatWithKr(belop)}</div>
                                {inntektsgivere && <div>Arbeidsgiver: {inntektsgivere.join(", ")}</div>}
                            </div>
                        </Table.DataCell>
                    </Table.Row>
                ))}
                <Table.Row>
                    <Table.DataCell>
                        <b> Sum hittil i år: {numberFormatWithKr(belopSum(props.data))}</b>
                    </Table.DataCell>
                </Table.Row>
            </Table.Body>
        </Table>
    );
};
