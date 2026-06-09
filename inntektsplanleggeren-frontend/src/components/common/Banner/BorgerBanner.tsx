
export default function BorgerBanner  ()  {
    return (
        <div >
            <representasjon-banner
                representasjonstyper="UFORETRYGD_LES, UFORETRYGD_SKRIV, VERGE_UFORETRYGD_LES, VERGE_UFORETRYGD_SKRIV"
                redirectTo={`${window.location.origin}/uforetrygd/selvbetjening`}
            ></representasjon-banner>
        </div>
    )
}

