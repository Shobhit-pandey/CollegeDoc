package collegedoc.com.collegedoc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class StartActivity extends AppCompatActivity {

    ImageView imageview;
    TextView textview;
    String TAG = "";
    static boolean calledAlready = false;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        imageview = (ImageView) findViewById(R.id.imageview);
        textview = (TextView) findViewById(R.id.textview);
        if (!calledAlready)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            calledAlready = true;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //user=firebaseAuth.getCurrentUser();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAuthListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // User is signed in
                            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                            int sub=1;
                            Intent i = new Intent(StartActivity.this, Subjects.class);
                            i.putExtra ("SU" ,sub);
                            startActivity(i);
                            //startActivity(new Intent(StartActivity.this, Subjects.class));
                            finish();

                        } else {
                            // User is signed out
                            //Log.d(TAG, "onAuthStateChanged:signed_out");
                            Log.d(TAG, "onAuthStateChanged:signed_out");
                        }
                    }
                };
                // do something
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }
        }, 2000);

    }
}
