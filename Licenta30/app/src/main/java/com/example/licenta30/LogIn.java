package com.example.licenta30;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogIn extends AppCompatActivity {
    private EditText username, password;
    private Button logIn;
    private FirebaseAuth auth;
    private ProgressBar logInProgress;
    private TextView registerPane;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        auth = FirebaseAuth.getInstance();
        logIn = (Button) findViewById(R.id.loginButton);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        logInProgress = (ProgressBar) findViewById(R.id.progressBar);
        registerPane = (TextView) findViewById(R.id.registerPane);
        mAuth = FirebaseAuth.getInstance();

        setRegisterAction();
        logInMethod();
    }
    private void setRegisterAction(){
        registerPane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegisterAvtivity();
            }
        });
    }
    private void startRegisterAvtivity(){
        Intent intent = new Intent(getApplicationContext(), Register.class);
        startActivity(intent);
        finish();
    }
    private void startMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void logInMethod(){
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInProgress.setVisibility(View.VISIBLE);
                String email, passcode;
                email = String.valueOf(username.getText());
                passcode = String.valueOf(password.getText());

                if(fieldsValidation(email, passcode) == false){
                    logInProgress.setVisibility(View.GONE);
                    return;
                }
                verifyAccount(email, passcode);

            }
        });
    }

    private void verifyAccount(String email, String passcode) {
        mAuth.signInWithEmailAndPassword(email, passcode)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        logInProgress.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(LogIn.this, "Authentication succesfuly.",Toast.LENGTH_SHORT).show();
                            UserInfo userInfo =UserInfo.getInstance(email);
                            startMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogIn.this, prepareFailMessage(task),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private Boolean fieldsValidation(String email, String password){
        if(email.contains(" ") || password.contains(" ")){
            Toast.makeText(LogIn.this, "Email or password contains spaces!",Toast.LENGTH_SHORT).show();
            return true;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LogIn.this, "Username is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(LogIn.this, "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private String prepareFailMessage(Task<AuthResult> task){
        String result = "Authentication failed: ";
        String input = task.getException().toString();
        int colonIndex = input.indexOf(':');
        // Check if colon is found and if there is text after the colon
        if (colonIndex != -1 && colonIndex + 1 < input.length()) {
            // Extract and return the substring after the colon
            return input.substring(colonIndex + 1).trim();
        }
        return  result;
    }

}