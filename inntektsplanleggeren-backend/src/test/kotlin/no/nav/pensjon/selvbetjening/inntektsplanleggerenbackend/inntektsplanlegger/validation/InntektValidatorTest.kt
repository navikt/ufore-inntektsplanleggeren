package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.Inntektshendelse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.InntekterHittilIAar
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.Maanedsinntekt
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.Personinntekt
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.PersonInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.time.LocalDate
import kotlin.test.assertTrue

class InntektValidatorTest {

    private val inntektService = mock(InntektService::class.java)
    private val inntektValidator = InntektValidator(inntektService)

    @BeforeEach
    fun setup() {
        `when`(inntektService.getInntekterHittilIAar(any(), any(), anyInt())).thenReturn(
            InntekterHittilIAar(
                arbeidsinntektOgPensjonsgivendeYtelser = listOf(
                    Maanedsinntekt(5, 150000.0, "9092093"),
                    Maanedsinntekt(6, 1200.0, "9092093")
                ),
                arbeidsinntektOgPensjonsgivendeYtelserEps = listOf(
                    Maanedsinntekt(5, 34000.0, "34093"),
                    Maanedsinntekt(6, 18200.0, "34093")
                ),
                pensjonerFraAndreEnnFolketrygden = listOf(
                    Maanedsinntekt(5, 23343.0, "9092093"),
                    Maanedsinntekt(6, 23343.0, "9092093")
                ),
                pensjonerFraAndreEnnFolketrygdenEps = listOf(
                    Maanedsinntekt(5, 54230.0, "34093"),
                    Maanedsinntekt(6, 54230.0, "34093")
                )
            )
        )
        `when`(inntektService.getForventedeInntekter(any(), any(), anyInt())).thenReturn(
            ForventedeInntekterSummary(
                no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                    no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                        Personinntekt(304382, Inntektshendelse.BENYTTET),
                        null,
                        null,
                        null,
                        null
                    ), null
                ), 304382,
                null
            )
        )
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
        val pensjonsdata = pensjonsdata(false, false)

        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(InntektsplanleggerMessageCode.INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG, result[0].messageCode)
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)

    }

    @Test
    fun `should return INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG when pensjonUtland given when no barnetillegg`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = 40032
            ), eps = null
        )
        val pensjonsdata = pensjonsdata(false, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(InntektsplanleggerMessageCode.INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG, result[0].messageCode)
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should not return INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG when pensjonUtland and andrePensjonsgivendeYtelser given when barnetillegg`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 2000,
                pensjonUtland = 1000
            ), eps = null
        )
        val pensjonsdata = pensjonsdata(false, true)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG == it.messageCode })
    }

    @Test
    fun `should return MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG when pensjonUtland missing when barnetillegg`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 49382,
                pensjonUtland = null
            ), eps = null
        )
        val pensjonsdata = pensjonsdata(false, true)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(InntektsplanleggerMessageCode.MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG, result[0].messageCode)
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should return MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG when andrePensjonsgivendeYtelser missing when barnetillegg`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = 3000
            ), eps = null
        )
        val pensjonsdata = pensjonsdata(false, true)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(InntektsplanleggerMessageCode.MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG, result[0].messageCode)
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)

    }

    @Test
    fun `should not return MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG when andrePensjonsgivendeYtelser and pensjonUtland given when barnetillegg`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 49382,
                pensjonUtland = 12
            ), eps = null
        )
        val pensjonsdata = pensjonsdata(false, true)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG == it.messageCode })
    }

    @Test
    fun `should return EPS_INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG_FELLESBARN when eps inntekter given but no barnetillegg fellesbarn`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 49382,
                pensjonUtland = 4532
            ), eps = PersonInntekter(
                arbeidsinntekt = 3000,
                naeringsinntekt = 10230,
                inntektUtland = 5400,
                andrePensjonsgivendeYtelser = 4382,
                pensjonUtland = 2333
            )
        )
        val pensjonsdata = pensjonsdata(false, true)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.EPS_INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG_FELLESBARN,
            result[0].messageCode
        )
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should not return EPS_INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG_FELLESBARN when eps inntekter given and barnetillegg fellesbarn`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 49382,
                pensjonUtland = 4532
            ), eps = PersonInntekter(
                arbeidsinntekt = 3000,
                naeringsinntekt = 10230,
                inntektUtland = 5400,
                andrePensjonsgivendeYtelser = 4382,
                pensjonUtland = 2333
            )
        )
        val pensjonsdata = pensjonsdata(true, true)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.EPS_INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG_FELLESBARN == it.messageCode })
    }

    @Test
    fun `should return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and missing eps arbeidsinntekt`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 49382,
                pensjonUtland = 4532
            ), eps = PersonInntekter(
                arbeidsinntekt = null,
                naeringsinntekt = 10230,
                inntektUtland = 5400,
                andrePensjonsgivendeYtelser = 4382,
                pensjonUtland = 2333
            )
        )
        val pensjonsdata = pensjonsdata(true, true)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN,
            result[0].messageCode
        )
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and missing eps naeringsinntekt`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 49382,
                pensjonUtland = 4532
            ), eps = PersonInntekter(
                arbeidsinntekt = 4342,
                naeringsinntekt = null,
                inntektUtland = 5400,
                andrePensjonsgivendeYtelser = 4382,
                pensjonUtland = 2333
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN,
            result[0].messageCode
        )
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and missing eps pensjonUtland`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 49382,
                pensjonUtland = 4532
            ), eps = PersonInntekter(
                arbeidsinntekt = 3453,
                naeringsinntekt = 10230,
                inntektUtland = 5400,
                andrePensjonsgivendeYtelser = 4382,
                pensjonUtland = null
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN,
            result[0].messageCode
        )
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and missing eps andrePensjonsgivendeYtelser`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 49382,
                pensjonUtland = 4532
            ), eps = PersonInntekter(
                arbeidsinntekt = 34523,
                naeringsinntekt = 10230,
                inntektUtland = 5400,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = 2333
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN,
            result[0].messageCode
        )
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and missing eps inntektUtland`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 49382,
                pensjonUtland = 4532
            ), eps = PersonInntekter(
                arbeidsinntekt = 567575,
                naeringsinntekt = 10230,
                inntektUtland = null,
                andrePensjonsgivendeYtelser = 4382,
                pensjonUtland = 2333
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN,
            result[0].messageCode
        )
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should not return MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN when barnetillegg fellesbarn and all eps inntekter given`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 20000,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = 49382,
                pensjonUtland = 4532
            ), eps = PersonInntekter(
                arbeidsinntekt = 45644,
                naeringsinntekt = 10230,
                inntektUtland = 5400,
                andrePensjonsgivendeYtelser = 4382,
                pensjonUtland = 2333
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { it.messageCode == InntektsplanleggerMessageCode.MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN })
    }

    @Test
    fun `should return FIELD_CAN_NOT_BE_NULL when arbeidsinntekt for bruker is null`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = null,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = null
            ), eps = null
        )
        val pensjonsdata = pensjonsdata()
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.FIELD_CAN_NOT_BE_NULL,
            result[0].messageCode
        )
        assertEquals(
            FieldReference.ARBEIDSINNTEKT_BRUKER.name,
            result[0].metadata[MetadataKey.AFFECTED_FIELD]
        )
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should not return FIELD_CAN_NOT_BE_NULL when arbeidsinntekt for bruker is not null`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 0,
                naeringsinntekt = 10000,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = null
            ), eps = null
        )
        val pensjonsdata = pensjonsdata()
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.FIELD_CAN_NOT_BE_NULL == it.messageCode })
    }

    @Test
    fun `should return FIELD_CAN_NOT_BE_NULL when naeringsinntekt for bruker is null`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 0,
                naeringsinntekt = null,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = null
            ), eps = null
        )
        val pensjonsdata = pensjonsdata()
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.FIELD_CAN_NOT_BE_NULL,
            result[0].messageCode
        )
        assertEquals(
            FieldReference.NAERINGSINNTEKT_BRUKER.name,
            result[0].metadata[MetadataKey.AFFECTED_FIELD]
        )
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should not return FIELD_CAN_NOT_BE_NULL when naeringsinntekt for bruker is not null`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 0,
                naeringsinntekt = 0,
                inntektUtland = 2000,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = null
            ), eps = null
        )
        val pensjonsdata = pensjonsdata()
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.FIELD_CAN_NOT_BE_NULL == it.messageCode })
    }

    @Test
    fun `should return FIELD_CAN_NOT_BE_NULL when inntektUtland for bruker is null`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 0,
                naeringsinntekt = 0,
                inntektUtland = null,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = null
            ), eps = null
        )
        val pensjonsdata = pensjonsdata()
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.FIELD_CAN_NOT_BE_NULL,
            result[0].messageCode
        )
        assertEquals(
            FieldReference.INNTEKT_UTLAND_BRUKER.name,
            result[0].metadata[MetadataKey.AFFECTED_FIELD]
        )
        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should not return FIELD_CAN_NOT_BE_NULL when inntektUtland for bruker is not null`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 0,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = null
            ), eps = null
        )
        val pensjonsdata = pensjonsdata()
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.FIELD_CAN_NOT_BE_NULL == it.messageCode })
    }

    @Test
    fun `should return ILLEGAL_INNTEKT_FIELD_VALUE when fields have negative value`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = -1,
                naeringsinntekt = -1,
                inntektUtland = -1,
                andrePensjonsgivendeYtelser = -1,
                pensjonUtland = -1
            ), eps = PersonInntekter(
                arbeidsinntekt = -1,
                naeringsinntekt = -1,
                inntektUtland = -1,
                andrePensjonsgivendeYtelser = -1,
                pensjonUtland = -1
            )
        )
        val pensjonsdata = pensjonsdata()
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)

        assertEquals(
            10,
            result.filter { InntektsplanleggerMessageCode.ILLEGAL_INNTEKT_FIELD_VALUE == it.messageCode && InntektsplanleggerMessageType.ERROR == it.type }.size
        )

        FieldReference.entries.forEach { fieldReference ->
            run {
                assertTrue(
                    result.any { it.metadata[MetadataKey.AFFECTED_FIELD] == fieldReference.name }
                )
            }
        }

    }

    @Test
    fun `should not return ILLEGAL_INNTEKT_FIELD_VALUE when fields are 0`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 0,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 0,
                pensjonUtland = 0
            ), eps = PersonInntekter(
                arbeidsinntekt = 0,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 0,
                pensjonUtland = 0
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)

        assertTrue(result.none { it.messageCode == InntektsplanleggerMessageCode.ILLEGAL_INNTEKT_FIELD_VALUE })
    }

    @Test
    fun `should return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given bruker arbeidsinntekt is lower than hittil i ar from inntektskomponenten`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 151199,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = null
            ), eps = null
        )
        val pensjonsdata = pensjonsdata()
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR,
            result[0].messageCode
        )
        assertEquals(
            FieldReference.ARBEIDSINNTEKT_BRUKER.name,
            result[0].metadata[MetadataKey.AFFECTED_FIELD]
        )
        assertEquals(
            151200.0,
            result[0].metadata[MetadataKey.SUM_HITTIL_I_AAR]
        )

        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should not return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given bruker arbeidsinntekt is equal to hittil i ar from inntektskomponenten`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 151200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = null
            ), eps = null
        )
        val pensjonsdata = pensjonsdata()
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given eps arbeidsinntekt is lower than hittil i ar from inntektskomponenten`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 151200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 0,
                pensjonUtland = 0
            ), eps = PersonInntekter(
                arbeidsinntekt = 52199,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 0,
                pensjonUtland = 0
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR,
            result[0].messageCode
        )
        assertEquals(
            FieldReference.ARBEIDSINNTEKT_EPS.name,
            result[0].metadata[MetadataKey.AFFECTED_FIELD]
        )

        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should not return ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR when given eps arbeidsinntekt is larger than hittil i ar from inntektskomponenten`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 151200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 100000,
                pensjonUtland = 100000
            ), eps = PersonInntekter(
                arbeidsinntekt = 52200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 200000,
                pensjonUtland = 100000
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR == it.messageCode })
    }

    @Test
    fun `should return ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR when given bruker andrePensjonsgivendeYtelser is lower than hittil i ar from inntektskomponenten`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 151200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 46685,
                pensjonUtland = 0
            ), eps = PersonInntekter(
                arbeidsinntekt = 52200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 300000,
                pensjonUtland = 0
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR,
            result[0].messageCode
        )
        assertEquals(
            46686.0,
            result[0].metadata[MetadataKey.SUM_HITTIL_I_AAR]
        )
        assertEquals(
            FieldReference.ANDRE_YTELSER_BRUKER.name,
            result[0].metadata[MetadataKey.AFFECTED_FIELD]
        )

        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)

    }

    @Test
    fun `should not return ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR when given bruker andrePensjonsgivendeYtelser is larger than hittil i ar from inntektskomponenten`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 151200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 46686,
                pensjonUtland = 0
            ), eps = PersonInntekter(
                arbeidsinntekt = 52200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 300000,
                pensjonUtland = 0
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR == it.messageCode })
    }

    @Test
    fun `should return ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR when given eps andrePensjonsgivendeYtelser is lower than hittil i ar from inntektskomponenten`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 151200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 46686,
                pensjonUtland = 0
            ), eps = PersonInntekter(
                arbeidsinntekt = 52200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 108459,
                pensjonUtland = 0
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR,
            result[0].messageCode
        )
        assertEquals(
            FieldReference.ANDRE_YTELSER_EPS.name,
            result[0].metadata[MetadataKey.AFFECTED_FIELD]
        )

        assertEquals(InntektsplanleggerMessageType.ERROR, result[0].type)
    }

    @Test
    fun `should not return ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR when given eps andrePensjonsgivendeYtelser is larger than hittil i ar from inntektskomponenten`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 151200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 46686,
                pensjonUtland = 0
            ), eps = PersonInntekter(
                arbeidsinntekt = 52200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 108460,
                pensjonUtland = 0
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR == it.messageCode })
    }

    @Test
    fun `should return ONE_OR_MORE_INNTEKT_HAS_STATUS_REGISTRERT when forventet inntekt from inntektkomponent contains inntekt that is REGISTRERT`() {
        `when`(inntektService.getForventedeInntekter(any(), any(), anyInt())).thenReturn(
            ForventedeInntekterSummary(
                no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                    no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                        Personinntekt(304382, Inntektshendelse.REGISTRERT),
                        null,
                        null,
                        null,
                        null
                    ), null
                ), 0,
                null
            )
        )
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 151200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 46686,
                pensjonUtland = 0
            ), eps = PersonInntekter(
                arbeidsinntekt = 52200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 108460,
                pensjonUtland = 0
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertEquals(
            InntektsplanleggerMessageCode.ONE_OR_MORE_INNTEKT_HAS_STATUS_REGISTRERT,
            result[0].messageCode
        )

        assertEquals(InntektsplanleggerMessageType.WARNING, result[0].type)
    }

    @Test
    fun `should not return ONE_OR_MORE_INNTEKT_HAS_STATUS_REGISTRERT when forventet no inntekt from inntektkomponent contains inntekt that is REGISTRERT`() {
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 151200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 46686,
                pensjonUtland = 0
            ), eps = PersonInntekter(
                arbeidsinntekt = 52200,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 108460,
                pensjonUtland = 0
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.ONE_OR_MORE_INNTEKT_HAS_STATUS_REGISTRERT == it.messageCode })

    }

    @Test
    fun `should return EPS_INNTEKT_CHANGED when oppgitt eps inntekt differs from eps inntekt in inntektskomponenten`() {
        `when`(inntektService.getForventedeInntekter(any(), any(), anyInt())).thenReturn(
            ForventedeInntekterSummary(
                no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                    no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET)
                    ), no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET)
                    )
                ), 5,
                5
            )
        )
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 1,
                naeringsinntekt = 1,
                inntektUtland = 1,
                andrePensjonsgivendeYtelser = 1,
                pensjonUtland = 1
            ), eps = PersonInntekter(
                arbeidsinntekt = 2,
                naeringsinntekt = 1,
                inntektUtland = 1,
                andrePensjonsgivendeYtelser = 1,
                pensjonUtland = 1
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.any { InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED == it.messageCode && InntektsplanleggerMessageType.WARNING == it.type })
    }

    @Test
    fun `should not return EPS_INNTEKT_CHANGED when oppgitt eps inntekt equals eps inntekt in inntektskomponenten`() {
        `when`(inntektService.getForventedeInntekter(any(), any(), anyInt())).thenReturn(
            ForventedeInntekterSummary(
                no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                    no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET)
                    ), no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET),
                        Personinntekt(1, Inntektshendelse.BENYTTET)
                    )
                ), 5,
                5
            )
        )
        val oppgitteInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 1,
                naeringsinntekt = 1,
                inntektUtland = 1,
                andrePensjonsgivendeYtelser = 1,
                pensjonUtland = 1
            ), eps = PersonInntekter(
                arbeidsinntekt = 1,
                naeringsinntekt = 1,
                inntektUtland = 1,
                andrePensjonsgivendeYtelser = 1,
                pensjonUtland = 1
            )
        )
        val pensjonsdata = pensjonsdata(true, false)
        val result =
            inntektValidator.validateInntekter(PID, oppgitteInntekter, pensjonsdata, null, LocalDate.now().year)
        assertTrue(result.none { InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED == it.messageCode })
    }

    private fun pensjonsdata(): Pensjonsdata =
        Pensjonsdata(
            inntektsgrense = 300000,
            kompensasjonsgrad = 65.5,
            grenseStoppAvUfoeretrygd = 500000,
            hasLopendeUforeVedtakThisYear = false,
            hasLopendeUforeVedtakNextYear = false,
            hasVarigTilrettelagtArbeid = false,
            hasGjenlevendeTillegg = false,
            uforeHeleAaret = false,
            barnetilleggSaerkullsbarn = false,
            barnetilleggFellesbarn = false,
            epsPid = null,
            inntekterFromOpenKravBruker = null,
            inntekterFromOpenKravEps = null,
            uforeFomDato = null
        )
    private fun pensjonsdata(
        hasBarnetilleggFellesbarn: Boolean,
        hasBarnetilleggSaerkullsbarn: Boolean
    ): Pensjonsdata =
        Pensjonsdata(
            inntektsgrense = 300000,
            kompensasjonsgrad = 65.5,
            grenseStoppAvUfoeretrygd = 500000,
            hasLopendeUforeVedtakThisYear = false,
            hasLopendeUforeVedtakNextYear = false,
            hasVarigTilrettelagtArbeid = false,
            hasGjenlevendeTillegg = false,
            uforeHeleAaret = false,
            barnetilleggSaerkullsbarn = hasBarnetilleggSaerkullsbarn,
            barnetilleggFellesbarn = hasBarnetilleggFellesbarn,
            epsPid = PID_EPS,
            inntekterFromOpenKravBruker = null,
            inntekterFromOpenKravEps = null,
            uforeFomDato = null
        )

    private fun <T> any(): T = Mockito.any()


    companion object {
        private const val PID = "00000000001"
        private const val PID_EPS = "00000000002"
    }
}