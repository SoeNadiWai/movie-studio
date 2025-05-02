package com.personalprojects.movielist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CategoryTabs(
    categories: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.fillMaxWidth(),
        edgePadding = 16.dp, // Padding at the start and end
        // Customize indicator
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp, // Adjust indicator height
                    color = MaterialTheme.colorScheme.primary // Adjust indicator color
                )
            }
        },
        divider = {}
    ) {
        categories.forEachIndexed { index, categoryTitle ->
            Tab(
                selected = (selectedTabIndex == index),
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = categoryTitle,
                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary, // Color for selected tab text
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant // Color for unselected tab text
            )
        }
    }
}