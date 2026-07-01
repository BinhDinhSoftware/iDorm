package com.bdsoftware.idorm.core.model

/**
 * Domain model representing full student profile information
 * fetched from api/Student/GetStudentInfo.
 *
 * Các trường có hậu tố "Id" là giá trị ID gốc từ API.
 * Các trường có hậu tố "Name" là giá trị đã được backend resolve.
 * Ưu tiên dùng Name, fallback về Id nếu Name null.
 * Tương lai sẽ gọi API lookup (GetListEthnic, GetListCountry, GetListPriority...)
 * để map Id → display value.
 */
data class StudentProfile(
    val id: Int = 0,
    val fullName: String = "",
    val studentCode: String = "",
    val dormitoryFullName: String = "",
    val profileImagePath: String = "",
    val mobile: String = "",
    val phone: String = "",
    // Thông tin chung
    val firstName: String = "",          // Họ và tên lót
    val lastName: String = "",           // Tên
    val birthday: String = "",           // ISO datetime
    val gender: String = "",             // "Nam" / "Nữ"
    val universityId: String = "",       // ID trường — dùng tạm, tương lai map qua API
    val universityName: String = "",     // Tên trường (resolved by backend)
    val departmentId: String = "",       // ID khoa
    val departmentName: String = "",     // Tên khoa (resolved by backend)
    val idCardNumber: String = "",       // CMND/CCCD
    val idCardDate: String = "",         // Ngày cấp
    val idCardIssued: String = "",       // ID nơi cấp
    val idCardIssuedName: String = "",   // Tên nơi cấp (resolved)
    val ethnic: String = "",             // ID dân tộc
    val ethnicName: String = "",         // Tên dân tộc (resolved)
    val religion: String = "",
    val countryId: String = "",          // ID quốc gia
    val countryName: String = "",        // Tên quốc gia (resolved)
    // Thông tin lưu trú
    val provineId: String = "",          // ID tỉnh (API dùng "Provine")
    val provinceName: String = "",       // Tên tỉnh (resolved)
    val districtId: String = "",         // ID quận/huyện
    val districtName: String = "",       // Tên quận/huyện (resolved)
    val wardsId: String = "",            // ID phường/xã
    val wardName: String = "",           // Tên phường/xã (resolved)
    val address: String = "",            // Số nhà/Tên đường
    val email: String = "",
    // Thông tin liên hệ
    val familyName: String = "",         // Tên người liên hệ
    val familyPhone: String = "",        // SĐT người liên hệ
    val familyAddress: String = "",      // Địa chỉ người liên hệ
    // Bảo hiểm
    val insuranceHospitalCode: String = "",  // Mã bệnh viện
    val insuranceCode: String = "",          // Số BHYT
    val healthInsuranceIssuedDate: String = "",
    val insuranceBeginDate: String = "",
    val insuranceExpiryDate: String = "",
    // Thông tin khác
    val priority: String = "",           // ID đối tượng ưu tiên — tương lai map qua GetListPriority
    val highSchoolName: String = "",
    val ability: String = "",            // Năng khiếu
    val isLeaguer: String = "",          // Đoàn viên ("Có" / "Không")
    val isUnionists: String = ""         // Đảng viên ("Có" / "Không")
)
