#include <WiFi.h>
#include <WebServer.h>
#include <DHT.h>
#include <HTTPClient.h>

// ----------------------------- PIN DEFINITIONS -------------------------------
#define DHTPIN         21
#define DHTTYPE        DHT11

#define PIR_PIN        22
#define TRIG_PIN       18
#define ECHO_PIN       19

#define MQ135_PIN      35
#define MQ6_PIN        34

#define FAN_IN1        25
#define FAN_IN2        26
#define FAN_PWM        27

// moved ALERT_LED to avoid conflict with HUMIDIFIER_PIN
#define ALERT_LED      32

// New additions
// 🟤 Dust Sensor (Sharp GP2Y1010AU0F)
#define DUST_LED_POWER 4    // Digital output to power IR LED (pulse)
#define DUST_ANALOG_PIN 33  // ADC (input-only) - read dust analog output

// 💧 Humidifier Control
#define HUMIDIFIER_PIN 23   // Relay/transistor control for humidifier

// PWM settings for ESP32 LEDC
#define FAN_PWM_CHANNEL 0
#define PWM_FREQ        1000
#define PWM_RESOLUTION  8

#define RELAY_ACTIVE_LOW true
// Safety distance (in cm) for auto fan shutdown - CHANGED TO 15 CM
#define SAFETY_DISTANCE 15  

// ----------------------------- CONFIGURATION --------------------------------
const char* ssid     = "Aditya's Galaxy F23 5G";
const char* password = "12345678";

// Telegram (left as you had it)
String botToken = "7656805521:AAGoCOI-UV5EZj_lLddfS-StShstalHbQTw";
String chatID   = "6837514414";

// ThingSpeak
String tsUrl = "http://api.thingspeak.com/update?api_key=WI6QOAHG8S7AMYP4";

// ----------------------------- GLOBALS --------------------------------------
DHT dht(DHTPIN, DHTTYPE);
WebServer server(80);

// thresholds
float tempThreshold     = 30.0;
float humidityThreshold = 70.0;
int gasHighThreshold    = 500;
int gasMediumThreshold  = 400;
int distanceThreshold   = 100;  // For motion detection

// Manual override variables
bool manualOverride = false;
float manualTemp    = 0;
float manualHum     = 0;
int manualMQ135     = 0;
int manualMQ6       = 0;
int manualFanSpeed  = 0;

unsigned long lastMotionTime = 0;
const unsigned long timeout = 2000;
bool airWasBadWithPerson = false;
int fanSpeed = 0;
bool alreadySent = false;

// humidifier state
bool humidifierOn = false;

// ThingSpeak timing
unsigned long lastTS = 0;
const unsigned long TS_INTERVAL = 16000;

// Telegram message tracking
String lastTelegramMessage = "";
unsigned long lastTelegramTime = 0;
const unsigned long TELEGRAM_INTERVAL = 30000; // 30 seconds between repeated messages

// -------------------------- FUNCTION PROTOTYPES ------------------------------
void stopFan();
void runFanHigh();
void runFanMedium();
void controlFanManual();
void sendDataToThingSpeak(float t, float h, int mq6v, int mq135v, int dustv, bool person, int fSpeed, bool humidOn);
void sendTelegramAlert(String message);
bool shouldSendTelegram(String newMessage);

bool detectPerson();
long getDistance();
int readDustSensor();
bool isPersonTooClose(); // New function for safety distance check

void handleRoot();
void handleData();
void handleSet();
void handleReset();
void handleDashboard();

