import org.junit.jupiter.api.Assertions.assertNotEquals
import pt.isel.Sha256TokenEncoder
import kotlin.test.Test

class Sha256TokenEncoderTests {
    @Test
    fun `should create a validation information`() {
        val encoder = Sha256TokenEncoder()
        val result = encoder.createValidationInformation("token")
        assertNotEquals("token", result.validationInfo)
    }
}
