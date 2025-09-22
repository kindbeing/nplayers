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

    // New state for search and filters
    const [searchTerm, setSearchTerm] = useState('');
    const [birthYearFilter, setBirthYearFilter] = useState('');
    const [countryFilter, setCountryFilter] = useState('');
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [totalPlayers, setTotalPlayers] = useState(0);

    useEffect(() => {
        fetchPlayers();
    }, [searchTerm, birthYearFilter, countryFilter, currentPage, pageSize]);

    const fetchPlayers = async () => {
        try {
            setLoading(true);
            const data = await fetchData(searchTerm, birthYearFilter, countryFilter, currentPage, pageSize);
            setPlayers(data.players);
            setFilteredPlayers(data.players);
            setTotalPlayers(data.players.length * (currentPage + 1)); // Approximate total for now
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

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

    const handleSearchSubmit = () => {
        setCurrentPage(0); // Reset to first page when searching
        fetchPlayers();
    }

    const handlePageChange = (newPage) => {
        setCurrentPage(newPage);
    }

    const handlePageSizeChange = (newSize) => {
        setPageSize(newSize);
        setCurrentPage(0); // Reset to first page
    }

    const clearFilters = () => {
        setSearchTerm('');
        setBirthYearFilter('');
        setCountryFilter('');
        setCurrentPage(0);
        setPageSize(10);
        fetchPlayers();
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

         {/* Search and Filter Section */}
         <div className="player-results-header" style={{marginTop: '20px', padding: '15px', backgroundColor: '#f8f9fa', borderRadius: '4px'}}>
             <h3>Search & Filter Players</h3>
             <div style={{display: 'flex', gap: '15px', flexWrap: 'wrap', alignItems: 'end'}}>
                 <div className="player-results-search">
                     <label>Search Name:</label>
                     <input
                         type="text"
                         value={searchTerm}
                         onChange={(e) => setSearchTerm(e.target.value)}
                         placeholder="First or last name"
                     />
                 </div>
                 <div className="player-results-search">
                     <label>Birth Year:</label>
                     <input
                         type="text"
                         value={birthYearFilter}
                         onChange={(e) => setBirthYearFilter(e.target.value)}
                         placeholder="e.g., 1981"
                     />
                 </div>
                 <div className="player-results-search">
                     <label>Country:</label>
                     <input
                         type="text"
                         value={countryFilter}
                         onChange={(e) => setCountryFilter(e.target.value)}
                         placeholder="e.g., USA"
                     />
                 </div>
                 <button onClick={handleSearchSubmit} style={{padding: '8px 16px'}}>Search</button>
                 <button onClick={clearFilters} style={{padding: '8px 16px', backgroundColor: '#6c757d'}}>Clear</button>
             </div>
         </div>

         {/* Pagination Controls */}
         <div className="player-results-header" style={{marginTop: '10px'}}>
             <div style={{display: 'flex', gap: '15px', alignItems: 'center', flexWrap: 'wrap'}}>
                 <div className="player-results-search">
                     <label>Page Size:</label>
                     <select value={pageSize} onChange={(e) => handlePageSizeChange(Number(e.target.value))}>
                         <option value={5}>5</option>
                         <option value={10}>10</option>
                         <option value={25}>25</option>
                         <option value={50}>50</option>
                     </select>
                 </div>
                 <div style={{display: 'flex', gap: '10px', alignItems: 'center'}}>
                     <button
                         onClick={() => handlePageChange(currentPage - 1)}
                         disabled={currentPage === 0}
                         style={{padding: '6px 12px'}}
                     >
                         Previous
                     </button>
                     <span>Page {currentPage + 1}</span>
                     <button
                         onClick={() => handlePageChange(currentPage + 1)}
                         disabled={filteredPlayers.length < pageSize}
                         style={{padding: '6px 12px'}}
                     >
                         Next
                     </button>
                 </div>
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
             <h3>Players ({filteredPlayers.length}) - Page {currentPage + 1}</h3>
             {(searchTerm || birthYearFilter || countryFilter) && (
                 <div style={{marginBottom: '10px', padding: '8px', backgroundColor: '#e3f2fd', borderRadius: '4px'}}>
                     <strong>Active Filters:</strong>
                     {searchTerm && <span> Name: "{searchTerm}"</span>}
                     {birthYearFilter && <span> Birth Year: {birthYearFilter}</span>}
                     {countryFilter && <span> Country: {countryFilter}</span>}
                 </div>
             )}
            {filteredPlayers.map((playerItem, index) => {
                return(
                    <div key={playerItem.playerId || index} style={{"display": "flex", "gap": "1vh", "padding": "8px", "borderBottom": "1px solid #eee", "alignItems": "center"}}>
                       <div style={{minWidth: '120px'}}><strong>{playerItem.playerId}</strong></div>
                       <div style={{minWidth: '150px'}}>{playerItem.firstName} {playerItem.lastName}</div>
                       <div style={{minWidth: '80px'}}>{playerItem.birthYear}</div>
                       <div style={{minWidth: '100px'}}>{playerItem.birthCountry}</div>
                       <div style={{marginLeft: 'auto'}}>
                           <button
                               onClick={() => {
                                   setPlayerIdInput(playerItem.playerId);
                                   handleSearchById();
                               }}
                               style={{padding: '4px 8px', fontSize: '12px'}}
                           >
                               View Details
                           </button>
                       </div>
                    </div>
                )
            })}
            {filteredPlayers.length === 0 && !loading && (
                <div style={{padding: '20px', textAlign: 'center', color: '#666'}}>
                    No players found matching your criteria.
                </div>
            )}
         </div>


    </div>
 )
}

export default PlayerResults;
