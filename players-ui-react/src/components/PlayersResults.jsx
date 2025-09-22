import React, { useEffect, useState } from 'react';

import {validateId, validateCountryCode, sanitizeInput} from "../utils";
import {fetchData, fetchPlayerDetails} from "../utils/DataFetcher";

function PlayerResults() {

    const [players, setPlayers] = useState([]);
    const [filteredPlayers, setFilteredPlayers] = useState([]);
    const [player, setPlayer] = useState(null);
    const [playerIdInput, setPlayerIdInput] = useState('');
    const [countryInput, setCountryInput] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchData()
            .then(data => {
                const subsetOfPlayers = data.players.slice(0,10);
                setPlayers(subsetOfPlayers);
                setFilteredPlayers(subsetOfPlayers);
                console.log(subsetOfPlayers);
            })
    }, []);

    const handleSearchById = async () => {
        if (!validateId(playerIdInput)) {
            setError('Player ID is required');
            return;
        }

        setLoading(true);
        setError(null);
        setPlayer(null);

        try {
            const data = await fetchPlayerDetails(playerIdInput.trim());
            setPlayer(data);
        } catch (error) {
            console.error('Error fetching player:', error);
            setError('Player not found or error occurred');
        } finally {
            setLoading(false);
        }
    }

    const handleSearchByCountry = () => {
        if (!validateCountryCode(countryInput)) {
            setError('Country code is required');
            return;
        }

        setError(null);
        const filtered = players.filter(player =>
            player.birthCountry &&
            player.birthCountry.toLowerCase().includes(countryInput.trim().toLowerCase())
        );
        setFilteredPlayers(filtered);
    }

 return (
     <div className="player-results">
         <div className="player-results-header">
            <div className="player-results-search">
                <label id="player-id-input">Player id:</label>
                <input
                    aria-labelledby="player-id-input"
                    type="text"
                    value={playerIdInput}
                    onChange={(e) => setPlayerIdInput(e.target.value)}
                />
                <button onClick={handleSearchById}>Submit</button>
            </div>
             <div className="player-results-search">
                 <label >Country Code:</label>
                 <input
                     type="text"
                     value={countryInput}
                     onChange={(e) => setCountryInput(e.target.value)}
                     placeholder="e.g., USA, CAN"
                 />
                 <button onClick={handleSearchByCountry}>Filter</button>
             </div>
         </div>
          {loading && (
              <div className="loading-section">
                  <p>Loading player details...</p>
              </div>
          )}
          
          {error && (
              <div className="error-section">
                  <p style={{color: 'red'}}>{error}</p>
              </div>
          )}
          
          {player && (
              <div className="player-details-section">
                  <h3>Player Details</h3>
                  <div>PlayerId: {player.playerId}</div>
                  <div>FirstName: {player.firstName}</div>
                  <div>LastName: {player.lastName}</div>
                  <div>Email: {player.email}</div>
                  <div>Birth Year: {player.birthYear}</div>
                  <div>Birth Country: {player.birthCountry}</div>
                  <div>Weight: {player.weight}</div>
                  <div>Height: {player.height}</div>
                  <div>Bats: {player.bats}</div>
                  <div>Throws: {player.throwStats}</div>
              </div>
          )}
         <div className="players-results-section">
             <h3>Players ({filteredPlayers.length})</h3>
            {filteredPlayers.map((playerItem, index) => {
                return(
                    <div key={playerItem.playerId || index} style={{"display": "flex", "gap": "1vh", "padding": "5px", "borderBottom": "1px solid #eee"}}>
                       <div><strong>{playerItem.playerId}</strong></div>
                       <div>{playerItem.firstName} {playerItem.lastName}</div>
                       <div>{playerItem.birthCountry}</div>
                    </div>
                )
            })}
         </div>


    </div>
 )
}

export default PlayerResults;
