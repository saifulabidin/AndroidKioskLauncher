package nu.brandrisk.kioskmode.utils

import android.content.Intent

sealed class UiEvent {
    data class Navigate(val route: String): UiEvent()
    data class ShowMessage(val message: String): UiEvent()
    data class StartActivity(val intent: Intent): UiEvent()
}
