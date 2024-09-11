import './App.css'
import "@navikt/ds-css";
import {Heading, Panel,  Button} from "@navikt/ds-react";
import {useContext, useState} from "react";
import {DataContext} from "@/DataContextProvider";
import {Loading} from "@/components/pageStatus/Loading";
import {Error} from "@/components/pageStatus/Error";
import {LoadingError} from "@/components/pageStatus/LoadingError";
import {Feilmelding} from "@/components/pageStatus/Feilmelding";
import {InitialView} from "@/components/initialView/InitialView";
import {YearView} from "@/components/YearView";
import {InntektFormView} from "@/components/inntektFormView/InntektFormView";
import {Outlet} from "react-router-dom";

export function App() {


    // const {
    //     error,
    //     loading,
    //     loadingError,
    //     feilmeldingkode
    // } = useContext(DataContext)

    // const [state, setState] = useState<string>("initial")

    return (
        <div className="mainBody">
            <div className="contentWrapper">
                <Heading size="xlarge" level="1" className="main-header">Inntektsplanneleggeren</Heading>
                <Outlet />
            </div>
        </div>
    )

    // return (
    //     <div className="mainBody">
    //         <div className="contentWrapper">
    //             <Panel>
    //                 {loading ? <Loading/> :
    //                     <>
    //                         <Heading size={"xlarge"} level={"1"} className="main-header">Inntektsplanneleger</Heading>
    //
    //
    //                         {state === "initial" ?
    //                             <>
    //                             <InitialView aktivSamboer={true}/>
    //                             <YearView availableYears={[2004, 2005]} infoType={1}/>
    //                             </>
    //                          : <></> }
    //
    //                         {state === "form" ?
    //                             <>
    //                                 <InntektFormView year={2004}></InntektFormView>
    //                             </>
    //                          : <></> }
    //
    //                         <Button variant="primary" onClick={() => setState("form")}>Start inntektsplanlegger</Button>
    //
    //
    //                         {loadingError ? <LoadingError/> : <>
    //                             {error ? <Error/> : <></>}
    //                             {feilmeldingkode ? <Feilmelding/> : <></>}
    //                         </>}
    //                     </>
    //                 }
    //             </Panel>
    //         </div>
    //     </div>
    // )
}
export default App
