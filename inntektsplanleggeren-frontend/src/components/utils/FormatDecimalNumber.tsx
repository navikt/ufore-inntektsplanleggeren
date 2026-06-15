import { numberFormat } from '@/common/Utils'

export const FormatDecimalNumber = ({ value }: { value: number }) => {
    return <span style={{ whiteSpace: 'nowrap' }}>{numberFormat(value)}</span>
}
