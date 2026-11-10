// MESIN KOPI QRIS Rp 5000 - WEMOS D1 MINI
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <EEPROM.h>
#define RELAY_HEATER D1
#define RELAY_POMPA D2
#define HARGA 5000
String ssid = "";
String pass = "";
String server = "https://server-lu.com/cek-bayar?harga=5000";

void setup() {
  Serial.begin(115200);
  EEPROM.begin(512);
  pinMode(RELAY_HEATER, OUTPUT);
  pinMode(RELAY_POMPA, OUTPUT);
  digitalWrite(RELAY_HEATER, HIGH);
  digitalWrite(RELAY_POMPA, HIGH);
  if (Serial.available()) {
    String data = Serial.readStringUntil('#');
    if (data.startsWith("WIFI:")) {
      int p = data.indexOf('|');
      ssid = data.substring(5, p);
      pass = data.substring(p + 1);
      for (int i = 0; i < ssid.length(); i++) EEPROM.write(i, ssid[i]);
      EEPROM.write(ssid.length(), 0);
      for (int i = 0; i < pass.length(); i++) EEPROM.write(100 + i, pass[i]);
      EEPROM.write(100 + pass.length(), 0);
      EEPROM.commit();
    }
  }
  if (ssid == "") {
    ssid = baca(0);
    pass = baca(100);
  }
  WiFi.begin(ssid.c_str(), pass.c_str());
  while (WiFi.status() != WL_CONNECTED) delay(500);
}

String baca(int a) {
  String h = "";
  for (int i = 0; i < 50; i++) {
    char c = EEPROM.read(a + i);
    if (c == 0) break;
    h += c;
  }
  return h;
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(server);
    if (http.GET() == 200) {
      if (http.getString() == "LUNAS") bikinKopi();
    }
    http.end();
  }
  delay(3000);
}

void bikinKopi() {
  digitalWrite(RELAY_HEATER, LOW);
  delay(15000);
  digitalWrite(RELAY_POMPA, LOW);
  delay(25000);
  digitalWrite(RELAY_POMPA, HIGH);
  digitalWrite(RELAY_HEATER, HIGH);
}
