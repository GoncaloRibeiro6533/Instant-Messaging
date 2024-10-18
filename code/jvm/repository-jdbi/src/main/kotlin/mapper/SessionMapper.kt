package mapper

import Session
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.SQLException

class SessionMapper : ColumnMapper<Session> {
    @Throws(SQLException::class)
    override fun map(
        r: java.sql.ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): Session {
        return Session(
            r.getString("token"),
            r.getInt("user_id"),
            r.getTimestamp("expirationdate"),
            r.getTimestamp("lasttimeused"),
        )
    }
}