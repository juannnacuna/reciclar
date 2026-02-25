package edu.unlp.reciclar.ui.trivia

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.unlp.reciclar.data.local.entity.Consejo
import edu.unlp.reciclar.data.local.entity.Trivia

@Composable
fun TriviaScreen(
    modifier: Modifier = Modifier,
    viewModel: TriviaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ───────────────────────────────────────────
        Text(
            text = "Eco Trivia",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // ── Estadísticas ─────────────────────────────────────
        EstadisticasCard(
            respondidas = uiState.estadisticas.respondidas,
            correctas = uiState.estadisticas.correctas,
            totalDisponibles = uiState.estadisticas.totalDisponibles
        )

        // ── Consejo del día ──────────────────────────────────
        uiState.consejoActual?.let { consejo ->
            ConsejoCard(consejo = consejo)
        }

        // ── Trivia ───────────────────────────────────────────
        uiState.triviaActual?.let { trivia ->
            TriviaCard(
                trivia = trivia,
                respuestaSeleccionada = uiState.respuestaSeleccionada,
                mostrandoResultado = uiState.mostrandoResultado,
                esCorrecta = uiState.esCorrecta,
                onRespuestaClick = { viewModel.seleccionarRespuesta(it) }
            )

            if (uiState.mostrandoResultado) {
                Button(
                    onClick = { viewModel.siguienteTrivia() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Siguiente pregunta")
                }
            }
        } ?: run {
            // No hay más trivias disponibles
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiNature,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¡Respondiste todas las trivias!",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Volvé más tarde para nuevas preguntas.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun EstadisticasCard(
    respondidas: Int,
    correctas: Int,
    totalDisponibles: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tu progreso",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$respondidas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Respondidas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$correctas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Correctas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val porcentaje = if (respondidas > 0) (correctas * 100 / respondidas) else 0
                    Text(
                        text = "$porcentaje%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Acierto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            if (totalDisponibles > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { respondidas.toFloat() / totalDisponibles },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                Text(
                    text = "$respondidas / $totalDisponibles trivias",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun ConsejoCard(consejo: Consejo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Consejo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = consejo.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = consejo.contenido,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun TriviaCard(
    trivia: Trivia,
    respuestaSeleccionada: Int?,
    mostrandoResultado: Boolean,
    esCorrecta: Boolean,
    onRespuestaClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pregunta
            Text(
                text = trivia.pregunta,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            // Opciones
            val opciones = listOf(trivia.opcionA, trivia.opcionB, trivia.opcionC, trivia.opcionD)
            opciones.forEachIndexed { index, opcion ->
                OpcionButton(
                    texto = opcion,
                    index = index,
                    respuestaCorrecta = trivia.respuestaCorrectaIndex,
                    seleccionada = respuestaSeleccionada == index,
                    mostrandoResultado = mostrandoResultado,
                    onClick = { onRespuestaClick(index) }
                )
            }

            // Resultado y explicación
            if (mostrandoResultado) {
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (esCorrecta)
                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (esCorrecta)
                                Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (esCorrecta) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (esCorrecta) "¡Correcto!" else "Incorrecto",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (esCorrecta) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = trivia.explicacion,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF424242)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpcionButton(
    texto: String,
    index: Int,
    respuestaCorrecta: Int,
    seleccionada: Boolean,
    mostrandoResultado: Boolean,
    onClick: () -> Unit
) {
    val prefijos = listOf("A", "B", "C", "D")

    val backgroundColor by animateColorAsState(
        targetValue = when {
            mostrandoResultado && index == respuestaCorrecta -> Color(0xFFE8F5E9)
            mostrandoResultado && seleccionada && index != respuestaCorrecta -> Color(0xFFFFEBEE)
            seleccionada -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "opcionColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            mostrandoResultado && index == respuestaCorrecta -> Color(0xFF4CAF50)
            mostrandoResultado && seleccionada && index != respuestaCorrecta -> Color(0xFFF44336)
            seleccionada -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        label = "borderColor"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !mostrandoResultado,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (seleccionada || (mostrandoResultado && index == respuestaCorrecta)) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${prefijos[index]}.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (mostrandoResultado) Color(0xFF616161)
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = if (mostrandoResultado) Color(0xFF616161)
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (mostrandoResultado && index == respuestaCorrecta) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Correcta",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            } else if (mostrandoResultado && seleccionada && index != respuestaCorrecta) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Incorrecta",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
