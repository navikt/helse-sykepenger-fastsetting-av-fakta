package no.nav.helse.sykepenger.fastsetting

sealed class Faktum<T>
data class UavklartFaktum<T>(val begrunnelse: String): Faktum<T>()
data class FastsattFaktum<T>(val faktum: T, val begrunnelse: String): Faktum<T>()
