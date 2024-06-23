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

public class Register extends AppCompatActivity {

    EditText username, password;
    Button register;
    FirebaseAuth mAuth;
    ProgressBar registePprogress;
    TextView logInPane;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        register = (Button) findViewById(R.id.registerButton);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        registePprogress = (ProgressBar) findViewById(R.id.progressBar);
        logInPane = (TextView) findViewById(R.id.logInPane);
        registerAction();
        setLogInButton();

    }
    private void setLogInButton(){
        logInPane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogInActivity();
            }
        });
    }
    private void startLogInActivity(){
        Intent intent = new Intent(getApplicationContext(), LogIn.class);
        startActivity(intent);
        finish();
    }
    private void registerAction() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registePprogress.setVisibility(View.VISIBLE);
                String email, passcode;
                email = String.valueOf(username.getText());
                passcode = String.valueOf(password.getText());
                if (setNotificationMessage(email, passcode)) return;
                createAccount(email, passcode);
            }
        });
    }
    private void createAccount(String email, String passcode) {
        mAuth.createUserWithEmailAndPassword(email, passcode)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            registePprogress.setVisibility(View.GONE);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(Register.this, "Account created succesfuly.", Toast.LENGTH_SHORT).show();
                            startLogInActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Register.this, prepareFailMessage(task), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private boolean setNotificationMessage(String email, String passcode) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(Register.this, "Username is required", Toast.LENGTH_SHORT).show();
            registePprogress.setVisibility(View.GONE);
            return true;
        }
        if (TextUtils.isEmpty(passcode)) {
            Toast.makeText(Register.this, "Password is required", Toast.LENGTH_SHORT).show();
            registePprogress.setVisibility(View.GONE);
            return true;
        }
        if(email.contains(" ") || passcode.contains(" ")){
            Toast.makeText(Register.this, "Email or password contains spaces!",Toast.LENGTH_SHORT).show();
            registePprogress.setVisibility(View.GONE);
            return true;
        }
        return false;
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