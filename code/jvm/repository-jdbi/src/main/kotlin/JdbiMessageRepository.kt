import org.jdbi.v3.core.Handle

class JdbiMessageRepository (
    private val handle: Handle
) :MessageRepository{
}