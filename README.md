# TalkRooms

**TalkRooms** is a web application that facilitates real-time group chats through channels. It provides a seamless messaging experience by integrating a REST API developed in Kotlin with Spring and a modern front-end built with TypeScript, React, and Material-UI (MUI).

## Introduction

TalkRooms allows users to exchange messages in real-time through dedicated channels. Whether you're collaborating with a team or catching up with friends, TalkRooms makes group chats easy and efficient.

## Functionalities

- **User Authentication**
  - Secure login and registration.
  - Session management using token-based authentication.

- **Messaging Channels**
  - Real-time message exchange through a robust REST API.
  - Support for multiple channels.
  - Persistent message history for offline access.

- **Profile Management**
  - View and update user profiles.

- **Channel Invitations**
  - Manage invitations by accepting or rejecting requests to join channels.

- **Notifications**
  - Instant notifications for new messages and channel updates.

## Technologies

- **Backend**
  - [Kotlin](https://kotlinlang.org/)
  - [Spring Framework](https://spring.io/)
  
- **Frontend**
  - [TypeScript](https://www.typescriptlang.org/)
  - [React](https://reactjs.org/)
  - [Material-UI (MUI)](https://mui.com/)

- **Other Tools**
  - REST API for real-time communication.
  - WebSocket and/or Server-Sent Events (SSE) for live updates.

## Documentation

- [Documentation Folder](docs)
  - [Backend Documentation](docs/Backend.md)
  - [OpenAPI Specification](docs/TalkRoomsOpenApi.yaml)
  - [Frontend Documentation](docs/Frontend.md)
  - [General Functionalities](docs/GeneralDoc.md)
- [Instructions to Build and Run the Project in JavaScript](code/js/README.md)
- [Instructions to Build and Run the Project in JVM with Docker](code/jvm/README.md)

## Testing Credentials

For testing purposes, use the following credentials:

- **User:** `user1`  
  **Password:** `Strong_password1234`
- **User:** `user2`  
  **Password:** `Strong_password1234`
- **User:** `user3`  
  **Password:** `Strong_password1234`
