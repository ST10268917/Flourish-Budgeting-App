package com.aj.flourish.supabase

import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

fun prepareImageForUpload(fileBytes: ByteArray, mimeType: String): MultipartBody {
    val bodyPart = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())

    return MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", "image.jpg", bodyPart)
        .build()
}
