import { VStack } from "@navikt/ds-react";
import Highcharts, {Options} from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import {SimulationResult} from "@/api/model/ApiRequests";

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

export const Graph = (props : { simulationResult : SimulationResult}) => {
    const tooltip: Options['tooltip'] = {
        headerFormat: '<b>{point.x}</b><br/>',
            pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
    };

    return (
        <VStack>
            <HighchartsReact highcharts={Highcharts} options={{...GRAPH_DATA, tooltip, series: [{
                name: props.simulationResult.gjenlevendetillegg ? 'Uføretrygd inkludert gjenlevendetillegg' : 'Uføretrygd',
                dataLabels: {
                enabled: false
                },
                data: [
                    (props.simulationResult.uforetrygd.yearly.before ?? 0) + (props.simulationResult.gjenlevendetillegg?.yearly.before ?? 0),
                    (props.simulationResult.uforetrygd.yearly.after ?? 0) + (props.simulationResult.gjenlevendetillegg?.yearly.after ?? 0)],
                color: "var(--a-deepblue-500)"
            }, {
                name: 'Din forventede inntekt',
                dataLabels: {
                enabled: false
                },
                data: [
                    props.simulationResult.forventetInntekt.yearly.before ?? 0,
                    props.simulationResult.forventetInntekt.yearly.after ?? 0],
                color: "var(--a-green-200)"
            },
                    props.simulationResult.barnetilleggFellesbarn || props.simulationResult.barnetilleggSaerkullsbarn ? {
                name: 'Barnetillegg',
                dataLabels: {
                enabled: false
                },
                data: [
                    (props.simulationResult.barnetilleggSaerkullsbarn?.yearly.before ?? 0) + (props.simulationResult.barnetilleggFellesbarn?.yearly.before ?? 0),
                    (props.simulationResult.barnetilleggSaerkullsbarn?.yearly.after ?? 0) + (props.simulationResult.barnetilleggFellesbarn?.yearly.after ?? 0)],
                color: "var(--a-purple-400)"
            } : undefined,
            ].filter(isNotUndefined)}} />
        </VStack>
    )
}

const isNotUndefined = <T,>(value: T | undefined): value is T => value !== undefined;
