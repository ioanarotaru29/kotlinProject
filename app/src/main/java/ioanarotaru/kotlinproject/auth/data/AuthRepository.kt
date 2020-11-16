package ioanarotaru.kotlinproject.auth.data

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import ioanarotaru.kotlinproject.auth.data.remote.RemoteAuthDataSource
import ioanarotaru.kotlinproject.core.Api
import ioanarotaru.kotlinproject.core.Result
import ioanarotaru.kotlinproject.core.sp


object AuthRepository {

    var user: User? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        user = null
    }

    fun logout() {
        user = null
        Api.tokenInterceptor.token = null
        sp?.edit()?.clear()?.apply()
    }

    suspend fun login(username: String, password: String): Result<TokenHolder> {
        val user = User(username, password)
        val result = RemoteAuthDataSource.login(user)
        if (result is Result.Success<TokenHolder>) {
            setLoggedInUser(user, result.data)
        }
        return result
    }

    public fun setLoggedInUser(user: User, tokenHolder: TokenHolder) {
        this.user = user
        Api.tokenInterceptor.token = tokenHolder.token
        sp?.edit()?.putString("token", tokenHolder.token)?.apply()
        sp?.edit()?.putString("username",user.username)?.apply()
        sp?.edit()?.putString("password", user.password)?.apply()
    }
}
