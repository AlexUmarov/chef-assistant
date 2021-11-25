package ru.uao.chef.assistant.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import ru.uao.chef.assistant.MainActivity
import ru.uao.chef.assistant.R
import ru.uao.chef.assistant.ui.login.PreferenceHelper.clearValues
import ru.uao.chef.assistant.ui.login.PreferenceHelper.customPreference
import ru.uao.chef.assistant.ui.login.PreferenceHelper.password
import ru.uao.chef.assistant.ui.login.PreferenceHelper.userEmail


class LoginActivity: AppCompatActivity() {

    //private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    val CUSTOM_PREF_NAME = "User_data"

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        prefs = customPreference(this, CUSTOM_PREF_NAME)
        var etMail = findViewById<EditText>(R.id.etMail)
        var etPassword = findViewById<EditText>(R.id.etPassword)
        var btnLogin = findViewById<Button>(R.id.btLogin)
        var btnRegist = findViewById<Button>(R.id.btRegist)
        var tvAppVersion = findViewById<TextView>(R.id.tvAppVersion)

        tvAppVersion.setText("version: ${GetAppVersion(this)}")

        etMail.setText(prefs.userEmail)
        etPassword.setText(prefs.password)

        btnLogin.setOnClickListener{
            var email = etMail.text.toString()
            var password = etPassword.text.toString()
            if (validateCred(email,password)){
                signIn(email,password)
            }else{
                hideKeyboard()
                Toast.makeText(
                    baseContext, "Укажите логин и пароль для входа",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        btnRegist.setOnClickListener {
            var email = etMail.text.toString()
            var password = etPassword.text.toString()
            if (validateCred(email,password)){
                createAccount(email,password)
            }else{
                hideKeyboard()
                Toast.makeText(
                    baseContext, "Укажите логин и пароль для регистрации",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // your code to validate the user_name and password combination
            // and verify the same

        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        //val currentUser = auth.currentUser
        //updateUI(currentUser)
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    prefs.userEmail = email
                    prefs.password = password
                    Toast.makeText(this@LoginActivity, "Aвторизация успешна", Toast.LENGTH_SHORT).show()
                    this@LoginActivity.startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else
                    Toast.makeText(this@LoginActivity, "Aвторизация провалена", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createAccount(email:String, password:String) {
        prefs.clearValues
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    prefs.userEmail = email
                    prefs.password = password
                    findViewById<EditText>(R.id.etMail).setText(prefs.userEmail)
                    findViewById<EditText>(R.id.etPassword).setText(prefs.password)
                    // Sign in success, update UI with the signed-in user's information

                    //writeNewUser(auth.uid, auth.currentUser!!.displayName, auth.currentUser!!.email)
                    Toast.makeText(
                        baseContext, "Регистрация успешна!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivityForResult(intent, 1)
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Регистрация провалена. ${task.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                    //updateUI(null)
                }

                // ...
            }
    }

    private fun validateCred(email:String, password:String): Boolean{
        if(email != "" && password != ""){
            if(password.length>=6){
                return true
            }else{
                Toast.makeText(
                    baseContext, "Пароль должен содержать не менее 6 символов.",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        return false
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun GetAppVersion(context: Context): String {
        var version = ""
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return version
    }
}

object PreferenceHelper {

    val USER_EMAIL = "USER_EMAIL"
    val USER_PASSWORD = "PASSWORD"

    fun defaultPreference(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun customPreference(context: Context, name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        editMe.apply()
    }

    var SharedPreferences.userEmail
        get() = getString(USER_EMAIL, "")
        set(value) {
            editMe {
                it.putString(USER_EMAIL, value)
            }
        }

    var SharedPreferences.password
        get() = getString(USER_PASSWORD, "")
        set(value) {
            editMe {
                it.putString(USER_PASSWORD, value)
            }
        }

    var SharedPreferences.clearValues
        get() = { }
        set(value) {
            editMe {
                it.clear()
            }
        }
}