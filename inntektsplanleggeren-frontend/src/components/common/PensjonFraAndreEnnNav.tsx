import { BodyLong, Heading } from '@navikt/ds-react'
import type { InntektDetaljer } from '@/api/model/ApiRequests'
import { belopSum } from '@/common/Utils'
import { FormatKroner } from '../utils/FormatKroner'
import { DinInntektTable } from './DinInntektTable'

interface IProps {
    uforeHeleAaret: boolean
    pensjonFraAndre: InntektDetaljer[]
}

const PensjonFraAndreEnnNav: React.FC<IProps> = (props) => {
    return (
        <DinInntektTable data={props.pensjonFraAndre} type="pensjonsordning">
            <Heading size={'xsmall'}>Pensjoner fra andre enn Nav</Heading>
            <BodyLong>
                Vi har registrert at du har fått{' '}
                <strong>
                    <FormatKroner value={belopSum(props.pensjonFraAndre)} />
                </strong>{' '}
                i pensjoner fra andre enn Nav
                {props.uforeHeleAaret ? ' hittil i år.' : ' i perioden du har hatt uføretrygd.'}{' '}
            </BodyLong>
        </DinInntektTable>
    )
}

export default PensjonFraAndreEnnNav
