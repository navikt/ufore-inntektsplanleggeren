import './App.css'
import "@navikt/ds-css";
import {Heading} from "@navikt/ds-react";
import {Outlet, useLocation} from "react-router-dom";
import {FormStateComponent} from "@/context/FormData";
import {useEffect} from "react";



export function App() {
    const location = useLocation();

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [location]);

    return (
        <FormStateComponent>
            <main className="mainBody">
                <article className="contentWrapper">
                    <Heading size="xlarge" level="1" className="main-header">Inntektsplan-leggeren</Heading>
                    <Outlet />
                </article>
            </main>
        </FormStateComponent>
    )
}
export default App
