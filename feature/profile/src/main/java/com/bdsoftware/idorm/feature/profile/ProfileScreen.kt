package com.bdsoftware.idorm.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bdsoftware.idorm.core.designsystem.theme.ComponentStyles
import com.bdsoftware.idorm.core.model.StudentProfile
import com.bdsoftware.idorm.core.designsystem.component.IDormTopAppBar
import androidx.compose.ui.res.stringResource
import com.bdsoftware.idorm.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val primaryBlue = ComponentStyles.PrimaryBlue
    val backgroundColor = Color(0xFFF8F9FA)

    Scaffold(
        topBar = {
            IDormTopAppBar(
                title = stringResource(DesignR.string.profile_screen_title),
                onBack = onBack,
                onHomeClick = onHomeClick
            )
        },
        containerColor = backgroundColor,
        modifier = modifier
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileViewModel.ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryBlue)
                }
            }

            is ProfileViewModel.ProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color.Red.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                        ) {
                            Text(stringResource(DesignR.string.profile_retry_button))
                        }
                    }
                }
            }

            is ProfileViewModel.ProfileUiState.Success -> {
                val profile = state.profile
                ProfileContent(
                    profile = profile,
                    primaryBlue = primaryBlue,
                    scrollState = scrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: StudentProfile,
    primaryBlue: Color,
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Thông tin xác thực
        ProfileCard(
            title = stringResource(DesignR.string.profile_card_identity_title),
            subtitle = stringResource(DesignR.string.profile_card_identity_subtitle),
            icon = Icons.Default.VerifiedUser,
            primaryColor = primaryBlue
        ) {
            ProfileInfoItem(Icons.Default.Person, stringResource(DesignR.string.profile_label_name), profile.fullName, primaryBlue)
            ProfileInfoItem(Icons.Default.Face, stringResource(DesignR.string.profile_label_gender), profile.gender, primaryBlue)
            ProfileInfoItem(Icons.Default.DateRange, stringResource(DesignR.string.profile_label_birthday), profile.birthday, primaryBlue)
            ProfileInfoItem(Icons.Default.CreditCard, stringResource(DesignR.string.profile_label_id_card), profile.idCardNumber, primaryBlue)
            ProfileInfoItem(Icons.Default.DateRange, stringResource(DesignR.string.profile_label_id_card_date), profile.idCardDate, primaryBlue)
            ProfileInfoItem(Icons.Default.Groups, stringResource(DesignR.string.profile_label_ethnicity), profile.ethnicName.ifEmpty { profile.ethnic }, primaryBlue)
            ProfileInfoItem(Icons.Default.VolunteerActivism, stringResource(DesignR.string.profile_label_religion), profile.religion, primaryBlue)
            ProfileInfoItem(Icons.Default.School, stringResource(DesignR.string.profile_label_university), profile.universityName.ifEmpty { profile.universityId }, primaryBlue)
            ProfileInfoItem(Icons.Default.Class, stringResource(DesignR.string.profile_label_department), profile.departmentName.ifEmpty { profile.departmentId }, primaryBlue)
            ProfileInfoItem(Icons.Default.Badge, stringResource(DesignR.string.profile_label_student_code), profile.studentCode, primaryBlue, showDivider = false)
        }

        // Card 2: Thông tin cơ bản (Lưu trú)
        ProfileCard(
            title = stringResource(DesignR.string.profile_card_basic_title),
            subtitle = stringResource(DesignR.string.profile_card_basic_subtitle),
            icon = Icons.Default.ContactMail,
            primaryColor = primaryBlue
        ) {
            ProfileInfoItem(Icons.Default.LocationCity, stringResource(DesignR.string.profile_label_province), profile.provinceName.ifEmpty { profile.provineId }, primaryBlue)
            ProfileInfoItem(Icons.Default.LocationOn, stringResource(DesignR.string.profile_label_district), profile.districtName.ifEmpty { profile.districtId }, primaryBlue)
            ProfileInfoItem(Icons.Default.Home, stringResource(DesignR.string.profile_label_address), profile.address, primaryBlue)
            ProfileInfoItem(Icons.Default.Email, stringResource(DesignR.string.profile_label_email), profile.email, primaryBlue)
            ProfileInfoItem(Icons.Default.Phone, stringResource(DesignR.string.profile_label_phone), profile.mobile, primaryBlue, showDivider = false)
        }

        // Card 3: Thông tin liên hệ
        ProfileCard(
            title = stringResource(DesignR.string.profile_card_contact_title),
            subtitle = stringResource(DesignR.string.profile_card_contact_subtitle),
            icon = Icons.Default.ContactPhone,
            primaryColor = primaryBlue
        ) {
            ProfileInfoItem(Icons.Default.Person, stringResource(DesignR.string.profile_label_contact_name), profile.familyName, primaryBlue)
            ProfileInfoItem(Icons.Default.Phone, stringResource(DesignR.string.profile_label_contact_phone), profile.familyPhone, primaryBlue)
            ProfileInfoItem(Icons.Default.LocationOn, stringResource(DesignR.string.profile_label_contact_address), profile.familyAddress, primaryBlue, showDivider = false)
        }

        // Card 4: Bảo hiểm
        ProfileCard(
            title = stringResource(DesignR.string.profile_card_insurance_title),
            subtitle = stringResource(DesignR.string.profile_card_insurance_subtitle),
            icon = Icons.Default.HealthAndSafety,
            primaryColor = primaryBlue
        ) {
            ProfileInfoItem(Icons.Default.Numbers, stringResource(DesignR.string.profile_label_hospital_code), profile.insuranceHospitalCode, primaryBlue)
            ProfileInfoItem(Icons.Default.CreditCard, stringResource(DesignR.string.profile_label_insurance_code), profile.insuranceCode, primaryBlue)
            ProfileInfoItem(Icons.Default.DateRange, stringResource(DesignR.string.profile_label_insurance_issued), profile.healthInsuranceIssuedDate, primaryBlue)
            ProfileInfoItem(Icons.Default.DateRange, stringResource(DesignR.string.profile_label_insurance_from), profile.insuranceBeginDate, primaryBlue)
            ProfileInfoItem(Icons.Default.DateRange, stringResource(DesignR.string.profile_label_insurance_to), profile.insuranceExpiryDate, primaryBlue, showDivider = false)
        }

        // Card 5: Thông tin bổ sung
        ProfileCard(
            title = stringResource(DesignR.string.profile_card_additional_title),
            subtitle = stringResource(DesignR.string.profile_card_additional_subtitle),
            icon = Icons.Default.AddCard,
            primaryColor = primaryBlue
        ) {
            ProfileInfoItem(Icons.Default.School, stringResource(DesignR.string.profile_label_high_school), profile.highSchoolName, primaryBlue)
            ProfileInfoItem(Icons.Default.EmojiEvents, stringResource(DesignR.string.profile_label_ability), profile.ability, primaryBlue)
            ProfileInfoItem(Icons.Default.Groups, stringResource(DesignR.string.profile_label_league_member), profile.isLeaguer, primaryBlue)
            ProfileInfoItem(Icons.Default.Flag, stringResource(DesignR.string.profile_label_party_member), profile.isUnionists, primaryBlue, showDivider = false)
        }

        // Card 6: Thử nghiệm Crashlytics
        // ProfileCard(
        //     title = "Thử nghiệm Crashlytics",
        //     subtitle = "Tính năng dành cho nhà phát triển để kiểm tra Firebase",
        //     icon = Icons.Default.BugReport,
        //     primaryColor = Color.Red
        // ) {
        //     Button(
        //         onClick = {
        //             throw RuntimeException("Thử nghiệm Crashlytics - Crash hệ thống chủ động")
        //         },
        //         colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
        //         modifier = Modifier
        //             .fillMaxWidth()
        //             .padding(vertical = 4.dp)
        //     ) {
        //         Text("Kích hoạt System Crash (Fatal)", color = Color.White)
        //     }

        //     Button(
        //         onClick = {
        //             try {
        //                 throw Exception("Thử nghiệm Crashlytics - Ngoại lệ caught (Non-fatal)")
        //             } catch (e: Exception) {
        //                 com.bdsoftware.idorm.core.common.util.CrashlyticsUtils.recordException(
        //                     throwable = e,
        //                     contextKeys = mapOf(
        //                         "test_key_string" to "test_value_123",
        //                         "test_key_boolean" to true,
        //                         "test_key_int" to 456
        //                     )
        //                 )
        //             }
        //         },
        //         colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
        //         modifier = Modifier
        //             .fillMaxWidth()
        //             .padding(vertical = 4.dp)
        //     ) {
        //         Text("Ghi nhận Non-fatal Exception", color = Color.White)
        //     }
        // }
    }
}

@Composable
private fun ProfileCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    primaryColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(primaryColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(
                color = Color.Black.copy(alpha = 0.06f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Content Items
            content()
        }
    }
}

@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    primaryColor: Color,
    showDivider: Boolean = true
) {
    val displayValue = value.ifEmpty { stringResource(DesignR.string.profile_not_updated) }
    val isPlaceholder = value.isEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.4f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium
            ),
            color = if (isPlaceholder) Color(0xFF9E9E9E) else Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }

    if (showDivider) {
        HorizontalDivider(
            color = Color.Black.copy(alpha = 0.04f),
            modifier = Modifier.padding(start = 30.dp)
        )
    }
}
