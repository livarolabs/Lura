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
import androidx.compose.ui.text.style.TextAlign
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
fun BookGridItem(
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
            containerColor = LuraObsidian.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Cover Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.67f)
                    .background(LuraObsidian.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverImagePath != null) {
                    AsyncImage(
                        model = book.coverImagePath,
                        contentDescription = "Cover of ${book.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback cover
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LuraIndigo.copy(alpha = 0.2f))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = LuraIndigo.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = LuraGhostWhite.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                    }
                }
                
                // Reading status badge
                if (book.readingStatus != ReadingStatus.UNREAD) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = when (book.readingStatus) {
                            ReadingStatus.READING -> LuraIndigo
                            ReadingStatus.FINISHED -> Color(0xFF4CAF50)
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = when (book.readingStatus) {
                                ReadingStatus.READING -> "Reading"
                                ReadingStatus.FINISHED -> "Finished"
                                else -> ""
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }
            
            // Book info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = LuraGhostWhite,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = LuraGhostWhite.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
                
                // Progress indicator
                if (book.progressPercentage > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { book.progressPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = LuraIndigo,
                        trackColor = LuraObsidian.copy(alpha = 0.3f),
                    )
                    Text(
                        text = "${book.progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = LuraGhostWhite.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
