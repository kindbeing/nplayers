import {useEffect, useState} from "react";
import {fetchUserData} from "../utils/DataFetcher";

export default function Users() {
    const [users, setUsers] = useState([])
    const [isLoading, setIsLoading] = useState(true)
    const [hasError, setHasError] = useState("")

    useEffect(() => {
        fetchUserData()
            .then((data) => {
                setUsers(data.users)
                setIsLoading(false)
            })
            .catch((error) => {
                console.log(error.message)
                setIsLoading(false)
                setHasError(error.message)
            })
    }, []);

    if (isLoading) {
        return <div>Loading...</div>
    }

    if (hasError) {
        return <div>There was an error: {hasError}. Please try again!</div>
    }

    return <div> Users
        {
            <table className="flex">
                <thead>
                <tr>
                    <th>userId</th>
                    <th>email</th>
                    <th>fullName</th>
                    <th>age</th>
                    <th>address</th>
                </tr>
                </thead>
                <tbody>
                {users.map((user) => {
                    return <tr key={user.userId}>
                        <td>{user.userId}</td>
                        <td>{user.email}</td>
                        <td>{user.fullName}</td>
                        <td>{user.age}</td>
                        <td>{user.address}</td>
                    </tr>
                })}
                </tbody>
            </table>
        }
    </div>
}