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
 * Ponto central de acesso ao banco de dados SQLite via Room.
 * 
 * Define as entidades do sistema, versionamento e fornece instâncias dos DAOs.
 * Utiliza o padrão Singleton para garantir uma única conexão aberta com o banco.
 */
@Database(
    entities = [
        Jogador::class,
        Jogo::class,
        Participacao::class,
        PresencaDia::class
    ],
    version = 6,
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

        /**
         * Retorna a instância única do banco de dados.
         */
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
