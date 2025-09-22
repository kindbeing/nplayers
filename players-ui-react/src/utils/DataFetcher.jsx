export async function fetchData() {
    return fetch('/v1/players')
        .then(response => response.json())
        .then(data => {
            return data;
        }).catch(error => {
            console.log('oops there was an error', error)
        })
}

export async function fetchPlayerDetails(playerId) {
    let response
    try {
        response = await fetch(`/v1/players/${playerId}`);
    } catch (e) {
        console.log('oops there was an error', e)
    }
    if (response.status === 200) {
        return response.json()
    } else {
        throw Error("Api did not respond with a 200")
    }
}