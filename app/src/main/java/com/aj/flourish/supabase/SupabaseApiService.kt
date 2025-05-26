package com.aj.flourish.supabase

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SupabaseApiService {

    @PUT("storage/v1/object/{bucket}/{fileName}")
    suspend fun uploadImage(
        @Header("Authorization") authHeader: String,
        @Header("Content-Type") contentType: String,
        @Path("bucket") bucket: String,
        @Path("fileName") fileName: String,
        @Body file: RequestBody
    ): Response<ResponseBody>
}
