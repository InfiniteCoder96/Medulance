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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private Button btnReg;
    private EditText email,password,name,address,mobile,guardianName, gurdianEmail;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;
    String UId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        firebaseAuth=FirebaseAuth.getInstance();

        btnReg = findViewById(R.id.btnReg);
        email= findViewById(R.id.editText7);
        password=findViewById(R.id.editText13);
        name=findViewById(R.id.editText8);
        address=findViewById(R.id.editText9);
        mobile=findViewById(R.id.editText10);
        guardianName=findViewById(R.id.editText11);
        gurdianEmail=findViewById(R.id.editText12);

        progressDialog=new ProgressDialog(this);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerUser();



            }
        });

    }

    private void sendUserToMain()
    {
        Intent homeIntent=new Intent(RegisterActivity.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }

    private void registerUser()
    {

        String Email=email.getText().toString();
        String Password=password.getText().toString();

        if(TextUtils.isEmpty(Email))
        {
            Toast.makeText(this,"Please Enter Your Email", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(Password))
        {
            Toast.makeText(this,"Please Enter Your Password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressDialog.setMessage("Registering User...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            firebaseAuth.createUserWithEmailAndPassword(Email,Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this,"Registered Succusfully!!!",Toast.LENGTH_SHORT).show();
                                sendEmailVerificationMessage();
                                saveToDatabase();


                            }
                            else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this,"Oops Try Again!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

    private void senduserToLogin()
    {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        RegisterActivity.this.finish();
    }

    private void sendEmailVerificationMessage()
    {
        FirebaseUser user=firebaseAuth.getCurrentUser();

        if(user!=null)
        {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(RegisterActivity.this,"Registration Succusfull!! Please Check your inbox to verify your account.....",Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                    }



                    else
                    {
                        Toast.makeText(RegisterActivity.this,"Error Occured!!",Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                    }

                }
            });
        }
    }
    private void saveToDatabase()
    {
        UId=firebaseAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(UId);
        String Name=name.getText().toString();
        String phone=mobile.getText().toString();
        String Email=email.getText().toString();
        String Address=address.getText().toString();
        String GuardianName=guardianName.getText().toString();
        String GuardianEmail=gurdianEmail.getText().toString();

        if(TextUtils.isEmpty(Name))
        {
            Toast.makeText(this,"Please Enter Your Full Name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phone))
        {
            Toast.makeText(this,"Please Enter Your Phone Number", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Email))
        {
            Toast.makeText(this,"Please Enter Your Email", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Address))
        {
            Toast.makeText(this,"Please Enter Your Address", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(GuardianEmail))
        {
            Toast.makeText(this,"Please Enter Guardian Email", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(GuardianName))
        {
            Toast.makeText(this,"Please Enter Guardian Name", Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressDialog.setMessage("Updating Profile...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            HashMap usermap=new HashMap();
            usermap.put("Email", Email);
            usermap.put("Phone Number", phone);
            usermap.put("Fullname", Name);
            usermap.put("Address", Address);
            usermap.put("Guardian Name", GuardianName);
            usermap.put("Guardian Email", GuardianEmail);

            userRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,"Profile Updated!!", Toast.LENGTH_SHORT).show();
                        senduserToLogin();

                    }
                    else
                    {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,"Error!! Please Try Again!!", Toast.LENGTH_SHORT).show();
                    }


                }

            });
        }
    }

}
