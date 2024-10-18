package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InntektValidatorTest {

    @Test
    fun `should return INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG when andrePensjonsgivendeYtelser given when no barnetillegg`() {

    }

    @Test
    fun `should return INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG when pensjonUtland given when no barnetillegg`() {

    }

    @Test
    fun `should not return INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG when pensjonUtland and andrePensjonsgivendeYtelser given when barnetillegg`() {

    }

    @Test
    fun `should return MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG when pensjonUtland missing when barnetillegg`() {

    }

    @Test
    fun `should return MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG when andrePensjonsgivendeYtelser missing when barnetillegg`() {

    }

    @Test
    fun `should not return MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG when andrePensjonsgivendeYtelser and pensjonUtland given when barnetillegg`() {

    }

    @Test
    fun `should return EPS_INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG_FELLESBARN when eps inntekter given but no barnetillegg fellesbarn`() {

    }

    @Test
    fun `should not return EPS_INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG_FELLESBARN when eps inntekter given and barnetillegg fellesbarn`() {

    }

    @Test
    fun `should return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and missing eps arbeidsinntekt`() {

    }

    @Test
    fun `should return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and missing eps naeringsinntekt`() {

    }

    @Test
    fun `should return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and missing eps pensjonUtland`() {

    }

    @Test
    fun `should return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and missing eps andrePensjonsgivendeYtelser`() {

    }

    @Test
    fun `should return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and missing eps inntektUtland`() {

    }

    @Test
    fun `should not return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and all eps inntekter given`() {

    }

    @Test
    fun `should return FIELD_CAN_NOT_BE_NULL when arbeidsinntekt for bruker is null`() {

    }

    @Test
    fun `should not return FIELD_CAN_NOT_BE_NULL when arbeidsinntekt for bruker is not null`() {

    }

    @Test
    fun `should return FIELD_CAN_NOT_BE_NULL when naeringsinntekt for bruker is null`() {

    }

    @Test
    fun `should not return FIELD_CAN_NOT_BE_NULL when naeringsinntekt for bruker is not null`() {

    }

    @Test
    fun `should return FIELD_CAN_NOT_BE_NULL when inntektUtland for bruker is null`() {

    }

    @Test
    fun `should not return FIELD_CAN_NOT_BE_NULL when inntektUtland for bruker is not null`() {

    }

    @Test
    fun `should return ILLEGAL_INNTEKT_FIELD_VALUE when fields have negative value`(){

    }

    @Test
    fun `should not return ILLEGAL_INNTEKT_FIELD_VALUE when fields are 0`(){

    }

    @Test
    fun`should return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given bruker arbeidsinntekt is lower than hittil i ar from inntektskomponenten`(){

    }

    @Test
    fun`should not return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given bruker arbeidsinntekt is larger than hittil i ar from inntektskomponenten`(){

    }

    @Test
    fun`should return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given eps arbeidsinntekt is lower than hittil i ar from inntektskomponenten`(){

    }

    @Test
    fun`should not return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given eps arbeidsinntekt is larger than hittil i ar from inntektskomponenten`(){

    }
}