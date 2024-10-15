import org.jdbi.v3.core.Handle

class JdbiInvitationRepository(
    private val handle: Handle
) :InvitationRepository{
}