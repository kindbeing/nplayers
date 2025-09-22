import React, { useEffect, useState } from 'react';

import {validateId, validateCountryCode, sanitizeInput} from "../utils";
import {fetchData, fetchPlayerDetails, fetchPlayerAnalysis} from "../utils/DataFetcher";

function PlayerResults() {

    const [players, setPlayers] = useState([]);
    const [filteredPlayers, setFilteredPlayers] = useState([]);
    const [player, setPlayer] = useState(null);
    const [playerIdInput, setPlayerIdInput] = useState('');
    const [countryInput, setCountryInput] = useState('');
    const [aiAnalysis, setAiAnalysis] = useState(null);
    const [aiLoading, setAiLoading] = useState(false);
    const [aiError, setAiError] = useState(null);
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

    const handleGetAIAnalysis = async () => {
        if (!player) return;

        setAiLoading(true);
        setAiError(null);
        setAiAnalysis(null);

        try {
            const analysis = await fetchPlayerAnalysis(player.playerId);
            setAiAnalysis(analysis);
        } catch (error) {
            setAiError(error.message);
        } finally {
            setAiLoading(false);
        }
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

                  <div className="ai-analysis-section" style={{marginTop: '20px'}}>
                      <h4>AI Performance Analysis</h4>
                      <button
                          onClick={handleGetAIAnalysis}
                          disabled={aiLoading}
                          style={{
                              padding: '8px 16px',
                              backgroundColor: aiLoading ? '#ccc' : '#007bff',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              cursor: aiLoading ? 'not-allowed' : 'pointer'
                          }}
                      >
                          {aiLoading ? 'Analyzing...' : 'Get AI Analysis'}
                      </button>

                      {aiLoading && (
                          <div style={{marginTop: '10px', color: '#666'}}>
                              Analyzing player performance with AI...
                          </div>
                      )}

                      {aiError && (
                          <div style={{marginTop: '10px', color: '#d32f2f', fontSize: '14px'}}>
                              {aiError}
                          </div>
                      )}

                      {aiAnalysis && (
                          <div style={{
                              marginTop: '15px',
                              padding: '15px',
                              backgroundColor: '#f8f9fa',
                              border: '1px solid #e9ecef',
                              borderRadius: '4px',
                              whiteSpace: 'pre-wrap',
                              fontSize: '14px',
                              lineHeight: '1.5'
                          }}>
                              <strong>AI Analysis:</strong><br />
                              {aiAnalysis}
                          </div>
                      )}
                  </div>
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
