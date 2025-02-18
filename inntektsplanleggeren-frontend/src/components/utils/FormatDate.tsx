import {formatDateTime, formatDate, formatDateLong} from "@/common/Utils";

export const FormatDate = ({ value }: { value: Date }) => {
    return <span style={{whiteSpace: "nowrap"}}>{formatDate(value)}</span>
}

export const FormatDateLong = ({ value }: { value: Date }) => {
    return <span style={{whiteSpace: "nowrap"}}>{formatDateLong(value)}</span>
}

export const FormatDateTime = ({ value }: { value: Date }) => {
    return <span style={{whiteSpace: "nowrap"}}>{formatDateTime(value)}</span>
}

