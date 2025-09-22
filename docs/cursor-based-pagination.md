# Cursor-Based Pagination Implementation for Players API

## Overview
Implement cursor-based pagination for the `/v1/players` endpoint using `?cursor={playerId}&limit={number}&direction={next|prev}` query parameters. This approach uses the `playerId` as a cursor to retrieve records after/before a specific point, providing better performance and consistency for large datasets.

## Backend Changes

### Files to Add

#### 1. `player-service-backend/src/main/java/com/app/playerservicejava/model/CursorPaginationMetadata.java`
```java
package com.app.playerservicejava.model;

public class CursorPaginationMetadata {
    private String nextCursor;
    private String previousCursor;
    private int limit;
    private boolean hasNext;
    private boolean hasPrevious;
    private long totalElements; // Optional: for display purposes

    // Constructor, getters, setters
}
```

#### 2. `player-service-backend/src/main/java/com/app/playerservicejava/model/CursorPaginatedPlayersResponse.java`
```java
package com.app.playerservicejava.model;

import java.util.List;

public class CursorPaginatedPlayersResponse {
    private List<Player> players;
    private CursorPaginationMetadata pagination;

    // Constructor, getters, setters
}
```

#### 3. `player-service-backend/src/main/java/com/app/playerservicejava/repository/PlayerRepository.java` (add to existing)
```java
public interface PlayerRepository extends JpaRepository<Player, String> {
    // Existing methods...

    // For cursor-based pagination
    List<Player> findByPlayerIdGreaterThanOrderByPlayerIdAsc(String playerId, Pageable pageable);
    List<Player> findByPlayerIdLessThanOrderByPlayerIdDesc(String playerId, Pageable pageable);
    List<Player> findAllByOrderByPlayerIdAsc(Pageable pageable);

    // For cursor validation
    boolean existsByPlayerId(String playerId);

    // For total count (optional)
    long count();
}
```

#### 4. `player-service-backend/src/test/java/com/app/playerservicejava/controller/PlayerControllerCursorPaginationTest.java`
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlayerControllerCursorPaginationTest {
    // Integration tests for cursor pagination
    // Test cursor/limit parameters, direction handling, invalid cursors
}
```

#### 5. `player-service-backend/src/test/java/com/app/playerservicejava/service/PlayerServiceCursorPaginationTest.java`
```java
@SpringBootTest
public class PlayerServiceCursorPaginationTest {
    // Unit tests for cursor pagination service logic
}
```

### Files to Update

#### 1. `player-service-backend/src/main/java/com/app/playerservicejava/service/PlayerService.java`
```java
@Service
public class PlayerService {
    // ... existing code ...

    public CursorPaginatedPlayersResponse getPlayersCursorPaginated(String cursor, int limit, String direction) {
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        List<Player> players;
        String nextCursor = null;
        String previousCursor = null;

        Pageable pageable = PageRequest.of(0, limit + 1); // +1 to check if there are more records

        if (cursor == null || cursor.trim().isEmpty()) {
            // First page
            players = playerRepository.findAllByOrderByPlayerIdAsc(pageable);
        } else {
            // Validate cursor exists
            if (!playerRepository.existsByPlayerId(cursor)) {
                throw new IllegalArgumentException("Invalid cursor: " + cursor);
            }

            if ("prev".equalsIgnoreCase(direction)) {
                // Previous page: get records before cursor in reverse order, then reverse the list
                List<Player> tempPlayers = playerRepository.findByPlayerIdLessThanOrderByPlayerIdDesc(cursor, pageable);
                Collections.reverse(tempPlayers);
                players = tempPlayers;
            } else {
                // Next page (default): get records after cursor
                players = playerRepository.findByPlayerIdGreaterThanOrderByPlayerIdAsc(cursor, pageable);
            }
        }

        // Check if there are more records and set cursors
        boolean hasMore = players.size() > limit;
        if (hasMore) {
            // Remove the extra record used for checking
            Player lastPlayer = players.remove(players.size() - 1);
            nextCursor = lastPlayer.getPlayerId();
        }

        // Set previous cursor if not first page
        if (cursor != null && !players.isEmpty()) {
            previousCursor = players.get(0).getPlayerId();
        }

        CursorPaginationMetadata metadata = new CursorPaginationMetadata(
            nextCursor,
            previousCursor,
            limit,
            hasMore,
            cursor != null && !"prev".equalsIgnoreCase(direction),
            playerRepository.count() // Optional
        );

        return new CursorPaginatedPlayersResponse(players, metadata);
    }
}
```

#### 2. `player-service-backend/src/main/java/com/app/playerservicejava/controller/PlayerController.java`
```java
@RestController
@RequestMapping(value = "v1/players", produces = { MediaType.APPLICATION_JSON_VALUE })
public class PlayerController {
    // ... existing code ...

