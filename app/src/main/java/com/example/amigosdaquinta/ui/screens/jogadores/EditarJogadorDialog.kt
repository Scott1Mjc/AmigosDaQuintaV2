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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarJogadorDialog(
    jogador: Jogador,
    onDismiss: () -> Unit,
    onConfirm: (nome: String, numero: Int, isGoleiro: Boolean) -> Unit
) {
    var nome by remember { mutableStateOf(jogador.nome) }
    var numero by remember { mutableStateOf(jogador.numeroCamisa.toString()) }
    var isGoleiro by remember { mutableStateOf(jogador.isPosicaoGoleiro) }
    var erroNome by remember { mutableStateOf(false) }
    var erroNumero by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Jogador") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo: Nome
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        erroNome = it.isBlank()
                    },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = erroNome,
                    supportingText = if (erroNome) {
                        { Text("Nome não pode ser vazio") }
                    } else null,
                    singleLine = true
                )

                // Campo: Número da camisa
                OutlinedTextField(
                    value = numero,
                    onValueChange = {
                        numero = it.filter { char -> char.isDigit() }
                        val num = numero.toIntOrNull()
                        erroNumero = num == null || num < 1 || num > 999
                    },
                    label = { Text("Número da Camisa") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = erroNumero,
                    supportingText = if (erroNumero) {
                        { Text("Número deve estar entre 1 e 999") }
                    } else null,
                    singleLine = true
                )

                // Campo: Posição
                Column {
                    Text(
                        "Posição",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isGoleiro,
                            onClick = { isGoleiro = true },
                            label = { Text("Goleiro") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isGoleiro,
                            onClick = { isGoleiro = false },
                            label = { Text("Linha") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = numero.toIntOrNull()
                    if (nome.isNotBlank() && num != null && num in 1..999) {
                        onConfirm(nome.trim(), num, isGoleiro)
                    }
                },
                enabled = nome.isNotBlank() && !erroNumero
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