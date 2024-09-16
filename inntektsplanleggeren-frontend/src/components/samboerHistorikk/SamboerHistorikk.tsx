import {Alert, Button, Heading, Table} from "@navikt/ds-react";
import {pidFormat, pidFormatAdjustableView} from "@/common/pidutils";
import {formatterNavStandardDato} from "@/common/timeutils";
import SlettSamboerModal, {Props as SlettSamboerModalProps} from "@/components/YearView/SlettSamboerModal";
import EndreSamboerforholdModal, {
    Props as EndreSamboerforholdModalProps
} from "@/components/endreSamboerModal/EndreSamboerModal";
import {useEffect, useState} from "react";
import "./SamboerHistorikk.css"

import {Samboerforhold} from "@/api/model/ApiRequests";

export default function SamboerHistorikk(props: {
    samboerforhold: Samboerforhold[]
}) {

    const [lokalSamboerState, setLokalSamboerState] = useState(props.samboerforhold)
    const [toggleEdit, setToggleEdit] = useState(false)
    const [width, setWidth] = useState<number>(window.innerWidth);

    const handleWindowSize = () => setWidth(window.innerWidth);

    useEffect(() => {
        window.addEventListener('resize', handleWindowSize);
        return () => window.removeEventListener('resize', handleWindowSize);
    });

    const isDesktop = width > 768;

    const tableTextSize = isDesktop ? "medium" : "small"

    function handleRefetchSamboerforhold(nyListe: Samboerforhold[]) {
        setLokalSamboerState(nyListe)
    }

    function getAvsluttetSamboerList() {
        return lokalSamboerState.filter(samboerforhold => samboerforhold.tom !== null)
    }

    return (
        <>
            <div id="samboer-historikk-tittel">
                <Heading size={"medium"} level={"2"}>
                    Samboerhistorikk
                </Heading>
                <Button variant="tertiary" size="small" className="edit-button" onClick={() => setToggleEdit(o => !o)}>
                    {toggleEdit ? "Avslutt redigering" : "Rediger historikk"}
                </Button>
            </div>

            {toggleEdit ?
                <div className="samboer-edit-alert">
                    <Alert variant="warning">
                        Du trenger ikke legge inn perioder hvor du ikke har fått pensjon eller uføretrygd. Du skal bare
                        endre eller slette samboerforhold som du har registrert feil. Endringer eller sletting av
                        samboerforhold kan gi endring i utbetaling av pensjon og uføretrygd.
                    </Alert>
                </div>
                : <></>}

            <Table>
                <Table.Header>
                    <Table.Row>
                        <Table.HeaderCell scope="col" textSize={tableTextSize}>{isDesktop ? "Fødselsnummer" : "Fnr."}</Table.HeaderCell>
                        <Table.HeaderCell scope="col" textSize={tableTextSize}>Fra og med</Table.HeaderCell>
                        <Table.HeaderCell scope="col" textSize={tableTextSize}>Til og med</Table.HeaderCell>
                        {toggleEdit ? <Table.HeaderCell scope="col"></Table.HeaderCell> : <></>}
                        {toggleEdit ? <Table.HeaderCell scope="col"></Table.HeaderCell> : <></>}
                    </Table.Row>
                </Table.Header>
                <Table.Body>
                    {getAvsluttetSamboerList()
                        .map(({pid, fom, tom, periodeId}, i) => {
                            return (
                                <Table.Row key={i + pid}>
                                    <Table.HeaderCell scope="row" textSize={tableTextSize}>{!toggleEdit ? pidFormat(pid) : pidFormatAdjustableView(pid, isDesktop)}</Table.HeaderCell>
                                    <Table.DataCell textSize={tableTextSize}>{formatterNavStandardDato(fom)}</Table.DataCell>
                                    <Table.DataCell textSize={tableTextSize}>{tom ? formatterNavStandardDato(tom) : undefined}</Table.DataCell>
                                    {toggleEdit ? <EditButtonWrapper periodeId={periodeId} fom={fom} tom={tom!}
                                                                     handleRefetchSamboerforhold={handleRefetchSamboerforhold} isDesktop={isDesktop} /> : <></>}
                                    {toggleEdit ? <SlettButtonWrapper periodeId={periodeId}
                                                                      isDesktop={isDesktop}/> : <></>}
                                </Table.Row>
                            );
                        })}
                </Table.Body>
            </Table>
        </>
    )
}

const EditButtonWrapper = (props: EndreSamboerforholdModalProps) => (
    <Table.DataCell className="edit-table-cell">
        <EndreSamboerforholdModal {...props} />
    </Table.DataCell>
)

const SlettButtonWrapper = (props: SlettSamboerModalProps) => (
    <Table.DataCell className="edit-table-cell">
        <SlettSamboerModal {...props}/>
    </Table.DataCell>
)
