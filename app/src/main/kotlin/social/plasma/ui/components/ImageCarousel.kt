package social.plasma.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImageCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(),
) {
    Column(
        modifier = modifier
    ) {
        HorizontalPager(
            modifier = Modifier.clip(MaterialTheme.shapes.medium),
            count = imageUrls.size,
            state = pagerState,
        ) {
            ZoomableImage(
                imageUrl = imageUrls[it],
            )
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            activeColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
        )
    }

}
