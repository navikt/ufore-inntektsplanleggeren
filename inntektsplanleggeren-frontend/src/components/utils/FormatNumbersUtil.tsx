export const formatInntekt = (amount?: number | string | null): string => {
  if (amount === null || amount === undefined || amount === '') return ''
  const integerAmount = typeof amount === 'string' ? parseInt(amount.replace(/\D+/g, ''), 10) : amount

  return !isNaN(integerAmount) ? FORMATTER.format(integerAmount) : ''
}

export const parseInntekt = (s: string) => {
  if (!s) return 0
  if (s.includes('.')) {
    return NaN
  }
  return Number(s.replace(/\s+/g, ''));
}

const FORMATTER = Intl.NumberFormat('nb-NO', {
  style: 'decimal',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
})
