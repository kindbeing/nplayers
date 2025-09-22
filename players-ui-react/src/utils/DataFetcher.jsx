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

export async function fetchPlayerAnalysis(playerId, playerData) {
    try {
        // Call the AI service directly (port 5000)
        const response = await fetch('http://localhost:5000/llm/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                system_prompt: `You are a baseball analyst providing performance insights. Analyze the player's career, playing style, and impact. Keep your response to 3-4 key insights, concise and professional.`,
                user_prompt: `Analyze this baseball player's performance data:

Player: ${playerData.firstName} ${playerData.lastName}
Born: ${playerData.birthYear} (${new Date().getFullYear() - playerData.birthYear} years old)
Physical: ${playerData.height} inches, ${playerData.weight} lbs
Batting: ${playerData.bats}, Throwing: ${playerData.throwStats}
Career: ${playerData.debut} to ${playerData.finalGame}

Please provide 3-4 key insights about their career performance and playing style.`
            })
        });

        if (!response.ok) {
            throw new Error(`AI service returned ${response.status}`);
        }

        const data = await response.json();
        return data.response;

    } catch (error) {
        console.error('Error fetching AI analysis:', error);
        throw new Error('AI analysis is currently unavailable. Please try again later.');
    }
}