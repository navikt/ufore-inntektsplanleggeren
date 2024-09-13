import {
    OpprettSamboerforholdRequest,
    EndreSamboerforholdRequest,
    HenteFamilierelasjonsResponse,
    RelasjonPersondata,
    Samboerforhold
} from "@/api/model/ApiRequests";

export interface Familierelasjon {
    pid: string,
    fom: string | undefined,
    tom: string | undefined,
    relasjonstype: string,
    relasjonPersondata: RelasjonPersondata | undefined,
}

export interface DisplayData {
    forventetInntekt: number
    forventetInntektAnnenForelder: number | null
    inntektsgrense: number
    kompensasjonsgrad: number
    grenseStoppAvUfoeretrygd: number
    aktuelleAar: number[]
    harVarigTilrettelagtArbeid: boolean
    harBarneTillegg: boolean
    harGjenlevendeTillegg: boolean
}

export async function hentDisplayData(): Promise<DisplayData> {

    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    let headers;

    if (pid) {
        headers =  {
            'Content-Type': 'application/json',
            'pid': pid
        }
    } else {
        headers = {
            'Content-Type': 'application/json'
        }
    }

    return await fetch(window.location.pathname + "api/inntektsplannleger", { //todo fix url
        method: "GET",
        credentials: "include",
        headers: headers
    })
        .then(response => response.json())
        .then(response => {
            return response.displayData
        }).catch(() => {
            throw new Error("Fikk ikke 2xx respons fra server");
        })

}

export async function hentSamboerforhold(): Promise<Samboerforhold[]> {

    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    let headers;

    if (pid) {
        headers =  {
            'Content-Type': 'application/json',
            'pid': pid
        }
    } else {
        headers = {
            'Content-Type': 'application/json'
        }
    }

    return await fetch(window.location.pathname + "api/samboer", {
        method: "GET",
        credentials: "include",
        headers: headers
    })
        .then(response => response.json())
        .then(samboerforholdResponse => {
            return samboerforholdResponse.samboerforhold
        }).catch(() => {
            throw new Error("Fikk ikke 2xx respons fra server");
        })

}

export async function opprettSamboerforholdForBruker(fom: string, pidSamboer: string): Promise<string> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid = searchParams.get("pid")

    const request : OpprettSamboerforholdRequest = {
        pidSamboer : pidSamboer,
        fom : fom
    }

    let headers;
    if (pid !== null) {
        headers =  {
            'Content-Type': 'application/json',
            'pid': pid,
        }
    } else {
        headers = {
            'Content-Type': 'application/json',
        }
    }


    return await fetch(window.location.pathname + "api/samboer", {
            method: "POST",
            credentials: "include",
            headers: headers,
            body: JSON.stringify(request)
        }
    ).then(
        response => {
            if (response.status >= 200 && response.status < 300) {
                return "OK"
            } else if (response.status == 400) {
                return response.json().then(
                    data => {
                        return data.message
                    }
                )
            } else {
                throw new Error("Fikk ikke 2xx respons fra server");
            }
        }
    )
}

export async function hentEpsOgBarnForBruker(pid: string | null): Promise<HenteFamilierelasjonsResponse> {

    let headers;
    if (pid !== null) {
        headers =  {
            'Content-Type': 'application/json',
            'pid': pid
        }
    } else {
        headers = {
            'Content-Type': 'application/json'
        }
    }

    return await fetch(window.location.pathname + "api/familieforhold", { // Må legge til pathname pga fetch fra frontend gir ikke riktig url
        method: "GET",
        credentials: "include",
        headers: headers
    })
        .then(response => response.json())
        .then(epsOgBarnResponse => {
            if (epsOgBarnResponse) {
                return epsOgBarnResponse
            }
        })

}

export async function sletteSamboerforholdForBruker(periodeId: number) {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    let headers ;

    if (pid) {
        headers =  {
            'Content-Type': 'application/json',
            'pid': pid
        }
    } else {
        headers = {
            'Content-Type': 'application/json'
        }
    }

    return await fetch(window.location.pathname + "api/samboer/" + periodeId, {
        method: "DELETE",
        credentials: "include",
        headers: headers
    }).then(
        response => {
            if (response.status < 200 || response.status >= 300) {
                throw new Error("Fikk ikke 2xx respons fra server")
            }
        }
    )
}

export async function endreSamboerforholdForBruker(periodeId: number, fom: string, tom: string | null) {
    const request : EndreSamboerforholdRequest = {
        fom : fom,
        tom : tom
    }

    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    let headers ;

    if (pid) {
        headers =  {
            'Content-Type': 'application/json',
            'pid': pid
        }
    } else {
        headers = {
            'Content-Type': 'application/json'
        }
    }

    return await fetch(window.location.pathname + "api/samboer/" + periodeId,
    {
        headers: headers,
        method: "PUT",
        credentials: "include",
        body: JSON.stringify(request)
    })
        .then(
        response => {
            if (response.status >= 200 && response.status < 300) {
                return "OK"
            }
            else if (response.status === 400) {
                return response.json().then(
                    data => {
                        return data.message
                    }
                )
            }
            else {
                throw new Error("Fikk ikke 2xx respons fra server")
            }

        }
    )
}