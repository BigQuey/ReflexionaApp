package com.qfnm.reflexionaapp.api


import com.qfnm.reflexionaapp.modelo.RespuestaApi
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ReflexionApi {
    @POST("resumen")
    fun enviarResumen(@Body request: ResumenRequest): Call<ResumenResponse>
}
data class ResumenRequest(
    val respuestas: List<RespuestaApi>
)

data class ResumenResponse(
    val mensaje: String
)