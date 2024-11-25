import * as React from "react";
import * as ReactDom from 'react-dom/client'

const root = ReactDom.createRoot(document.getElementById('container'))


const AboutElem: React.FC = () => {
    const creators = [
        { name: "Tiago Silva", email: "a48252@alunos.isel.pt", photo: "photo1.jpg" },
        { name: "Gonçalo Ribeiro", email: "a48xxx@alunos.isel.pt", photo: "photo2.jpg" },
        { name: "Rosário Machado", email: "a48xxx@alunos.isel.pt", photo: "photo3.jpg" },
    ];

    return (
        <div>
            <h1>About Us</h1>
            <div>
                <a href="https://github.com/isel-leic-daw/2024-daw-leic53d-g06-53d" target="_blank" rel="noopener noreferrer">
                    Our Repository
                </a>
            </div>
            <div style={{ display: "flex", justifyContent: "space-around" }}>
                {creators.map((creator, index) => (
                    <div key={index} style={{ textAlign: "center" }}>
                        <img src={creator.photo} alt={creator.name} style={{ width: "150px", height: "150px", borderRadius: "50%" }} />
                        <h2>{creator.name}</h2>
                        <a href={`mailto:${creator.email}`}>
                            <button>Email {creator.name}</button>
                        </a>
                    </div>
                ))}
            </div>
        </div>
    );
};

export function about(){
    root.render(
        <div>
            <AboutElem></AboutElem>
        </div>

    )
}