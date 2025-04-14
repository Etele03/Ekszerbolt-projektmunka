package com.example.ekszerboltprojekt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();

    EditText userNameEditText;
    EditText userEmailEditText;
    EditText passwordEditText;
    EditText passwordAgainEditText;
    EditText phoneNumberEditText;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        int secret_key = getIntent().getIntExtra("SECRET_KEY",0);

        if(secret_key != 99){
            finish();
        }

        userNameEditText = findViewById(R.id.userNameEditText);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        passwordEditText = findViewById(R.id.userPasswordEditText);
        passwordAgainEditText = findViewById(R.id.userPasswordAgainEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");

        userNameEditText.setText(username);
        passwordEditText.setText(password);

        Log.i(LOG_TAG, "onCreate");

    }

    public void register(View view) {

        String username = userNameEditText.getText().toString();
        String email = userEmailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordAgain = passwordAgainEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();


        boolean hasError = false;
        boolean hasPasswordError = false;


        if (username.isBlank()) {
            userNameEditText.setError("A felhasználónév kötelező!");
            hasError = true;
        }

        if (email.isEmpty()) {
            userEmailEditText.setError("Az email kötelező!");
            hasError = true;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("A jelszó kötelező!");
            hasError = true;
        }

        if (passwordAgain.isEmpty()) {
            passwordAgainEditText.setError("Ismételd meg a jelszót!");
            hasError = true;
        }

        if (phoneNumber.isEmpty()) {
            phoneNumberEditText.setError("Telefonszám kötelező!");
            hasError = true;
        }

        if (hasError) {
            Toast.makeText(this, "Kérlek, tölts ki minden mezőt helyesen!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordAgain)) {
            passwordAgainEditText.setError("A jelszavak nem egyeznek!");
            Toast.makeText(this, "A jelszavak nem egyeznek!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sikeres regisztráció
        Log.i(LOG_TAG, "Regisztrált: " + username + ", email: " + email);

        Intent intent = new Intent(this, IndexActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);


    }

    public void cancel(View view) {
        finish();
    }



    @Override
    public void onStart(){
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

}