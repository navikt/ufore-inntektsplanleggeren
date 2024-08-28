import './App.css'
import "@navikt/ds-css";
import {Heading, Panel,  Button} from "@navikt/ds-react";
import {useContext} from "react";
import {DataContext} from "@/DataContextProvider";
import {Loading} from "@/components/pageStatus/Loading";
import {Error} from "@/components/pageStatus/Error";
import {LoadingError} from "@/components/pageStatus/LoadingError";
import {Feilmelding} from "@/components/pageStatus/Feilmelding";
import {InitialView} from "@/components/initialView/InitialView";
import {YearView} from "@/components/YearView";

export function App() {

    const {
        error,
        loading,
        loadingError,
        feilmeldingkode
    } = useContext(DataContext)


    return (
        <div className="mainBody">
            <div className="contentWrapper">
                <Panel>
                    {loading ? <Loading/> :
                        <>
                            <Heading size={"xlarge"} level={"1"} className="main-header">Inntektsplanneleger</Heading>

                            {/*todo: extract this into a textbox*/}

                            <InitialView aktivSamboer={true}/>
                            <YearView availableYears={[2004, 2005]} infoType={1}/>


                            <Button variant="primary">Start inntektsplanlegger</Button>


                            {loadingError ? <LoadingError/> : <>
                                {error ? <Error/> : <></>}
                                {feilmeldingkode ? <Feilmelding/> : <></>}
                            </>}
                        </>
                    }
                </Panel>
            </div>
        </div>
    )
}
export default App
