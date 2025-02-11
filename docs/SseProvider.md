# SSE Provider Documentation

This file provides the implementation for managing **Server-Sent Events (SSE)** in a React application. The primary goal of the `SseProvider` is to handle real-time updates from the backend, propagate these updates to other parts of the application, and manage notifications triggered by these updates.

---

## 1. Key Components

### `SseContext`
The `SseContext` is a React context that shares the SSE connection and notifications across the application. It includes:
- **`sse`**: The `EventSource` instance for the SSE connection.
- **`setSse`**: A function to set or reset the `EventSource`.
- **`notifications`**: A list of notifications related to events.
- **`deleteNotification`**: A function to delete notifications by their `id`.

### `SseProvider`
This is the context provider that initializes the SSE connection, listens to server events, and provides the `SseContext` values to child components.

---

## 2. Detailed Explanation

### State Management
The provider uses React `useState` hooks to manage the following:
- **`sse`**: Stores the `EventSource` connection.
- **`notifications`**: Tracks a list of in-app notifications.

### Dependency Injection
The provider uses dependencies from:
- **`AuthProvider`**: To fetch the current authenticated user.
- **`DataProvider`**: To manage application data such as messages and channels.

---

## 3. Main Functionalities

### 3.1 SSE Initialization
- When the user logs in, an `EventSource` connection is established with the backend endpoint:  
  `http://localhost:8080/api/sse/listen`
- The connection listens for multiple event types sent by the server and processes them accordingly.
- The connection is closed automatically when the component unmounts.

---

## 4. Event Handlers

### `NewChannelMessage`
Triggered when a new message is added to a channel.  
**Action**: Maps the message data using `messageMapper` and adds it to the corresponding channel using `addMessages`.

### `ChannelNameUpdate`
Triggered when a channel's name is updated.  
**Action**: Maps the updated channel using `channelMapper`, updates the channel with `updateChannel`, and adds a notification.

### `NewMemberUpdate`
Triggered when a channel member is removed.  
**Action**: Maps the user and channel, then removes the member using `removeChannelMember`.

### `ChannelNewMemberUpdate`
Triggered when a new member is added to a channel.  
**Action**: Maps the channel and the new member using `memberMapper`, then adds the member with `addChannelMember`.

### `ChannelMemberExitedUpdate`
Triggered when a member exits a channel.  
**Action**: Maps the user and channel, then removes the member with `removeChannelMember`.

### `NewInvitationUpdate`
Triggered when a user receives a new channel invitation.  
**Action**: Maps the invitation data using `invitationMapper`, adds it using `addInvitation`, and adds a notification.

### Error Handling
- If an error occurs during the SSE connection, the connection is closed, and an error is logged to the console.

---

## 5. Helper Functions

### `messageMapper`
Maps raw JSON data into a `Message` object.  
Includes:
- **Creator**: Maps the channel creator.
- **Visibility**: Converts visibility into an enum.
- **Message**: Maps the sender, content, and timestamp.

### `channelMapper`
Maps raw JSON data into a `Channel` object.  
Includes:
- **Creator**: Maps the channel creator.
- **Visibility**: Converts visibility into an enum.

### `userMapper`
Maps raw JSON data into a `User` object.

### `memberMapper`
Maps a user and role into a `ChannelMember` object.

### `invitationMapper`
Maps raw JSON data into a `ChannelInvitation` object.  
Includes:
- **Sender and Receiver**: Maps the users.
- **Channel**: Maps the channel.
- **Role**: Converts role into an enum.
- **Timestamp**: Maps the invitation timestamp.

---

## 6. Notifications

### Adding Notifications
The function `addNotification(id: number, message: string)` appends a new notification to the list.

### Deleting Notifications
The function `deleteNotification(id: number)` removes a notification by filtering the list.

---

## 7. Custom Hook

### `useSse`
A custom hook that provides access to the SSE context.  
Returns:
1. The current `EventSource` instance.
2. A setter function to initialize or reset the `EventSource`.
3. A list of notifications.
4. A function to delete notifications.

---

## 8. Cleanup

When the `SseProvider` unmounts:
- The `EventSource` connection is closed to prevent resource leaks.

---

## 9. Usage

Wrap your application with the `SseProvider` to enable SSE functionality and provide real-time updates across the app:
```tsx
<SseProvider>
  <App />
</SseProvider>
