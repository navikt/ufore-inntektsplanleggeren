import './App.css'
import "@navikt/ds-css";
import {Heading} from "@navikt/ds-react";
import {Outlet} from "react-router-dom";
import {FormStateComponent} from "@/context/FormData";



export function App() {
    // const {
    //     error,
    //     loading,
    //     loadingError,
    //     feilmeldingkode
    // } = useContext(DataContext)

    // const [state, setState] = useState<string>("initial")

    return (
        <FormStateComponent>
            <main className="mainBody">
                <article className="contentWrapper">
                    <Heading size="xlarge" level="1" className="main-header">Inntektsplanneleggeren</Heading>
                    <Outlet />
                </article>
            </main>
        </FormStateComponent>
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
