package no.nav.helse.sykepenger.fastsetting

data class FastsattFaktagrunnlag(val faktagrunnlag: Faktagrunnlag,
                                 val sykepengegrunnlagIArbeidsgiverperioden: FastsattSykepengegrunnlag,
                                 val sykepengegrunnlagNårTrygdenYterSykepenger: FastsattSykepengegrunnlag)
