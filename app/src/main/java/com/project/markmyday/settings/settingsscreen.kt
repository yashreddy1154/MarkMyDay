package com.project.markmyday.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.R
import com.project.markmyday.viewmodel.SettingsUiState
import com.project.markmyday.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val scrollState = rememberScrollState()

    // Handle logout event from ViewModel
    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.settings), 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Profile Section
            SettingsSection(
                title = stringResource(R.string.profile_details),
                icon = Icons.Default.Person
            ) {
                when (val state = uiState) {
                    is SettingsUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    is SettingsUiState.Success -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.profile.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = state.profile.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = state.profile.role.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    is SettingsUiState.Error -> {
                        Text(stringResource(R.string.error_profile, state.message), color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // 2. Preferences Section
            SettingsSection(
                title = stringResource(R.string.preferences),
                icon = Icons.Default.Settings
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Language Toggle
                    val languages = listOf(
                        "English" to "en",
                        "Hindi" to "hi",
                        "Telugu" to "te"
                    )

                    Column {
                        Text(stringResource(R.string.language), style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            languages.forEach { (name, code) ->
                                FilterChip(
                                    selected = currentLanguage.contains(code),
                                    onClick = { viewModel.setLanguage(code) },
                                    label = { Text(name) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.alpha(0.5f))

                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.dark_mode))
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode(it) }
                        )
                    }

                    // Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.notifications))
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
                    }
                }
            }

            // 3. More Section
            SettingsSection(
                title = stringResource(R.string.more),
                icon = Icons.Default.Add
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ClickableSettingsItem(
                        icon = Icons.Default.Policy,
                        title = stringResource(R.string.terms_and_policies),
                        onClick = onNavigateToTerms
                    )
                    ClickableSettingsItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.about_app),
                        onClick = onNavigateToAbout
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // 4. Creators Section
            CreatorsSection()

            Spacer(modifier = Modifier.height(60.dp))

            // 5. Logout Button
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.logout), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CreatorsSection() {
    val creators = listOf(
        Creator("Tarak", stringResource(R.string.android_dev), R.drawable.tarak),
        Creator("Teja Reddy", stringResource(R.string.android_dev), R.drawable.teja),
        Creator("Yash", stringResource(R.string.android_dev), R.drawable.yash)
    )
    val pagerState = rememberPagerState(pageCount = { creators.size })

    // Auto-scroll logic
    LaunchedEffect(creators) {
        while (true) {
            yield()
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % creators.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_creators),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.align(Alignment.Start).padding(top = 24.dp, bottom = 24.dp)
        )

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 60.dp),
            modifier = Modifier.height(380.dp)
        ) { page ->
            val creator = creators[page]
            
            Card(
                modifier = Modifier
                    .graphicsLayer {
                        val pageOffset = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue
                        
                        // 3D rotation effect
                        rotationY = lerp(
                            start = 0f,
                            stop = 40f,
                            fraction = pageOffset.coerceIn(0f, 1f)
                        ) * (if (pagerState.currentPage > page) 1f else -1f)

                        // Scale effect
                        val scale = lerp(
                            start = 1f,
                            stop = 0.85f,
                            fraction = pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = scale
                        scaleY = scale
                        
                        // Transparency effect
                        alpha = lerp(
                            start = 1f,
                            stop = 0.5f,
                            fraction = pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .fillMaxWidth()
                    .aspectRatio(0.8f),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = creator.imageRes),
                        contentDescription = creator.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                    startY = 300f
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = creator.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = creator.role,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
        }
    }
}

data class Creator(val name: String, val role: String, val imageRes: Int)

@Composable
fun ClickableSettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
