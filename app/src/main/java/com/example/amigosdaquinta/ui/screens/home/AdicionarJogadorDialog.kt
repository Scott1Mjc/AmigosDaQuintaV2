package com.example.amigosdaquinta.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Dialog de cadastro de novo jogador.
 *
 * Validacoes ativas:
 * - Nome nao pode ser vazio.
 * - Numero de camisa deve ser um inteiro entre 1 e 99.
 *
 * O campo de numero aceita apenas digitos e limita a entrada a 2 caracteres via [onValueChange].
 * O botao de confirmar permanece desabilitado ate que nome e numero sejam validos.
 *
 * Dialog para adicionar um novo jogador.
 * @param onConfirm Retorna (nome, numeroCamisa, isGoleiro) apos validacao
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarJogadorDialog(
    onDismiss: () -> Unit,
    onConfirm: (nome: String, numero: Int, isGoleiro: Boolean) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var isGoleiro by remember { mutableStateOf(false) }
    var erroNome by remember { mutableStateOf(false) }
    var erroNumero by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Jogador") },
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
                        erroNumero = num == null || num < 0 || num > 999
                    },
                    label = { Text("Número da Camisa") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = erroNumero,
                    supportingText = if (erroNumero) {
                        { Text("Número deve estar entre 0 e 999") }
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
                    if (nome.isNotBlank() && num != null && num in 0..999) {
                        onConfirm(nome.trim(), num, isGoleiro)
                    }
                },
                enabled = nome.isNotBlank() && !erroNumero
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}