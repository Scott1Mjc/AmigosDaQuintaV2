package com.example.amigosdaquinta.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.amigosdaquinta.data.local.dao.JogadorDao
import com.example.amigosdaquinta.data.local.dao.JogoDao
import com.example.amigosdaquinta.data.local.dao.ParticipacaoDao
import com.example.amigosdaquinta.data.local.dao.PresencaDao
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.PresencaDia

/**
 * Banco de dados local do app.
 *
 * Entidades registradas: [Jogador], [Jogo], [Participacao], [PresencaDia].
 *
 * [fallbackToDestructiveMigration] está ativo para facilitar o desenvolvimento.
 */
@Database(
    entities = [
        Jogador::class,
        Jogo::class,
        Participacao::class,
        PresencaDia::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jogadorDao(): JogadorDao
    abstract fun jogoDao(): JogoDao
    abstract fun participacaoDao(): ParticipacaoDao
    abstract fun presencaDao(): PresencaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "amigos_quinta_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
