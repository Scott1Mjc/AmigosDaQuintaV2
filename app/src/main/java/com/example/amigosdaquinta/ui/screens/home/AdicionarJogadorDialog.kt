package com.example.amigosdaquinta.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Diálogo para cadastro de um novo atleta no sistema.
 *
 * Realiza validações de campo para garantir que o nome não esteja vazio e o número
 * da camisa esteja no intervalo permitido (0-999).
 *
 * @param onDismiss Callback para fechar o diálogo sem salvar.
 * @param onConfirm Callback que retorna os dados validados (nome, número, éGoleiro).
 */
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
        title = { Text("Novo Atleta", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        erroNome = it.isBlank()
                    },
                    label = { Text("Nome Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = erroNome,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = numero,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }.take(3)
                        numero = filtered
                        val num = filtered.toIntOrNull()
                        erroNumero = num == null || num !in 0..999
                    },
                    label = { Text("Nº da Camisa") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = erroNumero,
                    placeholder = { Text("0-999") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Column {
                    Text("Posição", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
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
                            label = { Text("Jogador de Linha") },
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
                    if (nome.isNotBlank() && num != null) {
                        onConfirm(nome.trim(), num, isGoleiro)
                    }
                },
                enabled = nome.isNotBlank() && !erroNumero && numero.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
            ) {
                Text("CADASTRAR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        }
    )
}