// ------------------------------- SETUP --------------------------------------
void setup() {
  Serial.begin(115200);
  delay(10);
  Serial.println();
  Serial.println("=== Smart Ventilation System (ESP32) - with Dust & Humidifier ===");
  Serial.println("=== Fan Auto Shutdown at 15cm & Continuous Telegram ===");

  // Init DHT
  dht.begin();

  // Pins
  pinMode(PIR_PIN, INPUT);

  pinMode(TRIG_PIN, OUTPUT);
  digitalWrite(TRIG_PIN, LOW);
  pinMode(ECHO_PIN, INPUT);

  pinMode(FAN_IN1, OUTPUT);
  pinMode(FAN_IN2, OUTPUT);
  pinMode(FAN_PWM, OUTPUT);

  pinMode(ALERT_LED, OUTPUT);
  digitalWrite(ALERT_LED, LOW);

  // New hardware pins
  pinMode(DUST_LED_POWER, OUTPUT);
  digitalWrite(DUST_LED_POWER, LOW); // default off
  pinMode(HUMIDIFIER_PIN, OUTPUT);
  digitalWrite(HUMIDIFIER_PIN, LOW); // humidifier OFF by default

  // PWM
  ledcSetup(FAN_PWM_CHANNEL, PWM_FREQ, PWM_RESOLUTION);
  ledcAttachPin(FAN_PWM, FAN_PWM_CHANNEL);

  stopFan();

  pinMode(HUMIDIFIER_PIN, OUTPUT);
  if (RELAY_ACTIVE_LOW) {
    digitalWrite(HUMIDIFIER_PIN, HIGH);  // keep OFF
  } else {
    digitalWrite(HUMIDIFIER_PIN, LOW);   // keep OFF
  }
  delay(500);

  // Connect WiFi
  Serial.print("Connecting to WiFi");
  WiFi.begin(ssid, password);
  unsigned long wifiStart = millis();
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print('.');
    if (millis() - wifiStart > 30000) {
      Serial.println();
      Serial.println("WiFi connection timeout (will retry later)");
      break;
    }
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println();
    Serial.print("Connected. IP = ");
    Serial.println(WiFi.localIP());
    
    // Send startup message to Telegram
    String startupMsg = "🚀 Smart Ventilation System Started!\n";
    startupMsg += "📍 IP: " + WiFi.localIP().toString() + "\n";
    startupMsg += "⏰ " + String(__DATE__) + " " + String(__TIME__) + "\n";
    startupMsg += "🔧 Features: Fan auto-shutdown at " + String(SAFETY_DISTANCE) + "cm enabled";
    sendTelegramAlert(startupMsg);
  } else {
    Serial.println("WiFi not connected at setup end.");
  }

  // Routes
  server.on("/", handleRoot);
  server.on("/data", handleData);
  server.on("/set", handleSet);
  server.on("/reset", handleReset);
  server.on("/dashboard", handleDashboard);

  server.begin();
  Serial.println("HTTP server started");
  if (WiFi.status() == WL_CONNECTED) {
    Serial.print("Dashboard URL: http://");
    Serial.print(WiFi.localIP());
    Serial.println("/dashboard");
  }
}

