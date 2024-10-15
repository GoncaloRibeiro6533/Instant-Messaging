import org.jdbi.v3.core.Handle

class JdbiSessionRepository(
    private val handle: Handle
) :SessionRepository {
}