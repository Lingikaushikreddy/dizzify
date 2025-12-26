package app.dizzify.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.dizzify.data.AppModel
import app.dizzify.ui.theme.*

enum class CardStyle {
    STANDARD,    // Regular app grid card
    COMPACT,     // Smaller, icon-focused
    BANNER,      // Wide, for featured content
    MINIMAL      // Icon only with label on focus
}

@Composable
fun AppCard(
    app: AppModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CardStyle = CardStyle.STANDARD,
    focusRequester: FocusRequester = remember { FocusRequester() },
    showNewBadge: Boolean = app.isNew
) {
    var isFocused by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    
    val cardShape = RoundedCornerShape(
        when (style) {
            CardStyle.COMPACT -> 12.dp
            CardStyle.MINIMAL -> 16.dp
            else -> 20.dp
        }
    )
    
    val cardSize = when (style) {
        CardStyle.STANDARD -> Modifier.size(LauncherCardSizes.appCardWidth, LauncherCardSizes.appCardHeight)
        CardStyle.COMPACT -> Modifier.size(120.dp, 150.dp)
        CardStyle.BANNER -> Modifier.size(LauncherCardSizes.featuredCardWidth, LauncherCardSizes.featuredCardHeight)
        CardStyle.MINIMAL -> Modifier.size(LauncherCardSizes.smallCardSize + 20.dp)
    }
    
    // Animation values
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isFocused -> 1.08f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 24.dp else 4.dp,
        animationSpec = tween(LauncherAnimation.FastDuration),
        label = "card_elevation"
    )
    
    val borderAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = tween(LauncherAnimation.FastDuration),
        label = "border_alpha"
    )
    
    // Glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Box(
        modifier = modifier
            .then(cardSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isFocused) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = LauncherColors.AccentBlue.copy(alpha = glowAlpha * 0.3f),
                            cornerRadius = CornerRadius(24.dp.toPx()),
                            size = size.copy(
                                width = size.width + 20.dp.toPx(),
                                height = size.height + 20.dp.toPx()
                            ),
                            topLeft = Offset(-10.dp.toPx(), -10.dp.toPx())
                        )
                    }
                } else Modifier
            )
            .shadow(elevation, cardShape)
            .clip(cardShape)
            .background(
                if (isFocused) {
                    Brush.verticalGradient(
                        colors = listOf(
                            LauncherColors.DarkCardBackground,
                            LauncherColors.DarkCardBackground.copy(alpha = 0.95f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            LauncherColors.DarkCardBackground,
                            LauncherColors.DarkSurface
                        )
                    )
                }
            )
            .then(
                if (borderAlpha > 0f) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = borderAlpha),
                        shape = cardShape
                    )
                } else Modifier
            )
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
                isFocused = state.isFocused
            }
            .onKeyEvent { event ->
                when (event.type) {
                    KeyEventType.KeyDown if (event.key == Key.DirectionCenter || event.key == Key.Enter) -> {
                        isPressed = true
                        true
                    }
                    KeyEventType.KeyUp if (event.key == Key.DirectionCenter || event.key == Key.Enter) -> {
                        isPressed = false
                        onClick()
                        true
                    }
                    KeyEventType.KeyDown if event.key == Key.Menu -> {
                        onLongClick()
                        true
                    }
                    else -> false
                }
            }
            .focusable()
    ) {
        when (style) {
            CardStyle.STANDARD -> StandardCardContent(app, isFocused, showNewBadge)
            CardStyle.COMPACT -> CompactCardContent(app, isFocused, showNewBadge)
            CardStyle.BANNER -> BannerCardContent(app, isFocused)
            CardStyle.MINIMAL -> MinimalCardContent(app, isFocused)
        }
    }
}

@Composable
private fun StandardCardContent(
    app: AppModel,
    isFocused: Boolean,
    showNewBadge: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon container with subtle animation
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Icon background glow when focused
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            LauncherColors.AccentBlue.copy(alpha = 0.1f),
                            CircleShape
                        )
                )
            }
            
            AppIcon(
                app = app,
                size = LauncherCardSizes.appIconLarge,
                showShadow = isFocused
            )
            
            // New badge
            if (showNewBadge) {
                NewBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // App name
        Text(
            text = app.appLabel,
            style = MaterialTheme.typography.titleMedium,
            color = if (isFocused) Color.White else LauncherColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Package name (shown on focus)
        AnimatedVisibility(visible = isFocused) {
            Text(
                text = app.appPackage.substringAfterLast('.'),
                style = MaterialTheme.typography.labelSmall,
                color = LauncherColors.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun CompactCardContent(
    app: AppModel,
    isFocused: Boolean,
    showNewBadge: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            AppIcon(
                app = app,
                size = LauncherCardSizes.appIconMedium,
                showShadow = isFocused
            )
            
            if (showNewBadge) {
                NewBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = app.appLabel,
            style = MaterialTheme.typography.labelLarge,
            color = if (isFocused) Color.White else LauncherColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BannerCardContent(
    app: AppModel,
    isFocused: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            app = app,
            size = LauncherCardSizes.appIconLarge,
            showShadow = isFocused
        )
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appLabel,
                style = MaterialTheme.typography.headlineMedium,
                color = if (isFocused) Color.White else LauncherColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = app.appPackage,
                style = MaterialTheme.typography.bodyMedium,
                color = LauncherColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MinimalCardContent(
    app: AppModel,
    isFocused: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(
            app = app,
            size = LauncherCardSizes.appIconMedium,
            showShadow = isFocused
        )
    }
}

@Composable
fun AppIcon(
    app: AppModel,
    size: Dp,
    modifier: Modifier = Modifier,
    showShadow: Boolean = false,
) {
    val icon = app.appIcon
    
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showShadow) {
                    Modifier.shadow(8.dp, RoundedCornerShape(size / 4))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = app.appLabel,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(size / 4)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(size / 4))
                    .background(LauncherColors.DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.6f),
                    tint = LauncherColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun NewBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(LauncherColors.AccentBlue, CircleShape)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.NewReleases,
            contentDescription = "New",
            modifier = Modifier.size(12.dp),
            tint = Color.White
        )
    }
}

