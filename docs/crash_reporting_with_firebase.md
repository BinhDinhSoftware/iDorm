# Báo cáo Lỗi với Firebase (Crash Reporting with Firebase)

> **Cảnh báo (Đã ngừng hỗ trợ):** SDK Firebase Crash Reporting (`com.google.firebase:firebase-crash`) được mô tả trên trang này đã bị thay thế hoàn toàn bởi Firebase Crashlytics (`com.google.firebase:firebase-crashlytics`). Trang tổng quan và SDK Crash Reporting ban đầu không còn được hỗ trợ — xem [hướng dẫn nâng cấp của Google](https://firebase.google.com/docs/crashlytics/upgrade-from-crash-reporting) và bài đăng trên blog của Firebase [Đã đến lúc nâng cấp lên SDK Firebase Crashlytics mới!](https://firebase.googleblog.com/2020/10/its-time-to-upgrade-to-new-firebase.html). Đối với mã nguồn mới, hãy làm theo hướng dẫn [Báo cáo Lỗi với Crashlytics](https://github.com/codepath/android_guides/wiki/Crash-Reporting-with-Crashlytics). Nội dung dưới đây được lưu trữ chỉ cho mục đích tham khảo lịch sử.

Nguồn: https://github.com/codepath/android_guides/wiki/Crash-Reporting-with-Firebase

```groovy
com.google.firebase:firebase-crash
com.google.firebase:firebase-crashlytics
```

Tính năng báo cáo lỗi (Crash Reporting) được công bố là một trong những tính năng ban đầu của nền tảng Firebase được cải tiến tại Google I/O 2016. Hướng dẫn này lưu lại các bước thiết lập ban đầu cho SDK Firebase Crash Reporting độc lập cũ. Các dự án mới bắt buộc phải sử dụng Firebase Crashlytics — các bước dưới đây sẽ không tạo ra một tích hợp hoạt động bình thường ngày hôm nay.

---

## Tài liệu lịch sử: Thiết lập Firebase Crash Reporting
Artifact xuất bản cuối cùng của SDK Firebase Crash Reporting là `com.google.firebase:firebase-crash:16.2.1`. Trang tổng quan điều khiển và các cổng tiếp nhận báo cáo lỗi đã ngừng hoạt động (trang tài liệu gốc tại `firebase.google.com/docs/crash/android` hiện chuyển hướng đến trang sản phẩm Crashlytics). Các phiên bản được ghim bên dưới là các giá trị hiện hành tại thời điểm tài liệu này được viết; chúng được ghi nhận chỉ cho mục đích lưu trữ lịch sử.

```groovy
com.google.firebase:firebase-crash:16.2.1
```

1. Để sử dụng Firebase Crash Reporting, trước tiên bạn phải thực hiện các bước thiết lập cơ bản cho Firebase. Xem hướng dẫn [Xây dựng ứng dụng hướng dữ liệu với Firebase](https://github.com/codepath/android_guides/wiki/Building-Data-driven-Apps-with-Firebase).
2. Tiếp theo là thêm thư viện Firebase Crash Android vào file `build.gradle` của module app:

### build.gradle
```groovy
implementation 'com.google.firebase:firebase-crash:9.0.0'
```

Cuối cùng, file `build.gradle` của bạn sẽ trông giống như thế này:

```groovy
apply plugin: 'com.android.application'

android {
    // ...
}

dependencies {
    // ...
    // các thư viện khác trong dự án của bạn
    // thư viện firebase core
    implementation 'com.google.firebase:firebase-core:9.0.0'
    // thư viện firebase crash
    implementation 'com.google.firebase:firebase-crash:9.0.0'
}

// Thêm vào cuối file
apply plugin: 'com.google.gms.google-services'
```

---

## Gửi báo cáo lỗi
Bước này giả định rằng bạn đã hoàn thành thiết lập Firebase cơ bản.
Để gửi một báo cáo lỗi tùy chỉnh (lỗi non-fatal), ứng dụng thêm một dòng mã như:

```java
FirebaseCrash.report(new Exception("Lỗi non-fatal Firebase đầu tiên của tôi trên Android"));
```

Sau vài phút (khoảng 20 phút theo tài liệu Firebase Crash Reporting gốc), lỗi sẽ xuất hiện trên trang quản trị:
Trang quản trị cung cấp một tính năng gọi là "Clusters" (Cụm), giúp nhóm các ngoại lệ có stack trace tương tự nhau.
Không giống như nhiều thư viện báo cáo lỗi khác chỉ yêu cầu một dòng mã duy nhất để khởi tạo trên toàn bộ ứng dụng, Firebase Crash Reporting không đi kèm phương thức tự động như vậy.
Các ứng dụng thường thêm khối mã sau vào lớp Application chính để tự động bắt và gửi đi các ngoại lệ chưa được bắt (uncaught exceptions):

```java
Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        FirebaseCrash.report(ex);
    }
});
```

---

## Gửi log tùy chỉnh
Các tin nhắn log tùy chỉnh cũng có thể được gửi đi. SDK cung cấp:

```java
FirebaseCrash.log("MainActivity started");
```

Một biến thể ghi log kèm Logcat cũng khả dụng thông qua `FirebaseCrash.logcat()`:

```java
FirebaseCrash.logcat(Log.DEBUG, "My tag", "My message");
```

---

## Giải mã (Deobfuscate) các nhãn ProGuard
Firebase Crash Reporting chấp nhận tải lên file ánh xạ ProGuard (mapping file) để dịch ngược stack trace bị mã hóa thành dạng dễ đọc. Khi ProGuard được bật, file `mapping.txt` sẽ được tạo ra tại thư mục:

```
<project-root>/<app-module>/build/outputs/mapping/<build-type>/<appname>-proguard-mapping.txt
```

Ví dụ: `app/build/outputs/mapping/debug/app-proguard-mapping.txt`.
Để biết thêm về ProGuard, hãy xem hướng dẫn [Cấu hình ProGuard](https://github.com/codepath/android_guides/wiki/Configuring-ProGuard). File mapping sẽ được tải lên từ tab "Mapping files" trên trang điều khiển Firebase.

---

## Các vấn đề đã biết
Tại thời điểm tài liệu này được soạn thảo, Firebase Crash Reporting đang là sản phẩm thử nghiệm (Beta) và vẫn đang trong quá trình phát triển tích cực.
Trình báo cáo chạy trên một tiến trình (process) riêng biệt. Trang "Các vấn đề đã biết" (Known issues) gốc tại `firebase.google.com/docs/crash/android` không còn truy cập được — hiện tại Firebase Crashlytics là phiên bản thay thế được hỗ trợ chính thức.

---

## Tài liệu tham khảo
- [Firebase Crashlytics — Bắt đầu (Android)](https://firebase.google.com/docs/crashlytics/get-started?platform=android) — sản phẩm kế thừa chính thức
- [Nâng cấp từ Firebase Crash Reporting lên Firebase Crashlytics](https://firebase.google.com/docs/crashlytics/upgrade-from-crash-reporting)
- [Cấu hình ProGuard](https://github.com/codepath/android_guides/wiki/Configuring-ProGuard)
