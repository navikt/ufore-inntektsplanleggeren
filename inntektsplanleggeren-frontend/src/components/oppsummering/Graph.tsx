import { VStack } from "@navikt/ds-react";
import {useContext} from 'react';
import Highcharts, {Options} from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import { DataContext } from "@/DataContextProvider";

const NUMBER_FORMATTER = new Intl.NumberFormat('nb-NO', { style: 'decimal', useGrouping: true });

const GRAPH_DATA: Options = {
    chart: {
        type: 'column',
        marginTop: 40
    },
    title: undefined,
    credits: undefined,
    xAxis: {
        categories: ['I dag', 'Med dine endringer'],
        labels: {
            style: {
                fontWeight: 'bold'
            }
        },
        
    },
    yAxis: {
        min: 0,
        title: {
            align: 'high',
            offset: 15,
            text: 'Kroner',
            rotation: 0,
            y: -30,
            style: {
                fontSize: '15px',
            }
        },
        stackLabels: {
            enabled: true
        },
        labels: {
            formatter: ({value}) => NUMBER_FORMATTER.format(typeof value === "string" ? Number.parseInt(value, 10) : value),
        }
    },
    plotOptions: {
        column: {
            stacking: 'normal',
            dataLabels: {
                enabled: true
            },
        }
    },
    legend: {
        symbolHeight: 15, //size of legend circle
        itemStyle: {
            color: '#010B18AD',
            fontSize: '17px', // TODO: how to use stantdard nav font?
            newLine: true
        },
        itemDistance: 40,
        // itemWidth: 250,
        }

    };

export const Graph = () => {
    const { simulationResponse } = useContext(DataContext);

    const tooltip: Options['tooltip'] = {
        headerFormat: '<b>{point.x}</b><br/>',
            pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
    };

    return (
        <VStack>
            <HighchartsReact highcharts={Highcharts} options={{...GRAPH_DATA, tooltip, series: [{
                name: 'Uføretrygd inkludert gjenlevendetillegg',
                dataLabels: {
                enabled: false
                },
                data: [
                    (simulationResponse?.result?.uforetrygd.yearly.before ?? 0) + (simulationResponse?.result?.gjenlevendetillegg?.yearly.before ?? 0),
                    (simulationResponse?.result?.uforetrygd.yearly.after ?? 0) + (simulationResponse?.result?.gjenlevendetillegg?.yearly.after ?? 0)],
                color: "var(--a-deepblue-500)"
            }, {
                name: 'Din forventede inntekt',
                dataLabels: {
                enabled: false
                },
                data: [
                    simulationResponse?.result?.forventetInntekt.yearly.before ?? 0,
                    simulationResponse?.result?.forventetInntekt.yearly.after ?? 0],
                color: "var(--a-green-200)"
            },
                simulationResponse?.result.barnetilleggFellesbarn || simulationResponse?.result.barnetilleggSaerkullsbarn ? {
                name: 'Barnetillegg',
                dataLabels: {
                enabled: false
                },
                data: [
                    (simulationResponse?.result?.barnetilleggSaerkullsbarn?.yearly.before ?? 0) + (simulationResponse?.result?.barnetilleggFellesbarn?.yearly.before ?? 0),
                    (simulationResponse?.result?.barnetilleggSaerkullsbarn?.yearly.after ?? 0) + (simulationResponse?.result?.barnetilleggFellesbarn?.yearly.after ?? 0)],
                color: "var(--a-purple-400)"
            } : undefined,
            ].filter(isNotUndefined)}} />
        </VStack>
    )
}

const isNotUndefined = <T,>(value: T | undefined): value is T => value !== undefined;
