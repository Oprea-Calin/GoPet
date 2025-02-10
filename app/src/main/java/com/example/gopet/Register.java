package com.example.gopet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Register extends AppCompatActivity {

    EditText editUsernameLog , editPasswordLog ,editPasswordConfirmLog ;
    TextView txtInfoRegister ;
    Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        editUsernameLog = findViewById(R.id.inUsername);
        editPasswordLog = findViewById(R.id.inPassword);
        editPasswordConfirmLog = findViewById(R.id.inConfirmPassword);

        txtInfoRegister = findViewById(R.id.txtLogReg);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view)
            {
                Intent i = new Intent(Register.this, MainActivity.class);
                startActivity(i);

            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                String username =  editUsernameLog.getText().toString();
                String password =  editPasswordLog.getText().toString();
                String confirmPassword = editPasswordConfirmLog.getText().toString();

                dbConnect db = new dbConnect(Register.this);
                if(username.isEmpty() || password.isEmpty()) txtInfoRegister.setText("Field empty!");
                else if (password.equals(confirmPassword)) {
                    if(db.checkIfUserExists(username))
                    {
                        txtInfoRegister.setText("Username already exists!");

                    }
                    else
                    {
                        Users newUser = new Users(0, username, password);
                        db.addUser(newUser);
                        txtInfoRegister.setText("Registration successful!");

                        Intent i = new Intent(Register.this, MainActivity.class);
                        startActivity(i);
                    }

                }


            }
        });

    }
}