# 📱 Hide/Show App Labels Feature

## ✨ Fitur Baru: Toggle Visibility Label Aplikasi

Saya telah menambahkan fitur untuk menyembunyikan/menampilkan label aplikasi pada launcher yang sangat berguna ketika menggunakan ikon yang besar.

### 🎯 **Fitur yang Ditambahkan:**

1. **Toggle Button di Quick Actions**
   - Tombol mata (👁️) di sebelah kanan Quick Actions
   - Tekan untuk hide/show semua label aplikasi
   - Icon berubah sesuai status: Visibility/VisibilityOff

2. **Persistent Settings**
   - Pengaturan tersimpan secara otomatis
   - Tetap konsisten setelah restart aplikasi

3. **Responsive UI**
   - Quick Action buttons juga menyesuaikan show/hide label
   - Perubahan real-time tanpa perlu restart

### 🔧 **Files yang Dimodifikasi:**

#### 1. **LauncherSettings.kt** (NEW)
```kotlin
data class LauncherSettings(
    val showAppLabels: Boolean = true,
    val iconSize: IconSize = IconSize.MEDIUM,
    val gridColumns: Int = 4
)
```

#### 2. **LauncherSettingsRepository.kt** (NEW)
- Menyimpan pengaturan di SharedPreferences
- Method save/load launcher settings

#### 3. **AppIconItem.kt** (MODIFIED)
```kotlin
@Composable
fun AppIconItem(
    // ...
    showLabel: Boolean = true
)
```

#### 4. **LauncherView.kt** (MODIFIED)
- Tambah toggle button di Quick Actions
- Support show/hide untuk app labels dan quick action labels
- Reactive state dengan StateFlow

#### 5. **LauncherViewModel.kt** (MODIFIED)
- StateFlow untuk launcher settings
- Method toggleAppLabels()
- Dependency injection LauncherSettingsRepository

### 🚀 **Cara Penggunaan:**

1. **Untuk Hide Labels:**
   - Tekan tombol mata (👁️) di pojok kanan Quick Actions
   - Semua label aplikasi dan quick actions akan disembunyikan

2. **Untuk Show Labels:**
   - Tekan tombol mata dengan garis (🙈) 
   - Semua label akan muncul kembali

3. **Persistent:**
   - Pengaturan akan tersimpan otomatis
   - Tidak perlu setting ulang setelah restart

### 💡 **Benefits:**

- **Clean Interface**: Tampilan lebih bersih tanpa label saat menggunakan ikon besar
- **Space Efficient**: Lebih banyak ruang untuk ikon aplikasi
- **User Choice**: Pengguna bisa memilih sesuai preferensi
- **Professional Look**: Cocok untuk kiosk mode enterprise

### 🎨 **UI/UX Enhancements:**

- Icon toggle yang intuitive (mata terbuka/tertutup)
- Smooth transition saat hide/show
- Consistent design dengan theme aplikasi
- Strategic placement di Quick Actions area

### 🔄 **Technical Implementation:**

- **StateFlow** untuk reactive state management
- **SharedPreferences** untuk persistent storage
- **Compose** untuk declarative UI updates
- **Dependency Injection** dengan Hilt

---

**Ready to use!** 🎉 Fitur sudah siap digunakan dan terintegrasi dengan arsitektur aplikasi yang ada.
