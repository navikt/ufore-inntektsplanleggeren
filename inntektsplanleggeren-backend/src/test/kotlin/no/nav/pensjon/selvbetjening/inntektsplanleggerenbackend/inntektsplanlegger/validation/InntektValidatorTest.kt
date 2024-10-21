package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.InntekterHittilIAar
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.PersonInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.time.LocalDate

class InntektValidatorTest {

    private val inntektService = mock(InntektService::class.java)
    private val inntektValidator = InntektValidator(inntektService)

    @BeforeEach
    fun setup(){
        `when`(inntektService.getInntekterHittilIAar(any(), any(), anyInt())).thenReturn(InntekterHittilIAar(
            arbeidsinntektOgPensjonsgivendeYtelser = emptyList(),
            arbeidsinntektOgPensjonsgivendeYtelserEps = emptyList(),
            pensjonerFraAndreEnnFolketrygden = emptyList(),
            pensjonerFraAndreEnnFolketrygdenEps = emptyList()
        ))
        `when`(inntektService.getForventedeInntekter(any(), any(), anyInt())).thenReturn(ForventedeInntekterSummary(Forventede))
    }

    @Test
    fun `should return INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG when andrePensjonsgivendeYtelser given when no barnetillegg`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 30000,
                pensjonUtland = null
            ), eps = null
        )
        val pensjonsdata = Pensjonsdata(
            inntektsgrense = 300000,
            kompensasjonsgrad = 65.5,
            grenseStoppAvUfoeretrygd = 500000,
            hasLopendeUforeVedtakThisYear = false,
            hasLopendeUforeVedtakNextYear = false,
            hasVarigTilrettelagtArbeid = false,
            hasGjenlevendeTillegg = false,
            uforeHeleAaret = false,
            barnetilleggFellesbarn = false,
            barnetilleggSaerkullsbarn = false,
            grenseStoppAvBarnetilleggFellesbarn = 100000,
            grenseStoppAvBarnetilleggSaerkullsbarn = 200000,
            fribelopBarnetilleggSaerkullsbarn = 150000,
            fribelopBarnetilleggFellesbarn = 150000,
            epsPid = null,
            inntekterFromOpenKravBruker = null,
            inntekterFromOpenKravEps = null
        )
        val result = inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
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
    fun `should return ILLEGAL_INNTEKT_FIELD_VALUE when fields have negative value`() {

    }

    @Test
    fun `should not return ILLEGAL_INNTEKT_FIELD_VALUE when fields are 0`() {

    }

    @Test
    fun `should return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given bruker arbeidsinntekt is lower than hittil i ar from inntektskomponenten`() {

    }

    @Test
    fun `should not return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given bruker arbeidsinntekt is larger than hittil i ar from inntektskomponenten`() {

    }

    @Test
    fun `should return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given eps arbeidsinntekt is lower than hittil i ar from inntektskomponenten`() {

    }

    @Test
    fun `should not return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given eps arbeidsinntekt is larger than hittil i ar from inntektskomponenten`() {

    }

    @Test
    fun `should return ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR when given bruker andrePensjonsgivendeYtelser is lower than hittil i ar from inntektskomponenten`() {

    }

    @Test
    fun `should not return ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR when given bruker andrePensjonsgivendeYtelser is larger than hittil i ar from inntektskomponenten`() {

    }

    @Test
    fun `should return ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR when given eps andrePensjonsgivendeYtelser is lower than hittil i ar from inntektskomponenten`() {

    }

    @Test
    fun `should not return ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR when given eps andrePensjonsgivendeYtelser is larger than hittil i ar from inntektskomponenten`() {

    }

    @Test
    fun `should return ONE_OR_MORE_INNTEKT_HAS_STATUS_REGISTRERT when forventet inntekt from inntektkomponent contains inntekt that is REGISTRERT`() {

    }

    @Test
    fun `should not return ONE_OR_MORE_INNTEKT_HAS_STATUS_REGISTRERT when forventet no inntekt from inntektkomponent contains inntekt that is REGISTRERT`() {

    }

    @Test
    fun `should return EPS_INNTEKT_CHANGED when oppgitt eps inntekt differs from eps inntekt in inntektskomponenten`() {

    }

    @Test
    fun `should not return EPS_INNTEKT_CHANGED when oppgitt eps inntekt equals eps inntekt in inntektskomponenten`() {

    }
    private fun <T> any(): T = Mockito.any()


    companion object {
        private const val PID = "00000000001"
    }
}