    @GetMapping("/cursor")
    public ResponseEntity<CursorPaginatedPlayersResponse> getPlayersCursorPaginated(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "next") String direction) {

        try {
            CursorPaginatedPlayersResponse response = playerService.getPlayersCursorPaginated(cursor, limit, direction);
            return ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Keep existing endpoints
}
```

## Frontend Changes

### Files to Update

#### 1. `players-ui-react/src/components/PlayersResults.jsx`
```jsx
import React, { useEffect, useState } from 'react';

function PlayerResults() {
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentCursor, setCurrentCursor] = useState(null);
    const [pageSize, setPageSize] = useState(10);
    const [pagination, setPagination] = useState(null);
    const [cursorHistory, setCursorHistory] = useState([]); // For back navigation

    useEffect(() => {
        fetchPlayers(currentCursor, pageSize, null);
    }, []);

    const fetchPlayers = async (cursor, limit, direction) => {
        try {
            setLoading(true);
            const params = new URLSearchParams();
            if (cursor) params.append('cursor', cursor);
            params.append('limit', limit);
            if (direction) params.append('direction', direction);

            const response = await fetch(`/v1/players/cursor?${params}`);
            if (!response.ok) throw new Error('Failed to fetch players');
            const data = await response.json();

            setPlayers(data.players);
            setPagination(data.pagination);

            // Update cursor history for navigation
            if (direction === 'next' && data.pagination.nextCursor) {
                setCursorHistory(prev => [...prev, cursor].filter(Boolean));
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleNextPage = () => {
        if (pagination?.hasNext && pagination?.nextCursor) {
            setCurrentCursor(pagination.nextCursor);
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

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;

    return (
        <div className="player-results">
            {/* Existing search UI */}

            <div className="cursor-pagination-controls">
                <label>Page Size:
                    <select value={pageSize} onChange={(e) => handlePageSizeChange(Number(e.target.value))}>
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={25}>25</option>
                        <option value={50}>50</option>
                    </select>
                </label>

                <div className="cursor-navigation">
                    <button onClick={handleFirstPage} disabled={currentCursor === null}>
                        First
                    </button>
                    <button onClick={handlePreviousPage} disabled={currentCursor === null && cursorHistory.length === 0}>
                        Previous
                    </button>
                    <span>
                        {currentCursor ? `After: ${currentCursor.substring(0, 8)}...` : 'First Page'}
                        {pagination?.totalElements && ` (${pagination.totalElements} total)`}
                    </span>
                    <button onClick={handleNextPage} disabled={!pagination?.hasNext}>
                        Next
                    </button>
                </div>
            </div>

            <div className="players-results-section">
                {players.map((player) => (
                    <div key={player.playerId} style={{display: "flex", gap: "1vh"}}>
                        <div>{player.playerId}</div>
                        <div>{player.birthCountry}</div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default PlayerResults;
```

### Files to Add

#### 1. `players-ui-react/src/tests/PlayersResultsCursor.test.js`
```jsx
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import PlayerResults from "../components/PlayersResults";

// Mock fetch globally
global.fetch = jest.fn();

describe('PlayerResults Cursor Pagination', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe('Cursor Navigation', () => {
        test('loads first page on initial render', async () => {
            const mockResponse = {
                players: [
                    { playerId: 'player001', birthCountry: 'USA' },
                    { playerId: 'player002', birthCountry: 'CAN' }
                ],
                pagination: {
                    nextCursor: 'player010',
                    previousCursor: null,
                    limit: 10,
                    hasNext: true,
                    hasPrevious: false,
                    totalElements: 1000
                }
            };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockResponse)
            });

            render(<PlayerResults />);

            await waitFor(() => {
                expect(global.fetch).toHaveBeenCalledWith('/v1/players/cursor?limit=10');
            });

            expect(screen.getByText("First Page (1000 total)")).toBeInTheDocument();
        });

        test('navigates to next page using cursor', async () => {
            const firstPageResponse = {
                players: [{ playerId: 'player001' }],
                pagination: { nextCursor: 'player010', hasNext: true }
            };

            const secondPageResponse = {
                players: [{ playerId: 'player011' }],
                pagination: { nextCursor: 'player020', previousCursor: 'player001', hasNext: true }
            };

            global.fetch
                .mockResolvedValueOnce({
                    ok: true,
                    json: () => Promise.resolve(firstPageResponse)
                })
                .mockResolvedValueOnce({
                    ok: true,
                    json: () => Promise.resolve(secondPageResponse)
                });

            render(<PlayerResults />);

            // Wait for first page to load
            await waitFor(() => {
                expect(screen.getByText("First Page")).toBeInTheDocument();
            });

            // Click next
            const nextButton = screen.getByText("Next");
            fireEvent.click(nextButton);

            await waitFor(() => {
                expect(global.fetch).toHaveBeenCalledWith('/v1/players/cursor?cursor=player010&limit=10&direction=next');
            });
        });

        test('navigates back using cursor history', async () => {
            // Setup initial state with cursor history
            // Test back navigation logic
        });
    });

    describe('Error Handling', () => {
        test('handles invalid cursor gracefully', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: false,
                status: 400
            });

            render(<PlayerResults />);

            await waitFor(() => {
                expect(screen.getByText("Error: Failed to fetch players")).toBeInTheDocument();
            });
        });
    });

    // Additional tests for page size changes, boundary conditions, etc.
});
```

## Migration Steps

1. **Backend Implementation Order:**
   - Add new model classes (CursorPaginationMetadata, CursorPaginatedPlayersResponse)
   - Update PlayerRepository with cursor-based query methods
   - Update PlayerService with cursor pagination logic
   - Update PlayerController with new `/cursor` endpoint
   - Add comprehensive tests

2. **Frontend Implementation Order:**
   - Update PlayersResults component with cursor state management
   - Add cursor navigation UI and history tracking
   - Update API calls to use cursor endpoint
   - Add component tests for cursor navigation

3. **API Evolution Strategy:**
   - Keep existing `/v1/players` endpoint unchanged
   - Add new `/v1/players/cursor` endpoint for cursor pagination
   - Consider deprecating old endpoint after frontend migration

## Testing Strategy

- **Integration Tests:** Full API request/response cycles with cursor navigation, invalid cursors, boundary conditions
- **Unit Tests:** Cursor logic validation, edge cases (invalid cursors, direction handling)
- **Component Tests:** Cursor navigation UI, history management, loading states
- **Performance Tests:** Deep pagination scenarios, memory usage with large cursor histories

## Benefits

- **Performance:** Consistent query performance regardless of page depth
- **Real-time Safety:** Immune to data insertions/deletions affecting pagination
- **Scalability:** Better for large datasets with frequent navigation
- **Modern Approach:** Preferred for APIs serving mobile apps and SPAs

## Comparison with Offset-Based

| Aspect | Offset-Based | Cursor-Based |
|--------|-------------|--------------|
| Performance | Degrades with page depth | Consistent performance |
| Data Consistency | Affected by insertions/deletions | Immune to data changes |
| Implementation | Simpler | More complex |
| Use Case | Small datasets, admin interfaces | Large datasets, user-facing apps |

