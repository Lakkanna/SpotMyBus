package info.androidhive.firebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.internal.zzbjp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class StudentLogin extends AppCompatActivity {




    private EditText inputName,inputEmail, inputPassword,inputUSN,inputAddress,inputPhone;
    private Spinner inputGender;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    DatabaseReference databaseStudents;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        databaseStudents= FirebaseDatabase.getInstance().getReference("studentsUsers");
        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputName=(EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputUSN = (EditText) findViewById(R.id.usn);
        inputAddress = (EditText) findViewById(R.id.address);
        inputPhone = (EditText) findViewById(R.id.phno);
        inputGender = (Spinner) findViewById(R.id.gender);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StudentLogin.this, ResetPasswordActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String mobile=inputPhone.getText().toString().trim();
                String name = inputName.getText().toString();
                String usn = inputUSN.getText().toString();
                String gender = inputGender.getSelectedItem().toString();
                String area = inputAddress.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter valid Name", Toast.LENGTH_SHORT).show();
                    return;

                }

                if (mobile.isEmpty() || mobile.length() < 10 || mobile.length() > 10) {
                    Toast.makeText(getApplicationContext(), "Enter valid Phone Number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (usn.isEmpty()||usn.length()<12) {
                    Toast.makeText(getApplicationContext(), "Enter valid USN", Toast.LENGTH_SHORT).show();
                    return;

                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(area)){
                    Toast.makeText(getApplicationContext(), "Enter your Address", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(gender)){
                    Toast.makeText(getApplicationContext(), "Enter your Gender", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                //auth.signInWithCustomToken("hello");
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(StudentLogin.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(StudentLogin.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(StudentLogin.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    Intent studentIntent=new Intent(getApplicationContext(), MainActivity.class);
                                    studentIntent.putExtra("caller","StudentLogin");
                                    studentIntent.putExtra("loggedIn","true");
                                    startActivity(studentIntent);
                                    finish();
                                }
                            }
                        });
                //Write to DB
                String studentUserId=databaseStudents.push().getKey();
                StudentUser studentUser=new StudentUser(studentUserId,name,email,password,usn,area,mobile,gender,false);
                databaseStudents.child(usn).setValue(studentUser);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }


}
