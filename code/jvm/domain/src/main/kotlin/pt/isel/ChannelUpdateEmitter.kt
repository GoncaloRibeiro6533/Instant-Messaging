package pt.isel

interface ChannelUpdateEmitter {
    /**
     * Emits a new channel message.
     * @param signal the message to emit
     */
    fun emit(signal: ChannelUpdate)

    /**
     * Registers a callback to be invoked when the emitter completes.
     * @param callback the callback to invoke
     */
    fun onCompletion(callback: () -> Unit)

    /**
     * Registers a callback to be invoked when the emitter fails.
     * @param callback the callback to invoke
     */
    fun onError(callback: (Throwable) -> Unit)
}
