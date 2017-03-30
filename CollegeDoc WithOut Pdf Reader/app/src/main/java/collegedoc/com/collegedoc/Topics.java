package collegedoc.com.collegedoc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class Topics extends AppCompatActivity {
    private String u;
    private String sub;
    private ProgressDialog pDialog;
    private static String file_url;
    public boolean check=true;
    public boolean running=true;
    public String uri;
    private ListView topicrv;
    public static final int progress_bar_type = 0;
    public static final int progress_bar_type1 = 1;
    private int SELECT_PDF = 20;
    final Context context = this;
    private String new_topic="";
    private ProgressDialog progressDialog;
    private StorageReference mStorageRef;
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type1);
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                File Directory = new File("/sdcard/College Doc/"+u+"/");
                // have the object build the directory structure, if needed.
                Directory.mkdirs();
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream("/sdcard/College Doc/"+uri);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                checkconnection();
                Toast.makeText(Topics.this, "downloading failed", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type1);
            check=true;
            //pdfview = (PDFView) findViewById(R.id.pdfview);
            // Displaying downloaded image into image view
            // Reading image path from sdcard
            String imagePath = Environment.getExternalStorageDirectory().toString() + "/College Doc/"+uri;
            // setting downloaded into image view
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/College Doc/"+uri);
            if (file.exists()) {

               // pdfview.fromFile(file).load();
                Uri path = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(Topics.this,
                            "No Application Available to View PDF",
                            Toast.LENGTH_SHORT).show();
                    Log.w("", "failed", e);
                    //finish();
                }
            }

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);
        Intent j = getIntent();
        u = j.getStringExtra("SUBJECT");
        setTitle(u);
        Toolbar topictoolbar = (Toolbar) findViewById(R.id.topictoolbar);
        topicrv = (ListView) findViewById(R.id.topicrv);
        progressDialog = new ProgressDialog(this);
        pDialog = new ProgressDialog(this);
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
                sub = String.valueOf(parent.getItemAtPosition(position));
                Toast.makeText(Topics.this, sub, Toast.LENGTH_SHORT).show();
                uri = u + "/" + sub + ".pdf";
               /* Intent i = new Intent(Topics.this, PdfSelector.class);
                i.putExtra("URI", sub);
                i.putExtra("CAT", u);
                startActivity(i);*/
                File file = new File(Environment.getExternalStorageDirectory().toString() + "/College Doc/"+uri);
                if (file.exists()) {

                    //pdfview.fromFile(file).load();
                    Uri path = Uri.fromFile(file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(path, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(Topics.this,
                                "No Application Available to View PDF",
                                Toast.LENGTH_SHORT).show();
                        Log.w("", "failed", e);
                        //finish();
                    }
                }
                else if(!running)
                {
                    int su=0;
                    Toast.makeText(Topics.this, "No Internet... Downloading not possible", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(Topics.this, Subjects.class);
                    i.putExtra ("SU" ,su);
                    startActivity(i);
                    finish();
                }
                else {
                    check=false;
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    final StorageReference pdfdownload = storageRef.child(uri);
                    pdfdownload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uric) {
                            // Got the download URL for 'users/me/profile.png'
                            pdfdownload.getFile(uric);
                            //checkconnection();
                            //pdfview.loadPages();
                            //temp = (TextView) findViewById(R.id.temp);
                            //temp.setText(uric.toString());
                            Uri ur = Uri.parse(uric.toString());
                            file_url = uric.toString();
                            pDialog.setMessage("Downloading " + sub + ".pdf");
                            new DownloadFileFromURL().execute(file_url);
                            // startActivity(new Intent(PdfSelector.this,Topics.class));
            /*Intent webIntent = new Intent(Intent.ACTION_VIEW, ur);
            if (webIntent.resolveActivity(getPackageManager()) != null){
                startActivity(webIntent);
            }*/

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            //startActivity(new Intent(PdfSelector.this,Subjects.class));
                            check=true;
                            if(running)
                                Toast.makeText(Topics.this, "File Not Exist", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(Topics.this, "downloading failed", Toast.LENGTH_SHORT).show();
                            checkconnection();
                            finish();
                        }
                    });
                }
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

                progressDialog.setMessage("Uploading " + new_topic + ".pdf");
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
                        progressDialog.setProgress(0);
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
                progressDialog.setMessage("Uploading " + new_topic + ".pdf");
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

            case progress_bar_type1:
                pDialog.setMessage("Downloading " + sub + ".pdf");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.setCanceledOnTouchOutside(false);
                pDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Background", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Cancel download task
                        pDialog.cancel();
                        Toast.makeText(Topics.this,
                                "Downloading in background", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Topics.this, Subjects.class));
                        //finish();
                    }
                });
                pDialog.show();
                return pDialog;

            default:
                return null;

        }
    }
    public void checkconnection()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo datac = cm
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        //connection is avlilable
//no connection
        running = (wifi != null & datac != null)
                && (wifi.isConnected() | datac.isConnected());
    }
}
