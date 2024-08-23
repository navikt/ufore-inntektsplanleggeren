import "./IngenEpsView.css";
import "@navikt/ds-css";
import {Button, DatePicker, Heading, TextField, useDatepicker} from "@navikt/ds-react";
import {useContext, useState} from "react";
import {DataContext} from "@/DataContextProvider";
import {opprettSamboerforholdForBruker} from "@/api/apiFetching";
import {PlusCircleIcon} from "@navikt/aksel-icons";
import {isoFormatIgnoreTimezone} from "@/common/timeutils";

function IngenEpsView() {

    const {datepickerProps, inputProps, selectedDay} = useDatepicker({
        fromDate: new Date("Jan 01 1900"),
        toDate: new Date()
    });

    const [showForm, setShowForm] = useState<boolean>(false)

    const [samboerPid, setSamboerPid] = useState<string>("")
    const [loadingSubmit, setLoadingSubmit] = useState<boolean>(false)
    const {setError} = useContext(DataContext)
    // const {success, setSuccess} = useContext(DataContext)

    const [errorSamboerFodselsnummer, setErrorSamboerfodselsnummer] = useState<string>("")
    const [errorDatepicker, setErrorDatepicker] = useState<string>("")

    const {setRefetch} = useContext(DataContext)
    const {setFeilmeldingkode} = useContext(DataContext)

    async function registrerSamboer() {
        setError(false)
        const validFnr = validateFnrField()
        const validDate = validateDatepickerField()

        if (validFnr && validDate) {
            setLoadingSubmit(true)
            try {
                const responseMelding: string = await opprettSamboerforholdForBruker(isoFormatIgnoreTimezone(selectedDay!), samboerPid)
                if (responseMelding == "OK") {
                    setLoadingSubmit(false)
                    // setSuccess(true)
                    setRefetch(true)
                } else {
                    setFeilmeldingkode(responseMelding)
                    // setSuccess(false)
                    setLoadingSubmit(false)
                    setRefetch(true)
                }
            } catch (e) {
                setLoadingSubmit(false)
                // setSuccess(false)
                setError(true)
            }
        }
    }

    function validateFnrField() {
        if (samboerPid.length != 11) {
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            setErrorSamboerfodselsnummer("Feil lengde på fødselsnummer")
            return false
        }
        setErrorSamboerfodselsnummer("")
        return true
    }

    function validateDatepickerField() {
        if (selectedDay == null || undefined) {
            setErrorDatepicker("Startdato for samboerforholdet må oppgis")
            return false
        }
        if (selectedDay?.getTime() > Date.now()) {
            setErrorDatepicker("Startdato for samboerforhold kan ikke være i fremtiden.")
            return false
        }
        setErrorDatepicker("")
        return true
    }

    return (
        <>
            {showForm ?
                <div>
                    <Heading size={"medium"} level={"2"} className="registrer-samboer-header"> Registrer
                        samboer </Heading>

                    <div className="samboer-felt">
                        <TextField id="samboer_fodselsnummer" error={errorSamboerFodselsnummer}
                                   label="Samboers fødselsnummer" onChange={(e) => setSamboerPid(e.target.value)}/>
                    </div>

                    <div className="samboer-felt">
                        <DatePicker {...datepickerProps} dropdownCaption>
                            <DatePicker.Input {...inputProps} error={errorDatepicker}
                                              label="Fra dato (dd.mm.åååå)"/>
                        </DatePicker>
                    </div>
                    <div className="flex flex-wrap gap-2">
                        {loadingSubmit ?
                            <Button loading className="buttons">Loading</Button> :
                            <Button variant="primary" className="buttons"
                                    onClick={() => registrerSamboer()}>Lagre</Button>
                        }
                        <Button variant="secondary" className="buttons"
                                onClick={() => setShowForm(!showForm)}>Avbryt</Button>
                    </div>
                </div> :
                <div className="samboer-felt">
                    <Button icon={<PlusCircleIcon className={'kilde__icon'} aria-hidden="true"/>}
                            variant="tertiary"
                            onClick={() => setShowForm(!showForm)}> Registrer samboer </Button>
                </div>
            }
        </>
    )
}

export default IngenEpsView
