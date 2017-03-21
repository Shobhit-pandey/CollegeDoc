package collegedoc.com.collegedoc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class Topics extends AppCompatActivity {
    private String u;
    private ListView topicrv;
    public static final int progress_bar_type = 0;
    private int SELECT_PDF = 20;
    final Context context = this;
    private String new_topic="";
    private ProgressDialog progressDialog;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);
        Intent j = getIntent();
        u = j.getStringExtra("SUBJECT");
        setTitle(u);
        Toolbar topictoolbar = (Toolbar) findViewById(R.id.topictoolbar);
        topicrv = (ListView) findViewById(R.id.topicrv);
        setSupportActionBar(topictoolbar);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl
                ("https://college-doc.firebaseio.com/" + u);
        FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>(this,
                String.class,
                android.R.layout.simple_list_item_2,
                databaseReference) {
            @Override
            protected void populateView(View v, String model, int position) {
                TextView textView = (TextView) v.findViewById(android.R.id.text1);
                textView.setText(model);
            }
        };
        topicrv.setAdapter(firebaseListAdapter);
        topicrv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sub = String.valueOf(parent.getItemAtPosition(position));
                Toast.makeText(Topics.this, sub, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Topics.this, PdfSelector.class);
                i.putExtra("URI", sub);
                i.putExtra("CAT", u);
                startActivity(i);
            }

        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_topics, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if(id==R.id.upload)
        {
            final DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReferenceFromUrl
                    ("https://college-doc.firebaseio.com//"+u.toString());
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.prompts_topics, null);
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
                                    new_topic=userInput.getText().toString();
                                    confirmation_dialogbox(databaseReference1,new_topic);
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
        return super.onOptionsItemSelected(item);
    }
    public void confirmation_dialogbox(final DatabaseReference databaseReference1, final String new_topic)
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        databaseReference1.push().setValue(new_topic);
                        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                                ("gs://college-doc.appspot.com//"+u);
                        Intent intent = new Intent();
                        intent.setType("application/pdf");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        try {
                            startActivityForResult(Intent.createChooser(intent, "Select PDF of name "+new_topic+".pdf"), SELECT_PDF);
                        } catch (ActivityNotFoundException f) {
                            Toast.makeText(Topics.this, "Activity Not Found", Toast.LENGTH_SHORT).show();
                        }
                        //Yes button clicked
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure to add "+new_topic+".pdf ?" +
                " in subject "+ u.toString()).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //PDF
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PDF) {

                Uri selectedUri_PDF = data.getData();
                //InputStream inputstream;
                showDialog(progress_bar_type);
                //inputstream = getContentResolver().openInputStream(selectedUri_PDF);
                //UploadTask uploadTask = mStorageRef.child(new_topic+".pdf").putStream(inputstream);
                UploadTask uploadTask = mStorageRef.child(new_topic+".pdf").putFile(selectedUri_PDF);
                // Listen for state changes, errors, and completion of the upload.
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //float progress =(float) (taskSnapshot.getBytesTransferred()) /(float) taskSnapshot.getTotalByteCount();
                       // progress*=100.0;
                        double progress = (double)((float)taskSnapshot.getBytesTransferred() / (float)taskSnapshot.getTotalByteCount());
                        progress*=100.0;
                        Log.d("Progress", "onProgress: The value of the max is: " + taskSnapshot.getTotalByteCount());
                        Log.d("Progress", "onProgress: The progress is: " + progress);
                        int currentprogress = (int) progress;
                        //progressBar.setProgress(currentprogress);
                        progressDialog.setProgress(currentprogress);
                    }
                }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                        System.out.println("Upload of "+new_topic+".pdf is paused");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        dismissDialog(progress_bar_type);
                        Toast.makeText(Topics.this, "Upload of "+new_topic+".pdf is failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Handle successful uploads on complete
                        //Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        dismissDialog(progress_bar_type);
                        Toast.makeText(Topics.this, new_topic+".pdf is uploaded", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        //pDialog.show();
        switch (id) {
            case progress_bar_type:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Uploading " + u + ".pdf");
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Background", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Cancel download task
                        progressDialog.cancel();
                        Toast.makeText(Topics.this,
                                "Uploading in background", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Topics.this, Subjects.class));
                        //finish();
                    }
                });
                progressDialog.show();
                return progressDialog;
            default:
                return null;
        }
    }
}
