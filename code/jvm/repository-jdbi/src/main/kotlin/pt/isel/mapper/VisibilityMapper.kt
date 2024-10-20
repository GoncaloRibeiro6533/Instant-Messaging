package pt.isel.mapper

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.Visibility
import java.sql.ResultSet

class VisibilityMapper : ColumnMapper<Visibility> {
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext,
    ): Visibility = Visibility.valueOf(rs.getString(columnNumber))
}
