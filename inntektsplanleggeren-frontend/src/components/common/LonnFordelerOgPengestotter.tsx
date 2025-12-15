import { belopSum } from '@/common/Utils'
import { BodyLong, Heading } from '@navikt/ds-react'
import { FormatKroner } from '../utils/FormatKroner'
import { InntektDetaljer } from '@/api/model/ApiRequests'
import { DinInntektTable } from './DinInntektTable'

interface IProps {
    uforeHeleAaret: boolean
    inntekter: InntektDetaljer[]
}

const LonnFordelerOgPengestotter: React.FC<IProps> = (props) => {
    return (
        <DinInntektTable data={props.inntekter} type="arbeidsgiver">
            <Heading size={'xsmall'}>Lønn, fordeler og noen pengestøtter fra Nav</Heading>

            {props.uforeHeleAaret ? (
                <BodyLong>
                    Vi har registrert at du har fått{' '}
                    <strong>
                        <FormatKroner value={belopSum(props.inntekter)} />
                    </strong>{' '}
                    i inntekt hittil i år.
                </BodyLong>
            ) : (
                <BodyLong>
                    Vi har registrert at du har fått inntekt på{' '}
                    <strong>
                        <FormatKroner value={belopSum(props.inntekter)} />
                    </strong>{' '}
                    i perioden du har hatt uføretrygd.
                </BodyLong>
            )}
            <BodyLong>
                Arbeidsgiver har frist for å sende inn opplysninger om din inntekt til Skatteetaten (A-ordningen) innen den 5. måneden etter pengene er
                utbetalt. Helligdager kan forsinke rapporteringen. Derfor kommer inntekten din først med i oversikten måneden etter at du har fått den utbetalt.
            </BodyLong>
        </DinInntektTable>
    )
}

export default LonnFordelerOgPengestotter
