package com.example.amigosdaquinta.ui.screens.jogadores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.amigosdaquinta.data.local.entity.Jogador

/**
 * Diálogo para edição de dados de um atleta existente.
 * 
 * Permite a atualização do nome, número da camisa e posição técnica.
 * 
 * @param jogador Atleta original para preenchimento dos campos.
 * @param onDismiss Callback para cancelamento.
 * @param onConfirm Callback para salvar as alterações realizadas.
 */
@Composable
fun EditarJogadorDialog(
    jogador: Jogador,
    onDismiss: () -> Unit,
    onConfirm: (Jogador) -> Unit
) {
    var nome by remember { mutableStateOf(jogador.nome) }
    var numero by remember { mutableStateOf(jogador.numeroCamisa.toString()) }
    var isGoleiro by remember { mutableStateOf(jogador.isPosicaoGoleiro) }

    var erroNome by remember { mutableStateOf(false) }
    var erroNumero by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
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
                    label = { Text("Nome do Atleta") },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Posição Técnica", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (isGoleiro) "Goleiro" else "Jogador de Linha",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isGoleiro,
                        onCheckedChange = { isGoleiro = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4B0082))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = numero.toIntOrNull()
                    if (nome.isNotBlank() && num != null) {
                        onConfirm(jogador.copy(nome = nome.trim(), numeroCamisa = num, isPosicaoGoleiro = isGoleiro))
                    }
                },
                enabled = nome.isNotBlank() && !erroNumero && numero.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B0082))
            ) {
                Text("SALVAR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        }
    )
}
