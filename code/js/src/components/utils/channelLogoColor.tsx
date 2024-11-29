const channelColors = new Map<number, string>();

export function getRandomColor(channelId: number): string {
    if (!channelColors.has(channelId)) {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        channelColors.set(channelId, color);
    }
    return channelColors.get(channelId)!;
}