---
name: styles
description: Sử dụng kỹ năng này để tích hợp Jetpack Compose Styles API vào một dự án Android. Kỹ năng này hướng dẫn bạn cách nâng cấp các dependencies, thiết lập themes cho component, tạo các components tùy chỉnh có thể style (styleable), và chuyển đổi các thuộc tính layout hiện tại sang sử dụng các style thống nhất. Nâng cấp các custom design system components, thay thế các tham số được hardcode bằng các thuộc tính Style, và sử dụng Modifier.styleable cho các trạng thái tương tác.
license: Các điều khoản đầy đủ xem tại LICENSE.txt
metadata:
  author: Google LLC
  last-updated: '2026-06-06'
  keywords:
  - Jetpack Compose
  - Styles
  - Theming with Styles
  - Migrate to Styles
  - Modifier.styleable
---

## Các Hạn Chế (Limitations)

- Cảnh báo người dùng rằng kỹ năng này đang trong giai đoạn THỬ NGHIỆM (EXPERIMENTAL) và yêu cầu cập nhật phiên bản Compose lên bản alpha, cũng như opt-in (đồng ý sử dụng) các Experimental APIs.
- Kỹ năng này chỉ hỗ trợ các UI component tùy chỉnh và các theme tùy chỉnh.
- Kỹ năng này không hỗ trợ các Styles của Material Design component.

## Điều kiện tiên quyết (Prerequisites)

### 1. Nâng cấp dependencies

- Dự án phải sử dụng `compileSdk` phiên bản 37 trở lên.
- Dự án phải sử dụng `androidx.compose.foundation:foundation` phiên bản `1.12.0-alpha01` trở lên.
- Hoặc dự án phải sử dụng Compose BOM phiên bản `2026.04.01` trở lên.
- API này yêu cầu chính xác package sau: `import androidx.compose.foundation.style.Style`

### 2. Cấu hình các tùy chọn compiler để bật experimental API

Bạn phải opt-in API thử nghiệm này ở cấp độ dự án. Hãy thêm block sau vào tệp `build.gradle.kts` ở module của bạn:

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("17")
            freeCompilerArgs.add("-opt-in=androidx.compose.foundation.style.ExperimentalFoundationStyleApi")
        }
    }

## Các quy trình và hướng dẫn cốt lõi (Core workflows and guides)

Tham khảo tài liệu chính thức để hoàn thành các tác vụ phát triển cụ thể:

- Cách sử dụng Style cơ bản: Để thiết lập backgrounds, kích thước, và căn chỉnh cho một component, hãy làm theo [Hướng dẫn Cơ bản về Compose Styles](references/android/develop/ui/compose/styles/fundamentals.md).
- State (Trạng thái) và Transitions (Chuyển tiếp): Để cấu hình thay đổi thuộc tính cho các trạng thái (như pressed hoặc hovered), hãy làm theo [Hướng dẫn Animation và Styling Dựa Trên State](references/android/develop/ui/compose/styles/state-animations.md).
- Đánh đổi Kiến trúc: Để quyết định khi nào nên dùng Style thay vì dùng Modifier tiêu chuẩn, hãy làm theo [So sánh giữa Styles và Modifiers](references/android/develop/ui/compose/styles/styles-vs-modifiers.md).
- Tích hợp cấp độ Theme: Để kết nối các định nghĩa style với custom theme, hãy làm theo [Theming với Styles](references/android/develop/ui/compose/styles/theming.md) và [Custom Themes trong Compose](references/android/develop/ui/compose/designsystems/custom.md).

## Quy trình Migration từng bước (Step-by-Step Migration Workflow)

### Bước 1: Phân tích cấu trúc theme

1. Tìm file theme trung tâm của bạn (ví dụ như `Theme.kt`).
2. Xác định các design tokens. Lưu ý các reference (tham chiếu) đến colors, typography, và shapes (ví dụ: `LocalColorScheme`, `LocalTypography`, hoặc `LocalShapes`).
3. Nếu dự án thiếu các dependencies của Jetpack Compose, hãy dừng lại. Hướng dẫn người dùng chuyển (migrate) sang Jetpack Compose trước.
4. Nếu dự án có import `androidx.compose.material.MaterialTheme`, khuyên người dùng nên migrate sang Material 3 trước khi tiếp tục.

### Bước 2: Thiết lập `ComponentStyles`

1. Tạo một file mới tên là `ComponentStyles.kt` trong thư mục theme của bạn.
2. Định nghĩa một data class (hoặc object) ở cấp ngoài cùng (top-level) để chứa các style của component, ví dụ trong Jetsnack gọi là `JetsnackStyles`:


   ```kotlin
   object ExampleComponentStyles {
       val customButtonStyle: Style = {

       }
       val customTextFieldStyle: Style = {

       }
   }
   ```

   <br />

