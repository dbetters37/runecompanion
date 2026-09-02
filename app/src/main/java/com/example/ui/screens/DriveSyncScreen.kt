package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.DriveSyncLogEntity
import com.example.data.drive.DriveAuthState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriveSyncScreen(
    localTextLogContent: String,
    syncLogs: List<DriveSyncLogEntity>,
    syncStatusMessage: String?,
    driveAuthState: DriveAuthState,
    onAuthenticateDrive: (String) -> Unit,
    onCreateFolder: () -> Unit,
    onDisconnectDrive: () -> Unit,
    onTriggerSync: () -> Unit,
    onRefreshTextLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Text File Preview, 1: Sync History
    var showAuthDialog by remember { mutableStateOf(false) }
    var inputEmail by remember { mutableStateOf(driveAuthState.accountEmail ?: "dbetters37@gmail.com") }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Text("Google Drive OAuth Sign-In", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Enter your Google Account email to authenticate and grant permission to create the dedicated pet history folder.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text("Google Account Email", color = Color(0xFF00F5D4)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F5D4),
                            unfocusedBorderColor = Color(0xFF7209B7),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "OAuth Scope: https://www.googleapis.com/auth/drive.file",
                        color = Color(0xFFFFD166),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAuthenticateDrive(inputEmail)
                        showAuthDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4))
                ) {
                    Text("Authenticate & Connect", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0F2B1A)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07140B))
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GOOGLE DRIVE SYNC & .TXT FILE",
                    color = Color(0xFFFFD166),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Automatic text file log for long-term memory retrieval",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = {
                onTriggerSync()
                onRefreshTextLog()
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color(0xFF00F5D4)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Authentication & Dedicated Folder Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1D0938),
            border = BorderStroke(1.dp, Color(0xFF431D75)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = if (driveAuthState.isAuthenticated) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = "Auth Status",
                            tint = if (driveAuthState.isAuthenticated) Color(0xFF00F5D4) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (driveAuthState.isAuthenticated) "AUTHENTICATED WITH GOOGLE DRIVE" else "NOT AUTHENTICATED",
                            color = if (driveAuthState.isAuthenticated) Color(0xFF00F5D4) else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Scope: drive.file",
                        color = Color(0xFFFFD166),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Email & Account Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "User", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Account: ${driveAuthState.accountEmail ?: "dbetters37@gmail.com"}",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dedicated Folder Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Folder, contentDescription = "Folder", tint = Color(0xFFFFD166), modifier = Modifier.size(16.dp))
                    Column {
                        Text(
                            text = "Dedicated Folder: ${driveAuthState.dedicatedFolderName ?: "Lifelong Pet History Logs"}",
                            color = Color(0xFFFFD166),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Folder ID: ${driveAuthState.dedicatedFolderId ?: "drive_folder_shaman_pet_logs_001"}",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAuthDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7209B7)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Auth", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (driveAuthState.isAuthenticated) "Re-Authenticate" else "Auth Drive", fontSize = 10.sp)
                    }

                    Button(
                        onClick = { onCreateFolder() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A0CA3)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "Folder", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Folder", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            onTriggerSync()
                            onRefreshTextLog()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync", tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Banner
        if (!syncStatusMessage.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2B1352),
                border = BorderStroke(1.dp, Color(0xFF00F5D4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Status",
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(text = syncStatusMessage, color = Color.White, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Sub tab toggle (0: View Text File, 1: Sync Activity Logs)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { activeSubTab = 0 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == 0) Color(0xFF7209B7) else Color(0xFF241042)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Text File",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Memory .txt File", fontSize = 11.sp)
            }

            Button(
                onClick = { activeSubTab = 1 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == 1) Color(0xFF7209B7) else Color(0xFF241042)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Logs",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Logs (${syncLogs.size})", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeSubTab == 0) {
            // Text File Terminal Preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF3B1566), RoundedCornerShape(16.dp)),
                color = Color(0xFF130726)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FILE: lifelong_pet_memory_log.txt",
                            color = Color(0xFF00F5D4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF251147)
                        ) {
                            Text(
                                text = "Drive Folder Synced",
                                color = Color(0xFFFFD166),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0A1F13), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = localTextLogContent,
                            color = Color(0xFFE2C6FF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            // Sync Activity Logs List
            if (syncLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sync activity recorded yet.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(syncLogs) { log ->
                        SyncLogCard(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun SyncLogCard(log: DriveSyncLogEntity) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val dateStr = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color(0xFF1E0A38)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = log.fileName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = log.status, color = if (log.status == "SUCCESS") Color(0xFF00F5D4) else Color(0xFFFF70A6), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = log.syncDetail, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = dateStr, color = Color.Gray, fontSize = 9.sp)
        }
    }
}

