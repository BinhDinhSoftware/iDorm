# Skill: Phân chia Feature theo kiến trúc Now in Android

## Mục tiêu

Sau khi đọc tài liệu này, bạn sẽ hiểu:

* Vì sao Google không chia project theo MVC/MVVM truyền thống.
* Vì sao Now in Android chia theo **Feature Module**.
* Vai trò của `api` và `impl`.
* Vai trò của các module `core`.
* Quy tắc dependency giữa các module.
* Cách áp dụng cho dự án thực tế.

---

# 1. Triết lý của Now in Android

Now in Android **không chia project theo layer**.

Ví dụ cách chia truyền thống:

```
ui/
repository/
viewmodel/
network/
database/
```

Hoặc

```
presentation/
domain/
data/
```

Google không sử dụng cách này.

Thay vào đó, project được chia theo **Business Feature (Vertical Slice)**.

Ví dụ:

```
Home

Search

Bookmarks

Interests

Topic

Settings
```

Mỗi feature đại diện cho **một nghiệp vụ hoàn chỉnh**.

Điều này giúp:

* Feature độc lập.
* Build nhanh hơn.
* Có thể phát triển song song.
* Dễ scale.

---

# 2. Cấu trúc tổng thể

```
app/

core/
    data/
    database/
    network/
    model/
    ui/
    designsystem/
    domain/

feature/
    foryou/
    search/
    bookmarks/
    interests/
    topic/
    settings/
```

Project chỉ gồm hai nhóm lớn:

```
core

feature
```

---

# 3. Feature là gì?

Feature là một nghiệp vụ hoàn chỉnh.

Ví dụ:

```
Home
```

Không phải:

```
Banner

Card

Recycler

Button
```

Những thứ đó chỉ là UI Component.

Ví dụ:

```
Invoice

├── Danh sách hóa đơn
├── Chi tiết hóa đơn
├── Thanh toán
└── Lịch sử thanh toán
```

Đây là **một Feature**.

Không nên chia:

```
InvoiceList

InvoiceDetail

InvoicePayment
```

trừ khi dự án cực lớn.

---

# 4. Feature không phải Screen

Đây là điểm rất nhiều người nhầm.

Ví dụ:

Bottom Navigation

```
Home

Invoice

Notification

Profile
```

=> Đây là 4 Feature.

Nhưng Home gồm:

```
Banner

Quick Action

Tin tức

Dịch vụ

Khuyến mãi
```

Không nên tạo:

```
feature/banner

feature/news

feature/service
```

Chúng chỉ là component của Home.

Nên đặt:

```
feature/home

    ui/

        Banner.kt

        NewsCard.kt

        ServiceGrid.kt
```

---

# 5. Feature chia thành API và Implementation

Ví dụ:

```
feature/home

    api/

    impl/
```

## API Module

API chỉ chứa những gì bên ngoài được phép biết.

Ví dụ:

```
HomeNavigation.kt

HomeRoute.kt

Destination.kt

Route constants
```

API KHÔNG chứa:

* ViewModel
* Screen
* Repository
* UI

API giống như public interface.

---

## Implementation Module

Implementation chứa toàn bộ logic.

Ví dụ:

```
HomeScreen

HomeViewModel

HomeUiState

HomeNavigation

DI

Composable

UseCase
```

---

# 6. Vì sao phải tách API và Implementation?

Giả sử:

```
Home

↓

Invoice Detail
```

Sai:

```
Home

↓

InvoiceScreen()
```

Điều này khiến Home phụ thuộc trực tiếp Invoice.

Đúng:

```
Home

↓

Invoice API

↓

navigateInvoice(id)
```

Home chỉ biết:

```
navigateInvoice(invoiceId)
```

Không biết Invoice được implement như thế nào.

Đây gọi là **Low Coupling**.

---

# 7. Dependency giữa các Feature

Sai:

```
Home

↓

Invoice Implementation
```

Đúng:

```
Home

↓

Invoice API
```

Dependency:

```
Home Impl

↓

Invoice API
```

Không bao giờ:

```
Home Impl

↓

Invoice Impl
```

---

# 8. Vai trò của app module

Module app không chứa business.

Nó chỉ làm:

```
MainActivity

Navigation

Bottom Navigation

Scaffold

App Theme
```

Ví dụ:

```
Scaffold

↓

BottomBar

↓

NavHost
```

