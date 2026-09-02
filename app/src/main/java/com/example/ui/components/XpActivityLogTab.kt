package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ActivityLog
import com.example.data.models.OsrsSkill
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun XpActivityLogTab(
    activityLogs: List<ActivityLog>,
    activeAfkName: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedSkillFilter by remember { mutableStateOf<OsrsSkill?>(null) }

    // Live 10-second countdown ticker for active AFK skilling
    var tickCountdownSec by remember { mutableIntStateOf(10) }
    LaunchedEffect(activeAfkName) {
        tickCountdownSec = 10
        while (true) {
            kotlinx.coroutines.delay(1000L)
            if (tickCountdownSec <= 1) {
                tickCountdownSec = 10
            } else {
                tickCountdownSec--
            }
        }
    }

    val filteredLogs = remember(activityLogs, selectedSkillFilter) {
        if (selectedSkillFilter == null) {
            activityLogs
        } else {
            activityLogs.filter { it.skill == selectedSkillFilter }
        }
    }

    val totalXpEarned = remember(activityLogs) {
        activityLogs.sumOf { it.xpGained }
    }

    val totalGpEarned = remember(activityLogs) {
        activityLogs.sumOf { it.coinsGained }
    }

    val topSkill = remember(activityLogs) {
        activityLogs.groupBy { it.skill }
            .maxByOrNull { entry -> entry.value.sumOf { it.xpGained } }?.key
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(OsrsLeatherMedium)
            .border(2.dp, OsrsGold, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag("xp_activity_log_tab"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header Title
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "📈 LATEST XP SUMMARY & LOGS",
                    color = OsrsTextYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Real-time record of all experience earned, actions performed, and rewards gained!",
                    color = OsrsParchment,
                    fontSize = 11.sp
                )
            }
        }

        // 1.5. Live AFK Countdown Timer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeAfkName != null) Color(0xFF1E2D1F) else Color(0xFF221A14)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (activeAfkName != null) Color(0xFF4CAF50) else Color(0xFF5A4432)
                )
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (activeAfkName != null) "⏱️ LIVE AFK SKILLING TIMER" else "⏱️ AFK TICK TIMER",
                                color = if (activeAfkName != null) Color(0xFF81C784) else OsrsParchment,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (activeAfkName != null) {
                            Surface(
                                color = Color(0xFF2E6B38),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "NEXT TICK IN ${tickCountdownSec}s ⚡",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Idle (No station active)",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (activeAfkName != null) {
                        Text(
                            text = "Currently training: $activeAfkName",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                        val progress = (10 - tickCountdownSec) / 10f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF111E12))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                    } else {
                        Text(
                            text = "Start an AFK station in POH, Slayer, Farm, or Seafaring Port to see live XP ticks counting down here!",
                            color = OsrsParchment,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // 2. Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricCard(
                    title = "Total XP",
                    value = "+${formatNumber(totalXpEarned)} XP",
                    valueColor = OsrsGold,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total GP",
                    value = "+${formatNumber(totalGpEarned)} GP",
                    valueColor = Color(0xFFFFD700),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Actions Logged",
                    value = "${activityLogs.size}",
                    valueColor = OsrsTextWhite,
                    modifier = Modifier.weight(1f)
                )
                if (topSkill != null) {
                    MetricCard(
                        title = "Top Skill",
                        value = "${topSkill.iconSymbol} ${topSkill.displayName}",
                        valueColor = OsrsTextYellow,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }

        // 3. Filter Chips by Skill
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Filter by Skill:", color = OsrsParchment, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        label = "All Skills (${activityLogs.size})",
                        isSelected = selectedSkillFilter == null,
                        onClick = { selectedSkillFilter = null }
                    )

                    OsrsSkill.entries.forEach { skill ->
                        val count = activityLogs.count { it.skill == skill }
                        if (count > 0) {
                            FilterChip(
                                label = "${skill.iconSymbol} ${skill.displayName} ($count)",
                                isSelected = selectedSkillFilter == skill,
                                onClick = { selectedSkillFilter = skill }
                            )
                        }
                    }
                }
            }
        }

        // 4. Activity Logs List
        if (filteredLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OsrsLeatherDark)
                        .border(1.dp, OsrsGold, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📜", fontSize = 32.sp)
                        Text(
                            text = if (selectedSkillFilter != null) "No recent logs for ${selectedSkillFilter?.displayName}." else "No XP logs recorded yet!",
                            color = OsrsTextYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Train skills, use Sawmill / POH stations, complete tasks, or do real-life activities to log XP gains!",
                            color = OsrsParchment,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                ActivityLogCard(log = log)
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(OsrsLeatherDark)
            .border(1.dp, Color(0xFF4A3828), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = OsrsParchment, fontSize = 9.sp)
            Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) OsrsRedFrame else OsrsLeatherDark)
            .border(1.dp, if (isSelected) OsrsGold else Color(0xFF4A3828), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) OsrsTextYellow else OsrsParchment,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ActivityLogCard(log: ActivityLog) {
    val timeFormatted = remember(log.timestamp) {
        val sdf = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF2A1E14))
            .border(1.dp, Color(0xFF5A4432), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skill Badge Icon Box
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(OsrsLeatherDark)
                    .border(1.dp, OsrsGold, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(log.skill.iconSymbol, fontSize = 20.sp)
            }

            // Title, description and details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.title,
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = timeFormatted,
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }

                Text(
                    text = log.description,
                    color = OsrsParchment,
                    fontSize = 10.5.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ +${formatNumber(log.xpGained)} ${log.skill.displayName} XP",
                        color = OsrsGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )

                    if (log.coinsGained > 0) {
                        Text(
                            text = "🪙 +${log.coinsGained} GP",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatNumber(num: Long): String {
    return String.format("%,d", num)
}
