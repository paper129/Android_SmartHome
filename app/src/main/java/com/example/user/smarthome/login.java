package com.example.user.smarthome;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login extends AppCompatActivity {
    private Button btn_register, btn_Login;
    private EditText name,password;
    private  FirebaseAuth firebaseAuth;
    private TextView tx1;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn_register = (Button) findViewById(R.id.btn_register);
        btn_Login = (Button) findViewById(R.id.btnLogin);
        tx1 = (TextView) findViewById(R.id.tx1);
        name = (EditText) findViewById(R.id.name);
        password = (EditText) findViewById(R.id.password);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null)
        {
            finish();
        }
        else {
            progressDialog.setMessage("Loading");
            progressDialog.show();
            progressDialog.dismiss();

        }

        btn_register.setOnClickListener(register_Lis);
        btn_Login.setOnClickListener(login_Lis);
    }

    private View.OnClickListener login_Lis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            name.onEditorAction(EditorInfo.IME_ACTION_DONE);
            password.onEditorAction(EditorInfo.IME_ACTION_DONE);
            validate(name.getText().toString(),password.getText().toString());
        }
    };
    private View.OnClickListener register_Lis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //startActivity(new Intent(this,RegistrationActivity.class));
        }
    };
    private void validate(String userName,String userPassword){
        progressDialog.setMessage("Loading");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(userName,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    checkEmailVerifcation();
                }
                else
                {
                    progressDialog.dismiss();
                    //Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void checkEmailVerifcation() {
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        boolean emailflag = firebaseUser.isEmailVerified();
        if(emailflag){
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            finish();
        }else{
            progressDialog.dismiss();
            Toast.makeText(this, "Verify your email", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }

}
