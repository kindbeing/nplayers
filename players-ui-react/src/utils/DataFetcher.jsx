export default async function fetchData() {
    return fetch('/v1/players')
        .then(response => response.json())
        .then(data => {
            return data;
        }).catch(error => {
            console.log('oops there was an error', error)
        })
};

export async function fetchUserData() {
    const response = await fetch('/v1/users').catch(error => {
        console.error('Network error while fetching user data:', error);
        throw error;
    });

    // Network/HTTP layer check
    if (!response.ok) {
        throw new Error(`HTTP error: ${response.status} ${response.statusText}`);
    }

    const data = await response.json();

    if (data.status && data.status !== 200) {
        throw new Error(`API error: ${data.status}`);
    }
    return data;
}


export async function addUser(request) {
    return fetch('/v1/users')
        .then(response => response.json())
        .then(data => {
            return data;
        }).catch(error => {
            console.log('oops there was an error', error)
        })
}

export async function fetchPlayersCursorPaginated(cursor = null, limit = 10, direction = 'next') {
    const params = new URLSearchParams();
    if (cursor) params.append('cursor', cursor);
    params.append('limit', limit.toString());
    if (direction !== 'next') params.append('direction', direction);

    const response = await fetch(`/v1/players/cursor?${params}`).catch(error => {
        console.error('Network error while fetching players:', error);
        throw error;
    });

    // Network/HTTP layer check
    if (!response.ok) {
        throw new Error(`HTTP error: ${response.status} ${response.statusText}`);
    }

    const data = await response.json();

    // API error check (if the API has a status field)
    if (data.status && data.status !== 200) {
        throw new Error(`API error: ${data.status}`);
    }

    return data;
}