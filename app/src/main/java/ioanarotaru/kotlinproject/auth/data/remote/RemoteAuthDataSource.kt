package ioanarotaru.kotlinproject.auth.data.remote

import ioanarotaru.kotlinproject.auth.data.TokenHolder
import ioanarotaru.kotlinproject.auth.data.User
import ioanarotaru.kotlinproject.core.Api
import ioanarotaru.kotlinproject.core.Result
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

object RemoteAuthDataSource {
    interface AuthService {
        @Headers("Content-Type: application/json")
        @POST("/api/auth/login")
        suspend fun login(@Body user: User): TokenHolder
    }

    private val authService: AuthService = Api.retrofit.create(AuthService::class.java)

    suspend fun login(user: User): Result<TokenHolder> {
        try {
            return Result.Success(authService.login(user))
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }
}

