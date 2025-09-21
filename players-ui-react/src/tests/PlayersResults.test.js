import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import PlayerResults from "../components/PlayersResults";
import { fetchPlayersCursorPaginated } from "../utils/DataFetcher";

// Mock the DataFetcher module
jest.mock("../utils/DataFetcher", () => ({
    fetchPlayersCursorPaginated: jest.fn(),
}));

const mockFetchPlayersCursorPaginated = fetchPlayersCursorPaginated;

describe('PlayerResults Cursor Pagination Component', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe('Loading State', () => {
        test('renders loading text initially while fetching data', async () => {
            // Arrange
            mockFetchPlayersCursorPaginated.mockImplementation(() => new Promise(() => {})); // Never resolves

            // Act
            render(<PlayerResults />);

            // Assert
            expect(screen.getByText("Loading players...")).toBeInTheDocument();
            expect(mockFetchPlayersCursorPaginated).toHaveBeenCalledTimes(1);
            expect(mockFetchPlayersCursorPaginated).toHaveBeenCalledWith(null, 10, null);
        });
    });

    describe('Error State', () => {
        test('renders error message when fetch fails', async () => {
            // Arrange
            const errorMessage = "Network error occurred";
            mockFetchPlayersCursorPaginated.mockRejectedValue(new Error(errorMessage));

            // Act
            render(<PlayerResults />);

            // Assert
            await waitFor(() => {
                expect(screen.getByText(`Error: ${errorMessage}`)).toBeInTheDocument();
            });
            expect(screen.queryByText("Loading players...")).not.toBeInTheDocument();
        });

        test('renders error message for HTTP errors', async () => {
            // Arrange
            const httpError = new Error("HTTP error: 500 Internal Server Error");
            mockFetchPlayersCursorPaginated.mockRejectedValue(httpError);

            // Act
            render(<PlayerResults />);

            // Assert
            await waitFor(() => {
                expect(screen.getByText(`Error: HTTP error: 500 Internal Server Error`)).toBeInTheDocument();
            });
        });
    });

    describe('Success State', () => {
        test('renders players table with data when fetch succeeds', async () => {
            // Arrange
            const mockResponse = {
                players: [
                    {
                        playerId: 'player001',
                        birthCountry: 'USA',
                        firstName: 'John',
                        lastName: 'Doe'
                    },
                    {
                        playerId: 'player002',
                        birthCountry: 'CAN',
                        firstName: 'Jane',
                        lastName: 'Smith'
                    }
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

            mockFetchPlayersCursorPaginated.mockResolvedValue(mockResponse);

            // Act
            render(<PlayerResults />);

            // Assert
            await waitFor(() => {
                expect(screen.queryByText("Loading players...")).not.toBeInTheDocument();
            });

            // Check header
            expect(screen.getByText("Players")).toBeInTheDocument();

            // Check player data rendering
            expect(screen.getByText(/player001/)).toBeInTheDocument();
            expect(screen.getByText(/USA/)).toBeInTheDocument();
            expect(screen.getByText(/John/)).toBeInTheDocument();
            expect(screen.getByText(/Doe/)).toBeInTheDocument();

            expect(screen.getByText(/player002/)).toBeInTheDocument();
            expect(screen.getByText(/CAN/)).toBeInTheDocument();
            expect(screen.getByText(/Jane/)).toBeInTheDocument();
            expect(screen.getByText(/Smith/)).toBeInTheDocument();

            // Check pagination info
            expect(screen.getByText(/First Page/)).toBeInTheDocument();
            expect(screen.getByText(/1000 total/)).toBeInTheDocument();
        });

        test('renders empty state when no players returned', async () => {
            // Arrange
            const mockResponse = {
                players: [],
                pagination: {
                    nextCursor: null,
                    previousCursor: null,
                    limit: 10,
                    hasNext: false,
                    hasPrevious: false,
                    totalElements: 0
                }
            };

            mockFetchPlayersCursorPaginated.mockResolvedValue(mockResponse);

            // Act
            render(<PlayerResults />);

            // Assert
            await waitFor(() => {
                expect(screen.getByText("No players found")).toBeInTheDocument();
            });
        });
    });

    describe('Pagination Controls', () => {
        test('displays pagination controls with proper state', async () => {
            // Arrange
            const mockResponse = {
                players: [{ playerId: 'player001', birthCountry: 'USA' }],
                pagination: {
                    nextCursor: 'player010',
                    previousCursor: null,
                    limit: 10,
                    hasNext: true,
                    hasPrevious: false,
                    totalElements: 25
                }
            };

            mockFetchPlayersCursorPaginated.mockResolvedValue(mockResponse);

            // Act
            render(<PlayerResults />);

            // Assert
            await waitFor(() => {
                expect(screen.getByText(/First Page/)).toBeInTheDocument();
                expect(screen.getByText(/25 total/)).toBeInTheDocument();
            });

            // Check navigation buttons
            const firstButton = screen.getByText("First");
            const previousButton = screen.getByText("Previous");
            const nextButton = screen.getByText("Next");

            expect(firstButton).toBeDisabled();
            expect(previousButton).toBeDisabled();
            expect(nextButton).not.toBeDisabled();
        });

        test('next button navigates to next page using cursor', async () => {
            // Arrange - First page response
            const firstPageResponse = {
                players: [{ playerId: 'player001', birthCountry: 'USA' }],
                pagination: {
                    nextCursor: 'player010',
                    previousCursor: null,
                    limit: 10,
                    hasNext: true,
                    hasPrevious: false,
                    totalElements: 25
                }
            };

            // Second page response
            const secondPageResponse = {
                players: [{ playerId: 'player011', birthCountry: 'CAN' }],
                pagination: {
                    nextCursor: 'player020',
                    previousCursor: 'player001',
                    limit: 10,
                    hasNext: true,
                    hasPrevious: true,
                    totalElements: 25
                }
            };

            mockFetchPlayersCursorPaginated
                .mockResolvedValueOnce(firstPageResponse)
                .mockResolvedValueOnce(secondPageResponse);

            // Act
            render(<PlayerResults />);

            // Wait for first page to load
            await waitFor(() => {
                expect(screen.getByText(/First Page/)).toBeInTheDocument();
            });

            // Click next button
            const nextButton = screen.getByText("Next");
            fireEvent.click(nextButton);

            // Assert - should call API with cursor and direction
            await waitFor(() => {
                expect(mockFetchPlayersCursorPaginated).toHaveBeenCalledWith('player010', 10, 'next');
            });

            // Check that cursor history shows the navigation
            expect(screen.getByText(/After: player01/)).toBeInTheDocument();
        });

        test('previous button navigates back using cursor history', async () => {
            // Arrange - simulate being on second page
            const firstPageResponse = {
                players: [{ playerId: 'player001', birthCountry: 'USA' }],
                pagination: {
                    nextCursor: 'player010',
                    previousCursor: null,
                    limit: 10,
                    hasNext: true,
                    hasPrevious: false,
                    totalElements: 25
                }
            };

            const secondPageResponse = {
                players: [{ playerId: 'player011', birthCountry: 'CAN' }],
                pagination: {
                    nextCursor: 'player020',
                    previousCursor: 'player001',
                    limit: 10,
                    hasNext: true,
                    hasPrevious: true,
                    totalElements: 25
                }
            };

            mockFetchPlayersCursorPaginated
                .mockResolvedValueOnce(firstPageResponse)
                .mockResolvedValueOnce(secondPageResponse);

            render(<PlayerResults />);

            // Navigate to second page first
            await waitFor(() => {
                expect(screen.getByText(/First Page/)).toBeInTheDocument();
            });

            const nextButton = screen.getByText("Next");
            fireEvent.click(nextButton);

            await waitFor(() => {
                expect(screen.getByText(/After: player01/)).toBeInTheDocument();
            });

            // Now test going back
            mockFetchPlayersCursorPaginated.mockResolvedValueOnce(firstPageResponse); // Back to first page
            const previousButton = screen.getByText("Previous");
            fireEvent.click(previousButton);

            // Assert - should call API with previous cursor and 'prev' direction
            await waitFor(() => {
                expect(mockFetchPlayersCursorPaginated).toHaveBeenCalledWith('player001', 10, 'prev');
            });
        });

        test('changes page size and resets to first page', async () => {
            // Arrange
            const mockResponse = {
                players: [],
                pagination: { totalElements: 25, hasNext: false }
            };

            mockFetchPlayersCursorPaginated.mockResolvedValue(mockResponse);

            // Act
            render(<PlayerResults />);

            // Wait for component to load and select to be available
            await waitFor(() => {
                expect(screen.getByText("Players")).toBeInTheDocument();
            });

            // Change page size
            const select = screen.getByRole("combobox");
            fireEvent.change(select, { target: { value: "25" } });

            // Assert - should reset to first page with new size
            await waitFor(() => {
                expect(mockFetchPlayersCursorPaginated).toHaveBeenCalledWith(null, 25, null);
            });
        });

        test('first button resets to first page', async () => {
            // Arrange - simulate being on a later page
            const firstPageResponse = {
                players: [{ playerId: 'player001', birthCountry: 'USA' }],
                pagination: {
                    nextCursor: 'player010',
                    previousCursor: null,
                    limit: 10,
                    hasNext: true,
                    hasPrevious: false,
                    totalElements: 25
                }
            };

            const secondPageResponse = {
                players: [{ playerId: 'player011', birthCountry: 'CAN' }],
                pagination: {
                    nextCursor: 'player020',
                    previousCursor: 'player001',
                    limit: 10,
                    hasNext: true,
                    hasPrevious: true,
                    totalElements: 25
                }
            };

            mockFetchPlayersCursorPaginated
                .mockResolvedValueOnce(firstPageResponse)
                .mockResolvedValueOnce(secondPageResponse);

            render(<PlayerResults />);

            // Navigate to second page
            await waitFor(() => {
                expect(screen.getByText(/First Page/)).toBeInTheDocument();
            });

            fireEvent.click(screen.getByText("Next"));

            await waitFor(() => {
                expect(screen.getByText(/After: player01/)).toBeInTheDocument();
            });

            // Click First button
            mockFetchPlayersCursorPaginated.mockResolvedValueOnce(firstPageResponse);
            const firstButton = screen.getByText("First");
            fireEvent.click(firstButton);

            // Assert - should go back to first page
            await waitFor(() => {
                expect(mockFetchPlayersCursorPaginated).toHaveBeenCalledWith(null, 10, null);
            });
        });
    });

    describe('Search Functionality', () => {
        test('search buttons exist and are clickable', async () => {
            // Arrange
            const mockResponse = {
                players: [],
                pagination: { hasNext: false, totalElements: 0 }
            };

            mockFetchPlayersCursorPaginated.mockResolvedValue(mockResponse);

            // Act
            render(<PlayerResults />);

            // Assert
            await waitFor(() => {
                const searchButtons = screen.getAllByText("Search");
                expect(searchButtons).toHaveLength(2);

                // Check labels exist
                expect(screen.getByText("Player ID:")).toBeInTheDocument();
                expect(screen.getByText("Country Code:")).toBeInTheDocument();
            });
        });
    });

    describe('Component Structure', () => {
        test('renders with proper semantic structure', async () => {
            // Arrange
            const mockResponse = {
                players: [{ playerId: 'player001', birthCountry: 'USA' }],
                pagination: {
                    nextCursor: null,
                    previousCursor: null,
                    limit: 10,
                    hasNext: false,
                    hasPrevious: false,
                    totalElements: 1
                }
            };

            mockFetchPlayersCursorPaginated.mockResolvedValue(mockResponse);

            // Act
            render(<PlayerResults />);

            // Assert
            await waitFor(() => {
                // Check main sections exist
                expect(screen.getByText("Players")).toBeInTheDocument();
                expect(screen.getAllByText("Search")).toHaveLength(2);

                // Check pagination controls exist
                expect(screen.getByText("Page Size:")).toBeInTheDocument();
                expect(screen.getByText("First")).toBeInTheDocument();
                expect(screen.getByText("Previous")).toBeInTheDocument();
                expect(screen.getByText("Next")).toBeInTheDocument();
            });
        });
    });
});
