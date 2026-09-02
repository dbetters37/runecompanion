package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun OsrsChatbox(
    messages: List<String>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF19130D))
            .border(2.dp, OsrsGold, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(messages) { msg ->
                val color = when {
                    msg.startsWith("System:") -> Color(0xFF52B788)
                    msg.contains("CONGRATULATIONS") -> OsrsTextYellow
                    msg.contains("gained") -> OsrsGold
                    msg.contains("Store:") -> Color(0xFF4361EE)
                    else -> OsrsParchmentLight
                }

                Text(
                    text = msg,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = if (msg.contains("CONGRATULATIONS")) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
