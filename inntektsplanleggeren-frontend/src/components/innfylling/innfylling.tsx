import {Button, ErrorSummary, ExpansionCard, Heading, HStack, TextField, VStack} from "@navikt/ds-react";
import React, {useContext, useState} from "react";
import {FormStateContext} from "@/form-container";
import {Link} from "react-router-dom";
import "./innfylling.css"

interface IFormData {
    arbeidsinntekt: string
    navYtelse: string
    naeringsinntekt: string
}

export const Innfylling = () => {
    const { year, setYear } = useContext(FormStateContext);
    // const [formState, setFormState] = useState({
    //     field1: '',
    //     field2: '',
    //     field3: ''
    // });

    const [errors, setErrors] = useState<Partial<Record<keyof IFormData, string>>>({})


    const handleSubmit:  React.FormEventHandler<HTMLFormElement> = (e) =>{
        e.preventDefault()
        const formData: IFormData = {
            arbeidsinntekt: e.currentTarget.arbeidsinntekt.value,
            navYtelse: e.currentTarget.navYtelse.value,
            naeringsinntekt: e.currentTarget.naeringsinntekt.value
        }

        const newErrors = Object.entries(formData).reduce((acc, [key, value]) => {
            if (value === '') {
                return {
                    ...acc,
                    [key]: 'Feltet er påkrevd'
                }
            } else if(isNaN(parseInt(value))) {
                return {
                    ...acc,
                    [key]: 'Maa vaere tall'
                }
            } else if(value < 0) {
                return {
                    ...acc,
                    [key]: 'Må ikke være mindre enn 0'
                }
            }
            return {}
        }, {})

        setErrors(newErrors)

    }

    console.log(errors)
    return (
        <>

            <Heading level="2" size="small">Din forventede intekter i ({year})</Heading>
            {Object.keys(errors).length > 0 && <ErrorSummary heading="Du må rette disse feilene før du kan sende inn søknaden:">
                {Object.entries(errors).map(([key, value]) =>
                    <ErrorSummary.Item href={`#${key}`}><>{value}</></ErrorSummary.Item>
                )}
            </ErrorSummary>}

            <HStack>
                <form onSubmit={handleSubmit}>
                    <VStack gap={"5 5"}>
                        <TextField label="Arbeidsinntekt fra arbeidsgiver" inputMode="numeric"
                                   size="medium" id="arbeidsinntekt"/>
                        <div className="description-card">
                            <ExpansionCard size="small" aria-label="Disse pensjonsgivende ytelsene skal med">
                                <ExpansionCard.Header>
                                    <ExpansionCard.Title>Denne arbeidsinnteken skal med</ExpansionCard.Title>
                                </ExpansionCard.Header>
                                <ExpansionCard.Content>
                                    Description
                                </ExpansionCard.Content>
                            </ExpansionCard>
                            {/*<style>{`*/}
                            {/*.description-card {*/}
                            {/*--ac-expansioncard-bg: var(--a-deepblue-50);*/}
                            {/*--ac-expansioncard-border-open-color: var(--a-border-alt-3);*/}
                            {/*--ac-expansioncard-border-hover-color: var(--a-border-alt-3); */}
                            {/*}`}</style>*/}
                        </div>
                    </VStack>


                    <VStack>
                    <TextField label="Pensjonsgivende ytelser fra oss/NAV" id="navYtelse"
                               description="Uføretrygden skal ikke tas med"/>
                    <ExpansionCard size="small" id="field" aria-label="Disse pensjonsgivende ytelsene skal med">
                        <ExpansionCard.Header>
                            <ExpansionCard.Title>Disse pensjonsgivende ytelsene skal med</ExpansionCard.Title>
                        </ExpansionCard.Header>
                        <ExpansionCard.Content>
                            Description
                        </ExpansionCard.Content>
                    </ExpansionCard>
                </VStack>

                <VStack>
                    <TextField label="Næringsinntekt" id="naeringsinntekt"/>
                    <ExpansionCard size="small" id="field" aria-label="Tekst tekst tekst">
                        <ExpansionCard.Header>
                            <ExpansionCard.Title>Disse pensjonsgivende ytelsene skal med</ExpansionCard.Title>
                        </ExpansionCard.Header>
                        <ExpansionCard.Content>
                            Description
                        </ExpansionCard.Content>
                    </ExpansionCard>
                </VStack>

                <VStack>
                    <TextField label="Inntekt fra utlandet, i norske kroner"/>
                    <ExpansionCard size="small" id="field" aria-label="Tekst tekst tekst">
                        <ExpansionCard.Header>
                            <ExpansionCard.Title>Disse pensjonsgivende ytelseneskal med</ExpansionCard.Title>
                        </ExpansionCard.Header>
                        <ExpansionCard.Content>
                            Description
                        </ExpansionCard.Content>
                    </ExpansionCard>
                </VStack>

                <VStack>
                    <TextField label="Pensjoner og uførepensjon fra andre enn folketrygden"/>
                    <ExpansionCard size="small" id="field" aria-label="Tekst tekst tekst">
                        <ExpansionCard.Header>
                            <ExpansionCard.Title>Disse pensjonsgivende ytelsene skal med</ExpansionCard.Title>
                        </ExpansionCard.Header>
                        <ExpansionCard.Content>
                            Description
                        </ExpansionCard.Content>
                    </ExpansionCard>
                </VStack>

                <VStack>
                    <TextField label="Inntekt fra utlandet, i norske kroner"/>
                    <ExpansionCard size="small" id="field" aria-label="Tekst tekst tekst">
                        <ExpansionCard.Header>
                            <ExpansionCard.Title>Disse pensjonsgivende ytelsene skal med</ExpansionCard.Title>
                        </ExpansionCard.Header>
                        <ExpansionCard.Content>
                            Description
                        </ExpansionCard.Content>
                    </ExpansionCard>
                </VStack>

                    <Button type="submit" variant="secondary">
                        send
                    </Button>

                </form>


            </HStack>

            <Heading level="2" size="small">Din inntekt hittil i år ({year})</Heading>
            <Button onClick={() => setYear((year) => year + 1)}>Increase Year</Button>

            <Button as={Link} to="/" variant="secondary">
                Tilbake
            </Button>
            <Button as={Link} to="/beregning" variant="primary" >
                Beregning
            </Button>
        </>
    );
};