package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CheckBox;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {

    private static final String TAG = "SignIn";
    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String STAY_SIGNED_IN = "StaySignedIn";
    private CheckBox staySignedInCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        mAuth = FirebaseAuth.getInstance();
        mEmailField = findViewById(R.id.editTextUsername);
        mPasswordField = findViewById(R.id.editTextPassword);
        staySignedInCheckBox = findViewById(R.id.staySignedIn);

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignIn.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                signIn(email, password);
            }
        });

        TextView loginLink = findViewById(R.id.signUp);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            }
        });

        //stay signed in
        initSharedPreferences();
        boolean isLogout = getIntent().getBooleanExtra("logout", false);
        if (!isLogout && shouldStaySignedIn()) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                // The user is signed in, navigate to the main activity
                navigateToHomeActivity();
            }
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveStaySignedIn(staySignedInCheckBox.isChecked());
                            Log.d(TAG, "signInWithEmail:success");
                            Intent intent = new Intent(SignIn.this, Home.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Stay Signed In
    private void initSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void saveStaySignedIn(boolean staySignedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(STAY_SIGNED_IN, staySignedIn);
        editor.apply();
    }

    private boolean shouldStaySignedIn() {
        return sharedPreferences.getBoolean(STAY_SIGNED_IN, false);
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(SignIn.this, Home.class);
        startActivity(intent);
        finish();
    }
}
