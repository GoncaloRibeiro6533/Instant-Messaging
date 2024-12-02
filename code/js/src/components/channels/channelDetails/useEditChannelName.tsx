import * as React from 'react';
import { services } from "../../../App";
import { AuthContext } from "../../auth/AuthProvider";

export function useEditChannelName(channelId: number, initialName: string, loadChannels: () => void) {
    const { user } = React.useContext(AuthContext);
    const [newChannelName, setNewChannelName] = React.useState(initialName);
    const [isEditing, setIsEditing] = React.useState(false);
    const [error, setError] = React.useState('');

    const handleEditClick = () => {
        setIsEditing(true);
    };

    const handleSaveClick = async () => {
        try {
            await services.channelService.updateChannelName(user.token, channelId, newChannelName);
            setIsEditing(false);
            loadChannels(); // Recarrega a lista de canais após a atualização
        } catch (err) {
            setError('Failed to update channel name.');
            console.error(err);
        }
    };

    return {
        newChannelName,
        setNewChannelName,
        isEditing,
        error,
        handleEditClick,
        handleSaveClick
    };
}