import { VStack } from "@navikt/ds-react";
import Highcharts, {Options} from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import {SimulationResult} from "@/api/model/ApiRequests";

const NUMBER_FORMATTER = new Intl.NumberFormat('nb-NO', { style: 'decimal', useGrouping: true });
const getXAxisCategories = (isBeforeValuesAvailable: boolean) => {
    if (isBeforeValuesAvailable) {
        return ['I dag', 'Med dine endringer']
    } else {
        return ['Med dine endringer']
    }
}

const GRAPH_DATA = (isBeforeValuesAvailable: boolean) => {
    return {
        chart: {
            type: 'column',
            marginTop: 40
        },
        title: undefined,
        credits: undefined,
        xAxis: {
            categories: getXAxisCategories(isBeforeValuesAvailable),
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
    }
};

const COULMN_STYLE = (isBeforeValuesAvailable: boolean) => {
    return {
        maxPointWidth: isBeforeValuesAvailable ? undefined : 200,
        borderWidth: 1,
        borderColor: "#303030",
        dataLabels: {
            enabled: false
        }
    }
}

export const Graph = (props : { simulationResult : SimulationResult}) => {
    const isBeforeValuesAvailable = props.simulationResult.sum.yearly.before !== null
    const tooltip: Options['tooltip'] = {
        headerFormat: '<b>{point.x}</b><br/>',
            pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
    };

    return (
        <VStack>
            <HighchartsReact highcharts={Highcharts} options={{...GRAPH_DATA(isBeforeValuesAvailable), tooltip, series: [{
                ...COULMN_STYLE(isBeforeValuesAvailable),
                name: props.simulationResult.gjenlevendetillegg ? 'Uføretrygd inkludert gjenlevendetillegg' : 'Uføretrygd',
                data: [
                    isBeforeValuesAvailable ? ((props.simulationResult.uforetrygd.yearly.before ?? 0) + (props.simulationResult.gjenlevendetillegg?.yearly.before ?? 0)) : undefined,
                    (props.simulationResult.uforetrygd.yearly.after ?? 0) + (props.simulationResult.gjenlevendetillegg?.yearly.after ?? 0)].filter(isNotUndefined),
                color: "var(--a-deepblue-500)"
                },
                props.simulationResult.barnetilleggFellesbarn || props.simulationResult.barnetilleggSaerkullsbarn ? {
                    ...COULMN_STYLE(isBeforeValuesAvailable),
                    name: 'Barnetillegg uføretrygd',
                    data: [
                        isBeforeValuesAvailable ? ((props.simulationResult.barnetilleggSaerkullsbarn?.yearly.before ?? 0) + (props.simulationResult.barnetilleggFellesbarn?.yearly.before ?? 0)): undefined,
                        (props.simulationResult.barnetilleggSaerkullsbarn?.yearly.after ?? 0) + (props.simulationResult.barnetilleggFellesbarn?.yearly.after ?? 0)].filter(isNotUndefined),
                    color: "var(--a-purple-400)"
                    } : undefined,
            {
                ...COULMN_STYLE(isBeforeValuesAvailable),
                name: 'Din forventede inntekt',
                data: [
                    isBeforeValuesAvailable ? (props.simulationResult.forventetInntekt.yearly.before ?? 0) : undefined,
                    props.simulationResult.forventetInntekt.yearly.after ?? 0].filter(isNotUndefined),
                color: "var(--a-green-200)"
            }
            ].filter(isNotUndefined)}} />
        </VStack>
    )
}

const isNotUndefined = <T,>(value: T | undefined): value is T => value !== undefined;