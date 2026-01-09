package com.lura.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lura.domain.model.Book
import com.lura.domain.model.ReadingStatus
import com.lura.ui.theme.LuraGhostWhite
import com.lura.ui.theme.LuraIndigo
import com.lura.ui.theme.LuraObsidian

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookListItem(
    book: Book,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = LuraObsidian.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Card(
                modifier = Modifier
                    .width(50.dp)
                    .height(70.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                if (book.coverImagePath != null) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(java.io.File(book.coverImagePath))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cover of ${book.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LuraIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = LuraIndigo.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Book Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = LuraGhostWhite,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
                
                // Author
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = LuraGhostWhite.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp
                )
                
                // Tags (if any)
                if (book.tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        book.tags.take(2).forEach { tag ->
                            Surface(
                                color = LuraIndigo.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LuraIndigo,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        if (book.tags.size > 2) {
                            Text(
                                text = "+${book.tags.size - 2}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LuraGhostWhite.copy(alpha = 0.4f),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
            
            // Progress & Status
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Status badge
                if (book.readingStatus != ReadingStatus.UNREAD) {
                    Surface(
                        color = when (book.readingStatus) {
                            ReadingStatus.READING -> LuraIndigo
                            ReadingStatus.FINISHED -> Color(0xFF10B981)
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = when (book.readingStatus) {
                                ReadingStatus.READING -> "Reading"
                                ReadingStatus.FINISHED -> "Done"
                                else -> ""
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Progress percentage
                if (book.progressPercentage > 0f) {
                    Text(
                        text = "${book.progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = LuraIndigo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    
                    // Mini progress bar
                    LinearProgressIndicator(
                        progress = { book.progressPercentage / 100f },
                        modifier = Modifier
                            .width(60.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = LuraIndigo,
                        trackColor = LuraObsidian.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}
