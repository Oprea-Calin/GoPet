package com.example.gopet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity {

    private FirebaseAuth auth;
    FirebaseFirestore db;
    String name, str;
    EditText editUsernameLog , editPasswordLog ,editPasswordConfirmLog, editEmailLog;
    TextView txtInfoRegister ;
    Button btnLogin, btnRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        auth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editUsernameLog = findViewById(R.id.inUsername);
        editPasswordLog = findViewById(R.id.inPassword);
        editEmailLog = findViewById(R.id.inEmail);
        editPasswordConfirmLog = findViewById(R.id.inConfirmPassword);

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
                Intent i = new Intent(Register.this, Login.class);
                startActivity(i);

            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                String username =  editUsernameLog.getText().toString();
                String email = editEmailLog.getText().toString().trim();
                String password =  editPasswordLog.getText().toString().trim();
                String confirmPassword = editPasswordConfirmLog.getText().toString();

                if(email.isEmpty()){
                    editEmailLog.setError("Please fill in email address!");
                }
                if(password.isEmpty())
                {
                    editPasswordLog.setError("Please fill in a password!");
                }
                else{
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                String userId = auth.getCurrentUser().getUid();

                                saveUserToFirestore(userId, username);
                                Toast.makeText(Register.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Register.this, Login.class));
                            }
                            else{
                                Toast.makeText(Register.this,"Signup failed!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });





    }
    private void saveUserToFirestore(String userId, String username) {
        User user = new User(username);

        db.collection("users")
                .document(userId)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "User saved to Firestore", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Register.this, "Failed to save user to Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static class User {
        private String username;

        public User(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }


}
