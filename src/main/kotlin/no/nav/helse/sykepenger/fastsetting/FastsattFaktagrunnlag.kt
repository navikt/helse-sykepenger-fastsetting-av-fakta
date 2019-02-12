package no.nav.helse.sykepenger.fastsetting

data class FastsattFaktagrunnlag(val faktagrunnlag: Faktagrunnlag,
                                 val sykepengegrunnlagIArbeidsgiverperioden: Faktum<Sykepengegrunnlag>,
                                 val sykepengegrunnlagNårTrygdenYterSykepenger: Faktum<Sykepengegrunnlag>)
