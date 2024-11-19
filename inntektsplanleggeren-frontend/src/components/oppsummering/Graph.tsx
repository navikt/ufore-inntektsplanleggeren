import { VStack } from "@navikt/ds-react";
import {useContext} from 'react';
import Highcharts, {Options} from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import { DataContext } from "@/DataContextProvider";

const GRAPH_DATA: Options = {
    chart: {
        type: 'column'
    },
    title: {
        text: undefined
    },
    xAxis: {
        categories: ['I dag', 'Med dine endringer']
    },
    yAxis: {
        min: 0,
        title: {
            text: 'kroner'
        },
        stackLabels: {
            enabled: true
        }
    },
    plotOptions: {
        column: {
            stacking: 'normal',
            dataLabels: {
                enabled: true
            }
        }
    },
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
                    data: [
                        (simulationResponse?.result?.uforetrygd.yearly.before ?? 0) + (simulationResponse?.result?.gjenlevendetillegg?.yearly.before ?? 0),
                        (simulationResponse?.result?.uforetrygd.yearly.after ?? 0) + (simulationResponse?.result?.gjenlevendetillegg?.yearly.after ?? 0)]
                }, {
                    name: 'Din forventede inntekt',
                    data: [
                        simulationResponse?.result?.forventetInntekt.yearly.before ?? 0,
                        simulationResponse?.result?.forventetInntekt.yearly.after ?? 0]
                },
                    simulationResponse?.result.barnetilleggFellesbarn || simulationResponse?.result.barnetilleggSaerkullsbarn ? {
                    name: 'Barnetillegg',
                    data: [
                        (simulationResponse?.result?.barnetilleggSaerkullsbarn?.yearly.before ?? 0) + (simulationResponse?.result?.barnetilleggFellesbarn?.yearly.before ?? 0),
                        (simulationResponse?.result?.barnetilleggSaerkullsbarn?.yearly.after ?? 0) + (simulationResponse?.result?.barnetilleggFellesbarn?.yearly.after ?? 0)]
                } : undefined,
                ].filter(isNotUndefined)}} />
        </VStack>
    )
}

const isNotUndefined = <T,>(value: T | undefined): value is T => value !== undefined;
