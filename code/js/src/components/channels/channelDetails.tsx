import * as React from 'react';
import { useParams } from 'react-router-dom';

export function ChannelDetails() {
    const { channelId } = useParams();

    return (
        <div>
            <h1>Channel Page</h1>
            <p>Channel ID: {channelId}</p>
        </div>
    );
}