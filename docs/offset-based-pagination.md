# Offset-Based Pagination Implementation for Players API

## Overview
Implement traditional offset-based pagination for the `/v1/players` endpoint using `?page={number}&size={number}` query parameters. This approach uses SQL `LIMIT` and `OFFSET` to skip and retrieve records.

## Backend Changes

### Files to Add

#### 1. `player-service-backend/src/main/java/com/app/playerservicejava/model/PaginationMetadata.java`
```java
package com.app.playerservicejava.model;

public class PaginationMetadata {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;

    // Constructor, getters, setters
}
```

#### 2. `player-service-backend/src/main/java/com/app/playerservicejava/model/PaginatedPlayersResponse.java`
```java
package com.app.playerservicejava.model;

import java.util.List;

public class PaginatedPlayersResponse {
    private List<Player> players;
    private PaginationMetadata pagination;

    // Constructor, getters, setters
}
```

#### 3. `player-service-backend/src/test/java/com/app/playerservicejava/controller/PlayerControllerPaginationTest.java`
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlayerControllerPaginationTest {
    // Integration tests for pagination endpoints
    // Test page/size parameters, edge cases, invalid inputs
}
```

#### 4. `player-service-backend/src/test/java/com/app/playerservicejava/service/PlayerServicePaginationTest.java`
```java
@SpringBootTest
public class PlayerServicePaginationTest {
    // Unit tests for pagination service logic
}
```

### Files to Update

#### 1. `player-service-backend/src/main/java/com/app/playerservicejava/repository/PlayerRepository.java`
```java
public interface PlayerRepository extends JpaRepository<Player, String> {
    Page<Player> findAll(Pageable pageable);
}
```

#### 2. `player-service-backend/src/main/java/com/app/playerservicejava/service/PlayerService.java`
```java
@Service
public class PlayerService {
    // ... existing code ...

    public PaginatedPlayersResponse getPlayersPaginated(int page, int size) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Max page size

        Pageable pageable = PageRequest.of(page, size, Sort.by("playerId").ascending());
        Page<Player> playerPage = playerRepository.findAll(pageable);

        PaginationMetadata metadata = new PaginationMetadata(
            page,
            size,
            playerPage.getTotalElements(),
            playerPage.getTotalPages(),
            playerPage.hasNext(),
            playerPage.hasPrevious(),
            playerPage.isFirst(),
            playerPage.isLast()
        );

        return new PaginatedPlayersResponse(playerPage.getContent(), metadata);
    }
}
```

#### 3. `player-service-backend/src/main/java/com/app/playerservicejava/controller/PlayerController.java`
```java
@RestController
@RequestMapping(value = "v1/players", produces = { MediaType.APPLICATION_JSON_VALUE })
public class PlayerController {
    // ... existing code ...

    @GetMapping
    public ResponseEntity<PaginatedPlayersResponse> getPlayersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedPlayersResponse response = playerService.getPlayersPaginated(page, size);
        return ok(response);
    }

    // Keep existing getPlayerById method
}
```

## Frontend Changes

### Files to Update

#### 1. `players-ui-react/src/components/PlayersResults.jsx`
```jsx
import React, { useEffect, useState } from 'react';
import { fetchData } from "../utils/DataFetcher";

