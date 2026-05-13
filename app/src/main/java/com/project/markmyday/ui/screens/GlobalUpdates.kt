package com.project.markmyday.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.project.markmyday.R
import com.project.markmyday.data.model.GNewsArticle
import com.project.markmyday.ui.components.DashboardBottomBar
import com.project.markmyday.ui.components.DashboardTopBar
import com.project.markmyday.viewmodel.GlobalUpdatesViewModel
import com.project.markmyday.viewmodel.NewsState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalUpdateScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    viewModel: GlobalUpdatesViewModel = viewModel()
) {
    val context = LocalContext.current
    val hotNewsState by viewModel.hotNews.collectAsState()
    val nationalNewsState by viewModel.nationalNews.collectAsState()
    val internationalNewsState by viewModel.internationalNews.collectAsState()
    val gkNewsState by viewModel.gkNews.collectAsState()
    val schoolNewsState by viewModel.schoolNews.collectAsState()
    val sscNewsState by viewModel.sscNews.collectAsState()
    
    val weather by viewModel.weatherState.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()

    var locationPermissionGranted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.title_global_updates),
                onBackClick = onBack,
                onNotificationClick = { /* Handle notifications */ },
                isBoldBackIcon = true
            )
        },
        bottomBar = {
            DashboardBottomBar(
                currentRoute = "global_updates",
                onNavigate = onNavigate
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Date and Weather Section (as per sketch: DD.MM.YYYY | DATE | WEATHER)
            item {
                WeatherDateSection(currentDate, weather)
            }

            // 2. Hot News Horizontal Scroll
            item {
                HorizontalNewsSection(
                    title = stringResource(R.string.hot_news),
                    state = hotNewsState
                )
            }

            // 3. International and National Categories
            item {
                CategoryGridSection(
                    onInternationalClick = { /* Filter */ },
                    onNationalClick = { /* Filter */ }
                )
            }

            // 5. School Related News
            item {
                VerticalNewsSection(
                    title = stringResource(R.string.school_news),
                    state = schoolNewsState
                )
            }

            // 6. GK for School Children
            item {
                VerticalNewsSection(
                    title = stringResource(R.string.gk_news),
                    state = gkNewsState
                )
            }

            // 6.5 SSC Related News
            item {
                VerticalNewsSection(
                    title = stringResource(R.string.ssc_news),
                    state = sscNewsState
                )
            }

            // 7. National highlights
            item {
                VerticalNewsSection(
                    title = stringResource(R.string.national_news),
                    state = nationalNewsState
                )
            }

            // 8. International highlights
            item {
                VerticalNewsSection(
                    title = stringResource(R.string.intl_news),
                    state = internationalNewsState
                )
            }
        }
    }
}

@Composable
fun WeatherDateSection(date: String, weather: com.project.markmyday.data.model.WeatherData?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "DATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            VerticalDivider(modifier = Modifier.height(30.dp), thickness = 1.dp)
            
            if (weather != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = weather.temperature,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "WEATHER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalNewsSection(title: String, state: NewsState) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        }
        
        when (state) {
            is NewsState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
            is NewsState.Success -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.articles) { article ->
                        HotNewsCard(article)
                    }
                }
            }
            is NewsState.Error -> {
                Text(text = state.message, modifier = Modifier.padding(20.dp), color = Color.Gray)
            }
        }
    }
}

@Composable
fun HotNewsCard(article: GNewsArticle) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clickable {
                article.url?.let {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                }
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box {
            AsyncImage(
                model = article.image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.markmydayicon),
                error = painterResource(R.drawable.markmydayicon)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Text(
                text = article.title ?: "",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CategoryGridSection(onInternationalClick: () -> Unit, onNationalClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CategoryCard(
            title = stringResource(R.string.intl_news),
            icon = Icons.Default.Public,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f),
            onClick = onInternationalClick
        )
        CategoryCard(
            title = stringResource(R.string.national_news),
            icon = Icons.Default.Flag,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f),
            onClick = onNationalClick
        )
    }
}

@Composable
fun CategoryCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        }
    }
}

@Composable
fun VerticalNewsSection(title: String, state: NewsState) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        when (state) {
            is NewsState.Loading -> {
                repeat(3) { NewsItemShimmer() }
            }
            is NewsState.Success -> {
                state.articles.take(5).forEach { article ->
                    NewsListItem(article) {
                        article.url?.let {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                        }
                    }
                }
            }
            is NewsState.Error -> {
                Text(text = state.message, modifier = Modifier.padding(20.dp), color = Color.Gray)
            }
        }
    }
}

@Composable
fun NewsListItem(article: GNewsArticle, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = article.image,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.markmydayicon),
                error = painterResource(R.drawable.markmydayicon)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = article.title ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.source?.name ?: "News",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun NewsItemShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(Color.LightGray.copy(alpha = 0.3f)))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(Color.LightGray.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp).background(Color.LightGray.copy(alpha = 0.3f)))
        }
    }
}
