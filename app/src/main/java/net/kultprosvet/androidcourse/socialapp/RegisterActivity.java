package net.kultprosvet.androidcourse.socialapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.kultprosvet.androidcourse.socialapp.models.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText mInputEmail, mInputPassword;
    private Button mBtnSignIn, mBtnRegister;
    private ProgressBar mProgressBar;
    private FirebaseAuth mAuth;
    public static final int MIN_PASSWRD_LENGTH = 6;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        findViews();

        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mInputEmail.getText().toString().trim();
                String password = mInputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mInputEmail.setError(getString(R.string.error_toast_enter_email));
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_toast_enter_email),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mInputEmail.getText().toString()).matches()){
                    mInputEmail.setError(getString(R.string.wrong_email_format));
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_toast_enter_password),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < MIN_PASSWRD_LENGTH) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_toast_short_password),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                mBtnSignIn.setEnabled(false);
                mBtnRegister.setEnabled(false);
                //create user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                String result = getString(R.string.toast_auth_completed) + task.isSuccessful();
                                Toast.makeText(RegisterActivity.this,
                                        result,
                                        Toast.LENGTH_SHORT).show();

                                mProgressBar.setVisibility(View.GONE);
                                mBtnSignIn.setClickable(true);
                                mBtnRegister.setClickable(true);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    result = getString(R.string.error_toast_auth_failed) + task.getException();
                                    mInputEmail.setError(getString(R.string.error_toast_auth_failed));
                                    Toast.makeText(RegisterActivity.this,
                                            result,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    onAuthSuccess(task.getResult().getUser());
                                }
                            }
                        });

            }
        });
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());
        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());
        // Go to MainActivity
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);
        mDatabase.child("users").child(userId).setValue(user);
    }

    private void findViews() {
        mBtnSignIn = (Button) findViewById(R.id.btn_sign_in);
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mInputEmail = (EditText) findViewById(R.id.email);
        mInputPassword = (EditText) findViewById(R.id.password);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
        mBtnSignIn.setClickable(true);
        mBtnRegister.setClickable(true);
    }
}