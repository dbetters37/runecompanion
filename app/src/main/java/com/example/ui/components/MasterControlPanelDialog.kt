package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.OsrsSkill
import com.example.data.models.TaskXpConfig
import com.example.data.models.TaskXpManager
import com.example.ui.theme.*

@Composable
fun MasterControlPanelDialog(
    onDismiss: () -> Unit,
    onTriggerTask: (TaskXpConfig, Long, Long) -> Unit,
    onResetAllData: () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { TaskXpManager(context) }

    var xpMultiplier by remember { mutableStateOf(manager.getXpMultiplier()) }
    var customPackageInput by remember { mutableStateOf("") }
    var customAppList by remember { mutableStateOf(manager.getCustomAppPackages().toList()) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Tasks & XP Rates, 1: Global Multiplier & Apps, 2: Skill XP Testing

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .border(2.dp, OsrsGold, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = OsrsLeatherDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⚙️", fontSize = 24.sp)
                        Column {
                            Text("MASTER CONTROL PANEL", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("XP Rates, Real-Life App Tracking & Task Config", color = OsrsParchment, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_master_control_panel")) {
                        Text("❌", color = OsrsTextYellow)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OsrsLeatherMedium, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("📋 Tasks & Rates", "⚡ XP Multiplier & Apps", "🎯 Test Skills")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Button(
                            onClick = { selectedTab = index },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) OsrsRedFrame else Color.Transparent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("control_panel_tab_$index")
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) OsrsTextYellow else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (selectedTab) {
                    0 -> {
                        // Tasks & Editable XP Rates
                        Text("Edit XP amounts, XP/min rates & GP for real-life tasks. Click 'Test Trigger' to grant XP:", color = OsrsParchment, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(TaskXpConfig.ALL_DEFAULT_TASKS) { task ->
                                var currentXp by remember(task.taskId, xpMultiplier) {
                                    mutableStateOf(manager.getTaskXp(task.taskId, task.defaultXp).toString())
                                }
                                var currentXpPerMin by remember(task.taskId, xpMultiplier) {
                                    mutableStateOf(manager.getTaskXpPerMin(task.taskId, task.defaultXpPerMin).toString())
                                }
                                var currentGp by remember(task.taskId, xpMultiplier) {
                                    mutableStateOf(manager.getTaskGp(task.taskId, task.defaultGp).toString())
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, OsrsGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                    colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(task.taskName, color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Skill: ${task.defaultSkill.displayName}", color = task.defaultSkill.accentColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(task.description, color = Color.LightGray, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                            Text("⚡ ${currentXpPerMin} XP/min", color = OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = currentXp,
                                                onValueChange = {
                                                    currentXp = it
                                                    it.toLongOrNull()?.let { newXp -> manager.setTaskXp(task.taskId, newXp) }
                                                },
                                                label = { Text("XP Amount", fontSize = 9.sp, color = OsrsParchment) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = OsrsGold,
                                                    unfocusedBorderColor = Color.Gray,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )

                                            OutlinedTextField(
                                                value = currentXpPerMin,
                                                onValueChange = {
                                                    currentXpPerMin = it
                                                    it.toLongOrNull()?.let { newRate -> manager.setTaskXpPerMin(task.taskId, newRate) }
                                                },
                                                label = { Text("XP / Min", fontSize = 9.sp, color = OsrsParchment) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = OsrsGold,
                                                    unfocusedBorderColor = Color.Gray,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )

                                            OutlinedTextField(
                                                value = currentGp,
                                                onValueChange = {
                                                    currentGp = it
                                                    it.toLongOrNull()?.let { newGp -> manager.setTaskGp(task.taskId, newGp) }
                                                },
                                                label = { Text("GP Reward", fontSize = 9.sp, color = OsrsParchment) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = OsrsGold,
                                                    unfocusedBorderColor = Color.Gray,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )

                                            Button(
                                                onClick = {
                                                    val finalXp = currentXp.toLongOrNull() ?: task.defaultXp
                                                    val finalGp = currentGp.toLongOrNull() ?: task.defaultGp
                                                    onTriggerTask(task, finalXp, finalGp)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.testTag("test_trigger_${task.taskId}")
                                            ) {
                                                Text("⚡ Test", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // XP Multiplier & Custom App Package Overrides
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, OsrsGold, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("⚡ GLOBAL XP MULTIPLIER", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Boost all real-life XP drops & task rewards instantly:", color = OsrsParchment, fontSize = 11.sp)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val multipliers = listOf(1.0f, 2.0f, 5.0f, 10.0f, 25.0f)
                                        multipliers.forEach { mult ->
                                            val isSel = xpMultiplier == mult
                                            Button(
                                                onClick = {
                                                    xpMultiplier = mult
                                                    manager.setXpMultiplier(mult)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSel) Color(0xFF00B4D8) else Color(0xFF381C10)
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("${mult.toInt()}x", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, OsrsGold, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("📱 CUSTOM APP PACKAGE TRACKER", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Add any Android package name (e.g., com.duolingo, com.spotify.music) to auto-grant XP when notifications arrive:", color = OsrsParchment, fontSize = 11.sp)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = customPackageInput,
                                            onValueChange = { customPackageInput = it },
                                            placeholder = { Text("e.g. com.duolingo", color = Color.Gray, fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = OsrsGold,
                                                unfocusedBorderColor = Color.Gray,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        Button(
                                            onClick = {
                                                if (customPackageInput.isNotBlank()) {
                                                    manager.addCustomAppPackage(customPackageInput)
                                                    customAppList = manager.getCustomAppPackages().toList()
                                                    customPackageInput = ""
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.testTag("add_custom_package_button")
                                        ) {
                                            Text("Add", color = OsrsTextYellow, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Tracked Custom Packages (${customAppList.size}):", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    customAppList.forEach { pkg ->
                                        Text("• $pkg", color = Color.LightGray, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Direct Skill XP Testing
                        Text("Grant +10,000 XP directly to any OSRS skill:", color = OsrsParchment, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(OsrsSkill.entries) { skill ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, skill.accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    colors = CardDefaults.cardColors(containerColor = OsrsLeatherMedium)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(skill.displayName, color = skill.accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(skill.description, color = Color.LightGray, fontSize = 10.sp)
                                        }

                                        Button(
                                            onClick = {
                                                val dummyConfig = TaskXpConfig("test_${skill.name}", "Tested ${skill.displayName}", skill, 10000L, 2500L, "⚡", "Manual Control Panel XP test")
                                                onTriggerTask(dummyConfig, (10000L * xpMultiplier).toLong(), (2500L * xpMultiplier).toLong())
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.testTag("test_skill_${skill.name}")
                                        ) {
                                            Text("+10,000 XP", color = OsrsTextYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Close & Reset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onResetAllData,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381C10)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                    ) {
                        Text("⚠️ Reset All XP", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, OsrsGold, RoundedCornerShape(6.dp))
                    ) {
                        Text("Done", color = OsrsTextYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
