package com.trial.safenote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

public class SignupActivity extends AppCompatActivity {

    private EditText emailtext;
    private EditText passwordtext;
    private Button signupbutton;
    private TextView backtologin;

    private FirebaseAuth firebaseAuth;

    private String hashPassword(String password) {
        String hashedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            hashedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedPassword;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailtext = findViewById(R.id.inputEmail);
        passwordtext = findViewById(R.id.inputPassword);
        signupbutton = findViewById(R.id.buttonSignup);
        backtologin = findViewById(R.id.backtologin);

        firebaseAuth = FirebaseAuth.getInstance();

        backtologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailtext.getText().toString().trim();
                String password = passwordtext.getText().toString().trim();
                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                }
                else if(!isValidPassword(password)){
                    Toast.makeText(getApplicationContext(), "Password must be 8-25 characters long and must contain at least one digit, special character, lower and upper case alphabet", Toast.LENGTH_SHORT).show();
                }
                else{
//                    firebaseAuth.createUserWithEmailAndPassword(email, hashPassword(password)).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                DocumentReference documentReference = firebaseFirestore
                                        .collection("details")
                                        .document(firebaseUser.getUid());
                                Map<String, Object> item = new HashMap<>();
                                String alias = randomString();
                                item.put("alias", alias);
                                documentReference.set(item);
                                sendEmailVerification();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Registration Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private String randomString() {
        String regex = "[A-Za-z0-9!@#$%^&*()_+-=[]{}|;:<>,.?]";
        int length = 10;
        StringBuilder randomString = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(regex.length());
            randomString.append(regex.charAt(randomIndex));
        }
        return randomString.toString();
    }

    private boolean isValidPassword(String password){
        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[!?@#$%^&*()+_=])"
                + "(?=\\S+$).{8,25}$";
        Pattern p = Pattern.compile(regex);
        if (password == null) {
            return false;
        }
        Matcher m = p.matcher(password);
        return m.matches();
    }

    private void sendEmailVerification(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getApplicationContext(), "Verification email sent. Login after completing email verification.", Toast.LENGTH_LONG).show();
                    firebaseAuth.signOut();
                    finish();
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), "Failed to Send Verification Email.", Toast.LENGTH_SHORT).show();
        }
    }

}