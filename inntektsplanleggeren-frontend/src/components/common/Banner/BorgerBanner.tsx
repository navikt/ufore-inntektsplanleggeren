export default function BorgerBanner() {
    return (
        <div>
            <representasjon-banner
                representasjonstyper="UFORETRYGD_LES,UFORETRYGD_SKRIV,VERGE_UFORETRYGD_LES,VERGE_UFORETRYGD_SKRIV"
                redirectTo={`${window.location.origin}/uforetrygd/selvbetjening/inntektsplanleggeren`}
                breadcrumbs={JSON.stringify([
                    { url: "https://www.nav.no/minside", title: "Min side" },
                    { url: "https://www.nav.no/uforetrygd/selvbetjening", title: "Din uføretrygd" },
                    { url: "https://www.nav.no/uforetrygd/selvbetjening/inntektsplanleggeren", title: "Inntektsplanleggeren" },
                ])}
            ></representasjon-banner>
        </div>
    )
}
