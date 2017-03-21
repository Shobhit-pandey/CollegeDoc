package collegedoc.com.collegedoc;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SigninActivity extends AppCompatActivity {

    private EditText email;
    private Button signin;
    String email_s;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        email = (EditText) findViewById(R.id.email);
        signin = (Button) findViewById(R.id.signin);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_s = email.getText().toString();
                if (TextUtils.isEmpty(email_s)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    signin.setBackgroundColor(Color.RED);
                    return;
                } else {
                    signin.setBackgroundColor(Color.argb(255,72,179,10));
                    Intent i = new Intent(SigninActivity.this, LoginActivity.class);
                    i.putExtra("EMAIL", email_s);
                    startActivity(i);
                    finish();
                }
            }
        });
    }


}
