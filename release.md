# Cấu hình triển khai iDorm lên Google Play

Cấu hình thành công các thiết lập cần thiết để chuẩn bị build release và upload ứng dụng iDorm lên Google Play Store.

## Các Thay Đổi Đã Thực Hiện

### 1. Cấu hình Signing & Release Build
- **[NEW] keystore.properties**: Đã tạo file cấu hình template tại thư mục gốc để lưu credentials của keystore (được tự động thêm vào `.gitignore` để bảo mật).
- **[MODIFY] app/build.gradle.kts**:
  - Tự động load `keystore.properties` nếu có.
  - Bật R8/minification và resource shrinking cho build `release`.
  - Cập nhật `versionName = "1.0.0"`.
- **[NEW] app/proguard-rules.pro**: Định nghĩa rules để giữ an toàn cho Retrofit, OkHttp, Kotlinx Serialization, Hilt, Firebase, và các data model khỏi bị strip/obfuscate bởi R8.

### 2. Giới hạn Cleartext Traffic (HTTP)
- **[MODIFY] network_security_config.xml**: Khóa cleartext traffic (HTTP) toàn cục để đảm bảo bảo mật cho Google Play, chỉ cho phép các domains/IPs nội bộ của KTX & server trường học:
  - `186.186.0.1` (WiFi gateway)
  - `v1.awingconnect.vn` (Awing portal)
  - `sv.ktxhcm.edu.vn` (Default server)
  - `hanhchinhmotcua.ktxhcm.edu.vn` (HCMC server)
  - `apisv.ktxhcm.edu.vn` (API server)
  - `ql.ktxhcm.edu.vn` (Image server)

### 3. Đơn giản hóa UI WiFi Config (Read-only)
- **[MODIFY] WifiConfigScreen.kt**:
  - Gỡ bỏ hoàn toàn nút "Thêm cấu hình WiFi mới".
  - Gỡ bỏ nút Sửa (Edit), nút Xóa (Delete) và các Dialog/BottomSheet tương ứng.
  - Người dùng chỉ xem danh sách các WiFi mặc định của KTX và kết nối thủ công (hoặc chạy tự động).
---

## Hướng Dẫn Hoàn Tất Build Release

Vì file `keystore.properties` hiện đang chứa thông tin placeholder, bạn cần làm theo các bước sau để build bản release hoàn chỉnh:

### Bước 1: Cập nhật keystore.properties
Mở file [keystore.properties](file:///e:/bd-software/iDorm/keystore.properties) ở thư mục gốc của dự án và điền thông tin thật của file keystore `idorm-keystore`:

```properties
storeFile=idorm-keystore
storePassword=MẬT_KHẨU_KEYSTORE_CỦA_BẠN
keyAlias=ALIAS_CỦA_KEYSTORE (ví dụ: idorm)
keyPassword=MẬT_KHẨU_KEY_CỦA_BẠN
```

### Bước 2: Build Release AAB (để upload lên Google Play)
Mở terminal ở thư mục gốc dự án và chạy command:

```powershell
.\gradlew :app:bundleRelease
.\gradlew.bat clean :app:bundleRelease --no-build-cache
```
Bản build output AAB sẽ nằm ở:
`app/build/outputs/bundle/release/app-release.aab`

### Bước 3: Build Release APK (để cài đặt test trực tiếp trên máy)
Nếu muốn test bản release trước khi upload lên store, bạn có thể build APK:

```powershell
.\gradlew :app:assembleRelease
```
Bản build output APK sẽ nằm ở:
`app/build/outputs/apk/release/app-release.apk`
