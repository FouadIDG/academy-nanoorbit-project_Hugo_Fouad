package fr.myefrei.nanoorbit.data.api

import com.google.gson.GsonBuilder
import fr.myefrei.nanoorbit.BuildConfig
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NanoOrbitApiClient {
    fun create(
        baseUrl: String = BuildConfig.NANOORBIT_API_BASE_URL
    ): NanoOrbitApi {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(NanoOrbitApi::class.java)
    }
}

private class LocalDateAdapter :
    com.google.gson.JsonSerializer<LocalDate>,
    com.google.gson.JsonDeserializer<LocalDate> {
    override fun serialize(
        src: LocalDate?,
        typeOfSrc: Type?,
        context: com.google.gson.JsonSerializationContext?
    ): com.google.gson.JsonElement? {
        return src?.let { com.google.gson.JsonPrimitive(it.toString()) }
    }

    override fun deserialize(
        json: com.google.gson.JsonElement?,
        typeOfT: Type?,
        context: com.google.gson.JsonDeserializationContext?
    ): LocalDate? {
        return json?.takeIf { !it.isJsonNull }?.asString?.let(LocalDate::parse)
    }
}

private class LocalDateTimeAdapter :
    com.google.gson.JsonSerializer<LocalDateTime>,
    com.google.gson.JsonDeserializer<LocalDateTime> {
    override fun serialize(
        src: LocalDateTime?,
        typeOfSrc: Type?,
        context: com.google.gson.JsonSerializationContext?
    ): com.google.gson.JsonElement? {
        return src?.let { com.google.gson.JsonPrimitive(it.toString()) }
    }

    override fun deserialize(
        json: com.google.gson.JsonElement?,
        typeOfT: Type?,
        context: com.google.gson.JsonDeserializationContext?
    ): LocalDateTime? {
        return json?.takeIf { !it.isJsonNull }?.asString?.let(LocalDateTime::parse)
    }
}
