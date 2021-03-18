package com.example.barcodemodule;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView messageText, messageFormat, nameText, expiryText, quantityText;
    String barcode, name, user;
    int expiry, currentQuantity;
    boolean entryExists;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // referencing and initializing
        // the button and textviews
        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Button logout = findViewById(R.id.logoutButton);
        messageText = findViewById(R.id.barcodeTextView);
        messageFormat = findViewById(R.id.barcodeFormatTextView);
        nameText = findViewById(R.id.nameTextView);
        expiryText = findViewById(R.id.expiryTextView);
        quantityText = findViewById(R.id.quantityTextView);
        db = FirebaseFirestore.getInstance();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    public void onClick(View view) {
        // we need to create the object
        // of IntentIntegrator class
        // which is the class of QR library
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode");
        intentIntegrator.setOrientationLocked(false);
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
                Toast.makeText(getBaseContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
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
                                if (task.isSuccessful()) {
                                    DocumentSnapshot snapshot = task.getResult();
                                    if (snapshot.exists()) {
                                        name = snapshot.getData().get("name").toString();
                                        expiry = Integer.parseInt(snapshot.getData().get("expiry").toString());
                                        nameText.setText(name);
                                        expiryText.setText("Expires in: " + expiry + " months");

                                        entryStatus();

                                        Log.i("xoxo", Boolean.toString(entryExists));

                                        if (!entryExists) {
                                            Map<String, Object> entry = new HashMap<>();
                                            entry.put("name", name);
                                            entry.put("expiry", expiry);
                                            entry.put("quantity", 1);

                                            db.collection(user).document(barcode).set(entry).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(MainActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                                                        quantityText.setText("Quantity: " + 1);
                                                    }
                                                }
                                            });

                                        }else{
                                            db.collection(user).document(barcode).get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            DocumentSnapshot snapshot = task.getResult();
                                                            if (snapshot.exists()) {
                                                                currentQuantity = Integer.parseInt(snapshot.getData().get("quantity").toString());
                                                                currentQuantity++;
                                                                db.collection("UserEntries").document(barcode).update("quantity", currentQuantity)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Toast.makeText(MainActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                                                                                quantityText.setText("Quantity: " + currentQuantity);
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }
                        });
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void entryStatus(){
        db.collection(user).document(barcode).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        entryExists = true;
                    } else {
                        entryExists = false;
                    }
                }
            }
        });
    }
}
