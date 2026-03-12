package com.example.amigosdaquinta.data.local

import androidx.room.TypeConverter
import com.example.amigosdaquinta.data.local.entity.StatusJogo
import com.example.amigosdaquinta.data.local.entity.TimeColor

/**
 * Conversores de tipos para persistência no Room.
 * 
 * Permite que tipos complexos como Enums sejam armazenados no banco de dados SQLite
 * como Strings e recuperados como objetos tipados.
 */
class Converters {

    /** Converte TimeColor para String. */
    @TypeConverter
    fun fromTimeColor(value: TimeColor?): String? = value?.name

    /** Converte String para TimeColor. */
    @TypeConverter
    fun toTimeColor(value: String?): TimeColor? = value?.let { TimeColor.valueOf(it) }

    /** Converte StatusJogo para String. */
    @TypeConverter
    fun fromStatusJogo(value: StatusJogo?): String? = value?.name

    /** Converte String para StatusJogo. */
    @TypeConverter
    fun toStatusJogo(value: String?): StatusJogo? = value?.let { StatusJogo.valueOf(it) }
}
