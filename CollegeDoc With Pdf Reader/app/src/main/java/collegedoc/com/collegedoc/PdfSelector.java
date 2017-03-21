package collegedoc.com.collegedoc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class PdfSelector extends AppCompatActivity {
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    private static String file_url;
    //public TextView temp;
    private PDFView pdfview;
    public String u;
    private String t;
    public String uri;
    public boolean check=true;
    public boolean running=true;
    private PowerManager.WakeLock mWakeLock;

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                File Directory = new File("/sdcard/College Doc/"+t+"/");
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
                OutputStream output = new FileOutputStream("/sdcard/College Doc/"+t+"/" + u + ".pdf");

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
                Toast.makeText(PdfSelector.this, "downloading failed", Toast.LENGTH_SHORT).show();
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
            dismissDialog(progress_bar_type);
            check=true;
            pdfview = (PDFView) findViewById(R.id.pdfview);
            // Displaying downloaded image into image view
            // Reading image path from sdcard
            String imagePath = Environment.getExternalStorageDirectory().toString() + "/College Doc/"+t+"/" + u + ".pdf";
            // setting downloaded into image view
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/College Doc/"+t+"/" + u + ".pdf");
            if (file.exists()) {

                pdfview.fromFile(file).load();
               /* Uri path = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(PdfSelector.this,
                            "No Application Available to View PDF",
                            Toast.LENGTH_SHORT).show();
                    Log.w("", "failed", e);
                    //finish();
                }*/
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_selector);
        checkconnection();
        Intent j = getIntent();
        u = j.getStringExtra("URI");
        t = j.getStringExtra("CAT");
        uri = t + "/" + u + ".pdf";
        pdfview = (PDFView) findViewById(R.id.pdfview);
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/College Doc/"+t+"/" + u + ".pdf");
        if (file.exists()) {

            pdfview.fromFile(file).load();
        }
        else if(!running)
        {
            int su=0;
            Toast.makeText(PdfSelector.this, "No Internet... Downloading not possible", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(PdfSelector.this, Subjects.class);
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
                        Toast.makeText(PdfSelector.this, "File Not Exist", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(PdfSelector.this, "downloading failed", Toast.LENGTH_SHORT).show();
                    checkconnection();
                    finish();
                }
            });
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        //pDialog.show();
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading " + u + ".pdf");
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
                        Toast.makeText(PdfSelector.this,
                                "Downloading in background", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PdfSelector.this, Subjects.class));
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
    @Override
    public void onBackPressed() {
        if(check)
        {
            finish();
        }
        else
        {
            startActivity(new Intent(PdfSelector.this,Subjects.class));
            finish();
        }

    }
}
