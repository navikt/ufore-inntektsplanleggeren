import './App.css'
import "@navikt/ds-css";
import {Heading} from "@navikt/ds-react";
import {Outlet, useLocation} from "react-router-dom";
import {FormStateComponent} from "@/context/FormData";
import React, {useEffect} from "react";



export function App() {
    const location = useLocation();

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [location]);

    return (
        <FormStateComponent>
            <main className="mainBody">
                <representasjon-banner
                    representasjonstyper="PENSJON_FULLSTENDIG,PENSJON_BEGRENSET,UFORETYGD_SKRIV,UFORETYGD_KOMMUNISER,UFORETYGD_LES,PENSJON_SUPERADMIN"></representasjon-banner>

                <article className="contentWrapper">
                    <Heading size="xlarge" level="1" className="main-header">Inntektsplanleggeren</Heading>
                    <Outlet/>
                </article>
            </main>
        </FormStateComponent>
    )
}

export default App
