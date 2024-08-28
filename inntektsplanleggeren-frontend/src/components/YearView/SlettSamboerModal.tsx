import React, {useState} from "react";
import {BodyLong, Button, HStack, Modal} from "@navikt/ds-react";
import {TrashIcon} from "@navikt/aksel-icons";
import {hentSamboerforhold, sletteSamboerforholdForBruker} from "@/api/apiFetching";
import {Samboerforhold} from "@/api/model/ApiRequests";

export interface Props {
    periodeId: number,
    handleRefetchSamboerforhold: (samboerforhold: Samboerforhold[]) => void,
    isDesktop: boolean
}

export default function SlettSamboerModal({periodeId, handleRefetchSamboerforhold, isDesktop}: Props) {
    const [open, setOpen] = useState(false)
    const [loadingSubmit, setLoadingSubmit] = useState<boolean>(false)


    async function slettSamboer(periodeId: number) {
        setLoadingSubmit(true)
        await sletteSamboerforholdForBruker(periodeId)
        const samboerforhold = await hentSamboerforhold()
        populerSamboerState(samboerforhold)
        setOpen(false)
        setLoadingSubmit(false)
    }

    function populerSamboerState(samboerforhold: Samboerforhold[]) {
        handleRefetchSamboerforhold(samboerforhold)
    }

    return (
        <>
            <HStack gap="6">
                <Button size="xsmall" onClick={() => setOpen(true)} variant="tertiary" icon={<TrashIcon aria-hidden/>}>
                    {!isDesktop ? "" : "Slett"}
                </Button>
            </HStack>

            <Modal
                open={open}
                onClose={() => setOpen(false)}
                header={{
                    heading: "Er du sikker?",
                    size: "small",
                    closeButton: false,
                }}
                width="small"
            >
                <Modal.Body>
                    <BodyLong>
                        Du skal kun slette samboerskapet hvis det er registrert feil eller hvis dere aldri har bodd
                        sammen.
                    </BodyLong>
                </Modal.Body>
                <Modal.Footer>
                    {loadingSubmit ? <Button variant="danger" loading>Loading</Button> :
                        <Button type="button" variant="danger" onClick={() => slettSamboer(periodeId)}>
                            Ja, jeg er sikker
                        </Button>}
                    <Button type="button" variant="secondary" onClick={() => {
                        setOpen(false)
                        setLoadingSubmit(false)
                    }}>Avbryt</Button>
                </Modal.Footer>
            </Modal>
        </>
    );
}