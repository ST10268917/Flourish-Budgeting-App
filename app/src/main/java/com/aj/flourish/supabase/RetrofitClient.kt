package com.aj.flourish.supabase

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

object RetrofitClient {

    private const val BASE_URL = "https://cbwdbipbwbyxszjmibnm.supabase.co"

    val instance: SupabaseApiService by lazy {
        val client = OkHttpClient.Builder().build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        retrofit.create(SupabaseApiService::class.java)
    }
}
