import {InntektDetaljer} from "@/api/model/ApiRequests";
import {format, } from "date-fns";

export function numberFormatWithKr(value: number): string {
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ") + " kr";
}

export function belopSum(data: InntektDetaljer[]): number {
    return data.reduce((acc, { belop }) => acc + belop, 0);
}

export function formatDateTime(value: Date): string {
    return value.toLocaleDateString('no-NO', {day: 'numeric', month: 'long', year: 'numeric'}) + ' kl. ' + value.toLocaleTimeString('no-NO', {hour: '2-digit', minute:'2-digit'})
}

export function formatDate(value: Date): string {
    return format(value, 'dd.MM.yyyy')
}

