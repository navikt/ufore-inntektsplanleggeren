
export default function BorgerBanner  ()  {
    return (
        <div >
            <representasjon-banner
                representasjonstyper="UFORETRYGD_LES,UFORETRYGD_SKRIV,VERGE_UFORETRYGD_LES,VERGE_UFORETRYGD_SKRIV"
                redirectTo={`${window.location.origin}/uforetrygd/selvbetjening/inntektsplanleggeren`}
                breadcrumbs={JSON.stringify([
                    { url: `${window.location.origin}/minside`, title: "Min side" },
                    { url: `${window.location.origin}/uforetrygd/selvbetjening`, title: "Din uføretrygd" },
                    { url: `${window.location.origin}/uforetrygd/selvbetjening/inntektsplanleggeren`, title: "Inntektsplanleggeren" },
                ])}
            ></representasjon-banner>
        </div>
    )
}

