package mapper

class MessageMapper
/*:ColumnMapper<Message> {
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): Message {
        return Message(
            id = r.getInt("id"),
            timestamp = r.getTimestamp("creationtime").toLocalDateTime(),
            sender = r.getInt("user_id"),
            channel = r.getInt("channel_id"),
            content = r.getString("message"),
        )
    }
}

 */
