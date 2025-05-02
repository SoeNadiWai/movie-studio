package com.personalprojects.moviestudio.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personalprojects.moviestudio.AppDestinations
import com.personalprojects.moviestudio.BottomNavItem
import com.personalprojects.moviestudio.bottomNavItems

@Composable
fun AppBottomNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            val selected = item.route == currentRoute
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item.route) },
                label = { stringResource(item.labelResId) },
                icon = {
                    Icon(
                        imageVector = if (selected) {
                            item.selectedIcon
                        } else {
                            item.unselectedIcon
                        },
                        contentDescription = stringResource(item.labelResId)
                    )
                }
            )
        }
    }
}

@Composable
fun RowScope.CustomBottomNavigationItem( // Use RowScope for weight if needed later
    item: BottomNavItem,
    isSelected: Boolean,
    selectedColor: Color, // Color for indicator background and selected icon/text
    unselectedColor: Color, // Color for unselected icon/text
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(durationMillis = 200) // Smooth color transition
    )

    Box(
        modifier = Modifier
            .weight(1f) // Each item takes equal space
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                // No default ripple if we have our own indicator
                indication = null,
                interactionSource = interactionSource,
                role = Role.Tab
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                // Indicator background: draw only if selected
                .then(
                    if (isSelected) Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(selectedColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp) // Padding inside indicator
                    else Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ) // Maintain size consistency
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = stringResource(item.labelResId),
                tint = if (isSelected) Color.White else contentColor, // White icon on red, gray otherwise
                modifier = Modifier.size(24.dp)
            )

            // Label: Show only when selected, use Crossfade for smooth transition
            Crossfade(
                targetState = isSelected,
                animationSpec = tween(durationMillis = 150), // Faster fade for label
                label = "labelFade"
            ) { isCurrentlySelected ->
                if (isCurrentlySelected) {
                    Spacer(modifier = Modifier.width(8.dp)) // Space between icon and label
                    Text(
                        text = stringResource(item.labelResId),
                        color = Color.White, // White text on red background
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp, // Adjust size
                        maxLines = 1
                    )
                }
                // When not selected, Crossfade will show nothing here
            }
        }
    }
}


@Preview
@Composable
fun PreviewAppBottomNavigationBar() {
    AppBottomNavigationBar(
        items = bottomNavItems,
        currentRoute = AppDestinations.HOME,
        onItemClick = {}
    )
}