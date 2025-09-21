import React, { useEffect, useState } from 'react';
import { fetchPlayersCursorPaginated } from "../utils/DataFetcher";

function PlayerResults() {
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentCursor, setCurrentCursor] = useState(null);
    const [pageSize, setPageSize] = useState(10);
    const [pagination, setPagination] = useState(null);
    const [cursorHistory, setCursorHistory] = useState([]); // For back navigation

    useEffect(() => {
        fetchPlayers(null, pageSize, null);
    }, []);

    const fetchPlayers = async (cursor, limit, direction) => {
        try {
            setLoading(true);
            const data = await fetchPlayersCursorPaginated(cursor, limit, direction);
            setPlayers(data.players);
            setPagination(data.pagination);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleNextPage = () => {
        if (pagination?.hasNext && pagination?.nextCursor) {
            setCurrentCursor(pagination.nextCursor);
            setCursorHistory(prev => [...prev, currentCursor].filter(Boolean));
            fetchPlayers(pagination.nextCursor, pageSize, 'next');
        }
    };

    const handlePreviousPage = () => {
        if (cursorHistory.length > 0) {
            const previousCursor = cursorHistory[cursorHistory.length - 1];
            setCursorHistory(prev => prev.slice(0, -1));
            setCurrentCursor(previousCursor);
            fetchPlayers(previousCursor, pageSize, 'prev');
        } else {
            // Go to first page
            setCurrentCursor(null);
            fetchPlayers(null, pageSize, null);
        }
    };

    const handlePageSizeChange = (newSize) => {
        setPageSize(newSize);
        setCurrentCursor(null);
        setCursorHistory([]);
        fetchPlayers(null, newSize, null);
    };

    const handleFirstPage = () => {
        setCurrentCursor(null);
        setCursorHistory([]);
        fetchPlayers(null, pageSize, null);
    };

    // Search functionality (placeholder for now)
    const handleSearchById = (input) => {
        // TODO: Implement search functionality
        console.log('Search by ID:', input);
    };

    const handleSearchByCountry = (input) => {
        // TODO: Implement search functionality
        console.log('Search by country:', input);
    };

    if (loading) return <div>Loading players...</div>;
    if (error) return <div>Error: {error}</div>;

    return (
        <div className="player-results">
            <div className="player-results-header">
                <h2>Players</h2>
                <div className="player-results-search">
                    <label>Player ID:</label>
                    <input type="text" placeholder="Enter player ID" />
                    <button onClick={(e) => handleSearchById(e.target.previousSibling.value)}>Search</button>
                </div>
                <div className="player-results-search">
                    <label>Country Code:</label>
                    <input type="text" placeholder="Enter country code" />
                    <button onClick={(e) => handleSearchByCountry(e.target.previousSibling.value)}>Search</button>
                </div>
            </div>

            <div className="cursor-pagination-controls">
                <label>Page Size:
                    <select
                        value={pageSize}
                        onChange={(e) => handlePageSizeChange(Number(e.target.value))}
                    >
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={25}>25</option>
                        <option value={50}>50</option>
                    </select>
                </label>

                <div className="cursor-navigation">
                    <button
                        onClick={handleFirstPage}
                        disabled={currentCursor === null}
                    >
                        First
                    </button>
                    <button
                        onClick={handlePreviousPage}
                        disabled={currentCursor === null && cursorHistory.length === 0}
                    >
                        Previous
                    </button>
                    <span>
                        {currentCursor ?
                            `After: ${currentCursor.substring(0, 8)}...` :
                            'First Page'
                        }
                        {pagination?.totalElements && ` (${pagination.totalElements} total)`}
                    </span>
                    <button
                        onClick={handleNextPage}
                        disabled={!pagination?.hasNext}
                    >
                        Next
                    </button>
                </div>
            </div>

            <div className="players-results-section">
                {players.length === 0 ? (
                    <div>No players found</div>
                ) : (
                    players.map((player) => (
                        <div key={player.playerId} style={{display: "flex", gap: "1vh", padding: "0.5rem", borderBottom: "1px solid #ccc"}}>
                            <div><strong>ID:</strong> {player.playerId}</div>
                            <div><strong>Country:</strong> {player.birthCountry || 'N/A'}</div>
                            <div><strong>Name:</strong> {player.firstName || 'N/A'} {player.lastName || 'N/A'}</div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

export default PlayerResults;
