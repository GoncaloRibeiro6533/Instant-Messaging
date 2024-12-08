import { useData } from '../../components/data/DataProvider';
import { useAuth } from '../../components/auth/AuthProvider';
import { useNavigate } from 'react-router-dom';

export async function handleResponse(response: Response): Promise<any> { 
    try{
        if (response.ok) {
        return await response.json();
    
    } else if(response.headers.get('Content-Type') === 'application/problem+json') {
        const problem = await response.json();
        const error = problem.title.split('-')
        const title = error[0].charAt(0).toUpperCase() + error[0].slice(1) + ' ' +error.slice(1).join(' ')
        throw new Error(title)
    } else if(response.headers.get('Authorization') === "WWW-Authenticate") {
       /* const navigate = useNavigate()
        const [user, setUser] = useAuth()
        const { clear } = useData()
        localStorage.clear()
        setUser(undefined)
        clear()
        navigate('/login')*/
        throw new Error('Not authenticated')
    }
    else if(response.status === 401) {
        /*const navigate = useNavigate()
        const [user, setUser] = useAuth()
        const { clear } = useData()
        localStorage.clear()
        setUser(undefined)
        clear()
        navigate('/login')*/
        throw new Error('Session expired') 
    }
    else throw new Error('Failed to fetch')
} catch(e) {
        if(e.message === "Failed to fetch") {
            throw new Error('Failed to connect to the server')
        } else throw e
    }
}

