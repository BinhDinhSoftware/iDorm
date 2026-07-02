package com.bdsoftware.idorm.core.data.repository

import android.util.Log
import com.bdsoftware.idorm.core.common.util.formatDateString
import com.bdsoftware.idorm.core.common.util.normalizeAddressString
import com.bdsoftware.idorm.core.model.StudentProfile
import com.bdsoftware.idorm.core.network.retrofit.RetrofitStudentNetwork
import kotlinx.coroutines.flow.firstOrNull
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val tokenManager: IDormPreferencesDataSource,
    private val network: RetrofitStudentNetwork
) {
    private fun formatDate(isoDate: String?): String = formatDateString(isoDate)
    private fun normalizeAddress(address: String?): String = normalizeAddressString(address)


    /**
     * Calls api/Student/GetStudentInfo and returns domain model directly.
     * Does NOT cache to DataStore — caller receives fresh API data.
     *
     * Cho các trường phụ thuộc (trường, tỉnh, dân tộc...):
     * - Ưu tiên dùng Name (đã resolve bởi backend) khi có.
     * - Fallback về Id khi Name = null.
     * - Tương lai sẽ call API lookup (GetListEthnic, GetListCountry...) để map Id → display value.
     */
    suspend fun getStudentInfo(): StudentProfile {
        val r = network.getProfile()
        return StudentProfile(
            id = r.Id,
            fullName = r.FullName,
            studentCode = r.StudentCode,
            dormitoryFullName = r.DormitoryFullName,
            profileImagePath = r.ProfileImagePath.orEmpty(),
            mobile = r.Mobile.orEmpty(),
            phone = r.Phone.orEmpty(),
            // Thông tin chung
            firstName = r.FirstName.orEmpty(),
            lastName = r.LastName.orEmpty(),
            birthday = formatDate(r.Birthday),
            gender = when (r.Gender) {
                false -> "Nam"
                true -> "Nữ"
                null -> ""
            },
            universityId = r.UniversityId?.toString().orEmpty(),
            universityName = r.UniversityName.orEmpty(),
            departmentId = r.DepartmentId?.toString().orEmpty(),
            departmentName = r.DepartmentName.orEmpty(),
            idCardNumber = r.IdCardNumber.orEmpty(),
            idCardDate = formatDate(r.IdCardDate),
            idCardIssued = r.IdCardIssued.orEmpty(),
            idCardIssuedName = r.IdCardIssuedName.orEmpty(),
            ethnic = r.Ethnic.orEmpty(),
            ethnicName = r.EthnicName.orEmpty(),
            religion = r.Religion.orEmpty(),
            countryId = r.CountryId.orEmpty(),
            countryName = r.CountryName.orEmpty(),
            // Thông tin lưu trú
            provineId = r.ProvineId.orEmpty(),
            provinceName = r.ProvinceName.orEmpty(),
            districtId = r.DistrictId.orEmpty(),
            districtName = r.DistrictName.orEmpty(),
            wardsId = r.WardsId.orEmpty(),
            wardName = r.WardName.orEmpty(),
            address = normalizeAddress(r.Address),
            email = r.Email.orEmpty(),
            // Thông tin liên hệ
            familyName = r.FamilyName.orEmpty(),
            familyPhone = r.FamilyPhone.orEmpty(),
            familyAddress = normalizeAddress(r.FamilyAddress),
            // Bảo hiểm
            insuranceHospitalCode = r.InsuranceHospitalCode.orEmpty(),
            insuranceCode = r.InsuranceCode.orEmpty(),
            healthInsuranceIssuedDate = formatDate(r.HealthInsuranceIssuedDate),
            insuranceBeginDate = formatDate(r.InsuranceBeginDate),
            insuranceExpiryDate = formatDate(r.InsuranceExpiryDate),
            // Thông tin khác
            priority = r.Priority.orEmpty(),
            highSchoolName = r.HighSchoolName.orEmpty(),
            ability = r.Ability.orEmpty(),
            isLeaguer = when (r.IsLeaguer) {
                true -> "Có"
                false -> "Không"
                null -> ""
            },
            isUnionists = when (r.IsUnionists) {
                true -> "Có"
                false -> "Không"
                null -> ""
            }
        )
    }

    /**
     * Fetches profile from API and saves basic info to DataStore for offline/dashboard use.
     * Uses Name fields when available, falls back to Id.
     */
    suspend fun fetchUserProfile(force: Boolean = false) {
        val currentName = tokenManager.userFullName.firstOrNull()
        val currentMobile = tokenManager.userMobile.firstOrNull()
        val currentUserId = tokenManager.userId.firstOrNull()
        if (force || currentName.isNullOrEmpty() || currentMobile.isNullOrEmpty() || currentUserId == null) {
            try {
                val r = network.getProfile()
                Log.d("UserRepository", "Profile: $r")
                tokenManager.saveUserInfo(
                    id = r.Id,
                    fullname = r.FullName,
                    room = r.DormitoryFullName,
                    avatarUrl = r.ProfileImagePath,
                    mobile = r.Mobile,
                    studentCode = r.StudentCode,
                    email = r.Email
                )
            } catch (e: Exception) {
                Log.e("UserRepository", "Error fetching profile", e)
            }
        }
    }
    suspend fun fetchUserProfileIfNeeded() {
        fetchUserProfile(force = false)
    }
}
