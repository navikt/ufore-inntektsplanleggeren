import { useEffect, useState } from 'react'

const BASE_PATH = '/uforetrygd/selvbetjening/inntektsplanleggeren'

export function useToggle(name: string): boolean {
    const [enabled, setEnabled] = useState(false)

    useEffect(() => {
        fetch(`${BASE_PATH}/toggles`)
            .then((res) => res.json())
            .then((toggles: Record<string, boolean>) => setEnabled(!!toggles[name]))
            .catch(() => setEnabled(false))
    }, [name])

    return enabled
}
