import { BodyLong, ExpansionCard, TextField} from "@navikt/ds-react";
import {useState} from "react";
import {Innfylling} from "@/components/innfylling/innfylling";


export function InntektFormView(props: {
    year: number
}) {

    const [, setInntektFraArbeidsgiver] = useState<string>("")


    return (
        <div>
            <header>Oppgi forventede inntekter</header>
            <BodyLong>
                Slik skal du oppgi inntekten
                <li>du skal kun oppgi inntekt for den perioden av året som du mottar uføretrygd</li>
                <li>før skatt</li>
                <li>det du tror du kommer til å ha tjent når året er slutt / årlig beløp (lett å misforstå som årslønn?) </li>
                <li>alltid i norske kroner</li>
            </BodyLong>

            <header>Dine forventede inntekter i {props.year}</header>

            <div className="samboer-felt">
                <TextField id="field1" label="Arbeidsinntekt fra arbeidsgiver" onChange={(e) => setInntektFraArbeidsgiver(e.target.value)}/>
                <ExpansionCard size="small" aria-label="Small-variant">
                    <ExpansionCard.Header>
                        <ExpansionCard.Title>Denne arbeidsinnteken skal med</ExpansionCard.Title>
                    </ExpansionCard.Header>
                    <ExpansionCard.Content>
                        Legg inn lønn fra arbeidsgiver som et årsbeløp før skatt. Ta med eventuell bonus og overtidsbetaling og feriepenger som blir utbetalt i 2024.
                    </ExpansionCard.Content>
                </ExpansionCard>
            </div>

            <Innfylling/>
        </div>
    )
}