// ------------------------------- LOOP ---------------------------------------
void loop() {
  // keep webserver responsive
  server.handleClient();

  // read sensors
  bool person = detectPerson();
  float temp = dht.readTemperature();
  float hum  = dht.readHumidity();
  long distance = getDistance();
  
  // NEW: Safety check - stop fan if person is too close (15cm)
  bool personTooClose = isPersonTooClose();
  if (personTooClose && fanSpeed > 0) {
    stopFan();
    String safetyMsg = "⚠️ SAFETY SHUTDOWN: Fan stopped - Person detected within " + String(SAFETY_DISTANCE) + "cm";
    Serial.println(safetyMsg);
    if (shouldSendTelegram(safetyMsg)) {
      sendTelegramAlert(safetyMsg);
      lastTelegramMessage = safetyMsg;
    }
  }

  // guard DHT NaN
  if (isnan(temp)) {
    Serial.println("DHT temp NaN, defaulting to 0");
    temp = 0.0;
  }
  if (isnan(hum)) {
    Serial.println("DHT hum NaN, defaulting to 0");
    hum = 0.0;
  }

  int mq135 = analogRead(MQ135_PIN);
  int mq6   = analogRead(MQ6_PIN);

  // read dust sensor (raw ADC)
  int dustADC = readDustSensor();

  // If manual override active — follow manual path
  if (manualOverride) {
    temp = manualTemp;
    hum  = manualHum;
    mq135 = manualMQ135;
    mq6   = manualMQ6;
    fanSpeed = manualFanSpeed;
    
    // Safety override: Even in manual mode, stop fan if too close
    if (personTooClose) {
      stopFan();
      Serial.println("🚫 Manual mode safety override: Fan stopped due to proximity");
    } else {
      controlFanManual();
    }

    if (millis() - lastTS >= TS_INTERVAL) {
      sendDataToThingSpeak(temp, hum, mq6, mq135, dustADC, person, fanSpeed, humidifierOn);
      lastTS = millis();
    }
    return; // skip automatic logic
  }

  // Automatic logic
  bool gasLeak = (mq135 > gasHighThreshold || mq6 > gasHighThreshold);
  bool airBad  = (temp > tempThreshold || hum > humidityThreshold ||
                  mq135 > gasMediumThreshold || mq6 > gasMediumThreshold);
  bool airNormal = (!gasLeak && !airBad);

  // Safety first: Don't run fan if person is too close
  if (personTooClose) {
    stopFan();
    Serial.println("🚫 Safety: Person too close, fan remains OFF");
  }
  // Only control fan if safety distance is maintained
  else if (gasLeak && !alreadySent) {
    String gasMsg = "⚠ DANGER: Gas Leak Detected! \n";
    gasMsg += "MQ135: " + String(mq135) + " | MQ6: " + String(mq6) + "\n";
    gasMsg += "Running fan at HIGH speed for ventilation";
    
    if (shouldSendTelegram(gasMsg)) {
      sendTelegramAlert(gasMsg);
      lastTelegramMessage = gasMsg;
    }
    alreadySent = true;
    runFanHigh();
    digitalWrite(ALERT_LED, HIGH);
    airWasBadWithPerson = true;
    Serial.println("⚠ GAS LEAK DETECTED! Fan HIGH");
  } else if (!gasLeak) {
    alreadySent = false;
    digitalWrite(ALERT_LED, LOW);
    if (person && airBad) {
      airWasBadWithPerson = true;
      runFanMedium();
      Serial.println("🟡 Person + bad air. Fan MEDIUM");
    } else if (airWasBadWithPerson && !airNormal) {
      runFanMedium();
      Serial.println("🟠 Holding fan due to earlier bad air. Fan MEDIUM");
    } else {
      airWasBadWithPerson = false;
      stopFan();
      Serial.println("🟢 Normal air. Fan OFF");
    }
  }

  // Continuous status updates to Telegram
  static unsigned long lastStatusUpdate = 0;
  if (millis() - lastStatusUpdate > 60000) { // Every minute
    String statusMsg = "📊 System Status Update:\n";
    statusMsg += "🌡️ Temp: " + String(temp, 1) + "°C | 💧 Hum: " + String(hum, 1) + "%\n";
    statusMsg += "📏 Distance: " + String(distance) + "cm | 👤 Motion: " + String(person ? "Yes" : "No") + "\n";
    statusMsg += "💨 Fan: " + String(fanSpeed) + "/255 | 💦 Humidifier: " + String(humidifierOn ? "ON" : "OFF");
    
    if (shouldSendTelegram(statusMsg)) {
      sendTelegramAlert(statusMsg);
      lastTelegramMessage = statusMsg;
    }
    lastStatusUpdate = millis();
  }

  Serial.print("Temp: "); Serial.print(temp);
  Serial.print(" °C, Hum: "); Serial.print(hum);
  Serial.print(" %, MQ135: "); Serial.print(mq135);
  Serial.print(", MQ6: "); Serial.print(mq6);
  Serial.print(", DustADC: "); Serial.print(dustADC);
  Serial.print(", Distance: "); Serial.print(distance);
  Serial.print("cm, Fan: "); Serial.println(fanSpeed);

  // ThingSpeak update (non-blocking)
  if (millis() - lastTS >= TS_INTERVAL) {
    sendDataToThingSpeak(temp, hum, mq6, mq135, dustADC, person, fanSpeed, humidifierOn);
    lastTS = millis();
  }

  // small loop delay (server handles clients)
  delay(50);
}

