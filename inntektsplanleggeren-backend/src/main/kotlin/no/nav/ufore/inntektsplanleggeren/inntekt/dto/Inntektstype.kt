package no.nav.ufore.inntektsplanleggeren.inntekt.dto

enum class Inntektstype(val code: String) {
    ARBEIDSINNTEKT_BRUKER("UFR_forv_arbeidsinnt"),
    NAERINGSINNTEKT_BRUKER("UFR_forv_naeringsinntekt"),
    UTENLANDSINNTEKT_BRUKER("UFR_forv_utenlandsinnt"),
    ANDRE_YTELSER_BRUKER("UFR_forv_andre_ytelser"),
    PENSJON_UTLAND_BRUKER("UFR_forv_pensjon_utland"),
    ARBEIDSINNTEKT_EPS("UFR_forv_arbeidsinnt_EPS"),
    NAERINGSINNTEKT_EPS("UFR_forv_naeringsinnt_EPS"),
    UTENLANDSINNTEKT_EPS("UFR_forv_utenlandsinnt_EPS"),
    ANDRE_YTELSER_EPS("UFR_forv_andre_ytelser_EPS"),
    PENSJON_UTLAND_EPS("UFR_forv_pensjon_utland_EPS"),

}