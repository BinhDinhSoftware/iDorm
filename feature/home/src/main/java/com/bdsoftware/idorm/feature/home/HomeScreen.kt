package com.bdsoftware.idorm.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.composed
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.text.HtmlCompat
import com.bdsoftware.idorm.core.designsystem.component.AppTimeline
import com.bdsoftware.idorm.core.designsystem.component.TimelineItemData
import com.bdsoftware.idorm.core.ui.rent.RentBadge
import com.bdsoftware.idorm.core.ui.notification.NotificationItem
import com.bdsoftware.idorm.core.ui.notification.NotificationDetailBottomSheet
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import com.bdsoftware.idorm.core.common.util.formatIsoDate
import com.bdsoftware.idorm.core.common.util.formatShortDate
import com.bdsoftware.idorm.core.network.model.NotificationData
import com.bdsoftware.idorm.core.network.model.NetworkRentItem
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import com.bdsoftware.idorm.core.model.Banner
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onViewAllNotifications: () -> Unit,
    onNavigateToHcmc: () -> Unit,
    onNavigateToRentHistory: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToWifiConfig: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val isWifiActive by viewModel.isWifiActive.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val ads by viewModel.ads.collectAsStateWithLifecycle()
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentWifiSsid by viewModel.currentWifiSsid.collectAsStateWithLifecycle()
    val isAwingConnected by viewModel.isAwingConnected.collectAsStateWithLifecycle()

    val campaignToShow by viewModel.campaignToShow.collectAsStateWithLifecycle()
    var selectedNotification by remember { mutableStateOf<NotificationData?>(null) }

    LaunchedEffect(selectedNotification) {
        selectedNotification?.let {
            if (!it.IsRead) {
                viewModel.markNotificationAsRead(it.Id)
            }
        }
    }

    campaignToShow?.let { campaign ->
        CampaignPopupDialog(
            campaign = campaign,
            onDismiss = { viewModel.dismissCampaign(campaign.Id) },
            onBannerClick = { viewModel.dismissCampaign(campaign.Id) }
        )
    }

    selectedNotification?.let { notification ->
        NotificationDetailBottomSheet(
            notification = notification,
            onDismiss = { selectedNotification = null }
        )
    }

    HomeScreenContent(
        isWifiActive = isWifiActive,
        isRefreshing = isRefreshing,
        isLoading = isLoading,
        onRefresh = viewModel::refresh,
        userProfile = userProfile,
        notifications = notifications,
        ads = ads,
        banners = banners,
        onWifiActionClick = onNavigateToWifiConfig,
        currentWifiSsid = currentWifiSsid,
        isAwingConnected = isAwingConnected,
        onViewAllNotifications = onViewAllNotifications,
        onNotificationClick = { selectedNotification = it },
        onNavigateToHcmc = onNavigateToHcmc,
        onRentHistoryClick = onNavigateToRentHistory,
        onFeedbackClick = onNavigateToFeedback,
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreenContent(
    isWifiActive: Boolean = false,
    isRefreshing: Boolean = false,
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    userProfile: HomeViewModel.UserProfile = HomeViewModel.UserProfile(),
    notifications: List<NotificationData> = emptyList(),
    ads: List<com.bdsoftware.idorm.core.model.PromotionalAd> = emptyList(),
    banners: List<com.bdsoftware.idorm.core.model.Banner> = emptyList(),
    onWifiActionClick: () -> Unit = {},
    currentWifiSsid: String? = null,
    isAwingConnected: Boolean = false,
    onViewAllNotifications: () -> Unit = {},
    onNotificationClick: (NotificationData) -> Unit = {},
    onNavigateToHcmc: () -> Unit = {},
    onRentHistoryClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val maxScrollPx = with(density) { 72.dp.toPx() }
    val scrollProgress by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (lazyListState.firstVisibleItemScrollOffset.toFloat() / maxScrollPx).coerceIn(0f, 1f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ComponentStyles.PrimaryBlue)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ProfileHeaderSection(userProfile = userProfile, scrollProgress = scrollProgress)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color.White)
            ) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                    ) {
                        if (isLoading && banners.isEmpty()) {
                            item { BannerSectionSkeleton() }
                        } else {
                            item { BannerSection(banners = banners) }
                        }

                        item { NationalPrideNotice() }

                        item {
                            QuickActionsGrid(
                                isWifiActive = isWifiActive,
                                onWifiActionClick = onWifiActionClick,
                                onHcmcActionClick = onNavigateToHcmc,
                                onRentHistoryClick = onRentHistoryClick,
                                onFeedbackClick = onFeedbackClick
                            )
                        }

                        if (isLoading && ads.isEmpty()) {
                            item { PromotionalSectionSkeleton() }
                        } else {
                            item { PromotionalSection(ads = ads) }
                        }

                        item {
                            NotificationsPreviewSection(
                                notifications = notifications,
                                onViewAllClick = onViewAllNotifications,
                                onNotificationClick = onNotificationClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderSection(
    userProfile: HomeViewModel.UserProfile,
    scrollProgress: Float
) {
    val avatarSize = (64 - (24 * scrollProgress)).dp
    val headerPaddingVertical = (16 - (8 * scrollProgress)).dp
    val topPadding = (16 - (4 * scrollProgress)).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = topPadding, bottom = headerPaddingVertical),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = userProfile.avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .size(avatarSize)
                .border(
                    width = (2 - (1 * scrollProgress)).dp,
                    color = Color.White.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .padding((2 - (1 * scrollProgress)).dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            if (scrollProgress < 0.8f) {
                Text(
                    text = stringResource(DesignR.string.home_welcome_prefix),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f * (1f - scrollProgress)),
                    modifier = Modifier.alpha(1f - scrollProgress)
                )
            }
            Text(
                text = userProfile.fullName.takeIf { it.isNotBlank() } ?: stringResource(DesignR.string.home_loading),
                style = if (scrollProgress > 0.5f) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BannerSection(banners: List<Banner>) {
    if (banners.isEmpty()) return

    val pageCount = Int.MAX_VALUE
    val startIndex = pageCount / 2
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { pageCount }
    )

    LaunchedEffect(pagerState.settledPage) {
        delay(3000)
        pagerState.animateScrollToPage(pagerState.settledPage + 1)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.8f),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val actualPage = page % banners.size
                BannerCard(banner = banners[actualPage])
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage % banners.size == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) ComponentStyles.PrimaryBlue
                            else Color.LightGray
                        )
                        .animateContentSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun BannerCard(banner: Banner) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = banner.imageUrl,
                contentDescription = banner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFD54F), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = banner.badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = banner.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun NationalPrideNotice() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFF78140F), CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = com.bdsoftware.idorm.core.designsystem.R.drawable.ic_vn_flag),
                contentDescription = "Vietnam Flag",
                tint = Color.Unspecified,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(DesignR.string.home_national_pride_notice),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    isWifiActive: Boolean,
    onWifiActionClick: () -> Unit,
    onHcmcActionClick: () -> Unit,
    onRentHistoryClick: () -> Unit,
    onFeedbackClick: () -> Unit
) {
    val items = listOf(
        com.bdsoftware.idorm.core.model.QuickAction(stringResource(DesignR.string.home_quick_action_rent_history), "Home"),
        com.bdsoftware.idorm.core.model.QuickAction(stringResource(DesignR.string.home_quick_action_hcmc), "Build"),
        com.bdsoftware.idorm.core.model.QuickAction(stringResource(DesignR.string.home_quick_action_wifi), "Wifi"),
        com.bdsoftware.idorm.core.model.QuickAction(stringResource(DesignR.string.home_quick_action_feedback), "Email")
    )

    val gridHeight = (((items.size - 1) / 4) + 1) * 110
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        userScrollEnabled = false
    ) {
        items(items) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        when (item.iconName) {
                            "Wifi" -> onWifiActionClick()
                            "Build" -> onHcmcActionClick()
                            "Home" -> onRentHistoryClick()
                            "Email" -> onFeedbackClick()
                        }
                    }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when(item.iconName) {
                        "Home" -> Icons.Default.Home
                        "Visibility" -> Icons.Default.Visibility
                        "Build" -> Icons.Default.Build
                        "Wifi" -> Icons.Default.Wifi
                        "List" -> Icons.Default.List
                        "Email" -> Icons.Default.Email
                        else -> Icons.Default.Home
                    }
                    Icon(
                        imageVector = iconVector,
                        contentDescription = item.label,
                        tint = ComponentStyles.PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    minLines = 2,
                    lineHeight = 14.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PromotionalSection(ads: List<com.bdsoftware.idorm.core.model.PromotionalAd>) {
    if (ads.isEmpty()) return

    val pageCount = Int.MAX_VALUE
    val startIndex = pageCount / 2
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { pageCount }
    )

    LaunchedEffect(pagerState.settledPage) {
        delay(5000)
        pagerState.animateScrollToPage(pagerState.settledPage + 1)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val actualPage = page % ads.size
            PromotionalItemCard(adItem = ads[actualPage])
        }
    }
}

