import * as React from "react"
import * as ReactDom from 'react-dom/client'
import Button from '@mui/material/Button'
import EmailIcon from '@mui/icons-material/Email'
import Link from '@mui/material/Link'

interface AuthorInfo {
    name: string
    email: string
    image: string
}

const authors: AuthorInfo[] = [
    {
        name: "Tiago Silva",
        email: "a48252@alunos.isel.pt",
        image: "userImg.png"
    },
    {
        name: "Gonçalo Ribeiro",
        email: "a48305@alunos.isel.pt",
        image: "userImg.png"
    },
    {
        name: "Rosário Machado",
        email: "a46042@alunos.isel.pt",
        image: "userImg.png"
    }
]

const githubRepo = "https://github.com/isel-leic-daw/2024-daw-leic53d-g06-53d"


export const About: React.FC = () => {
    return (
        <div>
            <h1>About Us</h1>
            <div style={{ display: "flex", justifyContent: "space-around" }}>
                {authors.map((creator, index) => (
                    <div key={index} style={{ textAlign: "center" }}>
                        <img src={creator.image} alt={creator.name} style={{ width: "150px", height: "150px", borderRadius: "50%" }} />
                        <h2>{creator.name}</h2>
                        <a href={`mailto:${creator.email}`}>
                            <Button variant="contained" startIcon={<EmailIcon />} sx={{ textTransform: 'none' }}>
                                Email {creator.name}
                            </Button>
                        </a>
                    </div>
                ))}
            </div>

            <div style={{textAlign: "center", marginTop: "120px"}}>
                <p>This project can be found at <Link href={githubRepo} target="_blank" rel="noopener noreferrer" underline="always">
                    our repository</Link>.</p>
            </div>
        </div>
    )
}

