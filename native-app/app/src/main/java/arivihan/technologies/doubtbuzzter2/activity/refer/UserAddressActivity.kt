package arivihan.technologies.doubtbuzzter2.activity.refer

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.compose.ArivihanWindowInsets
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanButton
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanButtonStyle
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanSnackbar
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanSnackbarType
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanTopAppBar
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys
import arivihan.technologies.doubtbuzzter2.model.book_order.AddressFormData
import arivihan.technologies.doubtbuzzter2.model.refer.CreateAddressRequest
import arivihan.technologies.doubtbuzzter2.model.refer.uistate.AddressListState
import arivihan.technologies.doubtbuzzter2.model.refer.uistate.PincodeValidationState
import arivihan.technologies.doubtbuzzter2.viewModel.ReferAndEarnViewModel


class UserAddressActivity : BaseActivity(), EdgeToEdgeHelper.SelfManagedInsets {


    private val viewModel: ReferAndEarnViewModel by lazy {
        ViewModelProvider(this)[ReferAndEarnViewModel::class.java]
    }





    companion object {
        private const val EXTRA_SUBJECT_CODES = "extra_subject_codes"

        fun startForCheckout(context: Context, subjectCodes: ArrayList<String>) {
            val intent = Intent(context, UserAddressActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_SUBJECT_CODES, subjectCodes)
            }
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val subjectCodes = intent.getStringArrayListExtra(EXTRA_SUBJECT_CODES) ?: arrayListOf()

        viewModel.loadAddressList()

        viewModel.createAddressState.observe(this) { state ->
            if (state.address != null) {
                OrderSuccessActivity.startForPayment(
                    context = this,
                    subjectCodes = subjectCodes,
                    addressId = state.address.id
                )
                finish()
            } else if (state.error != null) {
                Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show()
            }
        }



        setContent {
            val pincodeState = viewModel.pincodeState.observeAsState(PincodeValidationState())
            val addressListState = viewModel.addressListState.observeAsState(AddressListState())

            ArivihanComposeApp(
                enabled = true
            ) {
                val userName = MyApplication.getStringData(PreferenceKeys.NAME.key) ?: ""
                val userPhone = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.key) ?: ""

                UserAddressScreen(
                    pincodeState = pincodeState.value,
                    addressListState = addressListState.value,
                    isLoading = viewModel.createAddressState.observeAsState().value?.loading == true,
                    savedName = userName,
                    savedPhone = userPhone,
                    onBack = { finish() },
                    onPincodeValidate = { pincode ->
                        viewModel.validatePincode(pincode)
                    },
                    onAddressCreate = { addressData ->
                        viewModel.createAddress(

                            CreateAddressRequest(
                                fullName = addressData.name,
                                phone = addressData.contactNumber,
                                line1 = addressData.houseNo,
                                line2 = addressData.locality,
                                landmark = null,
                                pincode = addressData.pinCode,
                                city = addressData.city,
                                state = addressData.state,
                                makeDefault = true
                            )
                        )
                    }
                )
            }
        }
    }

}









