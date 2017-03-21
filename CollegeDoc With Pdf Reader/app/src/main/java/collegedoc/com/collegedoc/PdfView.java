package collegedoc.com.collegedoc;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class PdfView extends AppCompatActivity {

    private PDFView pdfview;
    private int SELECT_PDF = 20;
    private String SelectedPDF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);
        pdfview = (PDFView) findViewById(R.id.pdfview);
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a PDF "), SELECT_PDF);
        } catch (ActivityNotFoundException f) {
            Toast.makeText(PdfView.this, "Activity Not Found", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //PDF
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PDF) {

                Uri selectedUri_PDF = data.getData();
                InputStream inputstream;
                try {
                    inputstream = getContentResolver().openInputStream(selectedUri_PDF);
                    pdfview.fromStream(inputstream).load();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(PdfView.this, "unable to open", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            startActivity(new Intent(PdfView.this,Subjects.class));
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(PdfView.this, Subjects.class));
        finish();
    }
}
