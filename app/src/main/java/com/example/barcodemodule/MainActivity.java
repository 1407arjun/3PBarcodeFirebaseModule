package com.example.barcodemodule;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

// implements onClickListener for the onclick behaviour of button
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button scanBtn;
    TextView messageText, messageFormat, nameText, expiryText;
    String barcode, name;
    int expiry;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // referencing and initializing
        // the button and textviews
        scanBtn = findViewById(R.id.scanButton);
        messageText = findViewById(R.id.barcodeTextView);
        messageFormat = findViewById(R.id.barcodeFormatTextView);
        nameText = findViewById(R.id.nameTextView);
        expiryText = findViewById(R.id.expiryTextView);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View view) {
        // we need to create the object
        // of IntentIntegrator class
        // which is the class of QR library
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                messageText.setText(intentResult.getContents());
                messageFormat.setText(intentResult.getFormatName());
                barcode = intentResult.getContents();

                db.collection("Barcode Data").document(barcode).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot snapshot = task.getResult();
                                    if (snapshot.exists()) {
                                        name = snapshot.getData().get("name").toString();
                                        expiry = Integer.parseInt(snapshot.getData().get("expiry").toString());
                                        nameText.setText(name);
                                        expiryText.setText("Expires in: " + expiry + " months");
                                    }
                                }
                            }
                        });

                HashMap<String, Object> entry = new HashMap<>();
                entry.put("name", name);
                entry.put("expiry", expiry);
                entry.put("quantity", 1);

                db.collection("UserEntries").document(barcode).set(entry).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