@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAddressScreen(
    pincodeState: PincodeValidationState,
    addressListState: AddressListState,
    isLoading: Boolean = false,
    savedName: String = "",
    savedPhone: String = "",
    onBack: () -> Unit,
    onPincodeValidate: (String) -> Unit,
    onAddressCreate: (AddressFormData) -> Unit
) {
    val preFilledAddress = addressListState.addresses.firstOrNull()

    var name by remember { mutableStateOf(preFilledAddress?.fullName ?: savedName) }
    var contactNumber by remember { mutableStateOf(preFilledAddress?.phone ?: savedPhone) }
    var pinCode by remember { mutableStateOf(preFilledAddress?.pincode ?: "") }
    var houseNo by remember { mutableStateOf(preFilledAddress?.line1 ?: "") }
    var locality by remember { mutableStateOf(preFilledAddress?.line2 ?: "") }
    var city by remember { mutableStateOf(preFilledAddress?.city ?: "") }
    var state by remember { mutableStateOf(preFilledAddress?.state ?: "") }

    LaunchedEffect(preFilledAddress) {
        if (preFilledAddress != null) {
            name = preFilledAddress.fullName ?: savedName
            contactNumber = preFilledAddress.phone ?: savedPhone
            pinCode = preFilledAddress.pincode ?: ""
            houseNo = preFilledAddress.line1 ?: ""
            locality = preFilledAddress.line2 ?: ""
            city = preFilledAddress.city ?: ""
            state = preFilledAddress.state ?: ""
        }
    }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // Focus requesters for all fields
    val contactNumberFocusRequester = remember { FocusRequester() }
    val pincodeFocusRequester = remember { FocusRequester() }
    val houseNoFocusRequester = remember { FocusRequester() }
    val localityFocusRequester = remember { FocusRequester() }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(ArivihanSnackbarType.Negative) }
    var showValidation by remember { mutableStateOf(false) }

    val invalidPincodeMessage = stringResource(R.string.please_valid_pincode_daalein)
    val fillDetailsMessage = stringResource(R.string.order_dene_ke_liye_details_daalein)


    LaunchedEffect(pincodeState.data) {
        pincodeState.data?.let { data ->
            city = data.city ?: ""
            state = data.state ?: ""
        }
    }

    LaunchedEffect(pincodeState.error) {
        if (pincodeState.error != null) {
            city = ""
            state = ""
        }
    }


    val mandatoryFieldsConfig = MyApplication.getFirebaseRemoteConfig().getString("user_address_mandatory_fields")

    val mandatoryFields = if (mandatoryFieldsConfig.isNotEmpty()) {
        try {
            mandatoryFieldsConfig.split(",").map { it.trim() }.toSet()
        } catch (_: Exception) {
            setOf("name", "contactNumber", "pinCode", "houseNo", "locality", "city", "state")
        }
    } else {
        setOf("name", "contactNumber", "pinCode", "houseNo", "locality", "city", "state")
    }


    val isFormValid = (!mandatoryFields.contains("name") || name.isNotBlank()) &&
            (!mandatoryFields.contains("contactNumber") || (contactNumber.isNotBlank() && contactNumber.length == 10)) &&
            (!mandatoryFields.contains("pinCode") || (pinCode.isNotBlank() && pinCode.length == 6)) &&
            (!mandatoryFields.contains("houseNo") || houseNo.isNotBlank()) &&
            (!mandatoryFields.contains("locality") || locality.isNotBlank()) &&
            (!mandatoryFields.contains("city") || city.isNotBlank()) &&
            (!mandatoryFields.contains("state") || state.isNotBlank())





    Scaffold(
        contentWindowInsets = ArivihanWindowInsets.Default,
        topBar = {
            ArivihanTopAppBar(
                title = stringResource(R.string.apna_address_daalein),
                onBackClick = onBack
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (isFormValid) {
                            onAddressCreate(
                                AddressFormData(
                                    name = name,
                                    contactNumber = contactNumber,
                                    pinCode = pinCode,
                                    houseNo = houseNo,
                                    locality = locality,
                                    city = city,
                                    state = state
                                )
                            )
                        } else if (city.isBlank() || state.isBlank()) {
                            showValidation = true
                            snackbarType = ArivihanSnackbarType.Negative
                            snackbarMessage = invalidPincodeMessage
                        } else {
                            showValidation = true
                            snackbarType = ArivihanSnackbarType.Negative
                            snackbarMessage = fillDetailsMessage
                        }
                        val analyticsData = HashMap<String, String>()
                        analyticsData["user_name"] = name
                        analyticsData["user_phoneNo"] = contactNumber
                        analyticsData["user_pincode"] = pinCode
                        analyticsData["user_city"] = city
                        analyticsData["user_state"] = state
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_user_address_order_btn_click.actionName, analyticsData)
                    },
                    enabled = isFormValid && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E98AC),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledContentColor = Color(0xFF9E9E9E)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.order_karien),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F7F7))
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ContactDetailsSection(
                    name = name,
                    onNameChange = { name = it },
                    contactNumber = contactNumber,
                    onContactNumberChange = {
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            contactNumber = it
                        }
                    },
                    showValidation = showValidation,
                    mandatoryFields = mandatoryFields,
                    contactNumberFocusRequester = contactNumberFocusRequester,
                    onNameNext = { contactNumberFocusRequester.requestFocus() },
                    onContactNumberNext = { pincodeFocusRequester.requestFocus() }
                )

                AddressDetailsSection(
                    pinCode = pinCode,
                    onPinCodeChange = { newPinCode ->
                        if (newPinCode.length <= 6 && newPinCode.all { it.isDigit() }) {
                            pinCode = newPinCode
                            if (newPinCode.length == 6) {
                                onPincodeValidate(newPinCode)
                            }
                        }
                    },
                    houseNo = houseNo,
                    onHouseNoChange = { houseNo = it },
                    locality = locality,
                    onLocalityChange = { locality = it },
                    city = city,
                    onCityChange = { city = it },
                    state = state,
                    onStateChange = { state = it },
                    showValidation = showValidation,
                    mandatoryFields = mandatoryFields,
                    pincodeError = pincodeState.error != null,
                    pincodeFocusRequester = pincodeFocusRequester,
                    houseNoFocusRequester = houseNoFocusRequester,
                    localityFocusRequester = localityFocusRequester,
                    onPincodeNext = { houseNoFocusRequester.requestFocus() },
                    onHouseNoNext = { localityFocusRequester.requestFocus() },
                    onLocalityDone = { focusManager.clearFocus() }
                )
            }

            ArivihanSnackbar(
                message = snackbarMessage,
                type = snackbarType,
                onDismiss = { snackbarMessage = null },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun ContactDetailsSection(
    name: String,
    onNameChange: (String) -> Unit,
    contactNumber: String,
    onContactNumberChange: (String) -> Unit,
    showValidation: Boolean = false,
    mandatoryFields: Set<String> = setOf("name", "contactNumber"),
    contactNumberFocusRequester: FocusRequester,
    onNameNext: () -> Unit,
    onContactNumberNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.contact_details),
                modifier = Modifier
                    .padding(vertical = 4.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),

            )

            FormTextField(
                value = name,
                onValueChange = onNameChange,
                hint = stringResource(R.string.name),
                placeholder = stringResource(R.string.hint_name),
                keyboardType = KeyboardType.Text,
                isError = showValidation && mandatoryFields.contains("name") && name.isBlank(),
                isMandatory = mandatoryFields.contains("name"),
                imeAction = ImeAction.Next,
                onImeAction = onNameNext
            )

            FormTextField(
                value = contactNumber,
                onValueChange = onContactNumberChange,
                hint = stringResource(R.string.contact_number_address),
                placeholder = stringResource(R.string.hint_mobile_number),
                keyboardType = KeyboardType.Number,
                maxLength = 10,
                isError = showValidation && mandatoryFields.contains("contactNumber") && (contactNumber.isBlank() || contactNumber.length != 10),
                isMandatory = mandatoryFields.contains("contactNumber"),
                helperText = if (contactNumber.isNotEmpty() && contactNumber.length != 10) {
                    stringResource(R.string.PleaseEnterValiedNumber)
                } else {
                    null
                },
                prefix = {
                    Text(
                        text = "+91",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    )
                },
                imeAction = ImeAction.Next,
                focusRequester = contactNumberFocusRequester,
                onImeAction = onContactNumberNext
            )
        }
    }
}