3. Expose class này thông qua custom theme của bạn với một tham chiếu tĩnh, không sử dụng `CompositionLocals` ở đây vì điều đó không cần thiết.


   ```kotlin
   @Immutable
   class JetsnackTheme(
       // các thuộc tính Design system khác
   ) {
       companion object {
           val colors: CustomThemingWithStyles.JetsnackColors
               @Composable @ReadOnlyComposable
               get() = LocalJetsnackTheme.current.colors
           // ...

           // thêm tham chiếu tĩnh trợ giúp (helper static reference)
           val styles: ComponentStyles = ComponentStyles
       }
   }
   ```

   <br />

4. Cung cấp các extension trên `StyleScope` để tham chiếu trực tiếp đến các theme tokens nếu chúng được expose bằng `CompositionLocals`. Ví dụ:


   ```kotlin
   val StyleScope.colors: JetsnackColors
       get() = LocalJetsnackTheme.currentValue.colors

   val StyleScope.typography: androidx.compose.material3.Typography
       get() = LocalJetsnackTheme.currentValue.typography

   val StyleScope.shapes: Shapes
       get() = LocalJetsnackTheme.currentValue.shapes
   ```

   <br />

### Bước 3: Migrate một component sang Styles API

Đối với mỗi component tùy chỉnh (ví dụ `CustomButton`), thực hiện chuỗi các bước sau:

1. Nếu bạn có thể chạy được Android emulator, hãy xác định bài test ảnh chụp màn hình (screenshot test) hiện có cho component đó. Nếu chưa có, hãy tạo một screenshot test bằng framework testing đang có của dự án. Nếu chưa có framework, hãy sử dụng UI Automator hoặc Espresso để tạo screenshot test với thiết lập tối thiểu. Chạy test và chụp ảnh màn hình làm cơ sở (baseline) cho Component đó. NẾU KHÔNG THỂ, tiến hành bước tiếp theo mà không cần screenshot test.
2. **Loại bỏ các tham số styling riêng lẻ**: Bỏ các tham số styling như `backgroundColor`, `shape`, `textStyle`, và `contentPadding` ra khỏi signature (khai báo) của hàm - loại bỏ bất kỳ thứ gì mà `StyleScope` hỗ trợ.
3. **Thêm tham số style**: Thêm `style: Style = Style` vào signature của hàm.
4. **Khai báo state tracking**: Nếu component đó cho phép tương tác (interactable), tạo một `MutableStyleState` bằng interaction source. Cập nhật các trường state (như `isEnabled`) bên trong Composable để theo dõi state một cách chuẩn xác.
5. **Áp dụng modifier styleable**: Thay thế các layout modifier cụ thể ở phần tử gốc (root element) bằng `Modifier.styleable()`.
6. **Chuyển defaults sang ComponentStyles**: Chuyển các giá trị hardcode từ định nghĩa component sang một instance `Style` chuyên dụng ở trong file `ComponentStyles.kt`.
7. **Kiểm tra (Validate) component:** So sánh ảnh chụp màn hình cơ sở lúc đầu với kết xuất Compose Preview của composable mới. Bỏ qua các chuỗi nội dung; hãy tập trung vào layout và styling. Tinh chỉnh code Compose cho đến khi đạt được sự cân bằng trực quan. Khi đã xác nhận đúng, hãy viết một Compose UI test cho composable mới này.

#### Ví dụ chuyển đổi (Migration example)

Trước khi chuyển đổi:


```kotlin
@Composable
fun CustomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = JetsnackTheme.colors.brandLight,
    disabledBackgroundColor: Color = JetsnackTheme.colors.brandSecondary,
    shape: Shape = JetsnackTheme.shapes.extraLarge,
    textStyle: TextStyle = JetsnackTheme.typography.labelLarge,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier
            .clickable(onClick = onClick, indication = null, interactionSource = interactionSource)
            .background(if (enabled) backgroundColor else disabledBackgroundColor, shape)
            .defaultMinSize(58.dp, 40.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
```

<br />

Sau khi chuyển đổi:


```kotlin
// Được expose qua ComponentStyles.kt
object ComponentStyles {
    val buttonStyle = Style {
        background(colors.brandLight)
        shape(shapes.extraLarge)
        minWidth(58.dp)
        minHeight(40.dp)
        textStyle(typography.labelLarge)
        disabled {
            background(colors.brandSecondary)
        }
    }
}

@Composable
fun CustomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: Style = Style,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }
    Row(
        modifier
            .clickable(onClick = onClick, indication = null, interactionSource = interactionSource)
            .styleable(styleState, JetsnackTheme.styles.buttonStyle, style),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
```

<br />

### Bước 4: Kiểm tra (Validate) những thay đổi

1. Build dự án. Đảm bảo rằng không có lỗi compile (biên dịch) nào.
2. Chạy screenshot tests của module của bạn.
3. So sánh các đầu ra trực quan của toàn bộ app giữa trước và sau khi cập nhật các components. Xác minh rằng không có các suy thoái giao diện (layout regressions) trực quan nào.
