import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Users from "../components/Users";
import { fetchUserData } from "../utils/DataFetcher";

// Mock the DataFetcher module
jest.mock("../utils/DataFetcher", () => ({
    fetchUserData: jest.fn()
}))

const mockFetchUserData = fetchUserData;

describe('Users Component', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe('Loading State', () => {
        test('renders loading text initially while fetching data', async () => {
            // Arrange
            mockFetchUserData.mockImplementation(() => new Promise(() => {})); // Never resolves

            // Act
            render(<Users />);

            // Assert
            expect(screen.getByText("Loading...")).toBeInTheDocument();
            expect(mockFetchUserData).toHaveBeenCalledTimes(1);
        });
    });

    describe('Error State', () => {
        test('renders error message when fetchUserData throws an error', async () => {
            // Arrange
            const errorMessage = "Network error occurred";
            mockFetchUserData.mockRejectedValue(new Error(errorMessage));

            // Act
            render(<Users />);

            // Assert
            await waitFor(() => {
                expect(screen.getByText(`There was an error: ${errorMessage}. Please try again!`)).toBeInTheDocument();
            });
            expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
        });

        test('renders error message for HTTP errors', async () => {
            // Arrange
            const httpError = new Error("HTTP error: 500 Internal Server Error");
            mockFetchUserData.mockRejectedValue(httpError);

            // Act
            render(<Users />);

            // Assert
            await waitFor(() => {
                expect(screen.getByText(`There was an error: HTTP error: 500 Internal Server Error. Please try again!`)).toBeInTheDocument();
            });
        });
    });

    describe('Success State', () => {
        test('renders users table with data when fetch succeeds', async () => {
            // Arrange
            const mockUsers = [
                {
                    userId: 1,
                    email: "john.doe@example.com",
                    fullName: "John Doe",
                    age: 30,
                    address: "123 Main St"
                },
                {
                    userId: 2,
                    email: "jane.smith@example.com",
                    fullName: "Jane Smith",
                    age: 25,
                    address: "456 Oak Ave"
                }
            ];

            mockFetchUserData.mockResolvedValue({ users: mockUsers });

            // Act
            render(<Users />);

            // Assert
            await waitFor(() => {
                expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
            });

            // Check main header
            expect(screen.getByText("Users")).toBeInTheDocument();

            // Check table headers
            expect(screen.getByText("userId")).toBeInTheDocument();
            expect(screen.getByText("email")).toBeInTheDocument();
            expect(screen.getByText("fullName")).toBeInTheDocument();
            expect(screen.getByText("age")).toBeInTheDocument();
            expect(screen.getByText("address")).toBeInTheDocument();

            // Check user data rendering
            expect(screen.getByText("1")).toBeInTheDocument();
            expect(screen.getByText("john.doe@example.com")).toBeInTheDocument();
            expect(screen.getByText("John Doe")).toBeInTheDocument();
            expect(screen.getByText("30")).toBeInTheDocument();
            expect(screen.getByText("123 Main St")).toBeInTheDocument();

            expect(screen.getByText("2")).toBeInTheDocument();
            expect(screen.getByText("jane.smith@example.com")).toBeInTheDocument();
            expect(screen.getByText("Jane Smith")).toBeInTheDocument();
            expect(screen.getByText("25")).toBeInTheDocument();
            expect(screen.getByText("456 Oak Ave")).toBeInTheDocument();
        });

        test('renders empty table when users array is empty', async () => {
            // Arrange
            mockFetchUserData.mockResolvedValue({ users: [] });

            // Act
            render(<Users />);

            // Assert
            await waitFor(() => {
                expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
            });

            // Check main header still renders
            expect(screen.getByText("Users")).toBeInTheDocument();

            // Check table headers are present but no data rows
            expect(screen.getByText("userId")).toBeInTheDocument();
            expect(screen.queryByText("1")).not.toBeInTheDocument();
        });
    });
});
