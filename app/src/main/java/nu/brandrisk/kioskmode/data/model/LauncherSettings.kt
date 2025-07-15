package nu.brandrisk.kioskmode.data.model

data class LauncherSettings(
    val showAppLabels: Boolean = true,
    val iconSize: IconSize = IconSize.MEDIUM,
    val gridColumns: Int = 4
)

enum class IconSize {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}
