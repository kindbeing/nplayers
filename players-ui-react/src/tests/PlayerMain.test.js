import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import PlayerMain from '../components/PlayerMain';
import {fetchData, fetchPlayerDetails} from "../utils/DataFetcher";

jest.mock("../utils/DataFetcher", () => ({
    fetchData: jest.fn(),
    fetchPlayerDetails: jest.fn()
}))

const fetchDataMock = fetchData;
const fetchPlayerDetailsMock = fetchPlayerDetails;

describe('PlayerMain', () => {
    const playersList = {
        players: [
            { playerId: "test01", firstName: "Test", lastName: "Player", birthCountry: "USA" }
        ]
    };
    const playerDetailsResponse = (playerId) => ({
        playerId: playerId,
        birthYear: "birthYear",
        birthMonth: "birthMonth",
        birthDay: "birthDay",
        birthCountry: "birthCountry",
        birthState: "birthState",
        birthCity: "birthCity",
        deathYear: "deathYear",
        deathMonth: "deathMonth",
        deathDay: "deathDay",
        deathCountry: "deathCountry",
        deathState: "deathState",
        deathCity: "deathCity",
        firstName: "firstName",
        lastName: "lastName",
        givenName: "givenName",
        weight: 200,
        height: 76,
        bats: "bats",
        throwStats: "throwStats",
        debut: "debut",
        finalGame: "finalGame",
        retroId: "retroId",
        bbrefId: "bbrefId",
        email: "email",
    })

    beforeEach(() => {
        fetchDataMock.mockResolvedValue(playersList);
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    test('renders main header', () => {
        render(<PlayerMain />);
        const element = screen.getByText("Hello Players");
        expect(element).toBeDefined();
    });

    test('fetches and displays player details when valid ID is entered', async () => {
        const testPlayerId = "testPlayer123";
        
        fetchPlayerDetailsMock.mockResolvedValue(playerDetailsResponse(testPlayerId));
        
        render(<PlayerMain />);
        await waitFor(() => {
            expect(fetchDataMock).toHaveBeenCalled();
        });
        
        const playerIdInput = screen.getByLabelText("Player id:");
        const submitButton = screen.getByRole("button", { name: /submit/i });
        
        await userEvent.type(playerIdInput, testPlayerId);
        
        await userEvent.click(submitButton);
        
        await waitFor(() => {
            expect(fetchPlayerDetailsMock).toHaveBeenCalledWith(testPlayerId);
        });
        
        // Assert player details appear on screen
        await waitFor(() => {
            expect(screen.getByText(`PlayerId: ${testPlayerId}`)).toBeInTheDocument();
        });
        expect(screen.getByText("FirstName: firstName")).toBeInTheDocument();
        expect(screen.getByText("LastName: lastName")).toBeInTheDocument();
    });

    test('shows loading state during player search', async () => {
        const testPlayerId = "testPlayer123";
        fetchPlayerDetailsMock.mockImplementation(() =>
            new Promise(resolve => setTimeout(() => resolve(playerDetailsResponse(testPlayerId)), 100))
        );

        render(<PlayerMain />);
        await waitFor(() => {
            expect(fetchDataMock).toHaveBeenCalled();
        });

        const playerIdInput = screen.getByLabelText("Player id:");
        const submitButton = screen.getByRole("button", { name: /submit/i });

        await userEvent.type(playerIdInput, testPlayerId);
        await userEvent.click(submitButton);

        // Check loading state appears
        expect(screen.getByText("Loading player details...")).toBeInTheDocument();

        // Wait for loading to complete
        await waitFor(() => {
            expect(fetchPlayerDetailsMock).toHaveBeenCalledWith(testPlayerId);
        });

        // Loading should disappear after fetch completes
        await waitFor(() => {
            expect(screen.queryByText("Loading player details...")).not.toBeInTheDocument();
        });
    });

    test('shows error when player ID is empty', async () => {
        render(<PlayerMain />);
        await waitFor(() => {
            expect(fetchDataMock).toHaveBeenCalled();
        });

        const submitButton = screen.getByRole("button", { name: /submit/i });

        // Click submit without entering any player ID
        await userEvent.click(submitButton);

        // Check error message appears
        expect(screen.getByText("Player ID is required")).toBeInTheDocument();
    });

    test('shows error when API call fails', async () => {
        const testPlayerId = "invalidPlayer";
        fetchPlayerDetailsMock.mockRejectedValue(new Error("API Error"));

        render(<PlayerMain />);
        await waitFor(() => {
            expect(fetchDataMock).toHaveBeenCalled();
        });

        const playerIdInput = screen.getByLabelText("Player id:");
        const submitButton = screen.getByRole("button", { name: /submit/i });

        await userEvent.type(playerIdInput, testPlayerId);
        await userEvent.click(submitButton);

        // Wait for error to appear
        await waitFor(() => {
            expect(screen.getByText("Player not found or error occurred")).toBeInTheDocument();
        });

        // Loading should disappear
        expect(screen.queryByText("Loading player details...")).not.toBeInTheDocument();
    });
});
