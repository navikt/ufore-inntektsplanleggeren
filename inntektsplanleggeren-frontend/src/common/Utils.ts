import {InntektDetaljer} from "@/api/model/ApiRequests";

export function numberFormat(value: number): string {
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
}

export function belopSum(data: InntektDetaljer[]): number {
    return data.reduce((acc, { belop }) => acc + belop, 0);
}