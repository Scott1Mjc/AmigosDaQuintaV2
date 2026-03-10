package com.example.amigosdaquinta.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.amigosdaquinta.data.local.dao.JogadorDao
import com.example.amigosdaquinta.data.local.dao.JogoDao
import com.example.amigosdaquinta.data.local.dao.ParticipacaoDao
import com.example.amigosdaquinta.data.local.dao.PresencaDao
import com.example.amigosdaquinta.data.local.entity.Jogador
import com.example.amigosdaquinta.data.local.entity.Jogo
import com.example.amigosdaquinta.data.local.entity.Participacao
import com.example.amigosdaquinta.data.local.entity.PresencaDia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Banco de dados local do app, implementado com Room.
 *
 * Entidades registradas: [Jogador], [Jogo], [Participacao], [PresencaDia].
 *
 * População inicial:
 * Na primeira criação do banco, todos os jogadores definidos em [JogadoresIniciais]
 * são automaticamente inseridos via callback [onCreate].
 *
 * [fallbackToDestructiveMigration] está ativo: qualquer incremento de [version]
 * sem migration explícita destrói e recria o banco. Remover essa flag antes de
 * publicar em produção para não perder dados dos usuários.
 *
 * Singleton implementado com double-checked locking via [@Volatile] + [synchronized].
 */
@Database(
    entities = [
        Jogador::class,
        Jogo::class,
        Participacao::class,
        PresencaDia::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jogadorDao(): JogadorDao
    abstract fun jogoDao(): JogoDao
    abstract fun participacaoDao(): ParticipacaoDao
    abstract fun presencaDao(): PresencaDao

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * CoroutineScope com SupervisorJob para operações de banco em background.
         * Garante que erros em uma coroutine não cancelem as outras.
         */
        private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "amigos_quinta_database"
                )
                    // ✅ CALLBACK PARA POPULAR BANCO NA CRIAÇÃO
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            Log.d(TAG, "Banco de dados criado pela primeira vez")
                            Log.d(TAG, "Iniciando população com ${JogadoresIniciais.total} jogadores")

                            // Popular banco em background
                            applicationScope.launch {
                                INSTANCE?.let { database ->
                                    popularBancoComJogadores(database.jogadorDao())
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Popula o banco com jogadores iniciais.
         *
         * Chamado automaticamente apenas na primeira criação do banco.
         * Insere todos os jogadores definidos em [JogadoresIniciais].
         *
         * @param jogadorDao DAO para inserção de jogadores
         */
        private suspend fun popularBancoComJogadores(jogadorDao: JogadorDao) {
            try {
                Log.d(TAG, "Iniciando inserção de jogadores...")

                var sucessos = 0
                var erros = 0

                JogadoresIniciais.lista.forEach { jogador ->
                    try {
                        jogadorDao.inserir(jogador)
                        sucessos++

                        if (sucessos % 10 == 0) {
                            Log.d(TAG, "Progresso: $sucessos/${JogadoresIniciais.total} jogadores inseridos")
                        }
                    } catch (e: Exception) {
                        erros++
                        Log.e(TAG, "Erro ao inserir jogador ${jogador.nome} #${jogador.numeroCamisa}", e)
                    }
                }

                Log.d(TAG, "População concluída!")
                Log.d(TAG, "✅ Sucessos: $sucessos")
                if (erros > 0) {
                    Log.w(TAG, "⚠️ Erros: $erros")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Erro fatal ao popular banco de dados", e)
            }
        }
    }
}