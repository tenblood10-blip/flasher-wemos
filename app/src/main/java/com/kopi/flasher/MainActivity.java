package com.kopi.flasher;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    EditText etIp, etMessage;
    Button btnFlash, btnTest;
    TextView tvStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIp = findViewById(R.id.etIp);
        etMessage = findViewById(R.id.etMessage);
        btnFlash = findViewById(R.id.btnFlash);
        btnTest = findViewById(R.id.btnTest);
        tvStatus = findViewById(R.id.tvStatus);
        btnFlash.setOnClickListener(v -> {
            tvStatus.setText("Mengirim ke " + etIp.getText().toString());
        });
        btnTest.setOnClickListener(v -> {
            tvStatus.setText("Test: " + etMessage.getText().toString());
        });
    }
  }
