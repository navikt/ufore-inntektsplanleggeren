package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.dto

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlFolkeregisterMetadata
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlMetadata


abstract class ParallellSannhet(open val pdlMetadata: PdlMetadata?,
                                open val folkeregistermetadata: PdlFolkeregisterMetadata?) {

}