function PlayerResults() {
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [pagination, setPagination] = useState(null);

    useEffect(() => {
        fetchPlayers(currentPage, pageSize);
    }, [currentPage, pageSize]);

    const fetchPlayers = async (page, size) => {
        try {
            setLoading(true);
            const response = await fetch(`/v1/players?page=${page}&size=${size}`);
            if (!response.ok) throw new Error('Failed to fetch players');
            const data = await response.json();
            setPlayers(data.players);
            setPagination(data.pagination);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handlePageChange = (newPage) => {
        setCurrentPage(newPage);
    };

    const handlePageSizeChange = (newSize) => {
        setPageSize(newSize);
        setCurrentPage(0); // Reset to first page
    };

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;

    return (
        <div className="player-results">
            {/* Existing search UI */}

            <div className="pagination-controls">
                <label>Page Size:
                    <select value={pageSize} onChange={(e) => handlePageSizeChange(Number(e.target.value))}>
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={25}>25</option>
                        <option value={50}>50</option>
                    </select>
                </label>

                <div className="page-navigation">
                    <button
                        onClick={() => handlePageChange(currentPage - 1)}
                        disabled={!pagination?.hasPrevious}
                    >
                        Previous
                    </button>
                    <span>Page {currentPage + 1} of {pagination?.totalPages}</span>
                    <button
                        onClick={() => handlePageChange(currentPage + 1)}
                        disabled={!pagination?.hasNext}
                    >
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

#### 1. `players-ui-react/src/tests/PlayersResults.test.js`
```jsx
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import PlayerResults from "../components/PlayersResults";

// Mock fetch globally
global.fetch = jest.fn();

describe('PlayerResults Component', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe('Loading State', () => {
        test('renders loading text initially', () => {
            global.fetch.mockImplementation(() => new Promise(() => {}));
            render(<PlayerResults />);
            expect(screen.getByText("Loading...")).toBeInTheDocument();
        });
    });

    describe('Pagination Controls', () => {
        test('displays pagination metadata', async () => {
            const mockResponse = {
                players: [
                    { playerId: '1', birthCountry: 'USA' },
                    { playerId: '2', birthCountry: 'CAN' }
                ],
                pagination: {
                    page: 0,
                    size: 10,
                    totalElements: 25,
                    totalPages: 3,
                    hasNext: true,
                    hasPrevious: false,
                    isFirst: true,
                    isLast: false
                }
            };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockResponse)
            });

            render(<PlayerResults />);

            await waitFor(() => {
                expect(screen.getByText("Page 1 of 3")).toBeInTheDocument();
            });
        });

        test('next button is disabled on last page', async () => {
            const mockResponse = {
                players: [],
                pagination: {
                    hasNext: false,
                    hasPrevious: true,
                    totalPages: 2,
                    page: 1
                }
            };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockResponse)
            });

            render(<PlayerResults />);

            await waitFor(() => {
                const nextButton = screen.getByText("Next");
                expect(nextButton).toBeDisabled();
            });
        });

        test('changes page size and resets to page 0', async () => {
            const mockResponse = {
                players: [],
                pagination: { totalPages: 1, page: 0 }
            };

            global.fetch.mockResolvedValue(mockResponse);

            render(<PlayerResults />);

            const select = screen.getByDisplayValue("10");
            fireEvent.change(select, { target: { value: "25" } });

            await waitFor(() => {
                expect(global.fetch).toHaveBeenCalledWith('/v1/players?page=0&size=25');
            });
        });
    });

    // Additional tests for error states, player rendering, etc.
});
```

## Migration Steps

1. **Backend Implementation Order:**
   - Add new model classes (PaginationMetadata, PaginatedPlayersResponse)
   - Update PlayerRepository with Pageable support
   - Update PlayerService with pagination logic
   - Update PlayerController with new endpoint
   - Add comprehensive tests

2. **Frontend Implementation Order:**
   - Update PlayersResults component with pagination state and UI
   - Add pagination controls and event handlers
   - Update API calls to use pagination parameters
   - Add component tests

3. **Backward Compatibility:**
   - Keep existing `/v1/players/{id}` endpoint unchanged
   - Consider maintaining old `/v1/players` endpoint temporarily

## Testing Strategy

- **Integration Tests:** Full API request/response cycles with various page/size combinations
- **Unit Tests:** Service layer pagination logic, edge cases (negative pages, oversized pages)
- **Component Tests:** UI interactions, pagination controls, loading states
- **Performance Tests:** Large dataset pagination, memory usage

## Benefits

- **Scalability:** Handle large datasets efficiently
- **User Experience:** Faster loading, predictable navigation
- **Resource Efficiency:** Reduced memory usage and bandwidth
- **Standard Pattern:** Widely adopted pagination approach
