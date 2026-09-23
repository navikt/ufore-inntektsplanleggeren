import { z } from 'zod'

const schema = z.object({
    INNTEKTSPLANLEGGEREN_BACKEND_URL: z.url(),
    INNTEKTSPLANLEGGEREN_BACKEND_AUDIENCE: z.string(),
    VITE_MODE: z.enum(['borger', 'veileder']),
})

type Env = z.infer<typeof schema>

// export const env = (): Env => schema.parse(process.env)

export function env<Key extends keyof Env>(key: Key): Env[Key] {
    return schema.parse(process.env)[key]
}
