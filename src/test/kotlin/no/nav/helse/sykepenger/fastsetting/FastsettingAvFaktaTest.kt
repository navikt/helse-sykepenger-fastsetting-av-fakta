package no.nav.helse.sykepenger.fastsetting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class FastsettingAvFaktaTest {

    @Test
    fun `opptjeningstid 28 dager`() {
        assertEquals(LocalDate.parse("2018-08-31"), LocalDate.parse("2018-09-27").minusDays(28 - 1))
        assertEquals(28, LocalDate.parse("2018-08-31").datesUntil(LocalDate.parse("2018-09-27").plusDays(1)).count())
    }

    @Test
    fun `previous month`() {
        val expected = YearMonth.of(2018, 2)
        val actual = LocalDate.parse("2018-03-30").minusMonths(1).yearMonth()

        assertEquals(expected, actual)
    }

    @Test
    fun `gjennomsnittet av de tre siste kalendermånedene før arbeidsuførhet skal legges til grunn`() {
        val førsteSykdomsdag = LocalDate.parse("2019-01-01")
        val inntekter = listOf(
                Inntekt(LocalDate.parse("2018-12-01"), 1),
                Inntekt(LocalDate.parse("2018-11-01"), 21),
                Inntekt(LocalDate.parse("2018-10-01"), 29),
                Inntekt(LocalDate.parse("2018-09-01"), 30),
                Inntekt(LocalDate.parse("2018-08-01"), 30),
                Inntekt(LocalDate.parse("2018-07-01"), 30),
                Inntekt(LocalDate.parse("2018-06-01"), 30),
                Inntekt(LocalDate.parse("2018-05-01"), 30)
        )
        val fastsattSykepengegrunnlag = fastsettingAvSykepengegrunnlagetIArbeidsgiverperioden(førsteSykdomsdag, inntekter)

        assertEquals(17, (fastsattSykepengegrunnlag as FastsattFaktum).faktum.aktuellMånedsinntekt)
    }

    @Test
    fun `gjennomsnittet av kortere periode skal legges til grunn`() {
        val førsteSykdomsdag = LocalDate.parse("2019-01-01")
        val inntekter = listOf(
                Inntekt(LocalDate.parse("2018-12-01"), 1),
                Inntekt(LocalDate.parse("2018-11-01"), 21)
        )
        val fastsattSykepengegrunnlag = fastsettingAvSykepengegrunnlagetIArbeidsgiverperioden(førsteSykdomsdag, inntekter)

        assertEquals(11, (fastsattSykepengegrunnlag as FastsattFaktum).faktum.aktuellMånedsinntekt)
    }

    @Test
    fun `uavklart sykepengegrunnlag når det ikke er noen inntekter i beregningsperioden`() {
        val førsteSykdomsdag = LocalDate.parse("2019-01-01")
        val inntekter = emptyList<Inntekt>()
        val fastsattSykepengegrunnlag = fastsettingAvSykepengegrunnlagetIArbeidsgiverperioden(førsteSykdomsdag, inntekter)

        assertEquals(UavklartFaktum::class, fastsattSykepengegrunnlag::class)
    }

    @Test
    fun `uavklart sykepengegrunnlag når det er flere enn tre inntekter i beregningsperioden`() {
        val førsteSykdomsdag = LocalDate.parse("2019-01-01")
        val inntekter = listOf(
                Inntekt(LocalDate.parse("2018-12-01"), 1),
                Inntekt(LocalDate.parse("2018-12-01"), 10),
                Inntekt(LocalDate.parse("2018-11-01"), 21),
                Inntekt(LocalDate.parse("2018-11-01"), 31)
        )
        val fastsattSykepengegrunnlag = fastsettingAvSykepengegrunnlagetIArbeidsgiverperioden(førsteSykdomsdag, inntekter)

        assertEquals(UavklartFaktum::class, fastsattSykepengegrunnlag::class)
    }
}
