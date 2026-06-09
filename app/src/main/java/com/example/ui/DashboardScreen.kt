package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.example.data.ResistanceTask
import com.example.data.TransmissionLog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: ResistanceViewModel) {
    val tasks by viewModel.uiTasks.collectAsState()
    val transmissions by viewModel.uiTransmissions.collectAsState()
    val syndicateInterference by viewModel.syndicateInterference.collectAsState()
    val networkLatency by viewModel.networkLatency.collectAsState()
    val activeDecryptions by viewModel.activeDecryptions.collectAsState()
    val systemLogs by viewModel.systemLogs.collectAsState()
    val glitchActive by viewModel.glitchActive.collectAsState()

    val haptic = LocalHapticFeedback.current
    val parallaxOffset by rememberParallaxOffset(sensitivity = 1.5f)

    // Central shader timer
    val shaderTimeSpec = rememberInfiniteTransition(label = "shader_time")
    val shaderTime by shaderTimeSpec.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shader_time_anim"
    )

    val runtimeShader = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.graphics.RuntimeShader(CYBERPUNK_SHADER)
        } else null
    }

    // Periodic acoustic alarms/crackles when terminal experiences syndicate interference glitches
    LaunchedEffect(glitchActive) {
        if (glitchActive) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            CyberBeep.playGlitch()
        }
    }

    var currentTab by remember { mutableStateOf(0) } // 0 = COMS, 1 = MISSIONS, 2 = SYSTEMS
    var showAddDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "terminal_scan")
    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_y"
    )

    // Glitchy offset shifts to mimic hardware interface instability
    val glitchOffsetX = if (glitchActive) kotlin.random.Random.nextInt(-5, 5).dp else 0.dp
    val glitchOffsetY = if (glitchActive) kotlin.random.Random.nextInt(-3, 3).dp else 0.dp

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SyndicateDark)
            .cyberpunkMonitor(glitchActive, scanlineY)
            .offset(glitchOffsetX, glitchOffsetY)
            .graphicsLayer {
                if (runtimeShader != null) {
                    runtimeShader.setFloatUniform("resolution", size.width, size.height)
                    runtimeShader.setFloatUniform("time", shaderTime)
                    renderEffect = android.graphics.RenderEffect.createRuntimeShaderEffect(
                        runtimeShader, "composable"
                    ).asComposeRenderEffect()
                }
            }
            .drawBehind {
                // Background scanline overlays for authentic terminal feel
                val linePosition = scanlineY * size.height
                drawLine(
                    color = ConsoleGlow,
                    start = Offset(0f, linePosition),
                    end = Offset(size.width, linePosition),
                    strokeWidth = 2f
                )
            },
        floatingActionButton = {
            if (currentTab == 1) {
                FloatingActionButton(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        CyberBeep.playClick()
                        showAddDialog = true 
                    },
                    containerColor = ResistanceGreen,
                    contentColor = SyndicateDark,
                    modifier = Modifier
                        .testTag("fab_add_mission")
                        .padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Initiate Mission Protocol",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .graphicsLayer {
                    translationX = parallaxOffset.x * 2.5f
                    translationY = parallaxOffset.y * 2.5f
                }
        ) {
            // Ultimate Resistance Hub Branding Banner
            TerminalHeader(
                syndicateInterference = syndicateInterference,
                networkLatency = networkLatency
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dual Grid Selector Navigation Row (Sturdy 48dp Touch Targets)
            TabSelectionRow(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Body Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(
                    targetState = currentTab,
                    modifier = Modifier.fillMaxSize(),
                    label = "tab_navigation_fade"
                ) { tab ->
                    when (tab) {
                        0 -> ComsConsoleTab(
                            transmissions = transmissions,
                            activeDecryptions = activeDecryptions,
                            onDecryptTrigger = { viewModel.decryptTransmission(it) },
                            onTransmitMessage = { sender, text -> viewModel.transmitLog(sender, text) },
                            onPurge = { viewModel.purgeTransmissions() }
                        )
                        1 -> MissionsTab(
                            tasks = tasks,
                            onUpdateStatus = { task, status -> viewModel.updateTaskStatus(task, status) },
                            onDeleteTask = { id -> viewModel.removeTask(id) }
                        )
                        2 -> SystemsNodeTab(
                            systemLogs = systemLogs,
                            syndicateInterference = syndicateInterference,
                            networkLatency = networkLatency
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMissionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, threat, sector, aesKey ->
                viewModel.submitTask(title, desc, threat, sector, aesKey)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TerminalHeader(syndicateInterference: Int, networkLatency: Int) {
    val coroutineScope = rememberCoroutineScope()
    var overwriteMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        // Top level metadata row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PHASE 2 // SYNCHRONIZATION",
                color = ResistanceGreen.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelMedium,
                fontSize = 11.sp,
                letterSpacing = 1.8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            // Double Live Green Dots: emerald-500 from the design HTML
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1250, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "live_dot_alpha"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(LiveEmerald.copy(alpha = alpha), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(LiveEmerald.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Large display header title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "ENGINEER",
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            // Dynamic live operational parameters row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "LATENCY: ${networkLatency}ms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (networkLatency > 50) AlertRed else LiveEmerald,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { overwriteMode = !overwriteMode }
                        .testTag("latency_metric")
                )
                Text(
                    text = "BLOAD: ${syndicateInterference}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (syndicateInterference > 30) AlertRed else ResistanceGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Oracle alert briefing card box (Amber background with 5% alpha, 20% border opacity)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = ResistanceGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    color = ResistanceGreen.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = "[ORACLE]: Ghost-protocol active. The Syndicate's perimeter is breached; proceed with Phase 2 deployment immediately.",
                color = ResistanceGreen,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TabSelectionRow(currentTab: Int, onTabSelected: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val tabs = listOf(
        Triple("COMS_DOWNLINK", Icons.AutoMirrored.Filled.Send, "tab_coms"),
        Triple("TACTICAL_MISSIONS", Icons.AutoMirrored.Filled.List, "tab_missions"),
        Triple("SYS_NODES", Icons.Default.Info, "tab_systems")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .background(ConsoleSurface)
            .padding(6.dp)
    ) {
        tabs.forEachIndexed { index, pair ->
            val isSelected = currentTab == index
            val outlineColor = if (isSelected) ResistanceGreen.copy(alpha = 0.3f) else Color.Transparent
            val textColor = if (isSelected) ResistanceGreen else TerminalGray

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) ResistanceGreen.copy(alpha = 0.1f) else Color.Transparent)
                    .border(1.dp, outlineColor, RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        CyberBeep.playClick()
                        onTabSelected(index)
                    }
                    .testTag(pair.third)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = pair.second,
                        contentDescription = pair.first,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = pair.first.substringBefore("_"),
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ComsConsoleTab(
    transmissions: List<TransmissionLog>,
    activeDecryptions: Set<Int>,
    onDecryptTrigger: (TransmissionLog) -> Unit,
    onTransmitMessage: (String, String) -> Unit,
    onPurge: () -> Unit
) {
    var inputSender by remember { mutableStateOf("") }
    var inputMessage by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedChannelProfile by remember { mutableStateOf("STATIONARY HANDSHAKE") }
    val channelProfiles = listOf("STATIONARY HANDSHAKE", "MILITARY BURST FREQ", "ORACLE DECOY BUBBLE")

    val filteredTransmissions by remember(transmissions, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                transmissions
            } else {
                transmissions.filter {
                    it.sender.contains(searchQuery, ignoreCase = true) ||
                            it.decryptedContent.contains(searchQuery, ignoreCase = true) ||
                            it.encryptedContent.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Logging terminal feed
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .background(ConsoleSurface)
                .padding(14.dp)
        ) {
            if (transmissions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Idle Feed",
                        tint = TerminalGray,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CONSOLE DOWNLINK QUIET",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TerminalGray
                    )
                }
            } else {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECURE LOG DOWNLINK BUFFER",
                            color = InfoCyan,
                            style = MaterialTheme.typography.labelMedium
                        )
                        IconButton(
                            onClick = onPurge,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("purge_terminal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Purge logs",
                                tint = AlertRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Searching text field inside Coms Log Feed
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("FILTER DOWNLINK BY SENDER OR CIPHER...", color = TerminalGray, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Log Feed", tint = InfoCyan, modifier = Modifier.size(16.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InfoCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = ConsoleText,
                            unfocusedContainerColor = SyndicateDark.copy(alpha = 0.4f),
                            focusedContainerColor = SyndicateDark.copy(alpha = 0.5f)
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .height(44.dp)
                            .testTag("coms_search_input")
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = false
                    ) {
                        items(filteredTransmissions, key = { it.id }) { log ->
                            TransmissionLogItem(
                                log = log,
                                isDecrypting = activeDecryptions.contains(log.id),
                                onDecrypt = { onDecryptTrigger(log) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Uplink Transmitter Panel
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .background(ConsoleSurface)
                .padding(14.dp)
        ) {
            Text(
                text = "COMMS UNIPORT BROADCASTER",
                color = ResistanceGreen,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Two input fields
            OutlinedTextField(
                value = inputSender,
                onValueChange = { inputSender = it },
                label = { Text("UPLINK_SENDER (Ex: NODE_09)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ResistanceGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                    focusedTextColor = ResistanceGreen,
                    unfocusedTextColor = ConsoleText,
                    focusedLabelColor = ResistanceGreen,
                    unfocusedLabelColor = TerminalGray
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("sender_text_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                label = { Text("RAW_TRANSMISSION_TEXT") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ResistanceGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                    focusedTextColor = ResistanceGreen,
                    unfocusedTextColor = ConsoleText,
                    focusedLabelColor = ResistanceGreen,
                    unfocusedLabelColor = TerminalGray
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("message_text_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Secure Channel Profile selection horizontal grid row matching style perfectly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                channelProfiles.forEach { profile ->
                    val isSelected = selectedChannelProfile == profile
                    val borderCol = if (isSelected) ResistanceGreen.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.05f)
                    val bgCol = if (isSelected) ResistanceGreen.copy(alpha = 0.12f) else SyndicateDark.copy(alpha = 0.3f)
                    val textCol = if (isSelected) ResistanceGreen else TerminalGray

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .background(bgCol, RoundedCornerShape(8.dp))
                            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                            .clickable { selectedChannelProfile = profile }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.substringBefore(" "),
                            color = textCol,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.6.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        val fullMessageText = "[$selectedChannelProfile] $inputMessage"
                        onTransmitMessage(inputSender, fullMessageText)
                        inputMessage = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ResistanceGreen,
                    contentColor = SyndicateDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("transmit_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Transmit Message",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENCRYPT & TRANSMIT SECURE FEED",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TransmissionLogItem(
    log: TransmissionLog,
    isDecrypting: Boolean,
    onDecrypt: () -> Unit
) {
    val highlightColor = if (log.isDecrypted) ResistanceGreen else WarningGold

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .background(ConsoleSurface, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (log.isDecrypted) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = if (log.isDecrypted) "Decrypted" else "Encrypted",
                    tint = highlightColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "[${log.sender}]",
                    color = highlightColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "SIG: ${log.signalStrength}%",
                color = TerminalGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (log.isDecrypted) {
            TypewriterText(
                text = log.decryptedContent,
                color = ConsoleText,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val haptic = LocalHapticFeedback.current
            Column {
                if (isDecrypting) {
                    MatrixCrackEffect()
                } else {
                    Text(
                        text = "CIPHER: ${log.encryptedContent}",
                        color = WarningGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        CyberBeep.playAlarm()
                        onDecrypt()
                    },
                    enabled = !isDecrypting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningGold,
                        contentColor = SyndicateDark,
                        disabledContainerColor = WarningGoldDim,
                        disabledContentColor = TerminalGray
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("decrypt_btn_${log.id}")
                ) {
                    if (isDecrypting) {
                        CircularProgressIndicator(
                            color = SyndicateDark,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Run Decryption Algorithm",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CRACK ENCRYPTION",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MissionsTab(
    tasks: List<ResistanceTask>,
    onUpdateStatus: (ResistanceTask, String) -> Unit,
    onDeleteTask: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSectorFilter by remember { mutableStateOf("ALL") }

    val distinctSectors by remember(tasks) {
        derivedStateOf {
            listOf("ALL") + tasks.map { it.targetSector }.distinct().filter { it.isNotBlank() }
        }
    }

    val filteredTasks by remember(tasks, searchQuery, selectedSectorFilter) {
        derivedStateOf {
            tasks.filter { task ->
                val matchesQuery = task.title.contains(searchQuery, ignoreCase = true) ||
                        task.description.contains(searchQuery, ignoreCase = true) ||
                        task.targetSector.contains(searchQuery, ignoreCase = true)
                val matchesSector = selectedSectorFilter == "ALL" || task.targetSector == selectedSectorFilter
                matchesQuery && matchesSector
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (tasks.isNotEmpty()) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("FILTER MISSIONS BY TITLE, DESC, OR CO-GRID...", color = TerminalGray, fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Missions", tint = ResistanceGreen, modifier = Modifier.size(16.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ResistanceGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = ConsoleText,
                    unfocusedContainerColor = SyndicateDark.copy(alpha = 0.4f),
                    focusedContainerColor = SyndicateDark.copy(alpha = 0.5f)
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("mission_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable row of custom designed sector chips matching style perfectly
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(distinctSectors) { sector ->
                    val isSel = selectedSectorFilter == sector
                    val borderCol = if (isSel) ResistanceGreen.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.05f)
                    val bgCol = if (isSel) ResistanceGreen.copy(alpha = 0.12f) else ConsoleSurface
                    val textCol = if (isSel) ResistanceGreen else TerminalGray

                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .background(bgCol, RoundedCornerShape(12.dp))
                            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                            .clickable { selectedSectorFilter = sector }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sector.uppercase(),
                            color = textCol,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, ResistanceGreenDim, RoundedCornerShape(16.dp))
                    .background(ConsoleSurface)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Zero objectives",
                        tint = WarningGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO TACTICAL MISSIONS CONFIGURED",
                        color = WarningGold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Bypass Syndicate networks by pressing the '+' floating key to initiate secure mission plans.",
                        color = ConsoleText,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .background(ConsoleSurface)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "No results",
                        tint = TerminalGray,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO TACTICAL MATCHES LOCATED",
                        color = TerminalGray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Modify your telemetry search key, or rotate the grid sector filters to isolate active plans.",
                        color = ConsoleText,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TacticalMissionItem(
                        task = task,
                        onUpdateStatus = { status -> onUpdateStatus(task, status) },
                        onDeleteTask = { onDeleteTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TacticalMissionItem(
    task: ResistanceTask,
    onUpdateStatus: (String) -> Unit,
    onDeleteTask: () -> Unit
) {
    val statusColor = when (task.status) {
        "ACTIVE" -> ResistanceGreen
        "PENDING" -> WarningGold
        "COMPLETED" -> InfoCyan
        else -> AlertRed
    }

    val threatColor = when (task.threatLevel) {
        "CRITICAL" -> AlertRed
        "HIGH" -> AlertRed.copy(alpha = 0.8f)
        "MEDIUM" -> WarningGold
        else -> ResistanceGreen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .background(ConsoleSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                // Priority badges with bg-color/10 and rounded-full to match HTML precisely
                Box(
                    modifier = Modifier
                        .background(threatColor.copy(alpha = 0.15f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = task.threatLevel,
                        color = threatColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDeleteTask,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("delete_task_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Abort Task",
                        tint = AlertRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = task.description,
            color = ConsoleText,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SECTOR: ${task.targetSector}",
                    color = TerminalGray,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "KEY: ${task.decryptionKey}",
                    color = TerminalGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "STATUS: ${task.status}",
                color = statusColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(color = ConsoleGlow, thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        // Actions Row with 48dp Tap support
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (task.status != "ACTIVE" && task.status != "COMPLETED") {
                Button(
                    onClick = { onUpdateStatus("ACTIVE") },
                    colors = ButtonDefaults.buttonColors(containerColor = ResistanceGreenGlow),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("update_status_active_${task.id}")
                ) {
                    Text(
                        text = "LAUNCH CODE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ResistanceGreen
                    )
                }
            }

            if (task.status == "ACTIVE") {
                Button(
                    onClick = { onUpdateStatus("COMPLETED") },
                    colors = ButtonDefaults.buttonColors(containerColor = InfoCyanDim),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("update_status_complete_${task.id}")
                ) {
                    Text(
                        text = "COMPLETE SECURE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = InfoCyan
                    )
                }
            }

            if (task.status != "COMPROMISED" && task.status != "COMPLETED") {
                Button(
                    onClick = { onUpdateStatus("COMPROMISED") },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRedDim),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("update_status_failsafe_${task.id}")
                ) {
                    Text(
                        text = "EMERGENCY FAILSAFE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlertRed
                    )
                }
            }
        }
    }
}

val ResistanceGreenGlow = Color(0x2200FF66)

@Composable
fun SystemsNodeTab(systemLogs: List<String>, syndicateInterference: Int, networkLatency: Int) {
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    var firewallPurgeInProgress by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Highly immersive dynamic radar sweep using Canvas drawing directly
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, ResistanceGreen, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = ConsoleSurface)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val transition = rememberInfiniteTransition(label = "radar")
                    val radarAngle by transition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(4000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "angle"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2.3f

                        // Draw background radar concentric circles
                        drawCircle(
                            color = ConsoleGlow,
                            radius = radius,
                            center = center,
                            style = Stroke(width = 1.5f)
                        )
                        drawCircle(
                            color = ConsoleGlow,
                            radius = radius * 0.6f,
                            center = center,
                            style = Stroke(width = 1.0f)
                        )
                        drawCircle(
                            color = ConsoleGlow,
                            radius = radius * 0.2f,
                            center = center,
                            style = Stroke(width = 1.0f)
                        )

                        // Radar axes lines
                        drawLine(
                            color = ConsoleGlow,
                            start = Offset(center.x - radius, center.y),
                            end = Offset(center.x + radius, center.y),
                            strokeWidth = 1.0f
                        )
                        drawLine(
                            color = ConsoleGlow,
                            start = Offset(center.x, center.y - radius),
                            end = Offset(center.x, center.y + radius),
                            strokeWidth = 1.0f
                        )

                        // Mock Resistance Station Nodes
                        drawCircle(
                            color = InfoCyan,
                            radius = 6.0f,
                            center = Offset(center.x - radius * 0.4f, center.y - radius * 0.3f)
                        )
                        drawCircle(
                            color = ResistanceGreen,
                            radius = 7.0f,
                            center = Offset(center.x + radius * 0.5f, center.y + radius * 0.2f)
                        )

                        // Syndicate intrusion signal if interference is high
                        if (syndicateInterference > 25) {
                            drawCircle(
                                color = AlertRed,
                                radius = 8.0f,
                                center = Offset(center.x + radius * 0.1f, center.y - radius * 0.5f)
                            )
                        }
                    }

                    // Rotating canvas layer for sweeping line (rotated purely by GPU via graphicsLayer)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = radarAngle
                            }
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2.3f

                        // Dynamic sweeping search indicator line
                        drawLine(
                            color = ResistanceGreen,
                            start = center,
                            end = Offset(center.x + radius, center.y),
                            strokeWidth = 3.0f
                        )
                    }

                    // Labels on Canvas
                    Text(
                        text = "RESISTANCE GRID CO-RADAR SWEEP",
                        color = ResistanceGreen,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    )

                    Text(
                        text = if (syndicateInterference > 25) "INTRUDER THREAT CONFIRMED" else "SYSTEMS FULLY SHIELDED",
                        color = if (syndicateInterference > 25) AlertRed else ResistanceGreen,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    )
                }
            }
        }

        item {
            // Anti-Syndicate countermeasure interactive card checklists
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ResistanceGreenDim, RoundedCornerShape(8.dp))
                    .background(ConsoleSurface)
                    .padding(12.dp)
            ) {
                Text(
                    text = "ANTIVIRAL COUNTERMEASURE ACTIONS",
                    color = ResistanceGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Diagnostic Check 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { check1 = !check1 }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = check1,
                        onCheckedChange = { check1 = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ResistanceGreen,
                            uncheckedColor = ResistanceGreenDim,
                            checkmarkColor = SyndicateDark
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Scrub Memory Allocation Tables",
                            color = ConsoleText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ensures zero telemetry memory leakage to the Syndicate mainframe.",
                            color = TerminalGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                HorizontalDivider(color = ConsoleGlow, thickness = 1.dp)

                // Diagnostic Check 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { check2 = !check2 }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = check2,
                        onCheckedChange = { check2 = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ResistanceGreen,
                            uncheckedColor = ResistanceGreenDim,
                            checkmarkColor = SyndicateDark
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Rotate Port Security Handshakes",
                            color = ConsoleText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Changes internal secure network keys dynamically every 3 seconds.",
                            color = TerminalGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // High visual action trigger
                Button(
                    onClick = {
                        scope.launch {
                            firewallPurgeInProgress = true
                            delay(2000)
                            firewallPurgeInProgress = false
                            check1 = true
                            check2 = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("firewall_purge_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (firewallPurgeInProgress) WarningGold else InfoCyan,
                        contentColor = SyndicateDark
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    if (firewallPurgeInProgress) {
                        CircularProgressIndicator(color = SyndicateDark, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMPILING STRATEGIC ENCRYPT BLOCKERS...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PURGE THREATS & SEAL MAINBOARD",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .background(ConsoleSurface)
                    .padding(14.dp)
            ) {
                Text(
                    text = "SYSTEM INTEGRITY MATRIX",
                    color = TerminalGray,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // PostgreSQL Load card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "POSTGRESQL LOAD",
                            color = TerminalGray,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${Math.round((12.4 + (syndicateInterference / 10.0)) * 10.0) / 10.0}%",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Redis Latency card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "REDIS LATENCY",
                            color = TerminalGray,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${Math.round(((networkLatency / 12.0).coerceAtLeast(1.0)) * 10.0) / 10.0}ms",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // CI/CD Health card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "CI/CD HEALTH",
                            color = TerminalGray,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PASS",
                            color = LiveEmerald,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Test Coverage card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "TEST COVERAGE",
                            color = TerminalGray,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "89.2%",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = ConsoleSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LIVE SHIELD WORKLOAD TELEMETRY",
                        color = InfoCyan,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SyndicateDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                            .height(130.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        systemLogs.toList().forEach { log ->
                            val color = when {
                                log.contains("[ALRT]") -> AlertRed
                                log.contains("[SYS]") -> ResistanceGreen
                                log.contains("[INF]") -> InfoCyan
                                else -> ConsoleText
                            }
                            Text(
                                text = log,
                                color = color,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddMissionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var threat by remember { mutableStateOf("HIGH") }
    var sector by remember { mutableStateOf("SECTOR_") }
    var key by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val threatLevels = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, ResistanceGreen, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp)),
            color = ConsoleSurface,
            contentColor = ConsoleText
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "INITIATE MISSION PROTOCOL DATAENTRY",
                    color = ResistanceGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        validationError = null
                    },
                    label = { Text("MISSION TITLE") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ResistanceGreen,
                        unfocusedBorderColor = ResistanceGreenDim,
                        focusedTextColor = ResistanceGreen,
                        unfocusedTextColor = ConsoleText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { 
                        desc = it
                        validationError = null
                    },
                    label = { Text("STRATEGIC DESCRIPTION") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ResistanceGreen,
                        unfocusedBorderColor = ResistanceGreenDim,
                        focusedTextColor = ResistanceGreen,
                        unfocusedTextColor = ConsoleText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("dialog_desc_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = sector,
                    onValueChange = { 
                        sector = it
                        validationError = null
                    },
                    label = { Text("TARGET CO-GRID SECTOR") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ResistanceGreen,
                        unfocusedBorderColor = ResistanceGreenDim,
                        focusedTextColor = ResistanceGreen,
                        unfocusedTextColor = ConsoleText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_sector_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = key,
                    onValueChange = { 
                        key = it
                        validationError = null
                    },
                    label = { Text("SECURITY AES KEY VISUAL") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ResistanceGreen,
                        unfocusedBorderColor = ResistanceGreenDim,
                        focusedTextColor = ResistanceGreen,
                        unfocusedTextColor = ConsoleText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_key_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Threat Level Selector
                Text(
                    text = "SYNDICATE RISK LEVEL ASSIGNMENT",
                    style = MaterialTheme.typography.labelMedium,
                    color = TerminalGray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    threatLevels.forEach { level ->
                        val isSel = threat == level
                        val bg = if (isSel) ConsoleGlow else Color.Transparent
                        val borderCol = if (isSel) ResistanceGreen else ResistanceGreenDim.copy(alpha = 0.5f)
                        val textCol = if (isSel) ResistanceGreen else TerminalGray

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(bg, RoundedCornerShape(4.dp))
                                .border(1.dp, borderCol, RoundedCornerShape(4.dp))
                                .clickable { threat = level }
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level,
                                color = textCol,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                validationError?.let { err ->
                    Text(
                        text = err,
                        color = AlertRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, AlertRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "CANCEL", style = MaterialTheme.typography.bodyMedium)
                    }

                    Button(
                        onClick = {
                            if (title.trim().isBlank()) {
                                validationError = "* MISSION TITLE LOGICAL SPECIFICATION REQUIRED"
                            } else if (desc.trim().isBlank()) {
                                validationError = "* STRATEGIC INTEL DESCRIPTION LOGICAL SPECIFICATION REQUIRED"
                            } else if (sector.trim().isEmpty() || sector.trim() == "SECTOR_") {
                                validationError = "* TARGET CO-GRID SECTOR CANNOT BE EMPTY"
                            } else {
                                validationError = null
                                onConfirm(title.trim(), desc.trim(), threat, sector.trim(), key.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ResistanceGreen,
                            contentColor = SyndicateDark
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_mission_btn")
                    ) {
                        Text(
                            text = "COMMENCE",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Extensions and helper components for Hacker mode
object CyberBeep {
    private val toneGen = try {
        android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 70)
    } catch (e: Exception) {
        null
    }

    fun playClick() {
        try {
            toneGen?.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 40)
        } catch (e: Exception) {}
    }

    fun playGlitch() {
        try {
            toneGen?.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 60)
        } catch (e: Exception) {}
    }

    fun playAlarm() {
        try {
            toneGen?.startTone(android.media.ToneGenerator.TONE_PROP_PROMPT, 80)
        } catch (e: Exception) {}
    }
}

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.LocalTextStyle.current,
    typingSpeedMs: Long = 20L
) {
    var typedText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        typedText = ""
        text.forEach { char ->
            typedText += char
            delay(typingSpeedMs)
        }
    }
    Text(
        text = typedText,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        fontFamily = fontFamily,
        style = style
    )
}

@Composable
fun MatrixCrackEffect() {
    val symbols = "0101#@$%&8X*?!Z-+_[]{}/\\|~"
    var glitchText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            glitchText = (1..20).map { symbols.random() }.joinToString("")
            delay(60)
        }
    }
    Text(
        text = "DECRUNCHING: >> $glitchText <<",
        color = WarningGold,
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

const val CYBERPUNK_SHADER = """
    uniform shader composable;
    uniform float2 resolution;
    uniform float time;

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;
        
        // CRT curvature
        float2 crtUv = uv * 2.0 - 1.0;
        float offset = crtUv.y / 4.0;
        crtUv.x *= 1.0 + pow(offset, 2.0);
        uv = crtUv / 2.0 + 0.5;

        // If coordinate went out of screen, darken it
        if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
            return half4(0.0, 0.0, 0.0, 1.0);
        }

        // Chromatic Aberration (color convergence drift)
        float aberration = 0.003;
        half r = composable.eval(float2((uv.x + aberration) * resolution.x, uv.y * resolution.y)).r;
        half g = composable.eval(float2(uv.x * resolution.x, uv.y * resolution.y)).g;
        half b = composable.eval(float2((uv.x - aberration) * resolution.x, uv.y * resolution.y)).b;
        half a = composable.eval(fragCoord).a;

        // Vintage micro scanlines
        float scanline = sin(uv.y * 800.0 + time * 10.0) * 0.04;

        return half4(r - scanline, g - scanline, b - scanline, a);
    }
"""

@Composable
fun rememberParallaxOffset(sensitivity: Float = 1.5f): State<androidx.compose.ui.geometry.Offset> {
    val context = androidx.compose.ui.platform.LocalContext.current
    val parallaxOffset = remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as? android.hardware.SensorManager
        val gyroscope = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE)
        val accelerometer = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == android.hardware.Sensor.TYPE_GYROSCOPE) {
                    val dx = event.values[1] * sensitivity
                    val dy = event.values[0] * sensitivity
                    parallaxOffset.value = androidx.compose.ui.geometry.Offset(
                        x = dx.coerceIn(-12f, 12f),
                        y = dy.coerceIn(-12f, 12f)
                    )
                } else if (gyroscope == null && event.sensor.type == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                    // Fallback to accelerometer if gyroscope isn't present
                    val dx = event.values[0] * -sensitivity * 0.15f
                    val dy = event.values[1] * sensitivity * 0.15f
                    parallaxOffset.value = androidx.compose.ui.geometry.Offset(
                        x = dx.coerceIn(-12f, 12f),
                        y = dy.coerceIn(-12f, 12f)
                    )
                }
            }
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null) {
            if (gyroscope != null) {
                sensorManager.registerListener(listener, gyroscope, android.hardware.SensorManager.SENSOR_DELAY_GAME)
            } else if (accelerometer != null) {
                sensorManager.registerListener(listener, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_UI)
            }
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }
    return parallaxOffset
}

fun Modifier.cyberpunkMonitor(
    glitchActive: Boolean,
    scanlineY: Float
): Modifier = this.drawBehind {
    // Draw horizontal vintage scan lines on screen canvas
    val color = if (glitchActive) AlertRed.copy(alpha = 0.08f) else ResistanceGreen.copy(alpha = 0.03f)
    val lineSpacing = 16f
    var currentY = 0f
    while (currentY < size.height) {
        drawLine(
            color = color,
            start = Offset(0f, currentY),
            end = Offset(size.width, currentY),
            strokeWidth = 1.5f
        )
        currentY += lineSpacing
    }

    // Classic Vignette Shadow overlay around screen corners
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
            center = center,
            radius = size.maxDimension / 1.2f
        )
    )
}