@Composable
fun AddressDetailsSection(
    pinCode: String,
    onPinCodeChange: (String) -> Unit,
    houseNo: String,
    onHouseNoChange: (String) -> Unit,
    locality: String,
    onLocalityChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    state: String,
    onStateChange: (String) -> Unit,
    showValidation: Boolean = false,
    mandatoryFields: Set<String> = setOf("pinCode", "houseNo", "locality", "city", "state"),
    pincodeError: Boolean = false,
    pincodeFocusRequester: FocusRequester,
    houseNoFocusRequester: FocusRequester,
    localityFocusRequester: FocusRequester,
    onPincodeNext: () -> Unit,
    onHouseNoNext: () -> Unit,
    onLocalityDone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = stringResource(R.string.address_details),
                modifier = Modifier
                    .padding(vertical = 4.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
            )


            FormTextField(
                value = pinCode,
                onValueChange = onPinCodeChange,
                hint = stringResource(R.string.pin_code),
                placeholder = stringResource(R.string.hint_pincode),
                keyboardType = KeyboardType.Number,
                maxLength = 6,
                isError = pincodeError || (showValidation && mandatoryFields.contains("pinCode") && (pinCode.isBlank() || pinCode.length != 6)),
                isMandatory = mandatoryFields.contains("pinCode"),
                helperText = if (pincodeError) {
                    stringResource(R.string.please_valid_pincode_daalein)
                } else if (pinCode.isNotEmpty() && pinCode.length != 6) {
                    stringResource(R.string.please_valid_pincode_daalein)
                } else {
                    null
                },
                imeAction = ImeAction.Next,
                onImeAction = onPincodeNext,
                focusRequester = pincodeFocusRequester
            )

            FormTextField(
                value = houseNo,
                onValueChange = onHouseNoChange,
                hint = stringResource(R.string.house_no_village_block),
                placeholder = stringResource(R.string.hint_house_no),
                keyboardType = KeyboardType.Text,
                isError = showValidation && mandatoryFields.contains("houseNo") && houseNo.isBlank(),
                isMandatory = mandatoryFields.contains("houseNo"),
                imeAction = ImeAction.Next,
                onImeAction = onHouseNoNext,
                focusRequester = houseNoFocusRequester
            )

            FormTextField(
                value = locality,
                onValueChange = onLocalityChange,
                hint = stringResource(R.string.locality_area),
                placeholder = stringResource(R.string.hint_locality),
                keyboardType = KeyboardType.Text,
                isError = showValidation && mandatoryFields.contains("locality") && locality.isBlank(),
                isMandatory = mandatoryFields.contains("locality"),
                imeAction = ImeAction.Next,
                onImeAction = onLocalityDone,
                focusRequester = localityFocusRequester
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                FormTextField(
                    value = city,
                    onValueChange = onCityChange,
                    hint = stringResource(R.string.city),
                    placeholder = stringResource(R.string.hint_city),
                    isError = showValidation && mandatoryFields.contains("city") && city.isBlank(),
                    isMandatory = mandatoryFields.contains("city"),
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )

                FormTextField(
                    value = state,
                    onValueChange = onStateChange,
                    hint = stringResource(R.string.state),
                    placeholder = stringResource(R.string.hint_state),
                    isError = showValidation && mandatoryFields.contains("state") && state.isBlank(),
                    isMandatory = mandatoryFields.contains("state"),
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Next
                )
            }

        }
    }
}








