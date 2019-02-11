package no.nav.helse.sykepenger.fastsetting

import java.time.LocalDate
import java.time.YearMonth

fun LocalDate.yearMonth() = YearMonth.of(year, month.value)

data class Inntekt(val periode: LocalDate, val beløp: Long)

fun fastsettFakta(faktagrunnlag: Faktagrunnlag): FastsattFaktagrunnlag {

    val sykepengegrunnlagIArbeidsgiverperioden = fastsettingAvSykepengegrunnlagetIArbeidsgiverperioden(faktagrunnlag.førsteSykdomsdag, faktagrunnlag.inntekter)
    val sykepengegrunnlagNårTrygdenYterSykepenger = fastsettingAvSykepengegrunnlagetNårTrygdenYterSykepenger(faktagrunnlag.førsteSykdomsdag, faktagrunnlag.inntekter)

    return FastsattFaktagrunnlag(faktagrunnlag, sykepengegrunnlagIArbeidsgiverperioden, sykepengegrunnlagNårTrygdenYterSykepenger)
}

// https://lovdata.no/lov/1997-02-28-19/§8-28
fun fastsettingAvSykepengegrunnlagetIArbeidsgiverperioden(førsteSykdomsdag: LocalDate, inntekter: List<Inntekt>): FastsattSykepengegrunnlag {
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

    // § 8-28 andre ledd
    val aktuellMånedsinntekt = beregningsperiode.sumBy { periode ->
        periode.inntekt.beløp.toInt()
    } / beregningsperiode.size

    return FastsattSykepengegrunnlag(aktuellMånedsinntekt, beregningsperiode, "§ 8-28 andre ledd")
}

// § 8-30 første ledd
fun fastsettingAvSykepengegrunnlagetNårTrygdenYterSykepenger(førsteSykdomsdag: LocalDate, inntekter: List<Inntekt>): FastsattSykepengegrunnlag {
    val beregnetAktuellMånedsinntekt = fastsettingAvSykepengegrunnlagetIArbeidsgiverperioden(førsteSykdomsdag, inntekter)
    val omregnetÅrsinntekt = beregnetAktuellMånedsinntekt.aktuellMånedsinntekt * 12

    return FastsattSykepengegrunnlag(omregnetÅrsinntekt, beregnetAktuellMånedsinntekt.beregningsperiode, "§ 8-30 første ledd")
}

data class FastsattBeregningsperiode(val inntekt: Inntekt, val begrunnelse: String)
data class FastsattSykepengegrunnlag(val aktuellMånedsinntekt: Int, val beregningsperiode: List<FastsattBeregningsperiode>, val begrunnelse: String)
