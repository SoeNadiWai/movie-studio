package com.personalprojects

// Alternatively use: import androidx.compose.material.icons.filled.StarBorder (another empty style)
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun FiveStarRatingIndicator(
    modifier: Modifier = Modifier,
    ratingOutOf10: Double?,
    starSize: Dp = 18.dp,
    starColor: Color = Color.Yellow
) {
    if (ratingOutOf10 == null || ratingOutOf10 < 0) {
        // Optionally display something if rating is invalid or null
        // For now, just display nothing or 5 empty stars
        Row(modifier = modifier) {
            repeat(5) {
                Icon(
                    imageVector = Icons.Filled.StarOutline,
                    contentDescription = null, // Container will have description
                    tint = Color.Gray, // Placeholder color
                    modifier = Modifier.size(starSize)
                )
            }
        }
        return
    }

    // Convert 0-10 rating to 0-5 rating
    val ratingOutOf5 = (ratingOutOf10 / 2.0).coerceIn(0.0, 5.0)
    val fullStars = floor(ratingOutOf5).toInt()
    // Determine if there's a half star needed (more than ~0.25 qualifies for half visually)
    // Or use >= 0.5 for a stricter half-star threshold
    val needsHalfStar = (ratingOutOf5 - fullStars) >= 0.25f
    // Calculate empty stars (consider half star presence)
    val emptyStars = if (needsHalfStar) {
        (5 - ceil(ratingOutOf5)).toInt()
    } else {
        (5 - ratingOutOf5).toInt()
    }
    // Ensure total stars is always 5
    val actualHalfStars = if (needsHalfStar) 1 else 0
    val actualFullStars = fullStars
    // Recalculate empty stars based on full/half to guarantee 5 total
    val actualEmptyStars = (5 - actualFullStars - actualHalfStars).coerceAtLeast(0)

    // Accessibility description for the whole rating
    val description = "Rating: %.1f out of 5 stars".format(ratingOutOf5)

    Row(
        modifier = modifier.semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Draw full stars
        repeat(actualFullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null, // Handled by Row
                tint = starColor,
                modifier = Modifier.size(starSize)
            )
        }
        // Draw half star if needed
        if (needsHalfStar) {
            Icon(
                imageVector = Icons.Filled.StarHalf,
                contentDescription = null, // Handled by Row
                tint = starColor,
                modifier = Modifier.size(starSize)
            )
        }
        // Draw empty stars
        repeat(actualEmptyStars) {
            Icon(
                imageVector = Icons.Filled.StarOutline, // Use Outline or StarBorder
                contentDescription = null, // Handled by Row
                tint = starColor.copy(alpha = 0.6f), // Make empty stars slightly faded or use Gray
                modifier = Modifier.size(starSize)
            )
        }
    }
}
