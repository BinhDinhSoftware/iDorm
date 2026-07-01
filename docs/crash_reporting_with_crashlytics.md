# Báo cáo Lỗi với Firebase Crashlytics

> Tài liệu tham khảo chính thức từ: https://firebase.google.com/docs/crashlytics/get-started?platform=android

Firebase Crashlytics là công cụ báo cáo lỗi nhẹ, hoạt động theo thời gian thực, giúp bạn theo dõi, ưu tiên và khắc phục các vấn đề ổn định ứng dụng. Crashlytics giúp tiết kiệm thời gian khắc phục sự cố bằng cách nhóm các crash một cách thông minh và nêu bật các tình huống dẫn đến lỗi.

---

## Mục lục
1. [Điều kiện tiên quyết](#1-điều-kiện-tiên-quyết)
2. [Bước 1: Thêm Plugin Crashlytics Gradle](#2-bước-1-thêm-plugin-crashlytics-gradle)
3. [Bước 2: Thêm SDK Firebase Crashlytics](#3-bước-2-thêm-sdk-firebase-crashlytics)
4. [Bước 3: Buộc tạo crash thử nghiệm](#4-bước-3-buộc-tạo-crash-thử-nghiệm)
5. [Bước 4: Kiểm tra trên Firebase Console](#5-bước-4-kiểm-tra-trên-firebase-console)
6. [Tùy chỉnh Báo cáo Lỗi](#6-tùy-chỉnh-báo-cáo-lỗi)
7. [Vô hiệu hóa Crashlytics trên bản Debug](#7-vô-hiệu-hóa-crashlytics-trên-bản-debug)
8. [Giải mã Stack Trace với ProGuard/R8](#8-giải-mã-stack-trace-với-proguardr8)
9. [Tài liệu tham khảo](#9-tài-liệu-tham-khảo)

---

## 1. Điều kiện tiên quyết

Trước khi bắt đầu, hãy đảm bảo:

- Đã tạo **Firebase project** trên [Firebase Console](https://console.firebase.google.com).
- Đã đăng ký ứng dụng Android của bạn trong Firebase project.
- Đã tải file `google-services.json` và đặt vào thư mục `app/` của dự án.
- Nên bật **Google Analytics** trong Firebase project để có tính năng "breadcrumb logs" (nhật ký hành trình người dùng dẫn đến crash).

---

## 2. Bước 1: Thêm Plugin Crashlytics Gradle

Trong file **build.gradle.kts cấp project** (root), thêm plugin Crashlytics:

```kotlin
// build.gradle.kts (Root / Project-level)
plugins {
    // ... các plugin khác
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}
```

> **Lưu ý:** Nếu dự án sử dụng Version Catalog (`libs.versions.toml`), hãy khai báo phiên bản plugin trong file `.toml` và dùng `alias()`.

---

## 3. Bước 2: Thêm SDK Firebase Crashlytics

Trong file **build.gradle.kts cấp app** (module `app`), áp dụng plugin và thêm dependency:

```kotlin
// app/build.gradle.kts (App-level)
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")   // ← Thêm dòng này
}

dependencies {
    // Import Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))

    // Crashlytics SDK (không cần chỉ định version khi dùng BoM)
    implementation("com.google.firebase:firebase-crashlytics")

    // Google Analytics (khuyến nghị — cung cấp breadcrumb logs)
    implementation("com.google.firebase:firebase-analytics")
}
```

Sau đó **Sync** lại dự án trong Android Studio.

> **Quan trọng:** Crashlytics tự động khởi tạo khi ứng dụng khởi động. Bạn **không cần** viết thêm mã khởi tạo trong `Application` class.

---

## 4. Bước 3: Buộc tạo crash thử nghiệm

Để xác nhận Crashlytics đã hoạt động, hãy thêm đoạn mã sau vào một nơi dễ kích hoạt (ví dụ: khi nhấn nút):

```kotlin
// Ví dụ: trong onClick của một Button
throw RuntimeException("Test Crashlytics — đây là crash thử nghiệm")
```

Sau đó:
1. **Build** và chạy ứng dụng trên thiết bị/emulator.
2. **Nhấn nút** để kích hoạt crash.
3. **Mở lại** ứng dụng để Crashlytics gửi báo cáo lên server (báo cáo được gửi trong lần khởi chạy tiếp theo).

---

## 5. Bước 4: Kiểm tra trên Firebase Console

1. Truy cập [Firebase Console](https://console.firebase.google.com).
2. Chọn dự án của bạn.
3. Vào mục **Crashlytics** trong thanh điều hướng bên trái.
4. Sau vài phút, crash thử nghiệm sẽ xuất hiện trên dashboard.

> **Lưu ý:** Có thể mất khoảng 5–10 phút để crash đầu tiên xuất hiện trên dashboard.

---

## 6. Tùy chỉnh Báo cáo Lỗi

Crashlytics cung cấp 4 cơ chế để bổ sung ngữ cảnh vào báo cáo lỗi:

### 6.1. Khóa Tùy chỉnh (Custom Keys)
Gắn các cặp khóa-giá trị tùy ý để lọc và tìm kiếm crash trên dashboard.

- Hỗ trợ tối đa **64 cặp khóa/giá trị**.
- Mỗi cặp có kích thước tối đa **1 KB**.

```kotlin
import com.google.firebase.crashlytics.FirebaseCrashlytics

val crashlytics = FirebaseCrashlytics.getInstance()

crashlytics.setCustomKey("current_screen", "InvoiceScreen")
crashlytics.setCustomKey("user_tier", "premium")
crashlytics.setCustomKey("invoice_count", 15)
crashlytics.setCustomKey("is_wifi_connected", true)
```

### 6.2. Nhật ký Tùy chỉnh (Custom Logs)
Thêm tin nhắn log để hiểu chuỗi sự kiện dẫn đến crash. Các log này được hiển thị trực tiếp trên dashboard Crashlytics khi xem chi tiết một crash cụ thể.

```kotlin
FirebaseCrashlytics.getInstance().log("Người dùng nhấn nút 'Thanh toán'")
FirebaseCrashlytics.getInstance().log("Đang tải danh sách hóa đơn...")
```

> **Mẹo:** Nếu bạn sử dụng Google Analytics, các sự kiện Analytics sẽ tự động được ghi nhận như "breadcrumb logs" trên Crashlytics mà không cần viết thêm mã.

### 6.3. Định danh Người dùng (User Identifiers)
Liên kết các báo cáo crash với người dùng cụ thể. Nên sử dụng các mã định danh **không nhạy cảm** (ID, mã số, hash).

```kotlin
// Ví dụ: sau khi đăng nhập thành công
FirebaseCrashlytics.getInstance().setUserId("student_12345")
```

> **Cảnh báo:** Không sử dụng thông tin cá nhân nhạy cảm (email, số điện thoại, CMND...) làm User Identifier.

### 6.4. Ghi nhận Ngoại lệ Phi Hệ Thống (Non-Fatal Exceptions)
Ngoài việc tự động bắt các crash, bạn có thể chủ động báo cáo các exception đã được bắt (caught exceptions) mà không gây dừng ứng dụng.

```kotlin
try {
    // Mã nguồn có thể xảy ra lỗi
    processInvoice(invoice)
} catch (e: Exception) {
    // Ghi nhận lỗi lên Crashlytics (xuất hiện dưới dạng "Non-fatal")
    FirebaseCrashlytics.getInstance().recordException(e)
    // Vẫn xử lý lỗi ở phía ứng dụng
    showErrorMessage("Đã xảy ra lỗi xử lý hóa đơn")
}
```

> **Lưu ý:** Crashlytics chỉ lưu trữ **8 ngoại lệ** được ghi nhận gần nhất trong mỗi phiên chạy ứng dụng.

### Bảng tóm tắt

| Tính năng | Phương thức | Mục đích |
|:---|:---|:---|
| **Khóa tùy chỉnh** | `setCustomKey(key, value)` | Lọc và tìm crash theo trạng thái ứng dụng |
| **Nhật ký** | `log(message)` | Ghi lại ngữ cảnh / hành trình người dùng |
| **Định danh người dùng** | `setUserId(id)` | Liên kết crash với người dùng cụ thể |
| **Ngoại lệ non-fatal** | `recordException(e)` | Báo cáo lỗi không gây crash |

---

## 7. Vô hiệu hóa Crashlytics trên bản Debug

Để tránh làm ô nhiễm dữ liệu crash trên Firebase Console và tăng tốc build debug, bạn nên tắt Crashlytics ở runtime cho bản build debug:

```kotlin
// Trong lớp Application (ví dụ: IdormApplication.kt)
import com.google.firebase.crashlytics.FirebaseCrashlytics

override fun onCreate() {
    super.onCreate()
    // Chỉ bật Crashlytics trên bản release
    FirebaseCrashlytics.getInstance()
        .setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
}
```

> **Giải thích:** `BuildConfig.DEBUG` sẽ là `true` khi build ở chế độ debug → `setCrashlyticsCollectionEnabled(false)` sẽ tắt việc thu thập dữ liệu crash.

---

## 8. Giải mã Stack Trace với ProGuard/R8

Khi bật minification (`isMinifyEnabled = true`), stack trace sẽ bị mã hóa (obfuscated). Plugin Crashlytics Gradle sẽ **tự động** tải lên file mapping trong quá trình build, giúp bạn xem được tên class/method gốc trên Firebase Console.

Để đảm bảo stack trace dễ đọc nhất, hãy thêm dòng sau vào `proguard-rules.pro`:

```proguard
# Firebase Crashlytics — giữ lại tên file nguồn và số dòng
-keepattributes SourceFile,LineNumberTable
```

> **Lưu ý:** Plugin Crashlytics Gradle đã tự động xử lý việc upload file mapping. Bạn **không cần** tải lên thủ công.

---

## 9. Tài liệu tham khảo

- [Firebase Crashlytics — Bắt đầu (Android)](https://firebase.google.com/docs/crashlytics/get-started?platform=android) — Tài liệu chính thức
- [Tùy chỉnh Báo cáo Crash](https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android) — Custom keys, logs, user IDs
- [Nâng cấp từ Firebase Crash Reporting](https://firebase.google.com/docs/crashlytics/upgrade-from-crash-reporting) — Hướng dẫn migrate
- [NDK Crash Reports](https://firebase.google.com/docs/crashlytics/ndk-reports) — Cho ứng dụng sử dụng mã C/C++ native

---

*Tài liệu này được biên soạn bằng tiếng Việt cho dự án iDorm, dựa trên tài liệu chính thức của Firebase và hướng dẫn CodePath.*
