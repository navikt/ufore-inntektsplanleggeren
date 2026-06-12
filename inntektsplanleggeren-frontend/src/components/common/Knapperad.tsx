import { ArrowLeftIcon, ArrowRightIcon } from '@navikt/aksel-icons'
import { Button, HStack, VStack } from '@navikt/ds-react'
import { Link as RouterLink } from 'react-router-dom'
import { CancelConfirmationModal } from '@/components/common/CancelConfirmationModal'
import { getFullPathForPage, type PageLinks } from '@/FormContainer'

interface Props {
    handleSubmit: (e: React.MouseEvent | React.FormEvent) => void
    tilbakePageLink: PageLinks
    gåVidereTekst?: string
    laster?: boolean
    visAvbryt?: boolean
}

export default function Knapperad({ handleSubmit, tilbakePageLink, gåVidereTekst = 'Gå videre', laster = false, visAvbryt = true }: Props) {
    return (
        <VStack gap="space-16">
            <HStack gap="space-24">
                <Button as={RouterLink} to={getFullPathForPage(tilbakePageLink)} iconPosition="left" icon={<ArrowLeftIcon aria-hidden />} variant="secondary">
                    Gå tilbake
                </Button>
                <Button variant="primary" iconPosition="right" icon={<ArrowRightIcon aria-hidden />} onClick={handleSubmit} loading={laster}>
                    {gåVidereTekst}
                </Button>
            </HStack>
            {visAvbryt && <CancelConfirmationModal />}
        </VStack>
    )
}