// ------------------------- DASHBOARD / HTTP HANDLERS ------------------------
// [Your existing HTTP handlers remain the same...]
void handleRoot() {
  String page = "<!DOCTYPE html><html><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>ESP32 Sensors</title></head><body>";
  page += "<h3>ESP32 Smart Ventilation</h3>";
  page += "<p>Visit <a href=\"/dashboard\">/dashboard</a> for the web UI.</p>";
  page += "</body></html>";
  server.send(200, "text/html", page);
}

void handleData() {
  // read live values to return freshest data
  float temp = dht.readTemperature();
  float hum  = dht.readHumidity();
  if (isnan(temp)) temp = 0.0;
  if (isnan(hum)) hum = 0.0;

  int mq135 = analogRead(MQ135_PIN);
  int mq6   = analogRead(MQ6_PIN);
  int dustADC = readDustSensor();
  bool person = detectPerson();
  long distance = getDistance();
  bool tooClose = isPersonTooClose();

  String payload = "{";
  payload += "\"temperature\":" + String(temp,1) + ",";
  payload += "\"humidity\":" + String(hum,1) + ",";
  payload += "\"mq135\":" + String(mq135) + ",";
  payload += "\"mq6\":" + String(mq6) + ",";
  payload += "\"dust\":" + String(dustADC) + ",";
  payload += "\"distance\":" + String(distance) + ",";
  payload += "\"tooClose\":" + String(tooClose ? "true" : "false") + ",";
  payload += "\"motion\":" + String(person ? "true" : "false") + ",";
  payload += "\"fanSpeed\":" + String(fanSpeed) + ",";
  payload += "\"manualOverride\":" + String(manualOverride ? "true" : "false") + ",";
  payload += "\"humidifier\":" + String(humidifierOn ? "true" : "false") + ",";
  payload += "\"deviceIP\":\"" + String(WiFi.localIP().toString()) + "\"";
  payload += "}";
  server.send(200, "application/json", payload);
}

void handleSet() {
  bool responded = false;
  if (server.hasArg("fan")) {
    String f = server.arg("fan");
    int fs = f.toInt();
    manualOverride = true;
    manualFanSpeed = constrain(fs, 0, 255);
    
    // Safety check: Don't allow fan if person is too close
    if (!isPersonTooClose()) {
      controlFanManual();
      Serial.print("Manual fan set to: ");
      Serial.println(manualFanSpeed);
    } else {
      stopFan();
      Serial.println("Safety override: Cannot start fan - person too close");
    }
    responded = true;
  }

  if (server.hasArg("humidifier")) {
    String h = server.arg("humidifier");
    int hv = h.toInt();
    humidifierOn = (hv != 0);
    if (RELAY_ACTIVE_LOW)
      digitalWrite(HUMIDIFIER_PIN, humidifierOn ? LOW : HIGH);
    else
      digitalWrite(HUMIDIFIER_PIN, humidifierOn ? HIGH : LOW);

    Serial.print("Humidifier set to: ");
    Serial.println(humidifierOn ? "ON" : "OFF");
    responded = true;
  }

  if (responded) {
    server.send(200, "text/plain", "OK");
  } else {
    server.send(400, "text/plain", "No args");
  }
}

void handleReset() {
  // Reset manual override -> auto mode
  manualOverride = false;
  manualFanSpeed = 0;
  stopFan();
  server.send(200, "text/plain", "Auto");
}