@Composable
private fun PromotionalItemCard(adItem: com.bdsoftware.idorm.core.model.PromotionalAd) {
    val uriHandler = LocalUriHandler.current
    
    val hasActionText = adItem.ActionText.isNotBlank()
    val onRedirect = {
        adItem.UrlRedirect?.takeIf { it.isNotBlank() }?.let { url ->
            try {
                uriHandler.openUri(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(enabled = !hasActionText) {
                onRedirect()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            if (adItem.BannerUrl.isNotBlank()) {
                AsyncImage(
                    model = adItem.BannerUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = adItem.Overlay / 100f))
                )
            } else if (adItem.GradientStart != null && adItem.GradientEnd != null) {
                val startColor = try { Color(android.graphics.Color.parseColor(adItem.GradientStart)) } catch (e: Exception) { ComponentStyles.PrimaryBlue }
                val endColor = try { Color(android.graphics.Color.parseColor(adItem.GradientEnd)) } catch (e: Exception) { ComponentStyles.PrimaryBlue }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(startColor, endColor)
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(ComponentStyles.PrimaryBlue)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = stringResource(DesignR.string.home_ad_badge),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                adItem.Title?.takeIf { it.isNotBlank() }?.let { title ->
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                adItem.Description?.takeIf { it.isNotBlank() }?.let { description ->
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (hasActionText) {
                    val buttonAlignment = when (adItem.ActionTextPosition?.uppercase()) {
                        "CENTER" -> Alignment.CenterHorizontally
                        "END" -> Alignment.End
                        else -> Alignment.Start
                    }
    
                    val isOutline = adItem.ActionStyle?.uppercase() == "OUTLINE"
                    val buttonTextColor = if (isOutline) Color.White else ComponentStyles.PrimaryBlue
                    val buttonModifier = Modifier.align(buttonAlignment).let {
                        if (isOutline) {
                            it.border(1.dp, Color.White, RoundedCornerShape(20.dp))
                              .background(Color.Transparent, RoundedCornerShape(20.dp))
                        } else {
                            it.background(Color.White, RoundedCornerShape(20.dp))
                        }
                    }
    
                    Box(
                        modifier = buttonModifier
                            .clickable { onRedirect() }
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = adItem.ActionText,
                                color = buttonTextColor,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = buttonTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsPreviewSection(
    notifications: List<NotificationData>,
    onViewAllClick: () -> Unit,
    onNotificationClick: (NotificationData) -> Unit
) {
    if (notifications.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(DesignR.string.home_recent_notifications),
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )
            Text(
                text = stringResource(DesignR.string.home_view_all),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = ComponentStyles.PrimaryBlue,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                notifications.forEachIndexed { index, notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = { onNotificationClick(notification) }
                    )
                    if (index < notifications.size - 1) {
                        HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}



@Composable
internal fun CampaignPopupDialog(
    campaign: com.bdsoftware.idorm.core.model.Campaign,
    onDismiss: () -> Unit,
    onBannerClick: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false, dismissOnBackPress = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .defaultMinSize(minHeight = 200.dp)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    }
            ) {
                var imageLoaded by remember { mutableStateOf(false) }
                
                AsyncImage(
                    model = campaign.BannerUrl,
                    contentDescription = "Campaign Banner",
                    contentScale = ContentScale.FillWidth,
                    onSuccess = { imageLoaded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            campaign.UrlRedirect?.takeIf { it.isNotBlank() }?.let { url ->
                                try {
                                    uriHandler.openUri(url)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            onBannerClick(campaign.UrlRedirect.orEmpty())
                        }
                )

                if (imageLoaded) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(DesignR.string.close_button),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerSectionSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .aspectRatio(1.8f)
            .clip(RoundedCornerShape(16.dp))
            .shimmer()
    )
}

@Composable
private fun PromotionalSectionSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .shimmer()
    )
}

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
    background(brush = brush)
}

@Composable
private fun WifiStatusBar(
    currentSsid: String?,
    isAwing: Boolean
) {
    val backgroundColor: Color
    val iconTint: Color
    val textColor: Color
    val statusText: String

    when {
        currentSsid != null && isAwing -> {
            backgroundColor = Color(0xFF1B5E20).copy(alpha = 0.85f)
            iconTint = Color(0xFF81C784)
            textColor = Color.White
            statusText = stringResource(DesignR.string.home_wifi_connected_awing, currentSsid)
        }
        currentSsid != null -> {
            backgroundColor = Color(0xFFE65100).copy(alpha = 0.75f)
            iconTint = Color(0xFFFFB74D)
            textColor = Color.White
            statusText = stringResource(DesignR.string.home_wifi_connected_other, currentSsid)
        }
        else -> {
            backgroundColor = Color.White.copy(alpha = 0.15f)
            iconTint = Color.White.copy(alpha = 0.6f)
            textColor = Color.White.copy(alpha = 0.7f)
            statusText = stringResource(DesignR.string.home_wifi_disconnected)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 8.dp)
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = "WiFi Status",
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
