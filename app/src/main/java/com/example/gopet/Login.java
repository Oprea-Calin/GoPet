package com.example.gopet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    FirebaseAuth auth;
    EditText editEmailLog, editPasswordLog;
    Button btnLogin, btnRegister;
    CheckBox checkRememberMe;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        auth = FirebaseAuth.getInstance();
        editEmailLog = findViewById(R.id.inEmail);
        editPasswordLog = findViewById(R.id.inPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegisterGo);
        checkRememberMe = findViewById(R.id.checkBox);

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if(sharedPreferences.getBoolean("remember", false)){
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editEmailLog.getText().toString();
                String pass = editPasswordLog.getText().toString();
                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!pass.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                        if(checkRememberMe.isChecked())
                                        {
                                            editor.putString("email", email);
                                            editor.putString("password", pass);
                                            editor.putBoolean("remember", true);
                                            editor.apply();
                                        }else
                                        {
                                            editor.clear();
                                            editor.apply();
                                        }

                                        startActivity(new Intent(Login.this, MainActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Login.this, "Login failed!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        editPasswordLog.setError("Please fill in password!");
                    }
                } else if (email.isEmpty()) {
                    editEmailLog.setError("Please fill in email!");
                } else {
                    editEmailLog.setError("Please fill in a valid email!");
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view)
            {
                Intent i = new Intent(Login.this, Register.class);
                startActivity(i);

            }
        });
    }

    private void saveCredentials(String email, String password)
    {
        editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password",password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void loadSavedCredentials(){
        boolean isRemembered = sharedPreferences.getBoolean("remember", false);
        if(isRemembered)
        {
            editEmailLog.setText(sharedPreferences.getString("email",""));
            editPasswordLog.setText(sharedPreferences.getString("password",""));
            checkRememberMe.setChecked(true);
            btnLogin.performClick();

        }
    }
    private void clearCredentials(){
        editor.clear();
        editor.apply();
    }


}