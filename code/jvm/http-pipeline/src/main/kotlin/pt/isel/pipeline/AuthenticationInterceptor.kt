package pt.isel.pipeline

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import pt.isel.AuthenticatedUser

@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor: RequestTokenProcessor,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler is HandlerMethod &&
            handler.methodParameters.any {
                it.parameterType == AuthenticatedUser::class.java
            }
        ) {
            // TODO if getCookies is null, it launches an exception
            // enforce authentication
            if (request.cookies == null || request.cookies.isEmpty()) {
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                return false
            }

            val cookie = request.cookies.find { it.name == NAME_AUTHORIZATION_HEADER }
            if (cookie == null) {
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                return false
            }
            val user =
                authorizationHeaderProcessor
                    .processAuthorizationHeaderValue(cookie.value)
            return if (user == null) {
                response.status = 401
                response.addCookie(
                    Cookie(NAME_AUTHORIZATION_HEADER, "").apply {
                        maxAge = 0
                        path = "/"
                        domain = cookie.domain
                        isHttpOnly = cookie.isHttpOnly
                        secure = cookie.secure
                    },
                )

                false
            } else {
                AuthenticatedUserArgumentResolver.addUserTo(user, request)
                true
            }
        }

        return true
    }

    companion object {
        const val NAME_AUTHORIZATION_HEADER = "token"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
