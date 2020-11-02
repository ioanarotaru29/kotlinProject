package ro.ubbcluj.cs.ilazar.myapp2.todo.data.remote

import com.google.gson.GsonBuilder
import ioanarotaru.kotlinproject.issues_comp.data.Issue
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

object IssueApi {
    private const val URL = "http://192.168.0.104:3000/"

    interface Service {
        @GET("/issue")
        suspend fun find(): List<Issue>

        @GET("/issue/{id}")
        suspend fun read(@Path("id") issueId: String): Issue;

        @Headers("Content-Type: application/json")
        @POST("/issue")
        suspend fun create(@Body issue: Issue): Issue

        @Headers("Content-Type: application/json")
        @PUT("/issue/{id}")
        suspend fun update(@Path("id") issueId: String, @Body issue: Issue): Issue

        @Headers("Content-Type: application/json")
        @DELETE("/issue/{id}")
        suspend fun delete(@Path("id") issueId: String): Response<Void>
    }

    private val client: OkHttpClient = OkHttpClient.Builder().build()

    private var gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    val service: Service = retrofit.create(Service::class.java)
}