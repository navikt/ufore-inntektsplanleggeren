import { useState } from 'react'
import { BodyLong, Button, HStack, Modal } from '@navikt/ds-react'
import { getPidQueryParamString } from '@/components/utils/UrlUtil'

export function CancelConfirmationModal() {
    const [open, setOpen] = useState(false)

    const getUrl = () => {
        let url = import.meta.env.VITE_DIN_UFORETRYGD_URL
        if (import.meta.env.VITE_MODE === 'veileder') {
            url = url + getPidQueryParamString()
        }
        return url
    }

    return (
        <>
            <HStack gap="space-24">
                <Button type="button" onClick={() => setOpen(true)} variant="tertiary">
                    Avbryt
                </Button>
            </HStack>
            <Modal open={open} onClose={() => setOpen(false)} header={{ heading: 'Er du sikker?' }} closeOnBackdropClick width="medium">
                <Modal.Body>
                    <BodyLong>Hvis du avbryter nå lagres ikke dine opplysninger, og inntektsendringen sendes ikke inn til oss. Ønsker du å avbryte?</BodyLong>
                </Modal.Body>
                <Modal.Footer>
                    <Button
                        type="button"
                        variant="primary"
                        onClick={() => {
                            setOpen(false)
                        }}
                    >
                        Nei
                    </Button>
                    <Button type="button" as="a" href={getUrl()} variant="secondary">
                        Ja
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
}
