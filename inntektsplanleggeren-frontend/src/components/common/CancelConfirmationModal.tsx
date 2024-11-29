
import React, {useState} from "react";
import {BodyLong, Button, HStack, Modal} from "@navikt/ds-react";

export function CancelConfirmationModal() {
    const [open, setOpen] = useState(false)

    return (
        <>
            <HStack gap="6">
                <Button type="button" onClick={() => setOpen(true)} variant="tertiary">
                    Avbryt
                </Button>
            </HStack>

            <Modal open={open} onClose={() => setOpen(false)} header={{heading: "Er du sikker?"}} closeOnBackdropClick width="small">
                <Modal.Body>
                    <BodyLong>
                        Hvis du avbryter nå lagres ikke dine opplysninger, og inntektsendringen sendes ikke inn til oss. Ønsker du å avbryte?
                    </BodyLong>
                </Modal.Body>
                <Modal.Footer>
                    <Button type="button" variant="primary" onClick={() => {setOpen(false)}}>
                        Nei
                    </Button>
                    <Button type="button" as="a" href="https://nav.no" variant="secondary">
                        Ja
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
}