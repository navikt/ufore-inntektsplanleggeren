import {useContext, useEffect, useState} from "react";
import {Button, DatePicker, HStack, Modal, RangeValidationT, useRangeDatepicker} from "@navikt/ds-react";
import {PencilIcon} from "@navikt/aksel-icons";
import {endreSamboerforholdForBruker} from "@/api/apiFetching";
import {isoFormatIgnoreTimezone} from "@/common/timeutils";
import {Samboerforhold} from "@/api/model/ApiRequests";
import {DataContext} from "@/DataContextProvider";
import "./EndreSamboerModal.css"

export interface Props {
    periodeId: number,
    fom: string,
    tom: string | undefined,
    handleRefetchSamboerforhold: (samboerforhold: Samboerforhold[]) => void,
    isDesktop: boolean
}

export default function EndreSamboerforholdModal({periodeId, fom, tom, isDesktop}: Props) {
    const {setRefetch, setFeilmeldingkode} = useContext(DataContext)
    const [open, setOpen] = useState(false);
    const [loadingSubmit, setLoadingSubmit] = useState<boolean>(false)
    const [errorDatepickerFrom, setErrorDatepickerFrom] = useState<string>("")
    const [errorDatepickerTo, setErrorDatepickerTo] = useState<string>("")
    const [validationError, setValidationError] = useState(false)
    const {datepickerProps, toInputProps, fromInputProps, selectedRange, setSelected} =
        useRangeDatepicker({
            fromDate: new Date("Jan 01 1900"),
            toDate: new Date(),
            defaultSelected: {
                from: new Date(fom),
                to: tom ? new Date(tom!) : undefined
            },
            onValidate: (val) => validateDatePickerFields(val),
            allowTwoDigitYear: false
        });

    useEffect(() => {
        resetToDefaultValuesOnClose()
    },[open])

    const validateDatePickerFields = (val: RangeValidationT) => {
        const fromValidate = val.from
        const toValidate = val.to
        resetValidationErrors()

        if (fromValidate.isInvalid) {
            setValidationError(true)
            if (fromValidate.isEmpty) {
                setErrorDatepickerFrom("Samboerforholdet må ha en fra og med dato.")
            } else {
                setErrorDatepickerFrom("Ugyldig datoformat. Eksempel på gyldig dato: 'dd.MM.åååå")
            }
        }

        if (!toValidate.isEmpty && toValidate.isInvalid) {
            console.log(selectedRange?.to)
            setValidationError(true)
            setErrorDatepickerTo("Ugyldig datoformat. Eksempel på gyldig dato: 'dd.MM.åååå")
        }
    }

    const resetToDefaultValuesOnClose = () => {
        if (!open) {
            resetValidationErrors()
            setSelected({
                from: new Date(fom),
                to: tom ? new Date(tom!) : undefined
            })
        }
    }

    const resetValidationErrors = () => {
        setErrorDatepickerFrom("")
        setErrorDatepickerTo("")
        setValidationError(false)
    }

    function handleBekreft() {
        return async () => {
            if (!validationError) {
                setLoadingSubmit(true)
                const responseMelding: string = await endreSamboerforholdForBruker(periodeId, isoFormatIgnoreTimezone(selectedRange!.from!), selectedRange?.to ? isoFormatIgnoreTimezone(selectedRange.to) : null)
                if (responseMelding === "OK") {
                    if (!tom && selectedRange?.to !== undefined) {
                        setRefetch(true)
                    }
                } else {
                    setFeilmeldingkode(responseMelding)
                    setRefetch(true)
                }
                setOpen(false)
                setLoadingSubmit(false)
            }
        };
    }

    return (
        <>
            <HStack gap="6">
                <Button size="xsmall" onClick={() => setOpen(true)} variant="tertiary"
                        icon={<PencilIcon aria-hidden/>}>
                    {!isDesktop ? "" : "Endre"}
                </Button>
            </HStack>
            <Modal
                open={open}
                onClose={() => setOpen(false)}
                header={{
                    heading: "Rediger samboerperiode",
                    size: "small",
                    closeButton: false,
                }}
                width="small"
            >
                <Modal.Body>
                        <DatePicker {...datepickerProps} dropdownCaption={true}>
                            <div className="flex flex-wrap justify-center gap-4">
                                <DatePicker.Input {...fromInputProps} className="endre-samboer-text-field"
                                                  label="Fra og med dato" error={errorDatepickerFrom}/>
                                <DatePicker.Input {...toInputProps} label="Til og med dato" error={errorDatepickerTo}/>
                            </div>
                        </DatePicker>
                </Modal.Body>
                <Modal.Footer>
                    {loadingSubmit ? <Button variant="danger" loading>Loading</Button> :
                        <Button variant="danger" onClick={handleBekreft()}>Bekreft</Button>}
                    <Button type="button" variant="secondary" onClick={() => {
                        setOpen(false)
                        setLoadingSubmit(false)
                    }}>Avbryt</Button>
                </Modal.Footer>
            </Modal>
        </>
    );
}