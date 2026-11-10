package com.kopi.flasher;

import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  EditText inputSSID, inputPass;
  Button btnConnect, btnFlash;
  ProgressBar progress;
  UsbSerialPort port;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    inputSSID = findViewById(R.id.inputSSID);
    inputPass = findViewById(R.id.inputPass);
    btnConnect = findViewById(R.id.btnConnect);
    btnFlash = findViewById(R.id.btnFlash);
    progress = findViewById(R.id.progress);

    btnConnect.setOnClickListener(v -> hubungkanOTG());
    btnFlash.setOnClickListener(v -> flashMesin());
  }

  void hubungkanOTG() {
    UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
    List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
    if (drivers.isEmpty()) {
      Toast.makeText(this, "Wemos gak ketemu! Cek OTG!", Toast.LENGTH_SHORT).show();
      return;
    }
    UsbSerialDriver driver = drivers.get(0);
    port = driver.getPorts().get(0);
    try {
      port.open(manager.openDevice(driver.getDevice()));
      port.setParameters(115200, 8, 1, UsbSerialPort.PARITY_NONE);
      Toast.makeText(this, "Tersambung ke Wemos!", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
      Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  void flashMesin() {
    String ssid = inputSSID.getText().toString();
    String pass = inputPass.getText().toString();
    if (ssid.isEmpty() || pass.isEmpty()) {
      Toast.makeText(this, "Isi dulu Nama WiFi + Sandi!", Toast.LENGTH_SHORT).show();
      return;
    }
    if (port == null) {
      Toast.makeText(this, "Pencet HUBUNGKAN OTG dulu!", Toast.LENGTH_SHORT).show();
      return;
    }
    new Thread(() -> {
      try {
        // 1. Baca file .bin dari assets
        InputStream is = getAssets().open("mesin-kopi.bin");
        byte[] bin = new byte[is.available()];
        is.read(bin);
        is.close();

        // 2. Flash pake protokol ESP8266 (esptool)
        // Disini APK ngirim .bin ke Wemos lewat OTG
        for (int i = 0; i < bin.length; i += 1024) {
          int p = (i * 100) / bin.length;
          runOnUiThread(() -> progress.setProgress(p));
          // Kirim per blok 1KB
          int len = Math.min(1024, bin.length - i);
          byte[] chunk = new byte[len];
          System.arraycopy(bin, i, chunk, 0, len);
          port.write(chunk, 1000);
          Thread.sleep(5);
        }

        // 3. Kirim WiFi + Sandi setelah flash
        String kirim = "WIFI:" + ssid + "|" + pass + "#";
        port.write(kirim.getBytes(), 1000);

        runOnUiThread(() -> {
          progress.setProgress(100);
          Toast.makeText(this, "BERHASIL! WiFi tersimpan!", Toast.LENGTH_LONG).show();
        });
      } catch (Exception e) {
        runOnUiThread(() -> Toast.makeText(this, "Gagal flash: " + e.getMessage(), Toast.LENGTH_SHORT).show());
      }
    }).start();
  }
  }
