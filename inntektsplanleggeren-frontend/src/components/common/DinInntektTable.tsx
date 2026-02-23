import { Box, Button, HStack, Table, VStack } from '@navikt/ds-react'
import { InntektDetaljer } from '@/api/model/ApiRequests'
import { Month } from '@/common/MonthEnum'
import { belopSum } from '@/common/Utils'
import React, { useEffect, useState } from 'react'
import { ChevronDownIcon, ChevronUpIcon } from '@navikt/aksel-icons'
import { FormatKroner } from '@/components/utils/FormatKroner'
import { DESKTOP_WIDTH } from '@/FormContainer'
import { umami } from '@/common/umami' // Import the CSS file

interface DinInntektTableProps {
    type?: 'arbeidsgiver' | 'pensjonsordning'
    data: InntektDetaljer[]
    children: React.ReactNode
}

export const DinInntektTable = ({ data, children, type }: DinInntektTableProps) => {
    const [width, setWidth] = useState<number>(window.innerWidth)
    const [isOpen, setIsOpen] = React.useState(false)
    const [buttonText, setButtonText] = React.useState('')
    const closedText = 'Vis månedsoversikt'
    const openText = 'Skjul månedsoversikt'

    const handleWindowSize = () => setWidth(window.innerWidth)

    useEffect(() => {
        setButtonText(isOpen ? openText : closedText)
        window.addEventListener('resize', handleWindowSize)
        return () => window.removeEventListener('resize', handleWindowSize)
    }, [isOpen])

    const handleButton = () => {
        setIsOpen(!isOpen)
        setButtonText(isOpen ? openText : closedText)
        umami(isOpen ? 'accordion åpnet' : 'accordion lukket', {
            tekst: type == 'arbeidsgiver' ? 'Månedsoversikt lønn' : 'Månedsoversikt pensjoner andre enn Nav',
        })
    }

    const isDesktop = width > DESKTOP_WIDTH

    return (
        <Box borderRadius="12" padding="space-16" background="accent-soft">
            <VStack gap="space-24">
                {children}

                {isOpen ? isDesktop ? <Innhold data={data} type={type} /> : <InnholdMobile data={data} type={type} /> : null}

                <HStack justify="center">
                    <Button
                        data-color="neutral"
                        onClick={handleButton}
                        variant="secondary"
                        iconPosition="right"
                        icon={isOpen ? <ChevronUpIcon aria-hidden /> : <ChevronDownIcon aria-hidden />}
                    >
                        {buttonText}
                    </Button>
                </HStack>
            </VStack>
        </Box>
    )
}

const Innhold = (props: { data: InntektDetaljer[]; type?: string }) => {
    return (
        <Table>
            <Table.Header>
                <Table.Row shadeOnHover={false}>
                    <Table.HeaderCell scope="col">Måned</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Beløp per måned</Table.HeaderCell>
                    <Table.HeaderCell scope="col">{props.type === 'pensjonsordning' ? 'Pensjonsordning' : 'Utbetaler'} </Table.HeaderCell>
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {props.data.map(({ maned, belop, inntektsgivere }, i) => (
                    <Table.Row key={i} className="table-row" shadeOnHover={false}>
                        <Table.DataCell scope="row">{Month[maned]}</Table.DataCell>
                        <Table.DataCell>
                            <FormatKroner value={belop} />
                        </Table.DataCell>
                        <Table.DataCell>{inntektsgivere.join(', ')}</Table.DataCell>
                    </Table.Row>
                ))}
                <Table.Row shadeOnHover={false}>
                    <Table.HeaderCell scope="row">
                        <span style={{ whiteSpace: 'nowrap' }}>Sum hittil i år</span>
                    </Table.HeaderCell>
                    <Table.DataCell>
                        <strong>
                            <FormatKroner value={belopSum(props.data)} />
                        </strong>
                    </Table.DataCell>
                    <Table.DataCell></Table.DataCell>
                </Table.Row>
            </Table.Body>
        </Table>
    )
}

const InnholdMobile = (props: { data: InntektDetaljer[]; type?: string }) => {
    return (
        <Table>
            <Table.Body>
                {props.data.map(({ maned, belop, inntektsgivere }, i) => (
                    <Table.Row key={i} shadeOnHover={false}>
                        <Table.DataCell>
                            <div>
                                <strong>{Month[maned]}</strong>
                                <div>
                                    Beløp per måned: <FormatKroner value={belop} />
                                </div>
                                {inntektsgivere.length > 0 ? (
                                    <div>
                                        {props.type === 'pensjonsordning' ? 'Pensjonsordning' : 'Utbetaler'}: {inntektsgivere.join(', ')}
                                    </div>
                                ) : null}
                            </div>
                        </Table.DataCell>
                    </Table.Row>
                ))}
                <Table.Row shadeOnHover={false}>
                    <Table.DataCell>
                        <strong>
                            Sum hittil i år: <FormatKroner value={belopSum(props.data)} />
                        </strong>
                    </Table.DataCell>
                </Table.Row>
            </Table.Body>
        </Table>
    )
}
