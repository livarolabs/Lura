package com.lura.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lura.domain.model.Book
import com.lura.ui.theme.LuraGhostWhite
import com.lura.ui.theme.LuraIndigo
import com.lura.ui.theme.LuraObsidian
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun HeroBookSlot(
    currentBook: Book?,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentBook == null) {
        // Empty state
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = LuraObsidian.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Start reading a book to see it here",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LuraGhostWhite.copy(alpha = 0.5f)
                )
            }
        }
        return
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        colors = CardDefaults.cardColors(
            containerColor = LuraObsidian.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background cover image (blurred)
            if (currentBook.coverImagePath != null) {
                AsyncImage(
                    model = currentBook.coverImagePath,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LuraObsidian),
                    contentScale = ContentScale.Crop,
                    alpha = 0.2f
                )
            }
            
            // Gradient overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                LuraObsidian.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cover Image
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    if (currentBook.coverImagePath != null) {
                        AsyncImage(
                            model = currentBook.coverImagePath,
                            contentDescription = "Cover of ${currentBook.title}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LuraIndigo.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentBook.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = LuraGhostWhite.copy(alpha = 0.6f),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
                
                // Book Info & Actions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top section: Title, Author, Last Read
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Continue Reading",
                            style = MaterialTheme.typography.labelSmall,
                            color = LuraIndigo,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        
                        Text(
                            text = currentBook.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = LuraGhostWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            fontSize = 20.sp
                        )
                        
                        Text(
                            text = currentBook.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LuraGhostWhite.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        
                        if (currentBook.lastReadTimestamp > 0) {
                            Text(
                                text = "Last read ${getRelativeTime(currentBook.lastReadTimestamp)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = LuraGhostWhite.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                    // Bottom section: Progress & Resume Button
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Progress
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${currentBook.progressPercentage.toInt()}% complete",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LuraGhostWhite.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                                if (currentBook.estimatedReadingTimeMinutes > 0) {
                                    val remaining = ((100 - currentBook.progressPercentage) / 100 * currentBook.estimatedReadingTimeMinutes).toInt()
                                    Text(
                                        text = "${remaining}min left",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LuraGhostWhite.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                progress = { currentBook.progressPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LuraIndigo,
                                trackColor = LuraObsidian.copy(alpha = 0.3f),
                            )
                        }
                        
                        // Resume Button
                        Button(
                            onClick = onResumeClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuraIndigo
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Resume Reading",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