void handleDashboard() {
  // [Your existing dashboard HTML remains the same...]
  String html = R"HTML(
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width,initial-scale=1" />
<title>Smart Ventilation Dashboard</title>
<style>
  *{box-sizing:border-box;margin:0;padding:0}
  body{font-family:Arial,Helvetica,sans-serif;background:linear-gradient(135deg,#667eea,#764ba2);color:#fff;padding:18px}
  .dashboard{max-width:1100px;margin:0 auto}
  header{display:flex;justify-content:space-between;align-items:center;background:rgba(255,255,255,0.06);padding:12px;border-radius:10px;margin-bottom:14px}
  .controls{display:flex;gap:10px;align-items:center;margin-bottom:14px;flex-wrap:wrap}
  input[type="text"]{padding:6px;border-radius:6px;border:none}
  .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:12px;margin-bottom:14px}
  .card{background:rgba(255,255,255,0.05);padding:12px;border-radius:8px}
  .manual{background:rgba(255,255,255,0.05);padding:12px;border-radius:8px;margin-bottom:14px}
  .info{background:rgba(255,255,255,0.05);padding:12px;border-radius:8px}
  .progress{height:8px;background:rgba(255,255,255,0.06);border-radius:6px;overflow:hidden;margin-top:8px}
  .fill{height:100%;background:#ff6b6b;width:0%;transition:width .2s}
  .mode-buttons{display:flex;gap:8px;margin-top:8px;flex-wrap:wrap}
  .mode-btn{padding:8px 16px;border:none;border-radius:6px;cursor:pointer;transition:all 0.3s;font-weight:bold}
  .mode-btn.off{background:#ff4757;color:white}
  .mode-btn.medium{background:#ffa502;color:white}
  .mode-btn.high{background:#2ed573;color:white}
  .mode-btn.active{transform:scale(1.05);box-shadow:0 0 10px rgba(255,255,255,0.5)}
  .mode-btn:hover{opacity:0.9}
  .small-btn{padding:6px 10px;border-radius:6px;border:none;cursor:pointer}
  .safety-alert{background:#ff4757;color:white;padding:8px;border-radius:6px;margin:8px 0;text-align:center}
</style>
</head>
<body>
  <div class="dashboard">
    <header>
      <div>
        <h2>🌬️ Smart Ventilation System</h2>
        <small>ESP32 Dashboard - Fan auto-shutdown at 15cm</small>
      </div>
      <div>
        <span id="connection-status">Not connected</span>
        <span id="status-dot" style="display:inline-block;width:12px;height:12px;border-radius:50%;background:#ff4757;margin-left:8px"></span>
      </div>
    </header>

    <div id="safety-alert" class="safety-alert" style="display:none">
      ⚠️ SAFETY: Person detected within 15cm - Fan disabled
    </div>

    <div class="controls">
      <label>ESP32 IP: <input id="esp32-ip" type="text" placeholder="192.168.1.x" /></label>
      <button id="connect-btn">Connect</button>
      <button id="refresh-btn">Refresh</button>
      <button id="auto-mode">Auto Mode</button>
    </div>

    <div class="grid">
      <div class="card"><h3>Temp</h3><div id="temp-value">-- °C</div><div id="temp-status"></div></div>
      <div class="card"><h3>Humidity</h3><div id="hum-value">-- %</div><div id="hum-status"></div></div>
      <div class="card"><h3>MQ135</h3><div id="mq135-value">--</div><div id="mq135-status"></div></div>
      <div class="card"><h3>MQ6</h3><div id="mq6-value">--</div><div id="mq6-status"></div></div>
      <div class="card"><h3>Dust (raw ADC)</h3><div id="dust-value">--</div><div id="dust-status"></div></div>
      <div class="card"><h3>Distance</h3><div id="distance-value">-- cm</div><div id="distance-status"></div></div>
      <div class="card"><h3>Motion</h3><div id="motion-value">--</div><div id="motion-status">No motion</div></div>
      <div class="card"><h3>Fan</h3><div id="fan-speed-value">-- /255</div><div class="progress"><div id="fan-progress" class="fill"></div></div><div id="fan-status">Auto</div></div>
      <div class="card"><h3>Humidifier</h3><div id="humid-value">--</div><div style="margin-top:8px"><button id="humid-toggle" class="small-btn">Toggle Humidifier</button></div></div>
    </div>

    <div class="manual">
      <h3>Manual Control</h3>
      <div>Current Speed: <span id="fan-display">0</span>/255</div>
      <div class="mode-buttons">
        <button class="mode-btn off" data-speed="0">OFF</button>
        <button class="mode-btn medium" data-speed="150">MEDIUM</button>
        <button class="mode-btn high" data-speed="255">HIGH</button>
      </div>
      <div style="margin-top:8px">
        <button id="reset-mode">Reset to Auto</button>
      </div>
    </div>

    <div id="system-info" class="info">Waiting for data...</div>
  </div>

<script>
class SV {
  constructor(){
    this.ip = '';
    this.data = {};
    this.interval = null;

    // Event listeners
    document.getElementById('connect-btn').addEventListener('click', ()=>this.connect());
    document.getElementById('refresh-btn').addEventListener('click', ()=>this.fetch());
    document.getElementById('reset-mode').addEventListener('click', ()=>this.resetMode());
    document.getElementById('auto-mode').addEventListener('click', ()=>this.resetMode());
    document.getElementById('humid-toggle').addEventListener('click', ()=>this.toggleHumidifier());

    // Mode button listeners
    document.querySelectorAll('.mode-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const speed = parseInt(e.target.getAttribute('data-speed'));
        this.setFanSpeed(speed);
      });
    });
  }

  async connect(){
    const ip = document.getElementById('esp32-ip').value.trim();
    if(!ip) return alert('Enter IP');
    this.ip = ip;
    try{
      const r = await fetch(`http://${ip}/data`);
      if(!r.ok) throw new Error('bad');
      document.getElementById('connection-status').textContent = `Connected ${ip}`;
      document.getElementById('status-dot').style.background = '#2ed573';
      if(this.interval) clearInterval(this.interval);
      this.interval = setInterval(()=>this.fetch(),2000);
      this.fetch();
    }catch(e){
      document.getElementById('connection-status').textContent = 'Connection failed';
      document.getElementById('status-dot').style.background = '#ff4757';
    }
  }

  async fetch(){
    if(!this.ip) return;
    try{
      const r = await fetch(`http://${this.ip}/data`);
      if(!r.ok) throw new Error('http '+r.status);
      this.data = await r.json();
      this.data.timestamp = new Date().toLocaleTimeString();
      this.updateUI();
    }catch(e){
      document.getElementById('connection-status').textContent = 'Connection lost';
      document.getElementById('status-dot').style.background = '#ff4757';
    }
  }

  updateUI(){
    // Update sensor values
    if(this.data.temperature!=null) document.getElementById('temp-value').textContent = this.data.temperature.toFixed(1)+' °C';
    if(this.data.humidity!=null) document.getElementById('hum-value').textContent = this.data.humidity.toFixed(1)+' %';
    if(this.data.mq135!=null) document.getElementById('mq135-value').textContent = this.data.mq135;
    if(this.data.mq6!=null) document.getElementById('mq6-value').textContent = this.data.mq6;
    if(this.data.dust!=null) document.getElementById('dust-value').textContent = this.data.dust;
    if(this.data.distance!=null) document.getElementById('distance-value').textContent = this.data.distance + ' cm';

    // Update motion
    if(this.data.motion!=null){
      document.getElementById('motion-value').textContent = this.data.motion ? 'YES' : 'NO';
      document.getElementById('motion-status').textContent = this.data.motion ? 'Motion Detected' : 'No Motion';
    }

    // Safety alert
    if(this.data.tooClose) {
      document.getElementById('safety-alert').style.display = 'block';
    } else {
      document.getElementById('safety-alert').style.display = 'none';
    }

    // Humidifier
    document.getElementById('humid-value').textContent = this.data.humidifier ? 'ON' : 'OFF';

    // Update fan display
    if(this.data.fanSpeed!=null){
      document.getElementById('fan-speed-value').textContent = this.data.fanSpeed+' /255';
      document.getElementById('fan-display').textContent = this.data.fanSpeed;
      document.getElementById('fan-progress').style.width = (this.data.fanSpeed/255*100)+'%';
      document.getElementById('fan-status').textContent = this.data.manualOverride ? 'Manual' : 'Auto';

      // Highlight active mode button
      document.querySelectorAll('.mode-btn').forEach(btn => {
        btn.classList.remove('active');
        if(parseInt(btn.getAttribute('data-speed')) === this.data.fanSpeed && this.data.manualOverride) {
          btn.classList.add('active');
        }
      });
    }

    // Update system info
    document.getElementById('system-info').innerHTML =
      `<b>Last:</b> ${this.data.timestamp} <br/>
       <b>IP:</b> ${this.data.deviceIP||'Unknown'} <br/>
       <b>Mode:</b> ${this.data.manualOverride ? 'Manual' : 'Auto'}`;
  }

  async setFanSpeed(speed){
    if(!this.ip) return alert('Not connected');
    try{
      const r = await fetch(`http://${this.ip}/set?fan=${speed}`);
      if(!r.ok) throw new Error('Set failed');
      this.data.manualOverride = true;
      this.data.fanSpeed = speed;
      this.updateUI();
      this.fetch();
    }catch(e){
      alert('Error setting fan speed: ' + e.message);
    }
  }

  async resetMode(){
    if(!this.ip) return alert('Not connected');
    try{
      const r = await fetch(`http://${this.ip}/reset`);
      if(!r.ok) throw new Error('Reset failed');
      this.data.manualOverride = false;
      this.fetch();
    }catch(e){ alert('Error resetting to auto: ' + e.message); }
  }

  async toggleHumidifier(){
    if(!this.ip) return alert('Not connected');
    const newState = this.data.humidifier ? 0 : 1;
    try{
      const r = await fetch(`http://${this.ip}/set?humidifier=${newState}`);
      if(!r.ok) throw new Error('Set failed');
      this.fetch();
    }catch(e){
      alert('Error toggling humidifier: ' + e.message);
    }
  }
}

window.addEventListener('DOMContentLoaded', ()=>{ window.dashboard = new SV(); });
</script>
</body>
</html>
)HTML";

  server.send(200, "text/html", html);
}

// ------------------------- SENSOR & FAN ROUTINES -----------------------------

bool detectPerson() {
  bool pir_ok = digitalRead(PIR_PIN);
  bool us_ok = (getDistance() < distanceThreshold);
  if (pir_ok || us_ok) {
    lastMotionTime = millis();
    return true;
  }
  return (millis() - lastMotionTime < timeout);
}

long getDistance() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);

  long dur = pulseIn(ECHO_PIN, HIGH, 20000);
  if (dur == 0) return 9999;
  long dist = (long)(dur * 0.0343 / 2.0);
  return dist;
}

