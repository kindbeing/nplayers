import logo from '../assets/logo.svg';
import '../styling/PlayersMain.css';
import PlayerResults from "./PlayersResults";
import Users from "./Users";

function PlayerMain() {
    return (
        <div className="players-main">
            <header className="players-header">
                <img src={logo} className="players-logo" alt="logo" />
                <p>
                    Hello Players
                </p>
            </header>
            <PlayerResults/>
            {/*<Users/>*/}
        </div>
    );
}

export default PlayerMain;