App chỉ là nơi lắp ráp các Feature.

---

# 9. Vai trò của Core Modules

## core:model

Chứa model dùng chung.

Ví dụ:

```
User

Invoice

Topic

NewsResource
```

Không chứa logic.

---

## core:network

Chứa:

```
Retrofit

ApiService

DTO

Network Config
```

Không chứa UI.

---

## core:database

Chứa:

```
Room

DAO

Entity

Migration
```

---

## core:data

Chứa:

```
Repository

RepositoryImpl

Offline Sync

Flow Combine

Cache
```

Repository chỉ xuất dữ liệu.

---

## core:domain

Chứa UseCase.

Ví dụ:

```
GetDashboardDataUseCase

GetInvoicesUseCase

GetUnreadNotificationsUseCase
```

UseCase chỉ được tạo khi:

* Combine nhiều Repository.
* Có Business Logic.
* Có Flow Transform.
* Có Logic dùng chung.

Không tạo UseCase cho mọi CRUD.

---

## core:ui

Chứa UI dùng chung.

Ví dụ:

```
Loading

Empty

Error

Avatar

Toolbar
```

---

## core:designsystem

Chứa Design System.

Ví dụ:

```
Theme

Typography

Button

Card

Color

Spacing

Icon

Chip
```

---

# 10. Cách Feature sử dụng Core

Ví dụ:

```
Home Feature

↓

GetDashboardUseCase

↓

Repository

↓

Network

↓

Database
```

UI không gọi Network.

UI không gọi Database.

---

# 11. Feature không gọi nhau

Ví dụ:

```
Notification

↓

InvoiceViewModel
```

Không được phép.

Nếu Notification cần Invoice:

```
Notification

↓

Invoice API
```

hoặc

```
Notification

↓

core:data
```

---

# 12. Ví dụ với ứng dụng KTX

```
app/

core/
    model/
    data/
    database/
    network/
    ui/
    designsystem/
    domain/

feature/

    auth/

    splash/

    home/

    invoice/

    room/

    contract/

    notification/

    profile/

    feedback/

    settings/
```

Bottom Navigation:

```
Home

Invoice

Notification

Profile
```

Chỉ là Navigation.

Không phải nơi chứa business.

---

# 13. Cấu trúc của một Feature

Ví dụ:

```
feature/invoice

api/

    InvoiceNavigation.kt

impl/

    navigation/

    presentation/

        InvoiceViewModel.kt

        InvoiceUiState.kt

        InvoiceEvent.kt

    ui/

        InvoiceScreen.kt

        InvoiceCard.kt

        InvoiceItem.kt

    di/

        InvoiceModule.kt
```

---

# 14. Quy tắc vàng

1. Chia theo Business Feature.
2. Không chia theo Screen.
3. Không chia theo Widget.
4. Feature không phụ thuộc Implementation của Feature khác.
5. Chỉ expose API.
6. App chỉ làm nhiệm vụ lắp ráp.
7. Dùng Core cho các thành phần dùng chung.
8. Chỉ tạo UseCase khi thật sự có Business Logic.
9. UI không biết Network.
10. UI không biết Database.

---

# 15. Khi nào KHÔNG cần api/impl?

Nếu dự án nhỏ hoặc chỉ có 1–2 lập trình viên:

```
feature/

    home/

    invoice/

    profile/
```

Mỗi Feature chỉ cần một module.

Khi dự án lớn hoặc có nhiều nhóm phát triển:

```
feature/

    home/
        api/
        impl/

    invoice/
        api/
        impl/

    notification/
        api/
        impl/
```

Việc tách `api` và `impl` giúp giảm coupling, cải thiện thời gian build và cho phép các nhóm phát triển độc lập hơn.

---

# 16. Nguyên tắc cốt lõi

Now in Android không cố gắng tạo thật nhiều module.

Mục tiêu của kiến trúc là:

* Chia theo nghiệp vụ (Business Feature).
* Độc lập giữa các Feature.
* Tối thiểu hóa phụ thuộc.
* Tái sử dụng thông qua Core Modules.
* Để `app` chỉ đóng vai trò "composition root" (lắp ráp toàn bộ ứng dụng).
* Chỉ tạo abstraction (API, UseCase...) khi nó mang lại giá trị thực sự.


# Skill: Phân biệt `core:designsystem` và `core:ui` theo kiến trúc Now in Android

