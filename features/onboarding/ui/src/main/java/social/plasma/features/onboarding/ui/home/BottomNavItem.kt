package social.plasma.features.onboarding.ui.home

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.slack.circuit.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data class BottomNavItem(
    val screen: Screen,
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
) : Parcelable
