package com.example.chatapp.view.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val userPhone = findViewById<EditText>(R.id.edtUserPhNumber)
        val buttonOTP = findViewById<Button>(R.id.btnOTP)
        val countryCodeSpinner = findViewById<Spinner>(R.id.countryCode)
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        var countryCode = ""
//        val countryNames = arrayOf(
//            "Afghanistan","+Albania",
//            "Algeria","+Andorra","+Angola","+Antarctica","+Argentina",
//            "Armenia","+Aruba","+Australia","+Austria","+Azerbaijan",
//            "Bahrain","+Bangladesh","+Belarus","+Belgium","+Belize","+Benin",
//            "Bhutan","+Bolivia","+Bosnia And Herzegovina","+Botswana",
//            "Brazil","+Brunei Darussalam","+Bulgaria","+Burkina Faso",
//            "Myanmar","+Burundi","+Cambodia","+Cameroon","+Canada",
//            "Cape Verde","+Central African Republic","+Chad","+Chile","+China",
//            "Christmas Island","+Cocos (keeling) Islands","+Colombia",
//            "Comoros","+Congo","+Cook Islands","+Costa Rica","+Croatia",
//            "Cuba","+Cyprus","+Czech Republic","+Denmark","+Djibouti",
//            "Timor-leste","+Ecuador","+Egypt","+El Salvador",
//            "Equatorial Guinea","+Eritrea","+Estonia","+Ethiopia",
//            "Falkland Islands (malvinas)","+Faroe Islands","+Fiji","+Finland",
//            "France","+French Polynesia","+Gabon","+Gambia","+Georgia",
//            "Germany","+Ghana","+Gibraltar","+Greece","+Greenland",
//            "Guatemala","+Guinea","+Guinea-bissau","+Guyana","+Haiti",
//            "Honduras","+Hong Kong","+Hungary","+India","+Indonesia","+Iran",
//            "Iraq","+Ireland","+Isle Of Man","+Israel","+Italy","+Ivory Coast",
//            "Jamaica","+Japan","+Jordan","+Kazakhstan","+Kenya","+Kiribati",
//            "Kuwait","+Kyrgyzstan","+Laos","+Latvia","+Lebanon","+Lesotho",
//            "Liberia","+Libya","+Liechtenstein","+Lithuania","+Luxembourg",
//            "Macao","+Macedonia","+Madagascar","+Malawi","+Malaysia",
//            "Maldives","+Mali","+Malta","+Marshall Islands","+Mauritania",
//            "Mauritius","+Mayotte","+Mexico","+Micronesia","+Moldova",
//            "Monaco","+Mongolia","+Montenegro","+Morocco","+Mozambique",
//            "Namibia","+Nauru","+Nepal","+Netherlands","+New Caledonia",
//            "New Zealand","+Nicaragua","+Niger","+Nigeria","+Niue","+Korea",
//            "Norway","+Oman","+Pakistan","+Palau","+Panama",
//            "Papua New Guinea","+Paraguay","+Peru","+Philippines","+Pitcairn",
//            "Poland","+Portugal","+Puerto Rico","+Qatar","+Romania",
//            "Russian Federation","+Rwanda","+Saint Barthélemy","+Samoa",
//            "San Marino","+Sao Tome And Principe","+Saudi Arabia","+Senegal",
//            "Serbia","+Seychelles","+Sierra Leone","+Singapore","+Slovakia",
//            "Slovenia","+Solomon Islands","+Somalia","+South Africa",
//            "Korea, Republic Of","+Spain","+Sri Lanka","+Saint Helena",
//            "Saint Pierre And Miquelon","+Sudan","+Suriname","+Swaziland",
//            "Sweden","+Switzerland","+Syrian Arab Republic","+Taiwan",
//            "Tajikistan","+Tanzania","+Thailand","+Togo","+Tokelau","+Tonga",
//            "Tunisia","+Turkey","+Turkmenistan","+Tuvalu",
//            "United Arab Emirates","+Uganda","+United Kingdom","+Ukraine",
//            "Uruguay","+United States","+Uzbekistan","+Vanuatu",
//            "Holy See (vatican City State)","+Venezuela","+Viet Nam",
//            "Wallis And Futuna","+Yemen","+Zambia","+Zimbabwe"
//        )

        val countryAreaCodes = arrayOf(
            "+61", "+91", "+93", "++355", "+213",
            "376", "+244", "+672", "+54", "+374", "+297", "+61", "+43", "+994", "+973",
            "880", "+375", "+32", "+501", "+229", "+975", "+591", "+387", "+267", "+55",
            "673", "+359", "+226", "+95", "+257", "+855", "+237", "+1", "+238", "+236",
            "235", "+56", "+86", "+61", "+61", "+57", "+269", "+242", "+682", "+506",
            "385", "+53", "+357", "+420", "+45", "+253", "+670", "+593", "+20", "+503",
            "240", "+291", "+372", "+251", "+500", "+298", "+679", "+358", "+33",
            "689", "+241", "+220", "+995", "+49", "+233", "+350", "+30", "+299", "+502",
            "224", "+245", "+592", "+509", "+504", "+852", "+36", "+91", "+62", "+98",
            "964", "+353", "+44", "+972", "+39", "+225", "+1876", "+81", "+962", "+7",
            "254", "+686", "+965", "+996", "+856", "+371", "+961", "+266", "+231",
            "218", "+423", "+370", "+352", "+853", "+389", "+261", "+265", "+60",
            "960", "+223", "+356", "+692", "+222", "+230", "+262", "+52", "+691",
            "373", "+377", "+976", "+382", "+212", "+258", "+264", "+674", "+977",
            "31", "+687", "+64", "+505", "+227", "+234", "+683", "+850", "+47", "+968",
            "92", "+680", "+507", "+675", "+595", "+51", "+63", "+870", "+48", "+351",
            "1", "+974", "+40", "+7", "+250", "+590", "+685", "+378", "+239", "+966",
            "221", "+381", "+248", "+232", "+65", "+421", "+386", "+677", "+252", "+27",
            "82", "+34", "+94", "+290", "+508", "+249", "+597", "+268", "+46", "+41",
            "963", "+886", "+992", "+255", "+66", "+228", "+690", "+676", "+216", "+90",
            "993", "+688", "+971", "+256", "+44", "+380", "+598", "+1", "+998", "+678",
            "39", "+58", "+84", "+681", "+967", "+260", "+263"
        )
        val adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countryAreaCodes)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        countryCodeSpinner.adapter = adapter
//        if(NetworkConnected(application).isInternetConnected()) {
        countryCodeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                countryCode = countryCodeSpinner.selectedItem.toString()
            }

        }
        buttonOTP.setOnClickListener {
            if (userPhone.text.toString().isNotEmpty()) {
                val intent = Intent(this, OTPActivity::class.java)
                intent.putExtra("phone", countryCode + userPhone.text.toString())
                startActivity(intent)
//                finish()
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content), "+Please Enter Mobile Number",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
//        }else{
//            Snackbar.make(
//                findViewById(android.R.id.content),"+Internet Not Connected",
//                Snackbar.LENGTH_SHORT
//            ).show()
//        }
    }
}