# Charge Monitor

Aplikasi Android sederhana untuk memantau arus (mA), tegangan (V), dan daya (W) yang masuk saat HP sedang di-charge.

## Cara pakai

1. Buka Android Studio, pilih **New Project > Empty Views Activity (Kotlin)**.
2. Beri nama package `com.denysusetio.chargemonitor` (harus sama biar tidak perlu ubah kode).
3. Setelah project dibuat, timpa/replace file-file berikut dengan yang ada di folder ini:
   - `app/src/main/java/com/denysusetio/chargemonitor/MainActivity.kt`
   - `app/src/main/res/layout/activity_main.xml`
   - `app/src/main/AndroidManifest.xml`
   - `app/src/main/res/values/strings.xml`
   - `app/src/main/res/values/themes.xml`
4. Sync Gradle, lalu Run ke HP fisik (bukan emulator — emulator tidak baca data baterai asli).
5. Colokkan charger, buka app, angka arus akan update tiap 1 detik.

## Cara kerja singkat

- **Arus**: dibaca lewat `BatteryManager.getIntProperty(BATTERY_PROPERTY_CURRENT_NOW)`, hasilnya dalam microampere lalu dikonversi ke mA. Nilai ini di-poll tiap 1 detik karena tidak ada broadcast otomatis untuk perubahan arus.
- **Tegangan & status charging**: didapat dari broadcast `ACTION_BATTERY_CHANGED` (sticky broadcast, terupdate otomatis tiap ada perubahan status baterai).
- **Daya (Watt)**: dihitung manual dari `arus x tegangan`.

## Catatan penting (keterbatasan)

- **Akurasi tergantung chipset/vendor.** Nilai dari `CURRENT_NOW` ini datang dari fuel gauge IC bawaan HP, bukan pengukuran independen. Sebagian besar cukup akurat, tapi tidak semua HP melaporkan dengan presisi yang sama — beberapa custom ROM/vendor bahkan melaporkan 0 terus.
- **Konvensi tanda (+/-) berbeda-beda antar merk.** Ada yang melaporkan angka negatif saat charging, ada yang positif — makanya di kode dipakai nilai absolut (`abs`) supaya tetap tampil sebagai angka charging yang wajar, bukan minus.
- **Tidak bisa membaca arus charger secara independen dari kabel/adaptor** — datanya murni dari apa yang dilaporkan chip baterai internal HP, bukan pengukuran fisik di titik USB.
- Untuk hasil paling akurat dan device-independent, alat fisik seperti USB power meter (inline, dicolok di antara charger dan kabel) tetap jadi patokan yang lebih bisa dipercaya dibanding software.
