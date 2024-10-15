import org.jdbi.v3.core.Handle

class JdbiUserRepository(
    private val handle: Handle
) :UserRepository {

}
