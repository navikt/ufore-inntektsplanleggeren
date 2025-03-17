import { VStack } from '@navikt/ds-react'
import Highcharts, { Options } from 'highcharts'
import HighchartsReact from 'highcharts-react-official'
import { SimulationResult } from '@/api/model/ApiRequests'
import { DESKTOP_WIDTH } from '@/FormContainer'
import { useEffect, useState } from 'react'

const NUMBER_FORMATTER = new Intl.NumberFormat('nb-NO', { style: 'decimal', useGrouping: true })

const formatYAxisNumber = (isDesktop: boolean, value: string | number) => {
  const valueAsString = value + ''
  if (!isDesktop && valueAsString.length > 3) {
    return NUMBER_FORMATTER.format(Number.parseInt(valueAsString.substring(0, valueAsString.length - 3), 10))
  }
  return NUMBER_FORMATTER.format(Number.parseInt(valueAsString))
}

const getXAxisCategories = (isBeforeValuesAvailable: boolean) => {
  if (isBeforeValuesAvailable) {
    return ['I dag', 'Med dine endringer']
  } else {
    return ['Med dine endringer']
  }
}

const numberOfItems = (simulationResult: SimulationResult) => {
  return simulationResult.barnetilleggFellesbarn || simulationResult.barnetilleggSaerkullsbarn ? 3 : 2
}

const GRAPH_DATA = (isBeforeValuesAvailable: boolean, isDesktop: boolean, numberOfItems: number) => {
  return {
    chart: {
      type: 'column',
      marginTop: 50,
      height: 400 + numberOfItems * 37,
    },
    title: undefined,
    credits: undefined,
    xAxis: {
      categories: getXAxisCategories(isBeforeValuesAvailable),
      labels: {
        style: {
          fontSize: isDesktop ? 18 : 16,
          fontWeight: 600,
          fontFamily: 'var(--a-font-family)',
          color: 'var(--a-grayalpha-700)',
        },
      },
    },
    yAxis: {
      min: 0,
      maxPadding: 0.1,
      title: {
        align: 'high',
        offset: isDesktop ? 13 : -50,
        text: isDesktop ? 'Kroner' : 'Tusen kroner',
        rotation: 0,
        y: -30,
        style: {
          fontSize: '16px',
          fontFamily: 'var(--a-font-family)',
          color: 'var(--a-grayalpha-700)',
        },
      },
      stackLabels: {
        enabled: true,
        formatter(this: Highcharts.StackItemObject) {
          return `Sum ${NUMBER_FORMATTER.format(this.total)} kr`
        },
        style: {
          fontSize: isDesktop ? 18 : 16,
          color: 'var(--a-grayalpha-700)',
          textOutline: false,
          fontWeight: 600,
          fontFamily: 'var(--a-font-family)',
          align: 'center',
        },
      },
      labels: {
        formatter: ({ value }: { value: string | number }) => formatYAxisNumber(isDesktop, value),
        style: {
          fontSize: 16,
          fontFamily: 'var(--a-font-family)',
          color: 'var(--a-grayalpha-700)',
        },
      },
    },
    plotOptions: {
      column: {
        stacking: 'normal',
        dataLabels: {
          enabled: true,
          padding: 10,
        },
      },
    },
    legend: {
      events: {
        itemClick: function () {
          return false
        },
      },
      align: 'left',
      x: 0,
      enableMouseTracking: false,
      symbolHeight: 15, //size of legend circle
      itemHoverStyle: {
        color: '#010B18AD',
      },
      itemStyle: {
        color: '#010B18AD',
        fontSize: '18px',
        newLine: true,
        cursor: 'auto',
      },
      itemDistance: numberOfItems > 2 ? 80 : 20,
      itemMarginBottom: 15,
    },
  }
}

const COLUMN_STYLE = {
  states: {
    hover: {
      enabled: false,
    },
    inactive: {
      opacity: 1,
    },
  },
  dataLabels: {
    enabled: false,
  },
}

export const Graph = (props: { simulationResult: SimulationResult }) => {
  const [width, setWidth] = useState<number>(window.innerWidth)
  const handleWindowSize = () => setWidth(window.innerWidth)
  useEffect(() => {
    window.addEventListener('resize', handleWindowSize)
    return () => window.removeEventListener('resize', handleWindowSize)
  })
  const isDesktop = width > DESKTOP_WIDTH

  const isBeforeValuesAvailable = props.simulationResult.sum.yearly.before !== null
  const tooltip: Options['tooltip'] = {
    enabled: false,
    headerFormat: '<b>{point.x}</b><br/>',
    pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}',
  }

  return (
    <VStack>
      <HighchartsReact
        highcharts={Highcharts}
        options={{
          ...GRAPH_DATA(isBeforeValuesAvailable, isDesktop, numberOfItems(props.simulationResult)),
          tooltip,
          series: [
            {
              ...COLUMN_STYLE,
              name: props.simulationResult.gjenlevendetillegg
                ? 'Uføretrygd inkludert gjenlevendetillegg'
                : 'Uføretrygd',
              data: [
                isBeforeValuesAvailable &&
                  (props.simulationResult.uforetrygd.yearly.before ?? 0) +
                    (props.simulationResult.gjenlevendetillegg?.yearly.before ?? 0),
                (props.simulationResult.uforetrygd.yearly.after ?? 0) +
                  (props.simulationResult.gjenlevendetillegg?.yearly.after ?? 0),
              ].filter(isNotFalse),
              color: 'var(--a-deepblue-500)',
            },
            props.simulationResult.barnetilleggFellesbarn || props.simulationResult.barnetilleggSaerkullsbarn
              ? {
                  ...COLUMN_STYLE,
                  name: 'Barnetillegg uføretrygd',
                  data: [
                    isBeforeValuesAvailable &&
                      (props.simulationResult.barnetilleggSaerkullsbarn?.yearly.before ?? 0) +
                        (props.simulationResult.barnetilleggFellesbarn?.yearly.before ?? 0),
                    (props.simulationResult.barnetilleggSaerkullsbarn?.yearly.after ?? 0) +
                      (props.simulationResult.barnetilleggFellesbarn?.yearly.after ?? 0),
                  ].filter(isNotFalse),
                  color: 'var(--a-purple-400)',
                }
              : undefined,
            {
              ...COLUMN_STYLE,
              name: 'Din forventede inntekt',
              data: [
                isBeforeValuesAvailable && (props.simulationResult.forventetInntekt.yearly.before ?? 0),
                props.simulationResult.forventetInntekt.yearly.after ?? 0,
              ].filter(isNotFalse),
              color: 'var(--a-green-400)',
            },
          ].filter(isNotUndefined),
        }}
      />
    </VStack>
  )
}

const isNotUndefined = (value: unknown | undefined) => value !== undefined
const isNotFalse = <T,>(value: T | undefined): value is T => value !== false
