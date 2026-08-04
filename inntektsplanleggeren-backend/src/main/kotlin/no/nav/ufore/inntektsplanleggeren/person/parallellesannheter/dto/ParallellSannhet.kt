package no.nav.ufore.inntektsplanleggeren.person.parallellesannheter.dto

import no.nav.ufore.inntektsplanleggeren.person.pdl.PdlFolkeregisterMetadata
import no.nav.ufore.inntektsplanleggeren.person.pdl.PdlMetadata


abstract class ParallellSannhet(open val pdlMetadata: PdlMetadata?,
                                open val folkeregistermetadata: PdlFolkeregisterMetadata?) {

}
