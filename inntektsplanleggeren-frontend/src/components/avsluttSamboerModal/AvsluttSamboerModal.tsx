import {useContext, useEffect, useState} from "react";
import {DataContext} from "@/DataContextProvider";
import {Button, DatePicker, DateValidationT, HStack, Modal, useDatepicker} from "@navikt/ds-react";
import {PencilIcon} from "@navikt/aksel-icons";
import {endreSamboerforholdForBruker} from "@/api/apiFetching";
import {isoFormatIgnoreTimezone} from "@/common/timeutils";
import {Samboerforhold} from "@/api/model/ApiRequests";

interface Props {
    periodeId: number,
    aktivSamboerFom: string,
    handleRefetchSamboerforhold: (samboerforhold: Samboerforhold[]) => void,
}

export default function AvsluttSamboerModal({periodeId, aktivSamboerFom, handleRefetchSamboerforhold}: Props) {
    const [open, setOpen] = useState(false);
    const [errorDatepickerText, setErrorDatepickerText] = useState<string>("")
    const [loadingSubmit, setLoadingSubmit] = useState<boolean>(false)
    const {setError} = useContext(DataContext)

    const forstTilgjengeligDato = aktivSamboerFom ? new Date(aktivSamboerFom) : new Date()
    forstTilgjengeligDato.setDate(forstTilgjengeligDato.getDate() + 1)

    const {datepickerProps, inputProps, selectedDay, setSelected} = useDatepicker({
        fromDate: forstTilgjengeligDato,
        toDate: new Date(),
        allowTwoDigitYear: false,
        onValidate: (val) => {
            validateDatepickerField(val)
        }
    });

    useEffect(() => {resetToDefaultValuesOnClose()},[open])

    async function submitBreakup( periodeId: number, fom: string) {
        if (!invalidDate) {
            setError(false)
            setLoadingSubmit(true)
            try {
                await endreSamboerforholdForBruker(periodeId, fom, isoFormatIgnoreTimezone(selectedDay!))
            } catch (e) {
                setError(true)
            }
            setLoadingSubmit(false)
            setOpen(false)
        }
    }

    function validateDatepickerField(val: DateValidationT) {
       resetValidationErrors()

        if (!val.isValidDate) {
            if (val.isEmpty) {
                setErrorDatepickerText("Samboerforholdet må ha en til og med dato.")
                // } else if (selectedDay && selectedDay > new Date()) {
                //     setErrorDatepickerText("Dato må ikke være frem i tid") //todo this is not working
            } else {
                setErrorDatepickerText("Ugyldig datoformat. Eksempel på gyldig dato: 'dd.MM.åååå")
            }
        }
    }

    const invalidDate = errorDatepickerText !== ""

    const resetValidationErrors = () => {
        setErrorDatepickerText("")
    }

    const resetToDefaultValuesOnClose = () => {
        if (!open) {
            resetValidationErrors()
            setSelected(undefined)
        }
    }


    return <>
        <HStack gap="6">
            <Button size="xsmall" onClick={() => setOpen(true)} variant="tertiary"
                    icon={<PencilIcon aria-hidden/>}>
                Avslutt samboerforhold
            </Button>
        </HStack>
        <Modal
            open={open}
            onClose={() => setOpen(false)}
            header={{
                heading: "Avslutt samboerperiode",
                size: "small",
                closeButton: false,
            }}
            width="small">
            <Modal.Body>
                <div className="min-h-96" id="datepicker">
                    <DatePicker {...datepickerProps} dropdownCaption={true}>
                        <DatePicker.Input {...inputProps} error={errorDatepickerText} label="Samboerskapet opphørte"/>
                    </DatePicker>
                </div>
            </Modal.Body>
            <Modal.Footer>
                {loadingSubmit ? <Button variant="danger" loading>Loading</Button> :
                    <Button variant="danger" onClick={() => submitBreakup(periodeId, aktivSamboerFom)}>Lagre</Button>}
                <Button type="button" variant="secondary" onClick={() => {
                    setOpen(false)
                    setLoadingSubmit(false)
                }}>Avbryt</Button>
            </Modal.Footer>
        </Modal>
    </>

}