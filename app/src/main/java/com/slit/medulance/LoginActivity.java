package com.slit.medulance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private Button btnReg;
    private EditText email,password;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Boolean emailchecker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        firebaseAuth=FirebaseAuth.getInstance();
        btnLogin = findViewById(R.id.btnLogin);
        email= findViewById(R.id.editText3);
        password= findViewById(R.id.editText4);
        progressDialog=new ProgressDialog(this);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        btnReg = findViewById(R.id.btnReg);
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currunt_user=firebaseAuth.getCurrentUser();
        if(currunt_user!=null)
        {
            verifyEmail();
        }


    }

    private void userLogin()
    {
        String Email=email.getText().toString();
        String Password= password.getText().toString();

        if(TextUtils.isEmpty(Email))
        {
            Toast.makeText(this,"Please Enter You Email", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(Password))
        {
            Toast.makeText(this,"Please Enter Your Password", Toast.LENGTH_SHORT).show();
        }

        else
        {
            progressDialog.setMessage("Logging To Account...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            firebaseAuth.signInWithEmailAndPassword(Email,Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                verifyEmail();

                            }
                            else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this,"Login Error!! Please Check Your Email And Password ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void verifyEmail()
    {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        emailchecker=user.isEmailVerified();

        if(emailchecker)
        {
            Toast.makeText(LoginActivity.this,"You Are Logged In!!", Toast.LENGTH_SHORT).show();
            sendUserToMain();
        }

        else
        {
            Toast.makeText(this,"Please Verify Your Account...",Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }

    }
    private void sendUserToMain()
    {
        Intent mainIntent=new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
