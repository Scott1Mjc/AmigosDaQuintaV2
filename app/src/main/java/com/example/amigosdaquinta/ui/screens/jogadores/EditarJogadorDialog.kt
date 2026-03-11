package com.example.amigosdaquinta.ui.screens.jogadores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador

/**
 * Dialog para editar dados de um jogador existente.
 *
 * Permite alterar:
 * - Nome do jogador
 * - Número da camisa (0 a 999)
 * - Posição (Goleiro ou Linha)
 *
 * Validações:
 * - Nome não pode ser vazio
 * - Número deve estar entre 0 e 999
 *
 * @param jogador Jogador a ser editado
 * @param onDismiss Callback ao fechar o dialog
 * @param onConfirm Callback ao confirmar, retorna o jogador atualizado
 */
@Composable
fun EditarJogadorDialog(
    jogador: Jogador,
    onDismiss: () -> Unit,
    onConfirm: (Jogador) -> Unit // ✅ RECEBE JOGADOR COMPLETO
) {
    var nome by remember { mutableStateOf(jogador.nome) }
    var numero by remember { mutableStateOf(jogador.numeroCamisa.toString()) }
    var isPosicaoGoleiro by remember { mutableStateOf(jogador.isPosicaoGoleiro) }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroNumero by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Jogador") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo Nome
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        erroNome = null
                    },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = erroNome != null,
                    supportingText = erroNome?.let { { Text(it) } }
                )

                // Campo Número
                OutlinedTextField(
                    value = numero,
                    onValueChange = {
                        // Aceitar apenas números
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            numero = it
                            erroNumero = null
                        }
                    },
                    label = { Text("Número da Camisa") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = erroNumero != null,
                    supportingText = erroNumero?.let { { Text(it) } },
                    placeholder = { Text("0 a 999") }
                )

                // Switch Posição
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Posição",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            if (isPosicaoGoleiro) "Goleiro" else "Jogador de Linha",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPosicaoGoleiro,
                        onCheckedChange = { isPosicaoGoleiro = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validação
                    val nomeValido = nome.isNotBlank()
                    val numeroInt = numero.toIntOrNull()
                    val numeroValido = numeroInt != null && numeroInt in 0..999

                    erroNome = if (!nomeValido) "Nome não pode ser vazio" else null
                    erroNumero = if (!numeroValido) "Número deve estar entre 0 e 999" else null

                    if (nomeValido && numeroValido) {
                        // ✅ RETORNA JOGADOR ATUALIZADO
                        onConfirm(
                            jogador.copy(
                                nome = nome.trim(),
                                numeroCamisa = numeroInt!!,
                                isPosicaoGoleiro = isPosicaoGoleiro
                            )
                        )
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}