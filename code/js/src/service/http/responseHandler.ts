import { useData } from '../../components/data/DataProvider';
import { useAuth } from '../../components/auth/AuthProvider';

export async function handleResponse(response: Response): Promise<any> { 
    if (response.ok) {
        return await response.json();
    } else if(response.headers.get('Content-Type') === 'application/problem+json') {
        const problem = await response.json();
        const error = problem.title.split('-')
        const title = error[0].charAt(0).toUpperCase() + error[0].slice(1) + ' ' +error.slice(1).join(' ')
        throw new Error(title)
    } else if(response.headers.get('Authorization') === "WWW-Authenticate") {
        const [user, setUser] = useAuth()
        const { clear } = useData()
        localStorage.clear()
        setUser(undefined)
        clear()
        throw new Error('Unauthorized')
    }
}

