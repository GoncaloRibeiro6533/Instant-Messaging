import org.jdbi.v3.core.Handle

class JdbiChannelRepository(
    private val handle: Handle,
) :ChannelRepository {

}
