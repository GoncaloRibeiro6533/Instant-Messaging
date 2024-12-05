import * as React from 'react';
import { services } from "../../../App";
import { AuthContext } from "../../auth/AuthProvider";
import { useData } from "../../data/DataProvider";

export function useEditChannelName(channelId: number, initialName: string, loadChannels: () => void) {
    const { user } = React.useContext(AuthContext);
    const [newChannelName, setNewChannelName] = React.useState(initialName);
    const [isEditing, setIsEditing] = React.useState(false);
    const [error, setError] = React.useState('');
    const { updateChannel}= useData();

    const handleEditClick = () => {
        setIsEditing(true);
    };

    const handleSaveClick = async () => {
        try {
            const result  = await services.channelService.updateChannelName(user.token, channelId, newChannelName);
            setIsEditing(false);
            loadChannels(); // Recarrega a lista de canais após a atualização
            updateChannel(result); // Atualiza o nome do canal na lista de canais
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