// NEW FUNCTION: Check if person is too close (within 15cm)
bool isPersonTooClose() {
  long distance = getDistance();
  return (distance > 0 && distance <= SAFETY_DISTANCE);
}

int readDustSensor() {
  digitalWrite(DUST_LED_POWER, LOW);
  delayMicroseconds(10);

  digitalWrite(DUST_LED_POWER, HIGH);
  delayMicroseconds(280);
  int VoMeasured = analogRead(DUST_ANALOG_PIN);
  delayMicroseconds(40);
  digitalWrite(DUST_LED_POWER, LOW);
  delayMicroseconds(9680); // completes ~10ms cycle

  // Convert to voltage
  float calcVoltage = VoMeasured * (3.3 / 4095.0);

  // Traditional datasheet equation (mg/m³)
  float dustDensity = (0.17 * calcVoltage) - 0.1;
  if (dustDensity < 0) dustDensity = 0;  // avoid negatives

  // Return in µg/m³ (1 mg/m³ = 1000 µg/m³)
  return (int)(dustDensity * 1000);
}

void runFanHigh() {
  // Safety check before running fan
  if (!isPersonTooClose()) {
    digitalWrite(FAN_IN1, HIGH);
    digitalWrite(FAN_IN2, LOW);
    fanSpeed = 255;
    ledcWrite(FAN_PWM_CHANNEL, fanSpeed);
  } else {
    stopFan();
    Serial.println("🚫 Safety: Cannot run fan HIGH - person too close");
  }
}