## Mục tiêu

Sau khi đọc tài liệu này, bạn sẽ hiểu:

* Vai trò của `core:designsystem`.
* Vai trò của `core:ui`.
* Khi nào đặt Component vào `designsystem`.
* Khi nào đặt Component vào `ui`.
* Quy tắc dependency giữa Feature → UI → Design System.

---

# 1. Kiến trúc của Google

Trong Now in Android, Google chia các module dùng chung thành nhiều tầng.

```text
Feature
    │
    ▼
core:ui
    │
    ▼
core:designsystem
    │
    ▼
Material3
```

Ý nghĩa:

* Feature chỉ xây dựng nghiệp vụ.
* UI dùng chung được đặt trong `core:ui`.
* Thành phần của Design System được đặt trong `core:designsystem`.

Feature **không được phụ thuộc Feature khác**.

---

# 2. Vai trò của core:designsystem

`core:designsystem` chứa **Design Language** của toàn bộ ứng dụng.

Nó trả lời câu hỏi:

> Ứng dụng được thiết kế như thế nào?

Module này **không biết ứng dụng đang quản lý hóa đơn, thông báo hay tin tức**.

Nó chỉ biết cách hiển thị.

Ví dụ:

```text
Theme

Color

Typography

Shape

Spacing

Elevation

Icon

Button

Card

Chip

Dialog

TextField

TopBar
```

Đây là những Primitive Component.

---

## Ví dụ

```kotlin
AppButton(...)
```

```kotlin
AppCard(...)
```

```kotlin
AppTextField(...)
```

```kotlin
AppToolbar(...)
```

Tất cả đều có thể dùng ở bất kỳ Feature nào.

---

## Cấu trúc

```text
core/designsystem/

    theme/

        Color.kt

        Theme.kt

        Typography.kt

        Shape.kt

    component/

        AppButton.kt

        AppCard.kt

        AppDialog.kt

        AppTextField.kt

        AppToolbar.kt

        AppLoading.kt

        AppChip.kt

        AppIconButton.kt
```

---

# 3. Vai trò của core:ui

`core:ui` chứa **UI Component có ý nghĩa nghiệp vụ nhưng được dùng chung bởi nhiều Feature**.

Nó trả lời câu hỏi:

> Component này biểu diễn dữ liệu gì?

Ví dụ:

```text
Notification Item

Invoice Card

Room Card

Contract Card

News Card

User Avatar
```

Những component này được xây dựng từ Design System.

Ví dụ:

```text
NotificationItem

↓

AppCard

↓

AppButton

↓

Material3
```

---

## Ví dụ

```kotlin
NotificationItem(...)
```

```kotlin
InvoiceCard(...)
```

```kotlin
RoomCard(...)
```

```kotlin
UserAvatar(...)
```

---

## Cấu trúc

```text
core/ui/

    notification/

        NotificationItem.kt

        NotificationEmpty.kt

    invoice/

        InvoiceCard.kt

        InvoiceStatusChip.kt

    room/

        RoomCard.kt

    contract/

        ContractCard.kt

    common/

        ErrorView.kt

        EmptyView.kt

        LoadingView.kt
```

---

# 4. Feature sử dụng như thế nào?

Ví dụ:

```text
Feature Home

↓

NotificationItem

↓

AppCard

↓

Material3
```

Feature chỉ biết:

```kotlin
NotificationItem(...)
```

NotificationItem bên trong sử dụng

```kotlin
AppCard(...)
```

AppCard bên trong sử dụng

```kotlin
Card(...)
```

của Material3.

---

# 5. Quy tắc Dependency

Đúng

```text
Feature

↓

core/ui

↓

core/designsystem

↓

Material3
```

Sai

```text
Feature A

↓

Feature B
```

Sai

```text
Feature

↓

Material3
```

trong trường hợp ứng dụng đã có Design System.

---

# 6. Quy tắc quyết định đặt Component ở đâu

## Bước 1

Component này chỉ là thành phần giao diện?

Ví dụ:

```text
Button

Card

Dialog

Toolbar

Chip

TextField
```

↓

Đặt trong

```text
core/designsystem
```

---

## Bước 2

Component này biểu diễn dữ liệu nghiệp vụ?

Ví dụ:

```text
Invoice Card

Notification Item

Room Card

Contract Card
```

↓

Đặt trong

```text
core/ui
```

