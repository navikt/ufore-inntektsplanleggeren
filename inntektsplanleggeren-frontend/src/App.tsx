import './App.css'
import "@navikt/ds-css";
import {BodyLong, Heading, Link, Panel} from "@navikt/ds-react";
import IngenEpsView from "@/components/ingenEpsView/IngenEpsView";
import EpsBarnView from "./components/epsBarnView/EpsBarnView";
import {useContext} from "react";
import {DataContext} from "@/DataContextProvider";
import {HarEpsView} from "@/components/eps/HarEpsView";
import {Loading} from "@/components/pageStatus/Loading";
import {Error} from "@/components/pageStatus/Error";
import {LoadingError} from "@/components/pageStatus/LoadingError";
import {Feilmelding} from "@/components/pageStatus/Feilmelding";
import SamboerHistorikk from "@/components/samboerHistorikk/SamboerHistorikk";

export function App() {

    const {
        brukersEps,
        samboerforhold,
        error,
        loading,
        loadingError,
        feilmeldingkode
    } = useContext(DataContext)

    const aktivSamboer = samboerforhold.filter(samboer => samboer.tom === null)[0]

    return (
        <div className="mainBody">
            <div className="contentWrapper">
                <Panel>
                    {loading ? <Loading/> :
                        <>
                            <Heading size={"xlarge"} level={"1"} className="main-header">Familieforhold</Heading>
                            <BodyLong>
                                Her ser du registrert informasjon om den nærmeste familien din. Husk at du har plikt til
                                å melde fra om endret sivilstand.
                                Mer informasjon finner du på <Link
                                href="https://www.skattetaten.no">skattetaten.no</Link>
                            </BodyLong>
                            <hr className="hr-spacing"/>
                            <BodyLong>
                                Hvis du inngår eller avslutter et samboerforhold, vil vi vurdere om det får betydning
                                for eventuelle utbetalinger du og samboeren din får fra oss. Hvis samboeren din ikke har
                                folkeregistrert adresse i Norge, må du kontakte oss for å registrere samboerskapet.
                            </BodyLong>

                            {loadingError ? <LoadingError/> : <>
                                {error ? <Error/> : <></>}
                                {feilmeldingkode ? <Feilmelding/> : <></>}
                                {brukersEps || aktivSamboer !== undefined ? <HarEpsView aktivSamboer={aktivSamboer}/> :
                                    <IngenEpsView/>}
                                <br/>
                                {samboerforhold && samboerforhold.filter(samboer => samboer.tom !== null).length > 0 ?
                                    <SamboerHistorikk samboerforhold={samboerforhold} /> : <></>}
                                <EpsBarnView/>
                            </>}
                        </>
                    }
                </Panel>
            </div>
        </div>
    )
}

export default App
