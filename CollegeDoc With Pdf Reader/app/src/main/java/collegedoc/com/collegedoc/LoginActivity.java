package collegedoc.com.collegedoc;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    String email;
    //EditText password;
    TextInputEditText password;
    Button login, reset;
    //ProgressBar progress;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent j = getIntent();
        email = j.getStringExtra("EMAIL");
        password = (TextInputEditText) findViewById(R.id.password);
        reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent j = new Intent(LoginActivity.this, ResetEmailPassword.class);
                j.putExtra("EMAIL", email);
                startActivity(j);
                finish();
            }
        });
        login = (Button) findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String password_s = password.getText().toString();
                if (TextUtils.isEmpty(password_s)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    login.setBackgroundColor(Color.RED);
                    return;
                }

                if (password.length() < 6) {
                    login.setBackgroundColor(Color.RED);
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                login.setBackgroundColor(Color.argb(255,72,179,10));
                mAuth.signInWithEmailAndPassword(email, password_s)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    // Toast.makeText(LoginActivity.this, "Invalid Password or No such Account exist", Toast.LENGTH_SHORT).show();
                                    mAuth.createUserWithEmailAndPassword(email, password_s)
                                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    // progress.setVisibility(View.GONE);
                                                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                                    // If sign in fails, display a message to the user. If sign in succeeds
                                                    // the auth state listener will be notified and logic to handle the
                                                    // signed in user can be handled in the listener.
                                                    if (!task.isSuccessful()) {
                                                        login.setBackgroundColor(Color.RED);
                                                        Toast.makeText(LoginActivity.this, "Invalid Password of \n" + email, Toast.LENGTH_SHORT).show();

                                                    } else {
                                                        //Toast.makeText(LoginActivity.this, "Succesfully created your account" + email, Toast.LENGTH_SHORT).show();
                                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                                        user.sendEmailVerification()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Log.d(TAG, "Email sent.");
                                                                            Toast.makeText(LoginActivity.this, "Email verification send .Check your email" + email, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                    }

                                                    // ...
                                                }
                                            });
                                } else {
                                    Toast.makeText(LoginActivity.this, "Welcome back... Nice to see you again \n" + email, Toast.LENGTH_SHORT).show();
                                }

                                // ...
                            }
                        });

            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                 if (user != null && user.isEmailVerified()) {
                    // User is signed in
                    startActivity(new Intent(LoginActivity.this, Subjects.class));
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    finish();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                     if(password.length()>=6 )
                    Toast.makeText(LoginActivity.this,
                            "Please verify your email to get in" + email, Toast.LENGTH_SHORT).show();
                }
                // ...
            }
        };


    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
