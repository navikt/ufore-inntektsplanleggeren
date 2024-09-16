import {useContext, useEffect, useState} from "react";
import {DataContext} from "@/DataContextProvider";
import {Button, DatePicker, DateValidationT, HStack, Modal, useDatepicker} from "@navikt/ds-react";
import {PencilIcon} from "@navikt/aksel-icons";
import {endreSamboerforholdForBruker, hentSamboerforhold} from "@/api/apiFetching";
import {isoFormatIgnoreTimezone} from "@/common/timeutils";
import {Samboerforhold} from "@/api/model/ApiRequests";

interface Props {
    periodeId: number,
    aktivSamboerFom: string,
    handleRefetchSamboerforhold: (samboerforhold: Samboerforhold[]) => void,
}

export default function EndreAktivSamboerModal({periodeId, aktivSamboerFom, handleRefetchSamboerforhold}: Props) {
    const [open, setOpen] = useState(false);
    const [loadingSubmit, setLoadingSubmit] = useState<boolean>(false)
    const [errorDatepickerText, setErrorDatepickerText] = useState<string>("")
    const [validationError, setValidationError] = useState(false)
    const {setError} = useContext(DataContext)

    const forstTilgjengeligDato = aktivSamboerFom ? new Date(aktivSamboerFom) : new Date()
    forstTilgjengeligDato.setDate(forstTilgjengeligDato.getDate() + 1)

    useEffect(() => {
        resetToDefaultValuesOnClose()
    },[open])

    const {datepickerProps, inputProps, selectedDay, setSelected} = useDatepicker({
        fromDate: new Date("Jan 01 1900"),
        toDate: new Date(),
        onValidate: (val) => {
            validateDatePickerFields(val)
        },
        defaultSelected: new Date(aktivSamboerFom),
        allowTwoDigitYear: false
    });

    const validateDatePickerFields = (val: DateValidationT) => {
        resetValidationErrors()

        if (!val.isValidDate) {
            setValidationError(true)
            if (val.isEmpty) {
                setErrorDatepickerText("Samboerforholdet må ha en fra og med dato.")
            // } else if (selectedDay && selectedDay > new Date()) {
            //     setErrorDatepickerText("Dato må ikke være frem i tid") //todo this is not working
            } else {
                setErrorDatepickerText("Ugyldig datoformat. Eksempel på gyldig dato: 'dd.MM.åååå")
            }
        }
    }

    const resetValidationErrors = () => {
        setErrorDatepickerText("")
        setValidationError(false)
    }

    const resetToDefaultValuesOnClose = () => {
        if (!open) {
            resetValidationErrors()
            setSelected(new Date(aktivSamboerFom))
        }
    }

    async function submitEndreFomDato(periodeId: number) {
        if (!validationError) {
            setError(false)
            setLoadingSubmit(true)
            try {
                await endreSamboerforholdForBruker(periodeId, isoFormatIgnoreTimezone(selectedDay!), null)

            } catch (e) {
                setError(true)
            }
            const samboerforhold = await hentSamboerforhold()
            handleRefetchSamboerforhold(samboerforhold)
            setLoadingSubmit(false)
            setOpen(false)
        }
    }

    return <>
        <HStack gap="6">
            <Button size="xsmall" onClick={() => setOpen(true)} variant="tertiary"
                    icon={<PencilIcon aria-hidden/>}>
                Endre startdato
            </Button>
        </HStack>
        <Modal
            open={open}
            onClose={() => setOpen(false)}
            header={{
                heading: "Endre samboerperiode",
                size: "small",
                closeButton: false,
            }}
            width="small">
            <Modal.Body>
                <div className="min-h-96" id="datepicker">
                    <DatePicker {...datepickerProps} dropdownCaption={true}>
                        <DatePicker.Input {...inputProps} error={errorDatepickerText} label="Fra og med dato"/>
                    </DatePicker>
                </div>
            </Modal.Body>
            <Modal.Footer>
                {loadingSubmit ? <Button variant="danger" loading>Loading</Button> :
                    <Button variant="danger" onClick={() => submitEndreFomDato(periodeId)}>Lagre</Button>}
                <Button type="button" variant="secondary" onClick={() => {
                    setOpen(false)
                    setLoadingSubmit(false)
                }}>Avbryt</Button>
            </Modal.Footer>
        </Modal>
    </>

}