void runFanMedium() {
  // Safety check before running fan
  if (!isPersonTooClose()) {
    digitalWrite(FAN_IN1, HIGH);
    digitalWrite(FAN_IN2, LOW);
    fanSpeed = 150;
    ledcWrite(FAN_PWM_CHANNEL, fanSpeed);
  } else {
    stopFan();
    Serial.println("🚫 Safety: Cannot run fan MEDIUM - person too close");
  }
}

void stopFan() {
  digitalWrite(FAN_IN1, LOW);
  digitalWrite(FAN_IN2, LOW);
  fanSpeed = 0;
  ledcWrite(FAN_PWM_CHANNEL, 0);
}

void controlFanManual() {
  // Safety check: Don't run fan if person is too close
  if (isPersonTooClose()) {
    stopFan();
    Serial.println("🚫 Safety override: Manual fan control disabled - person too close");
    return;
  }

  if (manualFanSpeed > 0) {
    digitalWrite(FAN_IN1, HIGH);
    digitalWrite(FAN_IN2, LOW);
    ledcWrite(FAN_PWM_CHANNEL, manualFanSpeed);
    fanSpeed = manualFanSpeed;
  } else {
    stopFan();
  }
}

// ------------------------- THINGSPEAK & TELEGRAM -----------------------------

