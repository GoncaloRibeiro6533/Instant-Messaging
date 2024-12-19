# Frontend Documentation

> This is the Frontend documentation for the TalkRooms project.

## Introduction

The frontend is a single page application built using React and Material-UI.
It is a responsive web application that can be used on any device.

This application is a client for the ChImp API, which is documented here.
For more information about the backend, please refer to the [backend documentation](./Backend.md).

## Internal software organization
- `code/js/src`
    - `components` - Contains the React components used in the application, each component is in a separate folder;
    - `domain` - Contains the core business logic and domain models of the application. This includes classes and functions
that represent the main entities and operations of the application;
    - `service` - Contains the services used in the application;
    - `types` - Contains the types used in the application;
    - the package.json file contains the dependencies used in the application.
    - tsconfig.json - Contains the configuration for the typescript compiler.
    - webpack.config.js - Contains the configuration for the webpack bundler.
- `code/js/public` - Contains the static files used in the application.

## Frontend Diagram
![Frontend RoadMap](FrontendRoadMap.png)