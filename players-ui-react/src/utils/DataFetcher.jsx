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

export async function fetchPlayerAnalysis(playerId) {
    try {
        // Call the Java backend AI analysis endpoint
        const response = await fetch(`/v1/players/${playerId}/analyze`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('Player not found');
            } else if (response.status === 503) {
                throw new Error('AI analysis is currently unavailable. Please try again later.');
            } else {
                throw new Error(`Analysis failed: ${response.status}`);
            }
        }

        return await response.text();

    } catch (error) {
        console.error('Error fetching AI analysis:', error);
        throw new Error(error.message || 'AI analysis is currently unavailable. Please try again later.');
    }
}