package com.example.amigosdaquinta.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
 * @param onConfirm Retorna (nome, numeroCamisa, isGoleiro) apos validacao.
 */
@Composable
fun AdicionarJogadorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Boolean) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var isGoleiro by remember { mutableStateOf(false) }

    val nomeValido = nome.isNotBlank()
    val numeroValido = numero.isNotBlank() && (numero.toIntOrNull() ?: 0) in 1..99
    val podeSalvar = nomeValido && numeroValido

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Jogador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    isError = nome.isNotBlank() && !nomeValido,
                    singleLine = true
                )

                OutlinedTextField(
                    value = numero,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 2) numero = input
                    },
                    label = { Text("Numero da camisa (1-99)") },
                    isError = numero.isNotBlank() && !numeroValido,
                    supportingText = {
                        if (numero.isNotBlank() && !numeroValido) {
                            Text(
                                text = "Numero deve estar entre 1 e 99",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isGoleiro, onCheckedChange = { isGoleiro = it })
                    Text("E goleiro?")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = podeSalvar,
                onClick = { onConfirm(nome.trim(), numero.toInt(), isGoleiro) }
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}