package collegedoc.com.collegedoc;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetEmailPassword extends AppCompatActivity {
    private String email;
    private Button reset, back;
    private FirebaseAuth auth;
    private TextView confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_email_password);
        Intent j = getIntent();
        email = j.getStringExtra("EMAIL");
        back = (Button) findViewById(R.id.back);
        auth = FirebaseAuth.getInstance();
        reset = (Button) findViewById(R.id.reset);
        confirm = (TextView) findViewById(R.id.confirm);
        confirm.setText("Confirm to Reset Password of \n" + email);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.setText("Sending email to " + email);
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    reset.setBackgroundColor(Color.argb(255,72,179,10));
                                    confirm.setText("Check your inbox of " + email);
                                    Toast.makeText(ResetEmailPassword.this, "Successful send", Toast.LENGTH_SHORT).show();
                                } else {
                                    reset.setBackgroundColor(Color.RED);
                                    confirm.setText("Invalid or unregistered email " + email);
                                    Toast.makeText(ResetEmailPassword.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
