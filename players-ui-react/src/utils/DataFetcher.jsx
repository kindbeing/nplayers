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