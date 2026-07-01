package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

/**
 * Network response from api/Student/GetStudentInfo.
 * Field names match the actual JSON keys from the API exactly.
 * See GetStudentInfo.json for sample response.
 */
@Serializable
data class NetworkUserProfileResponse(
    val Id: Int = 0,
    val FullName: String = "",
    val StudentCode: String = "",
    val DormitoryFullName: String = "",
    val ProfileImagePath: String? = null,
    val Mobile: String? = null,
    val Phone: String? = null,
    // Thông tin chung
    val FirstName: String? = null,      // Họ và tên lót (e.g. "Nguyễn Phương")
    val LastName: String? = null,        // Tên (e.g. "Tấn")
    val Birthday: String? = null,        // ISO datetime (e.g. "2004-10-10T00:00:00")
    val Gender: Boolean? = null,         // false = Nam, true = Nữ
    val UniversityId: Int? = null,        // ID trường — tương lai sẽ map qua API lookup
    val UniversityName: String? = null,   // Tên trường (resolved by backend)
    val DepartmentId: Int? = null,        // ID khoa — tương lai sẽ map qua API lookup
    val DepartmentName: String? = null,   // Tên khoa (resolved by backend)
    val IdCardNumber: String? = null,     // Số CMND/CCCD
    val IdCardDate: String? = null,       // Ngày cấp CMND
    val IdCardIssued: String? = null,     // ID nơi cấp — tương lai sẽ map
    val IdCardIssuedName: String? = null,  // Tên nơi cấp (resolved by backend)
    val Ethnic: String? = null,           // ID dân tộc — tương lai sẽ map qua GetListEthnic
    val EthnicName: String? = null,       // Tên dân tộc (resolved by backend)
    val Religion: String? = null,         // Tôn giáo (plain string)
    val CountryId: Int? = null,           // ID quốc gia — tương lai sẽ map qua GetListCountry
    val CountryName: String? = null,      // Tên quốc gia (resolved by backend)
    // Thông tin lưu trú
    val ProvineId: String? = null,        // ID tỉnh (lưu ý API dùng "Provine" không phải "Province")
    val ProvinceName: String? = null,     // Tên tỉnh (resolved by backend)
    val DistrictId: String? = null,       // ID quận/huyện
    val DistrictName: String? = null,     // Tên quận/huyện (resolved by backend)
    val WardsId: String? = null,          // ID phường/xã
    val WardName: String? = null,         // Tên phường/xã (resolved by backend)
    val Address: String? = null,          // Số nhà/Tên đường
    val Email: String? = null,
    // Thông tin liên hệ
    val FamilyName: String? = null,       // Tên người liên hệ
    val FamilyPhone: String? = null,      // SĐT người liên hệ
    val FamilyAddress: String? = null,    // Địa chỉ người liên hệ
    // Bảo hiểm
    val InsuranceHospitalCode: String? = null,  // Mã bệnh viện BHYT
    val InsuranceCode: String? = null,          // Số BHYT
    val HealthInsuranceIssuedDate: String? = null,
    val InsuranceBeginDate: String? = null,
    val InsuranceExpiryDate: String? = null,
    // Thông tin khác
    val Priority: String? = null,         // ID đối tượng ưu tiên — tương lai map qua GetListPriority
    val HighSchoolName: String? = null,
    val Ability: String? = null,          // Năng khiếu
    val IsLeaguer: Boolean? = null,       // Đoàn viên
    val IsUnionists: Boolean? = null      // Đảng viên
)
