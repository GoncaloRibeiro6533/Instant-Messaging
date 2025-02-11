# DataContext and DataProvider Documentation

This implementation manages the application's state related to channels, messages, invitations, and members using React Context and Hooks.

---

## Key Features

### 1. **Context Design**
- The `DataContext` defines a structure for storing and managing the application's state.
- Includes default implementations for operations such as:
    - Adding, updating, and removing **channels**.
    - Managing **messages** and **channel members**.
    - Handling **invitations**.

### 2. **State Management**
State is managed using React's `useState` for the following:

- **Channels**:
    - Stored as a `Map<Number, Channel>` to associate each channel with it's id.
- **Roles**:
    - Stored as a `Map<Number,Role>` to associate each channel with a user's role.
- **Messages**:
    - Stored as a `Map<Number, Message[]>` to group messages by channel ID.
- **Invitations**:
    - Stored as an `Array<ChannelInvitation>`.
- **Channel Members**:
    - Stored as a `Map<Number, ChannelMember[]>`, associating channel IDs with their members.

### 3. **Reusable Hook**
- The `useData` hook provides components with:
    - Access to the current state.
    - Functions to update the context state.

### 4. **Persistence**
- The `clear` function resets all stored data and removes local storage entries for `channels`, `messages` and `invitations`

---

## Key Functions

### **Channels**
- `addChannel(channel: Channel, role: Role)`: Adds a channel with its associated role.
- `removeChannel(channel: Channel)`: Removes a channel and its related messages.
- `updateChannel(channel: Channel)`: Updates channel details while preserving its role.

### **Messages**
- `addMessages(channel: Channel, messages: Message[])`: Adds new messages for a specific channel.
- `loadMessages(channel: Channel, messages: Message[])`: Appends messages to an existing list of messages for a channel.

### **Invitations**
- `addInvitation(invitation: ChannelInvitation)`: Adds a new invitation to the list.
- `removeInvitation(invitation: ChannelInvitation)`: Removes an invitation from the list.

### **Channel Members**
- `addChannelMember(channelId: Number, user: ChannelMember[])`: Adds members to a specific channel.
- `removeChannelMember(channelId: Number, user: User)`: Removes a member from a specific channel.

### **General**
- `clear()`: Resets all data and clears local storage.


### Why Use `state` to Store Data with SSE?

By using React's `state` to store data received via SSE (Server-Sent Events), we leverage React's **reactivity** to automatically update the user interface whenever the state changes. Here's why this approach works so well:

---

#### **1. Reactivity in React**
React's `state` is designed to be **reactive**, meaning:
- Any change in the state triggers a recalculation of the component tree.
- Only components dependent on the changed state are updated.
- This eliminates the need for direct DOM manipulation.

---

#### **2. Integration with SSE**
SSE is a technology that allows the server to send real-time updates to the client. By storing these updates in React's state:
- The state gets updated with new data sent by the server.
- React automatically detects state changes and re-renders the user interface in real time.

**Example:**
1. The server sends an SSE event containing a new message.
2. The event handler updates the `state` with the new message.
3. React re-renders components depending on the message list, displaying the updated data.

---

#### **3. Benefits of This Approach**
##### **a. Real-Time Updates**
- The user interface reflects changes as soon as new data is received from the server.

##### **b. Simplicity**
- There's no need to manually synchronize data and UI; React takes care of it automatically.

##### **c. Scalability**
- This approach works efficiently even in applications with multiple streams of data (e.g., messages, channels, or members). State changes are isolated to relevant components, minimizing unnecessary re-renders.

##### **d. Persistence**
- Because the state is stored in the context instead of a component, when the component is unmounted, the state is not lost.


---

## Example Usage

### Wrapping the Application
```tsx
import { DataProvider } from './data/DataProvider';

function App() {
  return (
    <DataProvider>
      <YourComponents />
    </DataProvider>
  );
}

//Access to the state value
const {messages} = useData()

 