// NEW FUNCTION: Check if we should send Telegram message (avoid spam)
bool shouldSendTelegram(String newMessage) {
  unsigned long currentTime = millis();
  
  // Don't send if it's the same message and too soon
  if (newMessage == lastTelegramMessage && 
      (currentTime - lastTelegramTime) < TELEGRAM_INTERVAL) {
    return false;
  }
  
  lastTelegramTime = currentTime;
  return true;
}

void sendDataToThingSpeak(float t, float h, int mq6v, int mq135v, int dustv, bool person, int fSpeed, bool humidOn) {
  if (WiFi.status() != WL_CONNECTED) return;

  String fullUrl = tsUrl;
  fullUrl += "&field1=" + String(t);
  fullUrl += "&field2=" + String(h);
  fullUrl += "&field3=" + String(mq6v);
  fullUrl += "&field4=" + String(mq135v);
  fullUrl += "&field5=" + String(person ? 1 : 0);
  fullUrl += "&field6=" + String(fSpeed);
  fullUrl += "&field7=" + String(dustv);
  fullUrl += "&field8=" + String(humidOn ? 1 : 0);

  HTTPClient http;
  http.begin(fullUrl);
  int code = http.GET();
  Serial.print("ThingSpeak response: ");
  Serial.println(code);
  http.end();
}

void sendTelegramAlert(String message) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Telegram: No WiFi");
    return;
  }

  String url = "https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatID + "&text=" + message;
  HTTPClient http;
  http.begin(url);
  int code = http.GET();
  if (code > 0) {
    Serial.println("Telegram: sent - " + message.substring(0, 30) + "...");
  } else {
    Serial.println("Telegram: failed");
  }
  http.end();
}
