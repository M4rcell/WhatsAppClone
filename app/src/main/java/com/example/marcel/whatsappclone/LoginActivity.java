package com.example.marcel.whatsappclone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText mphoneNumber,mcode;
    private Button msend;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;

    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        userIsLoggedIn();

        mphoneNumber=findViewById(R.id.phoneNumber);
        mcode = findViewById(R.id.code);

        msend= findViewById(R.id.send);



        msend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mVerificationId != null)
                {
                    verifyPhoneNumberWithCode();
                }
                else {
                    startPhoneNumberVerification();
                }
            }
        });

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {}

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);

                mVerificationId = verificationId;

                msend.setText("Verificar Codigo");

            }
        };

    }

    private void verifyPhoneNumberWithCode()
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,mcode.getText().toString());
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {

        Task<AuthResult> authResultTask = FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null) {
                        final DatabaseReference  mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());

                        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (!dataSnapshot.exists())
                                {
                                    Map<String,Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userMap.put("name", user.getPhoneNumber());

                                    mUserDB.updateChildren(userMap);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }
                }

                //userIsLoggedIn();
            }
        });
    }

    private void userIsLoggedIn() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            startActivity(new Intent(getApplicationContext(),MainPageActivity.class));
            finish();
            return;
        }
    }

    private void startPhoneNumberVerification() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mphoneNumber.getText().toString(),
                60,
                TimeUnit.SECONDS,
                this,
                mCallBacks
        );
    }
}
