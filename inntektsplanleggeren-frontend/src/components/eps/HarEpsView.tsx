import {useContext, useState} from "react";
import {DataContext} from "@/DataContextProvider";
import "./HarEpsView.css"
import {formatterNavStandardDato} from "@/common/timeutils";
import {pidFormat} from "@/common/pidutils";
import {Navn, Samboerforhold} from "@/api/model/ApiRequests";
import SlettSamboerModal from "@/components/slettSamboerModal/SlettSamboerModal";
import AvsluttSamboerModal from "@/components/avsluttSamboerModal/AvsluttSamboerModal";
import EndreAktivSamboerModal from "@/components/endreAktivSamboerModal/EndreAktivSamboerModal";
import {BodyShort, Button, VStack} from "@navikt/ds-react";

function checkForUndefined(value: string | undefined): string {
    if (value === undefined) return ""
    return value
}

export function HarEpsView(props: {
    aktivSamboer: Samboerforhold
}) {

    const {brukersEps, setRefetch} = useContext(DataContext)

    const [aktivSamboer, setAktivSamboer] = useState<Samboerforhold | undefined>(props.aktivSamboer)
    const [toggleAktivSamboerEdit, setToggleAktivSamboerEdit] = useState(false)

    function handleRefetchSamboerforhold(nyListe: Samboerforhold[]) {
        const aktivSamboerOppdatertListe = nyListe.filter(samboerforhold => samboerforhold.tom === null)
        if (aktivSamboerOppdatertListe.length > 0) {
            setAktivSamboer(aktivSamboerOppdatertListe[0])
        } else {
            setRefetch(true)
        }
    }

    const aktivSamboerFom = aktivSamboer ? new Date(aktivSamboer.fom) : new Date()
    aktivSamboerFom.setDate(aktivSamboerFom.getDate() + 1)

    const brukersnavn: Navn | undefined | null = brukersEps?.relasjonPersondata?.navn

    const brukernavnVisning: string = brukersnavn?.mellomnavn ?
        checkForUndefined(brukersnavn.fornavn) + " " + checkForUndefined(brukersnavn.mellomnavn) + " " + checkForUndefined(brukersnavn.etternavn) :
        checkForUndefined(brukersnavn?.fornavn) + " " + checkForUndefined(brukersnavn?.etternavn)

    return (
        <div>
            <>
                {
                    brukersEps ?
                        <VStack className="eps-navn-pid-container">
                            <BodyShort> <b> {brukersEps.relasjonstype.slice(0, 1) + brukersEps.relasjonstype.toLowerCase().slice(1)} </b> : </BodyShort> {brukernavnVisning}
                            <BodyShort> <b> Fødselsnummer: </b> {pidFormat(brukersEps.pid)} </BodyShort>
                        </VStack>
                        : aktivSamboer !== undefined ?
                            <>
                                <VStack className="eps-navn-pid-container">
                                    <BodyShort> <b> Samboers fødselsnummer: </b> {pidFormat(aktivSamboer.pid)} </BodyShort>
                                    <BodyShort> <b> Fra og med dato: </b> {formatterNavStandardDato(aktivSamboer.fom)} </BodyShort>
                                </VStack>

                                <Button variant="tertiary" size="small" className="edit-button" onClick={() => setToggleAktivSamboerEdit(o => !o)}>
                                    {toggleAktivSamboerEdit ? "Avslutt redigering" : "Rediger samboerforhold"}
                                </Button>


                                { toggleAktivSamboerEdit ?
                                    <>
                                    <AvsluttSamboerModal periodeId={aktivSamboer.periodeId} aktivSamboerFom={aktivSamboer.fom}
                                                         handleRefetchSamboerforhold={handleRefetchSamboerforhold}/>
                                    <EndreAktivSamboerModal periodeId={aktivSamboer.periodeId}
                                                            aktivSamboerFom={aktivSamboer.fom}
                                                            handleRefetchSamboerforhold={handleRefetchSamboerforhold}/>
                                    <SlettSamboerModal periodeId={aktivSamboer.periodeId}
                                                       handleRefetchSamboerforhold={handleRefetchSamboerforhold}
                                                       isDesktop={true}></SlettSamboerModal>
                                </> : <></> }

                            </> : <></>
                }
            </>
        </div>
    )
}
