package com.example.chatapp.view.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.example.chatapp.service.helper.NetworkConnected
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit


class OTPActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var phone: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var verificationId: String
    private lateinit var timer: TextView
    private lateinit var reSendCode: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        val otp = findViewById<EditText>(R.id.edtOtp)
        val tvOtpNum = findViewById<TextView>(R.id.tvOtpNumber)
        timer = findViewById(R.id.txtTimer)
        reSendCode = findViewById(R.id.txtResendCode)
        phone = intent.getStringExtra("phone")!!
        tvOtpNum.text = tvOtpNum.text.toString() + " " + phone
        val btnSignIn = findViewById<Button>(R.id.btnNext)
        mAuth = FirebaseAuth.getInstance()
        firebaseUser = mAuth!!.currentUser
        callBack()
        startPhoneNumberVerification(phone)
        startCountdown()
        reSendCode.setOnClickListener {
            resendVerificationCode(phone, resendToken)
            startCountdown()
        }
        btnSignIn.setOnClickListener {

            if (NetworkConnected(application).isInternetConnected()) {
                callBack()
                if (otp.text.toString().isNotBlank() && storedVerificationId!= null) {
                    verifyPhoneNumberWithCode(storedVerificationId, otp.text.toString())
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content), "OTP is empty",
                        Snackbar.LENGTH_SHORT
                    ).show()

                }
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content), "Internet Not Connected",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        // [END initialize_auth]


    }

    private fun callBack() {
        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.e(TAG, "onVerificationCompleted:$credential")
                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
//                    updateUI(STATE_VERIFY_SUCCESS, credential)
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e(TAG, "onVerificationFailed", e)


                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
//                        fieldPhoneNumber.error = "Invalid phone number."
                    // [END_EXCLUDE]
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(
                        findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED)
                // [END_EXCLUDE]
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.e(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT)
                // [END_EXCLUDE]
            }
        }
        // [END phone_auth_callbacks]

    }

    private fun updateUI(state: Int) {
        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleSmall)
        progressBar.visibility = View.VISIBLE
    }

    private fun startCountdown() {
        object : CountDownTimer(60000, 1000) {
            // adjust the milli seconds here
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                timer.text = "" + String.format(
                    "%d sec",
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    millisUntilFinished
                                )
                            )
                )
            }

            override fun onFinish() {
                timer.text = ""
                reSendCode.isEnabled = true
            }
        }.start()
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        verificationInProgress = true
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.e(TAG, "signInWithCredential:success")
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.e(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Invalid Code. Please enter the correct code you received in SMS.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks, // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks
    }

    // [END resend_verification]
    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
        // [END verify_with_code]
            signInWithPhoneAuthCredential(credential)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    companion object {
        private val TAG = "PhoneAuthActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6

    }
    // [END phone_auth_callbacks]
}

