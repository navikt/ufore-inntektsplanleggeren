package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.enhetsregister.EregService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.InntekterHittilIAar
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Inntektsgrunnlag
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InntektsgrunnlagType
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.*
import java.time.temporal.TemporalAdjusters.lastDayOfMonth
import kotlin.test.assertEquals

class InntektServiceTest {

    @Captor
    private lateinit var abonnerteInntekterCaptor: ArgumentCaptor<List<AbonnerteInntekterIdentOgPeriode>>

    @Captor
    private lateinit var filterCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var formalCaptor: ArgumentCaptor<String>

    private val inntektskomponentClient = mock(InntektskomponentClient::class.java)
    private val eregService = mock(EregService::class.java)
    private val inntektService = InntektService(inntektskomponentClient, eregService)

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should return empty inntekterHittilIAar when simuleringsaar not this year`() {
        val inntekterHittilIAar = inntektService.getInntekterHittilIAar(PID, pensjonsdata(), LocalDate.now().year + 1)
        assertEquals(InntekterHittilIAar(emptyList(), emptyList(), emptyList(), emptyList()), inntekterHittilIAar)
    }

    @Test
    fun `should populate arbeidsinntektOgPensjonsgivendeYtelser for bruker with inntekter from A-ordningen based on filter UfoereA-Inntekt and formaal Ufoere when no eps and no barnetillegg`() {
        val year = LocalDate.now().year

        val expectedaAbonnerteInntekterIdentOgPeriodeListe = listOf(
            AbonnerteInntekterIdentOgPeriode(
                ident = Aktoer(PID, "NATURLIG_IDENT"),
                spoerringPeriodeFom = LocalDate.of(year, Month.JANUARY.value, 1).toString(),
                spoerringPeriodeTom = LocalDate.of(year, Month.DECEMBER.value, 1).with(lastDayOfMonth()).toString()
            )
        )

        val expectedFilter = "UfoereA-Inntekt"
        val expectedFormal = "Ufoere"
        val expectedBeloep = 1.0
        val expectedMonth = YearMonth.now()
        val expectedUtbetaltFra = "Organisasjonen AS"

        `when`(
            inntektskomponentClient.hentAbonnerteInntekterBolk(
                expectedaAbonnerteInntekterIdentOgPeriodeListe,
                expectedFilter,
                expectedFormal
            )
        ).thenReturn(
            HentAbonnerteInntekterBolkResponse(
                listOf(
                    AbonnerteInntekterPerIdent(
                        Aktoer(PID, "NATURLIG_IDENT"), listOf(
                            AbonnerteInntekterMaaned(
                                expectedBeloep,
                                null,
                                expectedMonth,
                                listOf(SumOpplysningspliktig(expectedBeloep, null, Aktoer("org", "ORGANISASJON")))
                            )
                        )
                    )
                ),
                null
            )
        )

        `when`(eregService.getOrganisasjonsnavn("org")).thenReturn(expectedUtbetaltFra)

        val inntekterHittilIAar = inntektService.getInntekterHittilIAar(PID, pensjonsdata(), year)

        verify(inntektskomponentClient, times(1)).hentAbonnerteInntekterBolk(
            capture(abonnerteInntekterCaptor),
            capture(filterCaptor),
            capture(formalCaptor)
        )

        assertEquals(expectedaAbonnerteInntekterIdentOgPeriodeListe, abonnerteInntekterCaptor.allValues[0])
        assertEquals(expectedFilter, filterCaptor.allValues[0])
        assertEquals(expectedFormal, formalCaptor.allValues[0])

        assertEquals(1, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser.size)
        assertNull(inntekterHittilIAar.pensjonerFraAndreEnnFolketrygden)
        assertNull(inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelserEps)
        assertNull(inntekterHittilIAar.pensjonerFraAndreEnnFolketrygdenEps)

        assertEquals(expectedBeloep, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser[0].belop)
        assertEquals(expectedMonth.monthValue, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser[0].maned)
        assertEquals(expectedUtbetaltFra, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser[0].utbetaltFra)
    }

    @Test
    fun `should populate arbeidsinntektOgPensjonsgivendeYtelser for bruker and eps with inntekter from A-ordningen based on filter UfoereA-Inntekt and formaal Ufoeretrygdbarnetillegg when eps and barnetillegg fellesbarn`() {
        val year = LocalDate.now().year

        val expectedaAbonnerteInntekterIdentOgPeriodeListe = listOf(
            AbonnerteInntekterIdentOgPeriode(
                ident = Aktoer(PID, "NATURLIG_IDENT"),
                spoerringPeriodeFom = LocalDate.of(year, Month.JANUARY.value, 1).toString(),
                spoerringPeriodeTom = LocalDate.of(year, Month.DECEMBER.value, 1).with(lastDayOfMonth()).toString()
            ),
            AbonnerteInntekterIdentOgPeriode(
                ident = Aktoer(PID_EPS, "NATURLIG_IDENT"),
                spoerringPeriodeFom = LocalDate.of(year, Month.JANUARY.value, 1).toString(),
                spoerringPeriodeTom = LocalDate.of(year, Month.DECEMBER.value, 1).with(lastDayOfMonth()).toString()
            )
        )
        val expectedFilter = "UfoereA-Inntekt"
        val expectedFormal = "Ufoeretrygdbarnetillegg"
        val expectedBeloep = 3927.876
        val expectedMonth = YearMonth.now()
        val expectedUtbetaltFra = "Organisasjonen AS"

        val expectedBeloepEps = 19898.0
        val expectedMonthEps = YearMonth.now().plusMonths(2)
        val expectedUtbetaltFraEps = "Matbutikken AS"

        `when`(
            inntektskomponentClient.hentAbonnerteInntekterBolk(
                any(),
                any(),
                any()
            )
        ).thenReturn(
            HentAbonnerteInntekterBolkResponse(
                listOf(
                    AbonnerteInntekterPerIdent(
                        Aktoer(PID, "NATURLIG_IDENT"), listOf(
                            AbonnerteInntekterMaaned(
                                expectedBeloep,
                                null,
                                expectedMonth,
                                listOf(SumOpplysningspliktig(expectedBeloep, null, Aktoer("org", "ORGANISASJON")))
                            )
                        )
                    ),
                    AbonnerteInntekterPerIdent(
                        Aktoer(PID_EPS, "NATURLIG_IDENT"), listOf(
                            AbonnerteInntekterMaaned(
                                expectedBeloepEps,
                                null,
                                expectedMonthEps,
                                listOf(SumOpplysningspliktig(expectedBeloepEps, null, Aktoer("mat", "ORGANISASJON")))
                            )
                        )
                    )
                ),
                null
            )
        )

        `when`(eregService.getOrganisasjonsnavn("org")).thenReturn(expectedUtbetaltFra)
        `when`(eregService.getOrganisasjonsnavn("mat")).thenReturn(expectedUtbetaltFraEps)

        val inntekterHittilIAar = inntektService.getInntekterHittilIAar(
            PID,
            pensjonsdata(epsPid = PID_EPS, barnetilleggFellesbarn = true),
            year
        )

        verify(inntektskomponentClient, times(2)).hentAbonnerteInntekterBolk(
            capture(abonnerteInntekterCaptor),
            capture(filterCaptor),
            capture(formalCaptor)
        )

        assertEquals(expectedaAbonnerteInntekterIdentOgPeriodeListe, abonnerteInntekterCaptor.allValues[0])
        assertEquals(expectedFilter, filterCaptor.allValues[0])
        assertEquals(expectedFormal, formalCaptor.allValues[0])

        assertEquals(1, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser.size)
        assertEquals(1, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelserEps!!.size)

        assertEquals(expectedBeloep, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser[0].belop)
        assertEquals(expectedMonth.monthValue, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser[0].maned)
        assertEquals(expectedUtbetaltFra, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser[0].utbetaltFra)

        assertEquals(expectedBeloepEps, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelserEps[0].belop)
        assertEquals(
            expectedMonthEps.monthValue,
            inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelserEps[0].maned
        )
        assertEquals(
            expectedUtbetaltFraEps,
            inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelserEps[0].utbetaltFra
        )
    }

    @Test
    fun `should populate pensjonFraAndreEnnFolketrygden with inntekter from A-ordningen based on filter UfoereBarnetilleggA-inntekt when barnetillegg`() {
        val year = LocalDate.now().year

        val expectedaAbonnerteInntekterIdentOgPeriodeListe = listOf(
            AbonnerteInntekterIdentOgPeriode(
                ident = Aktoer(PID, "NATURLIG_IDENT"),
                spoerringPeriodeFom = LocalDate.of(year, Month.JANUARY.value, 1).toString(),
                spoerringPeriodeTom = LocalDate.of(year, Month.DECEMBER.value, 1).with(lastDayOfMonth()).toString()
            )
        )
        val expectedFilter = "UfoereBarnetilleggA-inntekt"
        val expectedFormal = "Ufoeretrygdbarnetillegg"
        val expectedBeloep = 3927.876
        val expectedMonth = YearMonth.now()
        val expectedUtbetaltFra = "Organisasjonen AS"

        `when`(
            inntektskomponentClient.hentAbonnerteInntekterBolk(
                any(),
                any(),
                any()
            )
        ).thenReturn(
            HentAbonnerteInntekterBolkResponse(
                listOf(
                    AbonnerteInntekterPerIdent(
                        Aktoer(PID, "NATURLIG_IDENT"), listOf(
                            AbonnerteInntekterMaaned(
                                expectedBeloep,
                                null,
                                expectedMonth,
                                listOf(SumOpplysningspliktig(expectedBeloep, null, Aktoer("org", "ORGANISASJON")))
                            )
                        )
                    )
                ),
                null
            )
        )

        `when`(eregService.getOrganisasjonsnavn("org")).thenReturn(expectedUtbetaltFra)

        val inntekterHittilIAar = inntektService.getInntekterHittilIAar(
            PID,
            pensjonsdata(epsPid = PID_EPS, barnetilleggSaerkullsbarn = true),
            year
        )

        verify(inntektskomponentClient, times(2)).hentAbonnerteInntekterBolk(
            capture(abonnerteInntekterCaptor),
            capture(filterCaptor),
            capture(formalCaptor)
        )

        assertEquals(expectedaAbonnerteInntekterIdentOgPeriodeListe, abonnerteInntekterCaptor.allValues[1])
        assertEquals(expectedFilter, filterCaptor.allValues[1])
        assertEquals(expectedFormal, formalCaptor.allValues[1])

        assertEquals(1, inntekterHittilIAar.pensjonerFraAndreEnnFolketrygden!!.size)
        assertNull(inntekterHittilIAar.pensjonerFraAndreEnnFolketrygdenEps)

        assertEquals(expectedBeloep, inntekterHittilIAar.pensjonerFraAndreEnnFolketrygden[0].belop)
        assertEquals(expectedMonth.monthValue, inntekterHittilIAar.pensjonerFraAndreEnnFolketrygden[0].maned)
        assertEquals(expectedUtbetaltFra, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser[0].utbetaltFra)
    }

    @Test
    fun `should not include inntekt in inntektHittilIAar when inntekt contains avviksbeskrivelse`() {
        val year = LocalDate.now().year
        val expectedBeloep = 3927.876
        val expectedMonth = YearMonth.now()
        val expectedUtbetaltFra = "Organisasjonen AS"

        `when`(
            inntektskomponentClient.hentAbonnerteInntekterBolk(
                any(),
                any(),
                any()
            )
        ).thenReturn(
            HentAbonnerteInntekterBolkResponse(
                listOf(
                    AbonnerteInntekterPerIdent(
                        Aktoer(PID, "NATURLIG_IDENT"), listOf(
                            AbonnerteInntekterMaaned(
                                expectedBeloep,
                                null,
                                expectedMonth,
                                listOf(
                                    SumOpplysningspliktig(expectedBeloep, null, Aktoer("org", "ORGANISASJON")),
                                    SumOpplysningspliktig(543534.98, "Avvik", Aktoer("org", "ORGANISASJON")),
                                    SumOpplysningspliktig(5094.98, "Avvik", Aktoer("org", "ORGANISASJON"))
                                )
                            )
                        )
                    )
                ),
                null
            )
        )

        `when`(eregService.getOrganisasjonsnavn("org")).thenReturn(expectedUtbetaltFra)

        val inntekterHittilIAar = inntektService.getInntekterHittilIAar(PID, pensjonsdata(), year)

        assertEquals(1, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser.size)

        assertEquals(expectedBeloep, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser[0].belop)
        assertEquals(expectedMonth.monthValue, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser[0].maned)
        assertEquals(expectedUtbetaltFra, inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser[0].utbetaltFra)
    }

    @Test
    fun `should set forventede inntekter to forventede inntekter from krav when open krav exists`() {
        val year = LocalDate.now().year

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                null
            )
        )
        val expectedArbeidsinntektBruker = 1000
        val expectedNaeringsinntektBruker = 1500
        val expectedInntektUtlandBruker = 70
        val expectedPensjonUtlandBruker = 1286
        val expectedAndreYtelserBruker = 7634

        val expectedArbeidsinntektEps = 74965
        val expectedNaeringsinntektEps = 34
        val expectedInntektUtlandEps = 0
        val expectedPensjonUtlandEps = 23675
        val expectedAndreYtelserEps = 0

        val pensjonsdata = pensjonsdata(
            barnetilleggFellesbarn = true,
            inntekterFromOpenKravBruker = listOf(
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.ARBEIDSINNTEKT.code,
                    belop = expectedArbeidsinntektBruker
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.NAERINGSINNTEKT.code,
                    belop = expectedNaeringsinntektBruker
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.INNTEKT_UTLAND.code,
                    belop = expectedInntektUtlandBruker
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.PENSJON_UTLAND.code,
                    belop = expectedPensjonUtlandBruker
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.ANDRE_YTELSER.code,
                    belop = expectedAndreYtelserBruker
                )
            ),
            inntekterFromOpenKravEps = listOf(
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.ARBEIDSINNTEKT.code,
                    belop = expectedArbeidsinntektEps
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.NAERINGSINNTEKT.code,
                    belop = expectedNaeringsinntektEps
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.INNTEKT_UTLAND.code,
                    belop = expectedInntektUtlandEps
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.PENSJON_UTLAND.code,
                    belop = expectedPensjonUtlandEps
                ),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.ANDRE_YTELSER.code, belop = expectedAndreYtelserEps)
            )
        )

        val forventedeInntekter = inntektService.getForventedeInntekter(PID, pensjonsdata, year)

        assertEquals(
            expectedArbeidsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.status
        )
        assertEquals(
            expectedNaeringsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.status
        )
        assertEquals(
            expectedInntektUtlandBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.status
        )
        assertEquals(
            expectedPensjonUtlandBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.pensjonUtland?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.pensjonUtland?.status
        )
        assertEquals(
            expectedAndreYtelserBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.andrePensjonsgivendeYtelser?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.andrePensjonsgivendeYtelser?.status
        )

        assertEquals(
            expectedArbeidsinntektEps,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.arbeidsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.arbeidsinntekt?.status
        )
        assertEquals(
            expectedNaeringsinntektEps,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.naeringsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.naeringsinntekt?.status
        )
        assertEquals(
            expectedInntektUtlandEps,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.inntektUtland?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.inntektUtland?.status
        )
        assertEquals(
            expectedPensjonUtlandEps,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.pensjonUtland?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.pensjonUtland?.status
        )
        assertEquals(
            expectedAndreYtelserEps,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.andrePensjonsgivendeYtelser?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.andrePensjonsgivendeYtelser?.status
        )
    }

    @Test
    fun `should not include eps inntekter from krav when no barnetillegg fellesbarn`() {
        val year = LocalDate.now().year

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                null
            )
        )
        val expectedArbeidsinntektBruker = 1000
        val expectedNaeringsinntektBruker = 1500
        val expectedInntektUtlandBruker = 70
        val expectedPensjonUtlandBruker = 1286
        val expectedAndreYtelserBruker = 7634

        val pensjonsdata = pensjonsdata(
            barnetilleggSaerkullsbarn = true,
            barnetilleggFellesbarn = false,
            inntekterFromOpenKravBruker = listOf(
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.ARBEIDSINNTEKT.code,
                    belop = expectedArbeidsinntektBruker
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.NAERINGSINNTEKT.code,
                    belop = expectedNaeringsinntektBruker
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.INNTEKT_UTLAND.code,
                    belop = expectedInntektUtlandBruker
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.PENSJON_UTLAND.code,
                    belop = expectedPensjonUtlandBruker
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.ANDRE_YTELSER.code,
                    belop = expectedAndreYtelserBruker
                )
            ),
            inntekterFromOpenKravEps = listOf(
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.ARBEIDSINNTEKT.code, belop = 4645),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.NAERINGSINNTEKT.code, belop = 35324),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.INNTEKT_UTLAND.code, belop = 35),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.PENSJON_UTLAND.code, belop = 66),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.ANDRE_YTELSER.code, belop = 324)
            )
        )

        val forventedeInntekter = inntektService.getForventedeInntekter(PID, pensjonsdata, year)

        assertEquals(
            expectedArbeidsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.status
        )
        assertEquals(
            expectedNaeringsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.status
        )
        assertEquals(
            expectedInntektUtlandBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.status
        )
        assertEquals(
            expectedPensjonUtlandBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.pensjonUtland?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.pensjonUtland?.status
        )
        assertEquals(
            expectedAndreYtelserBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.andrePensjonsgivendeYtelser?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.andrePensjonsgivendeYtelser?.status
        )

        assertNull(forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps)
    }

    @Test
    fun `should not include pensjon utland and andre ytelser from krav when no barnetillegg`() {
        val year = LocalDate.now().year

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                null
            )
        )
        val expectedArbeidsinntektBruker = 1000
        val expectedNaeringsinntektBruker = 1500
        val expectedInntektUtlandBruker = 70

        val pensjonsdata = pensjonsdata(
            barnetilleggSaerkullsbarn = false,
            barnetilleggFellesbarn = false,
            inntekterFromOpenKravBruker = listOf(
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.ARBEIDSINNTEKT.code,
                    belop = expectedArbeidsinntektBruker
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.NAERINGSINNTEKT.code,
                    belop = expectedNaeringsinntektBruker
                ),
                inntektsgrunnlag(
                    inntektType = InntektsgrunnlagType.INNTEKT_UTLAND.code,
                    belop = expectedInntektUtlandBruker
                ),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.PENSJON_UTLAND.code, belop = 3453),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.ANDRE_YTELSER.code, belop = 54)
            ),
            inntekterFromOpenKravEps = listOf(
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.ARBEIDSINNTEKT.code, belop = 4645),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.NAERINGSINNTEKT.code, belop = 35324),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.INNTEKT_UTLAND.code, belop = 35),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.PENSJON_UTLAND.code, belop = 66),
                inntektsgrunnlag(inntektType = InntektsgrunnlagType.ANDRE_YTELSER.code, belop = 324)
            )
        )

        val forventedeInntekter = inntektService.getForventedeInntekter(PID, pensjonsdata, year)

        assertEquals(
            expectedArbeidsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.status
        )
        assertEquals(
            expectedNaeringsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.status
        )
        assertEquals(
            expectedInntektUtlandBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.belop
        )
        assertEquals(
            Inntektshendelse.REGISTRERT,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.status
        )

        assertNull(forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.pensjonUtland)
        assertNull(forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.andrePensjonsgivendeYtelser)
        assertNull(forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps)
    }

    @Test
    fun `should set forventede inntekter to forventede inntekter from inntektskomponenten when no open krav exists`() {
        val year = LocalDate.now().year

        val expectedArbeidsinntektBruker = 1000
        val expectedNaeringsinntektBruker = 1500
        val expectedInntektUtlandBruker = 70
        val expectedPensjonUtlandBruker = 1286
        val expectedAndreYtelserBruker = 7634

        val expectedArbeidsinntektEps = 74965
        val expectedNaeringsinntektEps = 34
        val expectedInntektUtlandEps = 0
        val expectedPensjonUtlandEps = 23675
        val expectedAndreYtelserEps = 0

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                "",
                listOf(
                    ForventetInntekt(year.toString(), expectedArbeidsinntektBruker, "UFR_forv_arbeidsinnt", "Benyttet"),
                    ForventetInntekt(
                        year.toString(),
                        expectedNaeringsinntektBruker,
                        "UFR_forv_naeringsinntekt",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedInntektUtlandBruker,
                        "UFR_forv_utenlandsinnt",
                        "Benyttet"
                    ),
                    ForventetInntekt(year.toString(), expectedAndreYtelserBruker, "UFR_forv_andre_ytelser", "Benyttet"),
                    ForventetInntekt(
                        year.toString(),
                        expectedPensjonUtlandBruker,
                        "UFR_forv_pensjon_utland",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedArbeidsinntektEps,
                        "UFR_forv_arbeidsinnt_EPS",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedNaeringsinntektEps,
                        "UFR_forv_naeringsinntekt_EPS",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedInntektUtlandEps,
                        "UFR_forv_utenlandsinnt_EPS",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedAndreYtelserEps,
                        "UFR_forv_andre_ytelser_EPS",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedPensjonUtlandEps,
                        "UFR_forv_pensjon_utland_EPS",
                        "Benyttet"
                    )
                )
            )
        )
        val forventedeInntekter =
            inntektService.getForventedeInntekter(PID, pensjonsdata(barnetilleggFellesbarn = true), year)

        assertEquals(
            expectedArbeidsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.status
        )
        assertEquals(
            expectedNaeringsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.status
        )
        assertEquals(
            expectedInntektUtlandBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.status
        )
        assertEquals(
            expectedPensjonUtlandBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.pensjonUtland?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.pensjonUtland?.status
        )
        assertEquals(
            expectedAndreYtelserBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.andrePensjonsgivendeYtelser?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.andrePensjonsgivendeYtelser?.status
        )

        assertEquals(
            expectedArbeidsinntektEps,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.arbeidsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.arbeidsinntekt?.status
        )
        assertEquals(
            expectedNaeringsinntektEps,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.naeringsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.naeringsinntekt?.status
        )
        assertEquals(
            expectedInntektUtlandEps,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.inntektUtland?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.inntektUtland?.status
        )
        assertEquals(
            expectedPensjonUtlandEps,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.pensjonUtland?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.pensjonUtland?.status
        )
        assertEquals(
            expectedAndreYtelserEps,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.andrePensjonsgivendeYtelser?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps?.andrePensjonsgivendeYtelser?.status
        )
    }

    @Test
    fun `should not return eps forventede inntekter when not barnetillegg fellesbarn`() {
        val year = LocalDate.now().year

        val expectedArbeidsinntektBruker = 1000
        val expectedNaeringsinntektBruker = 1500
        val expectedInntektUtlandBruker = 70
        val expectedPensjonUtlandBruker = 1286
        val expectedAndreYtelserBruker = 7634

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                "",
                listOf(
                    ForventetInntekt(year.toString(), expectedArbeidsinntektBruker, "UFR_forv_arbeidsinnt", "Benyttet"),
                    ForventetInntekt(
                        year.toString(),
                        expectedNaeringsinntektBruker,
                        "UFR_forv_naeringsinntekt",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedInntektUtlandBruker,
                        "UFR_forv_utenlandsinnt",
                        "Benyttet"
                    ),
                    ForventetInntekt(year.toString(), expectedAndreYtelserBruker, "UFR_forv_andre_ytelser", "Benyttet"),
                    ForventetInntekt(
                        year.toString(),
                        expectedPensjonUtlandBruker,
                        "UFR_forv_pensjon_utland",
                        "Benyttet"
                    ),
                    ForventetInntekt(year.toString(), 7775, "UFR_forv_arbeidsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 434, "UFR_forv_naeringsinntekt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 5545, "UFR_forv_utenlandsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 22, "UFR_forv_andre_ytelser_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 46, "UFR_forv_pensjon_utland_EPS", "Benyttet")
                )
            )
        )
        val forventedeInntekter = inntektService.getForventedeInntekter(
            PID,
            pensjonsdata(barnetilleggFellesbarn = false, barnetilleggSaerkullsbarn = true),
            year
        )

        assertEquals(
            expectedArbeidsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.status
        )
        assertEquals(
            expectedNaeringsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.status
        )
        assertEquals(
            expectedInntektUtlandBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.status
        )
        assertEquals(
            expectedPensjonUtlandBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.pensjonUtland?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.pensjonUtland?.status
        )
        assertEquals(
            expectedAndreYtelserBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.andrePensjonsgivendeYtelser?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.andrePensjonsgivendeYtelser?.status
        )
        assertNull(forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps)

    }

    @Test
    fun `should not return inntekt utland and andre ytelser when not barnetillegg`() {
        val year = LocalDate.now().year

        val expectedArbeidsinntektBruker = 1000
        val expectedNaeringsinntektBruker = 1500
        val expectedInntektUtlandBruker = 70

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                "",
                listOf(
                    ForventetInntekt(year.toString(), expectedArbeidsinntektBruker, "UFR_forv_arbeidsinnt", "Benyttet"),
                    ForventetInntekt(
                        year.toString(),
                        expectedNaeringsinntektBruker,
                        "UFR_forv_naeringsinntekt",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedInntektUtlandBruker,
                        "UFR_forv_utenlandsinnt",
                        "Benyttet"
                    ),
                    ForventetInntekt(year.toString(), 8723, "UFR_forv_andre_ytelser", "Benyttet"),
                    ForventetInntekt(year.toString(), 5544, "UFR_forv_pensjon_utland", "Benyttet"),
                    ForventetInntekt(year.toString(), 7775, "UFR_forv_arbeidsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 434, "UFR_forv_naeringsinntekt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 5545, "UFR_forv_utenlandsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 22, "UFR_forv_andre_ytelser_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 46, "UFR_forv_pensjon_utland_EPS", "Benyttet")
                )
            )
        )
        val forventedeInntekter = inntektService.getForventedeInntekter(
            PID,
            pensjonsdata(barnetilleggFellesbarn = false, barnetilleggSaerkullsbarn = false),
            year
        )

        assertEquals(
            expectedArbeidsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.status
        )
        assertEquals(
            expectedNaeringsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.naeringsinntekt?.status
        )
        assertEquals(
            expectedInntektUtlandBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.belop
        )
        assertEquals(
            Inntektshendelse.BENYTTET,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.inntektUtland?.status
        )
        assertNull(forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.pensjonUtland)
        assertNull(forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.andrePensjonsgivendeYtelser)
        assertNull(forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.eps)
    }

    @Test
    fun `should pick most recent inntekt when multiple inntekt of same type`() {
        val year = LocalDate.now().year

        val expectedArbeidsinntektBruker = 1000

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                "",
                listOf(
                    ForventetInntekt(
                        year.toString(),
                        665,
                        "UFR_forv_arbeidsinnt",
                        "Benyttet",
                        "Bruker",
                        LocalDateTime.now().minusMonths(7)
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedArbeidsinntektBruker,
                        "UFR_forv_arbeidsinnt",
                        "Benyttet",
                        "Bruker",
                        LocalDateTime.now()
                    ),
                    ForventetInntekt(
                        year.toString(),
                        564646,
                        "UFR_forv_arbeidsinnt",
                        "Benyttet",
                        "Bruker",
                        LocalDateTime.now().minusMonths(5)
                    )
                )
            )
        )
        val forventedeInntekter = inntektService.getForventedeInntekter(
            PID,
            pensjonsdata(barnetilleggFellesbarn = false, barnetilleggSaerkullsbarn = false),
            year
        )

        assertEquals(
            expectedArbeidsinntektBruker,
            forventedeInntekter.mostRecentForventedeInntekterRegistrertAndBenyttet.bruker.arbeidsinntekt?.belop
        )
    }

    @Test
    fun `should calculate sumBenyttedeInntekterBruker from inntekter in inntektskomponenten with hendelse BENYTTET`() {
        val year = LocalDate.now().year

        val expectedArbeidsinntektBruker = 1000
        val expectedNaeringsinntektBruker = 1500
        val expectedInntektUtlandBruker = 70
        val expectedPensjonUtlandBruker = 1286
        val expectedAndreYtelserBruker = 7634

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                "",
                listOf(
                    ForventetInntekt(year.toString(), expectedArbeidsinntektBruker, "UFR_forv_arbeidsinnt", "Benyttet"),
                    ForventetInntekt(year.toString(), 424254, "UFR_forv_arbeidsinnt", "Registrert"),
                    ForventetInntekt(
                        year.toString(),
                        expectedNaeringsinntektBruker,
                        "UFR_forv_naeringsinntekt",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedInntektUtlandBruker,
                        "UFR_forv_utenlandsinnt",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedAndreYtelserBruker,
                        "UFR_forv_andre_ytelser",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedPensjonUtlandBruker,
                        "UFR_forv_pensjon_utland",
                        "Benyttet"
                    ),
                    ForventetInntekt(year.toString(), 7775, "UFR_forv_arbeidsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 434, "UFR_forv_naeringsinntekt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 5545, "UFR_forv_utenlandsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 22, "UFR_forv_andre_ytelser_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 46, "UFR_forv_pensjon_utland_EPS", "Benyttet")
                )
            )
        )

        val forventedeInntekter = inntektService.getForventedeInntekter(
            PID,
            pensjonsdata(barnetilleggFellesbarn = true),
            year
        )

        assertEquals(
            expectedArbeidsinntektBruker + expectedNaeringsinntektBruker + expectedInntektUtlandBruker + expectedPensjonUtlandBruker + expectedAndreYtelserBruker,
            forventedeInntekter.sumBenyttedeInntekterBruker
        )
    }

    @Test
    fun `should not include andre ytelser and pensjon utland in sumBenyttedeInntekterBruker when no barnetillegg`(){
        val year = LocalDate.now().year

        val expectedArbeidsinntektBruker = 1000
        val expectedNaeringsinntektBruker = 1500
        val expectedInntektUtlandBruker = 70

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                "",
                listOf(
                    ForventetInntekt(year.toString(), expectedArbeidsinntektBruker, "UFR_forv_arbeidsinnt", "Benyttet"),
                    ForventetInntekt(year.toString(), 424254, "UFR_forv_arbeidsinnt", "Registrert"),
                    ForventetInntekt(
                        year.toString(),
                        expectedNaeringsinntektBruker,
                        "UFR_forv_naeringsinntekt",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedInntektUtlandBruker,
                        "UFR_forv_utenlandsinnt",
                        "Benyttet"
                    ),
                    ForventetInntekt(year.toString(), 3454, "UFR_forv_andre_ytelser", "Benyttet"),
                    ForventetInntekt(year.toString(), 4355, "UFR_forv_pensjon_utland", "Benyttet"),
                    ForventetInntekt(year.toString(), 7775, "UFR_forv_arbeidsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 434, "UFR_forv_naeringsinntekt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 5545, "UFR_forv_utenlandsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 22, "UFR_forv_andre_ytelser_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 46, "UFR_forv_pensjon_utland_EPS", "Benyttet")
                )
            )
        )

        val forventedeInntekter = inntektService.getForventedeInntekter(
            PID,
            pensjonsdata(),
            year
        )

        assertEquals(
            expectedArbeidsinntektBruker + expectedNaeringsinntektBruker + expectedInntektUtlandBruker,
            forventedeInntekter.sumBenyttedeInntekterBruker
        )
    }

    @Test
    fun `should calculate sumBenyttedeInntekterEps from inntekter in inntektskomponenten with hendelse BENYTTET when user has EPS`() {
        val year = LocalDate.now().year

        val expectedArbeidsinntektEps = 1000
        val expectedNaeringsinntektEps = 1500
        val expectedInntektUtlandEps = 70
        val expectedPensjonUtlandEps = 1286
        val expectedAndreYtelserEps = 7634

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                "",
                listOf(
                    ForventetInntekt(year.toString(), 234234, "UFR_forv_arbeidsinnt", "Benyttet"),
                    ForventetInntekt(year.toString(), 24234, "UFR_forv_naeringsinntekt", "Benyttet"),
                    ForventetInntekt(year.toString(), 5646, "UFR_forv_utenlandsinnt", "Benyttet"),
                    ForventetInntekt(year.toString(), 8723, "UFR_forv_andre_ytelser", "Benyttet"),
                    ForventetInntekt(year.toString(), 5544, "UFR_forv_pensjon_utland", "Benyttet"),
                    ForventetInntekt(
                        year.toString(),
                        expectedArbeidsinntektEps,
                        "UFR_forv_arbeidsinnt_EPS",
                        "Benyttet"
                    ),
                    ForventetInntekt(year.toString(), 7775, "UFR_forv_arbeidsinnt_EPS", "Registrert"),
                    ForventetInntekt(
                        year.toString(),
                        expectedNaeringsinntektEps,
                        "UFR_forv_naeringsinntekt_EPS",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedInntektUtlandEps,
                        "UFR_forv_utenlandsinnt_EPS",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedPensjonUtlandEps,
                        "UFR_forv_andre_ytelser_EPS",
                        "Benyttet"
                    ),
                    ForventetInntekt(
                        year.toString(),
                        expectedAndreYtelserEps,
                        "UFR_forv_pensjon_utland_EPS",
                        "Benyttet"
                    )
                )
            )
        )

        val forventedeInntekter = inntektService.getForventedeInntekter(
            PID,
            pensjonsdata(barnetilleggFellesbarn = true),
            year
        )

        assertEquals(
            expectedArbeidsinntektEps + expectedNaeringsinntektEps + expectedInntektUtlandEps + expectedPensjonUtlandEps + expectedAndreYtelserEps,
            forventedeInntekter.sumBenyttedeInntekterEps
        )
    }

    @Test
    fun `should not calculate sumBenyttedeInntekterEps when no barnetillegg fellesbarn`(){
        val year = LocalDate.now().year

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                "",
                listOf(
                    ForventetInntekt(year.toString(), 234234, "UFR_forv_arbeidsinnt", "Benyttet"),
                    ForventetInntekt(year.toString(), 24234, "UFR_forv_naeringsinntekt", "Benyttet"),
                    ForventetInntekt(year.toString(), 5646, "UFR_forv_utenlandsinnt", "Benyttet"),
                    ForventetInntekt(year.toString(), 8723, "UFR_forv_andre_ytelser", "Benyttet"),
                    ForventetInntekt(year.toString(), 5544, "UFR_forv_pensjon_utland", "Benyttet"),
                    ForventetInntekt(year.toString(), 234234, "UFR_forv_arbeidsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 7775, "UFR_forv_arbeidsinnt_EPS", "Registrert"),
                    ForventetInntekt(year.toString(), 8723, "UFR_forv_naeringsinntekt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 345, "UFR_forv_utenlandsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 345345, "UFR_forv_andre_ytelser_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 657756, "UFR_forv_pensjon_utland_EPS", "Benyttet")
                )
            )
        )

        val forventedeInntekter = inntektService.getForventedeInntekter(PID, pensjonsdata(barnetilleggFellesbarn = false), year)

        assertNull(forventedeInntekter.sumBenyttedeInntekterEps)
    }

    @Test
    fun `should not calculate sumBenyttedeInntekterEps when user has no EPS`() {
        val year = LocalDate.now().year

        `when`(inntektskomponentClient.hentForventetInntekt(PID, listOf(year))).thenReturn(
            HentForventetInntektResponse(
                "",
                listOf(
                    ForventetInntekt(year.toString(), 234234, "UFR_forv_arbeidsinnt", "Benyttet"),
                    ForventetInntekt(year.toString(), 24234, "UFR_forv_naeringsinntekt", "Benyttet"),
                    ForventetInntekt(year.toString(), 5646, "UFR_forv_utenlandsinnt", "Benyttet"),
                    ForventetInntekt(year.toString(), 8723, "UFR_forv_andre_ytelser", "Benyttet"),
                    ForventetInntekt(year.toString(), 5544, "UFR_forv_pensjon_utland", "Benyttet"),
                    ForventetInntekt(year.toString(), 234234, "UFR_forv_arbeidsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 7775, "UFR_forv_arbeidsinnt_EPS", "Registrert"),
                    ForventetInntekt(year.toString(), 8723, "UFR_forv_naeringsinntekt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 345, "UFR_forv_utenlandsinnt_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 345345, "UFR_forv_andre_ytelser_EPS", "Benyttet"),
                    ForventetInntekt(year.toString(), 657756, "UFR_forv_pensjon_utland_EPS", "Benyttet")
                )
            )
        )

        val forventedeInntekter = inntektService.getForventedeInntekter(PID, pensjonsdata(barnetilleggFellesbarn = true, epsPid = null), year)

        assertNull(forventedeInntekter.sumBenyttedeInntekterEps)
    }

    private fun pensjonsdata(
        inntektsgrense: Int = 300000,
        kompensasjonsgrad: Double = 65.5,
        grenseStoppAvUfoeretrygd: Int = 500000,
        hasLopendeUforeVedtakThisYear: Boolean = false,
        hasLopendeUforeVedtakNextYear: Boolean = false,
        hasVarigTilrettelagtArbeid: Boolean = false,
        hasGjenlevendeTillegg: Boolean = false,
        uforeHeleAaret: Boolean = false,
        barnetilleggFellesbarn: Boolean = false,
        barnetilleggSaerkullsbarn: Boolean = false,
        grenseStoppAvBarnetilleggFellesbarn: Int = 100000,
        grenseStoppAvBarnetilleggSaerkullsbarn: Int = 200000,
        fribelopBarnetilleggSaerkullsbarn: Int = 150000,
        fribelopBarnetilleggFellesbarn: Int = 150000,
        epsPid: String? = PID_EPS,
        inntekterFromOpenKravBruker: List<Inntektsgrunnlag>? = null,
        inntekterFromOpenKravEps: List<Inntektsgrunnlag>? = null
    ): Pensjonsdata =
        Pensjonsdata(
            inntektsgrense = inntektsgrense,
            kompensasjonsgrad = kompensasjonsgrad,
            grenseStoppAvUfoeretrygd = grenseStoppAvUfoeretrygd,
            hasLopendeUforeVedtakThisYear = hasLopendeUforeVedtakThisYear,
            hasLopendeUforeVedtakNextYear = hasLopendeUforeVedtakNextYear,
            hasVarigTilrettelagtArbeid = hasVarigTilrettelagtArbeid,
            hasGjenlevendeTillegg = hasGjenlevendeTillegg,
            uforeHeleAaret = uforeHeleAaret,
            barnetilleggFellesbarn = barnetilleggFellesbarn,
            barnetilleggSaerkullsbarn = barnetilleggSaerkullsbarn,
            grenseStoppAvBarnetilleggFellesbarn = grenseStoppAvBarnetilleggFellesbarn,
            grenseStoppAvBarnetilleggSaerkullsbarn = grenseStoppAvBarnetilleggSaerkullsbarn,
            fribelopBarnetilleggSaerkullsbarn = fribelopBarnetilleggSaerkullsbarn,
            fribelopBarnetilleggFellesbarn = fribelopBarnetilleggFellesbarn,
            epsPid = epsPid,
            inntekterFromOpenKravBruker = inntekterFromOpenKravBruker,
            inntekterFromOpenKravEps = inntekterFromOpenKravEps
        )

    private fun inntektsgrunnlag(
        inntektsgrunnlagId: Long? = null,
        fomDato: OffsetDateTime = OffsetDateTime.now(),
        tomDato: OffsetDateTime? = null,
        endringstidspunkt: OffsetDateTime? = null,
        belop: Int = 0,
        bruk: Boolean = true,
        kopiertFraGammeltKrav: Boolean = false,
        registerOpprettetAv: String = "",
        grunnlagKilde: String = "BRUKER_OPP",
        registerKilde: String = "SELVBETJ",
        inntektType: String? = null,
        inntektHendelseType: String = "BENYTTET",
        grunnIkkeReduksjonType: String? = null,
        persongrunnlagId: Long = 1L,
        changeStamp: String? = null,
        version: Int? = null
    ) =
        Inntektsgrunnlag(
            inntektsgrunnlagId = inntektsgrunnlagId,
            fomDato = fomDato,
            tomDato = tomDato,
            endringstidspunkt = endringstidspunkt,
            belop = belop,
            bruk = bruk,
            kopiertFraGammeltKrav = kopiertFraGammeltKrav,
            registerOpprettetAv = registerOpprettetAv,
            grunnlagKilde = grunnlagKilde,
            registerKilde = registerKilde,
            inntektType = inntektType,
            inntektHendelseType = inntektHendelseType,
            grunnIkkeReduksjonType = grunnIkkeReduksjonType,
            persongrunnlagId = persongrunnlagId,
            changeStamp = changeStamp,
            version = version
        )

    private fun <T> any(): T = Mockito.any()
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

    companion object {
        private const val PID = "00000000001"
        private const val PID_EPS = "00000000002"
    }
}