---

## Bước 3

Component chỉ dùng trong một Feature?

Ví dụ:

```text
Invoice Filter Dialog
```

chỉ xuất hiện ở Feature Invoice.

↓

Không đưa lên Core.

↓

Giữ nguyên trong

```text
feature/invoice/ui
```

---

# 7. Ví dụ thực tế

## Notification

Được dùng ở:

```text
Home

Notification
```

↓

Đặt

```text
core/ui/

    notification/

        NotificationItem.kt
```

---

## Invoice Card

Được dùng ở:

```text
Home

Invoice
```

↓

Đặt

```text
core/ui/

    invoice/

        InvoiceCard.kt
```

---

## App Button

Được dùng ở:

```text
Toàn bộ ứng dụng
```

↓

Đặt

```text
core/designsystem/

    component/

        AppButton.kt
```

---

## App Dialog

↓

```text
core/designsystem
```

---

## App Toolbar

↓

```text
core/designsystem
```

---

# 8. Ví dụ KTX

```text
core/

    designsystem/

        component/

            AppButton

            AppCard

            AppDialog

            AppToolbar

            AppTextField

            AppLoading

        theme/

            Theme

            Color

            Typography

            Shape

    ui/

        notification/

            NotificationItem

            NotificationEmpty

        invoice/

            InvoiceCard

            InvoiceStatusChip

        room/

            RoomCard

        contract/

            ContractCard

        user/

            UserAvatar

        common/

            ErrorView

            EmptyView
```

---

# 9. Những gì KHÔNG nên đặt trong core:ui

Không đặt Component chỉ dùng cho một Feature.

Ví dụ:

```text
InvoiceFilterDialog

NotificationSearchBar

HomeBanner

ProfileAvatarEditor
```

Những component này vẫn thuộc Feature.

Ví dụ:

```text
feature/

    invoice/

        ui/

            InvoiceFilterDialog.kt
```

---

# 10. Những gì KHÔNG nên đặt trong core:designsystem

Không đặt Component mang ý nghĩa nghiệp vụ.

Ví dụ:

```text
InvoiceCard

NotificationItem

RoomCard

NewsCard
```

Chúng phải nằm trong

```text
core/ui
```

---

# 11. Quy tắc vàng

## Đặt trong core:designsystem nếu:

* Không chứa dữ liệu nghiệp vụ.
* Chỉ mô tả giao diện.
* Có thể dùng ở mọi nơi.
* Là Primitive Component.

Ví dụ:

```text
Button

Card

Dialog

Theme

Typography

Spacing

Color

Chip
```

---

## Đặt trong core:ui nếu:

* Biểu diễn dữ liệu nghiệp vụ.
* Có thể dùng ở nhiều Feature.
* Được xây dựng từ Design System.

Ví dụ:

```text
NotificationItem

InvoiceCard

RoomCard

ContractCard

UserAvatar
```

---

## Giữ trong Feature nếu:

* Chỉ được dùng trong đúng một Feature.
* Không có nhu cầu tái sử dụng.

Ví dụ:

```text
HomeBanner

InvoiceFilterDialog

NotificationSearchBar

ProfileEditDialog
```

---

# 12. Sơ đồ tổng thể

```text
feature/home
feature/invoice
feature/notification
          │
          ▼
      core/ui
          │
          ▼
  NotificationItem
      InvoiceCard
      RoomCard
          │
          ▼
core/designsystem
          │
          ▼
AppButton
AppCard
AppDialog
AppToolbar
Theme
Typography
          │
          ▼
Material3
```

---

# 13. Nguyên tắc cốt lõi

Google không phân chia module dựa trên số lượng Composable.

Google phân chia theo **mức độ trừu tượng**:

* `core:designsystem` cung cấp **các thành phần giao diện nền tảng** (UI primitives).
* `core:ui` cung cấp **các thành phần giao diện có ngữ nghĩa nghiệp vụ** (UI composites) được nhiều Feature dùng chung.
* `feature` chỉ lắp ráp các UI này để xây dựng màn hình và xử lý nghiệp vụ.

Nhờ cách phân tầng này:

* Feature không phụ thuộc lẫn nhau.
* UI được tái sử dụng hiệu quả.
* Design System được quản lý tập trung.
* Thay đổi giao diện có thể thực hiện ở một nơi mà không ảnh hưởng đến logic nghiệp vụ.
