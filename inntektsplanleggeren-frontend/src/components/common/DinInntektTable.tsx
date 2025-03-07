import { Box, Button, HStack, Table, VStack } from '@navikt/ds-react'
import './DinInntektTable.css'
import { InntektDetaljer } from '@/api/model/ApiRequests'
import { Month } from '@/common/MonthEnum'
import { belopSum } from '@/common/Utils'
import React, { useEffect, useState } from 'react'
import { ChevronDownIcon, ChevronUpIcon } from '@navikt/aksel-icons'
import { FormatKroner } from '@/components/utils/FormatKroner'
import { DESKTOP_WIDTH } from '@/FormContainer' // Import the CSS file

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
  }

  const isDesktop = width > DESKTOP_WIDTH

  return (
    <Box borderRadius="xlarge" padding="4" className="top-box">
      <VStack gap="6">
        {children}

        {isOpen ? isDesktop ? <Innhold data={data} type={type} /> : <InnholdMobile data={data} type={type} /> : null}

        <HStack justify="center">
          <Button
            onClick={handleButton}
            variant="secondary-neutral"
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
        <Table.Row>
          <Table.HeaderCell scope="col">Måned</Table.HeaderCell>
          <Table.HeaderCell scope="col">Beløp per måned</Table.HeaderCell>
          <Table.HeaderCell scope="col">
            {props.type === 'pensjonsordning' ? 'Pensjonsordning' : 'Utbetaler'}{' '}
          </Table.HeaderCell>
        </Table.Row>
      </Table.Header>
      <Table.Body>
        {props.data.map(({ maned, belop, inntektsgivere }, i) => (
          <Table.Row key={i} className="table-row">
            <Table.DataCell scope="row">{Month[maned]}</Table.DataCell>
            <Table.DataCell>
              <FormatKroner value={belop} />
            </Table.DataCell>
            <Table.DataCell>{inntektsgivere.join(', ')}</Table.DataCell>
          </Table.Row>
        ))}
        <Table.Row>
          <Table.HeaderCell scope="row">Sum hittil i år</Table.HeaderCell>
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
          <Table.Row key={i}>
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
        <Table.Row>
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
