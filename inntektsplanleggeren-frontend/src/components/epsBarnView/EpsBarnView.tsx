import "@navikt/ds-css";
import {BodyShort, Heading, Table} from "@navikt/ds-react";
import {useContext} from "react";
import {DataContext} from "@/DataContextProvider";
import "./EpsBarnView.css"
import {pidFormat} from "@/common/pidutils";
import {Navn} from "@/api/model/ApiRequests";

export function EpsBarnView() {

    const {brukersBarn} = useContext(DataContext)

    function nameFormatter(navn: Navn | undefined | null) {
        if (navn == undefined) {
            return ""
        }
        return (navn?.mellomnavn ?
            navn.fornavn + " " + navn.mellomnavn + " " + navn.etternavn :
            navn?.fornavn + " " + navn?.etternavn)
    }

    return (
        <div id="barn-div">
            <div id="barn-tittel">
                <Heading size={"medium"} level={"2"}>Barn under 18 år</Heading>
            </div>
            {brukersBarn.length > 0 ?
                <>
                    <Table>
                        <Table.Header>
                            <Table.Row>
                                <Table.HeaderCell scope="col">Navn</Table.HeaderCell>
                                <Table.HeaderCell scope="col">Fødselsnummer</Table.HeaderCell>
                            </Table.Row>
                        </Table.Header>
                        <Table.Body>
                            {brukersBarn.map(({pid, relasjonPersondata}, i) => {
                                return (
                                    <Table.Row key={i + pid.toString()}>
                                        <Table.HeaderCell
                                            scope="row">{nameFormatter(relasjonPersondata?.navn)}</Table.HeaderCell>
                                        <Table.DataCell>{pidFormat(pid)}</Table.DataCell>
                                    </Table.Row>
                                );
                            })}
                        </Table.Body>
                    </Table>
                </>

                :
                <BodyShort>Ifølge våre registre har du ingen barn under 18 år.</BodyShort>
            }
        </div>
    )
}

export default EpsBarnView