@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    helperText: String? = null,
    maxLength: Int? = null,
    isMandatory: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    prefix: (@Composable () -> Unit)? = null,
) {
    val placeholderText = placeholder.ifEmpty { hint }

    val borderColor = if (isError) Color(0xFFE53935) else Color(0xFFD3D3D4)
    val textColor = if (enabled) Color.Black else Color.Gray
    val backgroundColor =
        if (enabled) Color.Transparent else Color(0xFFF7F7F7)


    Column(modifier) {

        Text(
            text = buildAnnotatedString {
                append(hint)
                if (isMandatory) {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(" *")
                    }
                }
            },
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF949494),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            ),
            modifier = Modifier.padding(bottom = 7.dp)
        )


        BasicTextField(
            value = value,
            onValueChange = {
                if (maxLength == null || it.length <= maxLength) {
                    onValueChange(it)
                }
            },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction?.invoke() },
                onDone = { onImeAction?.invoke() },
                onGo = { onImeAction?.invoke() },
                onSearch = { onImeAction?.invoke() },
                onSend = { onImeAction?.invoke() }
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = textColor,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
            decorationBox = { innerTextField ->

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = borderColor,
                            shape = shape
                        )
                        .background(
                            color = backgroundColor,
                            shape = shape
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 14.dp
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        prefix?.invoke()

                        if (prefix != null) {
                            Spacer(Modifier.width(8.dp))
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholderText,
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            }
        )

        helperText?.let {
            Spacer(Modifier.height(4.dp))

            Text(
                text = it,
                color = Color.Red,
                fontSize = 12.sp,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
