package com.example.amigosdaquinta.data.local

import androidx.room.TypeConverter
import com.example.amigosdaquinta.data.local.entity.StatusJogo
import com.example.amigosdaquinta.data.local.entity.TimeColor

/**
 * Conversores de tipos customizados para o Room.
 *
 * Room nao sabe serializar enums diretamente. Estes conversores persistem
 * [TimeColor] e [StatusJogo] como String (nome do valor do enum) no banco,
 * e restauram o enum via [Enum.valueOf] na leitura.
 *
 * Valores null sao preservados em ambas as direcoes — campos nullable no banco
 * correspondem a campos nullable nas entidades.
 *
 * Registrado globalmente em [AppDatabase] via @TypeConverters.
 */
class Converters {

    @TypeConverter
    fun fromTimeColor(value: TimeColor?): String? = value?.name

    @TypeConverter
    fun toTimeColor(value: String?): TimeColor? = value?.let { TimeColor.valueOf(it) }

    @TypeConverter
    fun fromStatusJogo(value: StatusJogo?): String? = value?.name

    @TypeConverter
    fun toStatusJogo(value: String?): StatusJogo? = value?.let { StatusJogo.valueOf(it) }
}