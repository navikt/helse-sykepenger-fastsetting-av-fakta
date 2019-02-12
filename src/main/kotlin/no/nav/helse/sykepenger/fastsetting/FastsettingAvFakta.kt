package no.nav.helse.sykepenger.fastsetting

import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

fun LocalDate.yearMonth() = YearMonth.of(year, month.value)

data class Inntekt(val periode: LocalDate, val beløp: Long)

fun fastsettFakta(faktagrunnlag: Faktagrunnlag): FastsattFaktagrunnlag {

    val sykepengegrunnlagIArbeidsgiverperioden = fastsettingAvSykepengegrunnlagetIArbeidsgiverperioden(faktagrunnlag.førsteSykdomsdag, faktagrunnlag.inntekter)
    val sykepengegrunnlagNårTrygdenYterSykepenger = fastsettingAvSykepengegrunnlagetNårTrygdenYterSykepenger(faktagrunnlag.førsteSykdomsdag, faktagrunnlag.inntekter)

    return FastsattFaktagrunnlag(faktagrunnlag, sykepengegrunnlagIArbeidsgiverperioden, sykepengegrunnlagNårTrygdenYterSykepenger)
}

fun fastsettingAvAlder(fødselsdato: LocalDate): FastsattFaktum<Int> {
    val diff = Year.now().value - fødselsdato.year
    val age = if (LocalDate.now().withYear(fødselsdato.year) >= fødselsdato) diff else diff - 1
    return FastsattFaktum(age, "Per ${LocalDate.now()} er person født ${fødselsdato} ${age} år gammel")
}

// https://lovdata.no/lov/1997-02-28-19/§8-28
fun fastsettingAvSykepengegrunnlagetIArbeidsgiverperioden(førsteSykdomsdag: LocalDate, inntekter: List<Inntekt>): Faktum<Sykepengegrunnlag> {
    val enMånedFør = førsteSykdomsdag.minusMonths(1)
            .yearMonth()
    val treMånederFør = førsteSykdomsdag.minusMonths(3)
            .yearMonth()

    val beregningsperiode = inntekter.filter { inntekt ->
        inntekt.periode.yearMonth() in treMånederFør..enMånedFør
    }.map {
        FastsattBeregningsperiode(it, "§ 8-28 tredje ledd bokstav a) – De tre siste kalendermånedene før arbeidstakeren ble arbeidsufør (${førsteSykdomsdag}) legges til grunn.")
    }

    // TODO: sjekke om listen inneholder mer enn tre elementer? (hva om det er rapportert inn to inntekter for en måned?)

    return if (beregningsperiode.isEmpty()) {
        UavklartFaktum("Kan ikke avklare sykepengegrunnlaget fordi det ikke er inntekter i beregningsperioden")
    } else if (beregningsperiode.size > 3) {
        UavklartFaktum("Kan ikke avklare sykepengegrunnlaget fordi det er flere enn tre inntekter i beregningsperioden")
    } else {
        // § 8-28 andre ledd
        val aktuellMånedsinntekt = beregningsperiode.sumBy { periode ->
            periode.inntekt.beløp.toInt()
        } / beregningsperiode.size

        FastsattFaktum(Sykepengegrunnlag(aktuellMånedsinntekt, beregningsperiode), "§ 8-28 andre ledd")
    }
}

// § 8-30 første ledd
fun fastsettingAvSykepengegrunnlagetNårTrygdenYterSykepenger(førsteSykdomsdag: LocalDate, inntekter: List<Inntekt>): Faktum<Sykepengegrunnlag> {
    val beregnetAktuellMånedsinntekt = fastsettingAvSykepengegrunnlagetIArbeidsgiverperioden(førsteSykdomsdag, inntekter)
    if (beregnetAktuellMånedsinntekt is UavklartFaktum) {
        return beregnetAktuellMånedsinntekt
    }

    val omregnetÅrsinntekt = (beregnetAktuellMånedsinntekt as FastsattFaktum).faktum.aktuellMånedsinntekt * 12

    return FastsattFaktum(Sykepengegrunnlag(omregnetÅrsinntekt, beregnetAktuellMånedsinntekt.faktum.beregningsperiode), "§ 8-30 første ledd")
}

data class FastsattBeregningsperiode(val inntekt: Inntekt, val begrunnelse: String)

data class Sykepengegrunnlag(val aktuellMånedsinntekt: Int, val beregningsperiode: List<FastsattBeregningsperiode>)
