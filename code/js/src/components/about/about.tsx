import * as React from "react";
import { Button, Typography, Link, Box, Paper } from "@mui/material";
import EmailIcon from "@mui/icons-material/Email";

interface AuthorInfo {
    name: string;
    email: string;
    image: string;
}

const authors: AuthorInfo[] = [
    { name: "Tiago Silva", email: "a48252@alunos.isel.pt", image: "userImg.png" },
    { name: "Gonçalo Ribeiro", email: "a48305@alunos.isel.pt", image: "userImg.png" },
    { name: "Rosário Machado", email: "a46042@alunos.isel.pt", image: "userImg.png" },
];

const githubRepo = "https://github.com/isel-leic-daw/2024-daw-leic53d-g06-53d";

export const About: React.FC = () => {
    return (
        <Box sx={{ maxWidth: "900px", margin: "0 auto", textAlign: "center", padding: "40px" }}>
            <Typography variant="h3" gutterBottom sx={{ fontWeight: "bold", color: "#333" }}>
                About Us
            </Typography>

            <Box
                sx={{
                    display: "grid",
                    gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
                    gap: "30px",
                    justifyContent: "center",
                    marginTop: "30px",
                }}
            >
                {authors.map((creator, index) => (
                    <Paper
                        key={index}
                        elevation={3}
                        sx={{
                            padding: "20px",
                            borderRadius: "12px",
                            textAlign: "center",
                            backgroundColor: "#f9f9f9",
                        }}
                    >
                        <img
                            src={creator.image}
                            alt={creator.name}
                            style={{
                                width: "120px",
                                height: "120px",
                                borderRadius: "50%",
                                objectFit: "cover",
                                boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
                            }}
                        />
                        <Typography variant="h5" sx={{ fontWeight: "bold", marginTop: "10px" }}>
                            {creator.name}
                        </Typography>
                        <Button
                            variant="contained"
                            startIcon={<EmailIcon />}
                            href={`mailto:${creator.email}`}
                            sx={{ marginTop: "10px", textTransform: "none" }}
                        >
                            Email {creator.name}
                        </Button>
                    </Paper>
                ))}
            </Box>

            <Typography variant="body1" sx={{ marginTop: "60px", color: "#555" }}>
                This project can be found at{" "}
                <Link href={githubRepo} target="_blank" rel="noopener noreferrer" underline="hover">
                    our repository
                </Link>.
            </Typography>
        </Box>
    );
};
