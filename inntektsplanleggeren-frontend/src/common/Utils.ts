import {InntektDetaljer} from "@/api/model/ApiRequests";

export function numberFormatWithKr(value: number): string {
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ") + " kr";
}

export function belopSum(data: InntektDetaljer[]): number {
    return data.reduce((acc, { belop }) => acc + belop, 0);
}
