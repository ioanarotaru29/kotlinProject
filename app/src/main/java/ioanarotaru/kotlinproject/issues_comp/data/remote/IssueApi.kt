package ro.ubbcluj.cs.ilazar.myapp2.todo.data.remote

import com.google.gson.GsonBuilder
import ioanarotaru.kotlinproject.issues_comp.data.Issue
import ioanarotaru.kotlinproject.core.Api
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


object IssueApi {
    interface Service {
        @GET("/api/issue")
        suspend fun find(): List<Issue>

        @GET("/api/issue/{id}")
        suspend fun read(@Path("id") issueId: String): Issue;

        @Headers("Content-Type: application/json")
        @POST("/api/issue")
        suspend fun create(@Body issue: Issue): Issue

        @Headers("Content-Type: application/json")
        @PUT("/api/issue/{id}")
        suspend fun update(@Path("id") issueId: String, @Body issue: Issue): Issue

        @Headers("Content-Type: application/json")
        @DELETE("/api/issue/{id}")
        suspend fun delete(@Path("id") issueId: String): Response<Void>
    }



    val service: Service = Api.retrofit.create(Service::class.java)
}