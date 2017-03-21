package collegedoc.com.collegedoc;

import android.Manifest;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class Subjects extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener mAuthStateListner;
    private String new_subject = "";
    private int SELECT_PDF = 20;
    private String SelectedPDF;
    private String new_password = "";
    private String confirm_password = "";
    //private EditText search;
    //private SearchView mSearchView;
    final Context context = this;
    private FirebaseAuth mAuth;
    private String TAG = "";
    private ListView rv;
    private int REQUEST_INVITE=20;
    private  int su=0;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setTitle("College Doc");
        Intent j = getIntent();
        su = j.getIntExtra("SU",0);
        //if(su==1)
           // overridePendingTransition( R.anim.fadeout,R.anim.fadein);
        setContentView(R.layout.activity_subjects);
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(Subjects.this, MainActivity.class));
                    finish();
                }
            }
        };
        checkconnection();
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }
        rv = (ListView) findViewById(R.id.rvStudents);
        rv.setTextFilterEnabled(true);
        File Directory = new File("/sdcard/College Doc/");
        // have the object build the directory structure, if needed.
        Directory.mkdirs();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl
                ("https://college-doc.firebaseio.com//2nd year");
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        //DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("scores");
        // scoresRef.keepSynced(true);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        databaseReference.addValueEventListener(listener);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>(this,
                String.class,
                android.R.layout.simple_list_item_2,
                databaseReference) {
            @Override
            protected void populateView(View v, String model, int position) {
                TextView textView = (TextView) v.findViewById(android.R.id.text1);
                textView.setText(model);
            }
        };
        rv.setAdapter(firebaseListAdapter);
        rv.setTextFilterEnabled(true);
        rv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sub = String.valueOf(parent.getItemAtPosition(position));
                Toast.makeText(Subjects.this, sub, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Subjects.this, Topics.class);
                i.putExtra("SUBJECT", sub);
                startActivity(i);
            }

        });
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.ACCESS_NETWORK_STATE
                }, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                } else {
                    //not granted
                    Toast.makeText(Subjects.this,
                            "You cant download or view files without this permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(R.drawable.college_doc).setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                }).setNegativeButton("No", null).show();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_indiviual_person, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.signout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Subjects.this, MainActivity.class));
            finish();
            return true;
        }
        if (id == R.id.invite) {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.lenovo.anyshare.gps");
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }
            else
            {
                Intent launchIn = getPackageManager().getLaunchIntentForPackage("cn.xender");
                if (launchIn != null) {
                    startActivity(launchIn);//null pointer check in case package name was not found
                }
                else
                {
                    Toast.makeText(Subjects.this, "Niether Shareit nor Xender installed to share", Toast.LENGTH_SHORT).show();
                }
            }

            return true;
        }
        if(id==R.id.upload)
        {
            final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReferenceFromUrl
                    ("https://college-doc.firebaseio.com//2nd year");
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.prompts_subject, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);
            // set prompts_subject_subject.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);
            final EditText userInput = (EditText) promptsView
                    .findViewById(R.id.editTextDialogUserInput);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    // get user input and set it to result
                                    // edit text
                                    new_subject=userInput.getText().toString();
                                    confirmation_dialogbox(databaseReference1,new_subject);
                                    //result.setText(userInput.getText());
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            return true;
        }
        if(id==R.id.changePassword)
        {

             updatePassword();

        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListner);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().start(getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction());
        if (mAuthStateListner != null) {
            mAuth.removeAuthStateListener(mAuthStateListner);
        }
    }
    public void checkconnection()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo datac = cm
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ((wifi != null & datac != null)
                && (wifi.isConnected() | datac.isConnected())) {
            //connection is avlilable
            //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }else{
            //no connection
            Toast toast = Toast.makeText(Subjects.this, "You are Offline",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("Subjects", "http://[ENTER-YOUR-URL-HERE]");
    }
    public void confirmation_dialogbox(final DatabaseReference databaseReference1, final String new_subject)
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        databaseReference1.push().setValue(new_subject);
                        //Yes button clicked
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure want to create new subject with name "+new_subject+" ?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
    public void updatePassword()
    {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompt_newpassword, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        // set prompts_subject_subject.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText pass1 = (EditText) promptsView
                .findViewById(R.id.password1);
        final EditText pass2 = (EditText) promptsView
                .findViewById(R.id.password2);
        CheckBox checkBox=(CheckBox) promptsView.findViewById(R.id.checktext);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!isChecked) {
                    /*pass1.setTransformationMethod(PasswordTransformationMethod
                                    .getInstance());
                    pass2.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());*/
                    pass1.setTransformationMethod(new PasswordTransformationMethod());
                    pass2.setTransformationMethod(new PasswordTransformationMethod());
                } else
                    /*pass1.setTransformationMethod(HideReturnsTransformationMethod
                                    .getInstance());
                    pass2.setTransformationMethod(HideReturnsTransformationMethod
                        .getInstance());*/ {
                    pass1.setTransformationMethod(null);
                    pass2.setTransformationMethod(null);
                }

            }
        });
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                new_password=pass1.getText().toString();
                                confirm_password=pass2.getText().toString();
                                if(new_password.length()<6 )
                                {
                                    AlertDialog alertDialog = new AlertDialog.Builder(Subjects.this).create();
                                    alertDialog.setTitle("Alert");
                                    alertDialog.setMessage("Password Should be atleast 6 digit");
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    updatePassword();
                                                }
                                            });
                                    alertDialog.show();
                                }
                                else if(!(new_password.equals(confirm_password)))
                                {
                                    AlertDialog alertDialog = new AlertDialog.Builder(Subjects.this).create();
                                    alertDialog.setTitle("Alert");
                                    alertDialog.setMessage("Password did not match !!! ");
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    updatePassword();
                                                }
                                            });
                                    alertDialog.show();
                                }
                                else
                                {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    String newPassword = new_password;

                                    user.updatePassword(newPassword)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "User password updated.");
                                                        Toast.makeText(Subjects.this,
                                                                "Password Updated",Toast.LENGTH_SHORT).show();
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(Subjects.this,
                                                                "Password NOT Updated",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                                //result.setText(userInput.getText());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
