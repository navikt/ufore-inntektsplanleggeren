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

    const confirmationMessage = 'You have unsaved changes. Continue?';

    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
        event. = confirmationMessage;
        return confirmationMessage;
    };


    window.addEventListener('beforeunload', handleBeforeUnload);

    return (
        <FormStateComponent>
            <main className="mainBody">
                <representasjon-banner
                    representasjonstyper="PENSJON_FULLSTENDIG,PENSJON_BEGRENSET,UFORETRYGD_SKRIV,UFORETRYGD_KOMMUNISER,UFORETRYGD_LES,PENSJON_SUPERADMIN"
                    redirectTo={`${window.location.origin}/uforetrygd/selvbetjening/inntektsplanleggeren`}>
                </representasjon-banner>

                <article className="contentWrapper">
                    <Heading size="xlarge" level="1" className="main-header">Inntektsplanleggeren</Heading>
                    <Outlet/>
                </article>
            </main>
        </FormStateComponent>
    )
}

export default App
