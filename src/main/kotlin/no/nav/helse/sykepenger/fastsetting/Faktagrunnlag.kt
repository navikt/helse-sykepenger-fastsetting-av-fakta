package no.nav.helse.sykepenger.fastsetting

import java.time.LocalDate

data class Faktagrunnlag(val førsteSykdomsdag: LocalDate, val inntekter: List<Inntekt>)
