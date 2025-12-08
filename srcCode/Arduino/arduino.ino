#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <BluetoothSerial.h>  

#define MQ3_PIN 34     
#define LED_BLUE 2    
#define LED_RED 4     
#define BUTTON_PIN 33  

BluetoothSerial ESP_BT; 
LiquidCrystal_I2C lcd(0x27, 16, 2); 

float RL = 4.7;   
float R0 = 10.0;  
float MaxAlcohol = 0;
String AlcoholStatus = "";
bool playingBluetooth = false;
unsigned long buttonPressTime = 0;  
bool isButtonPressed = false;       
bool mesuring = false;
bool isConnected = false;

// --- Task Handles ---
TaskHandle_t connectionTaskHandle = NULL;  
TaskHandle_t checkConnectionTaskHandle = NULL;
bool recievedMsg = false;
int timeStartSending = 0;

// ==========================================
// 1. CÁC HÀM PHỤ TRỢ
// ==========================================

void BTLEDMode(void *pvParameters) {
  while (playingBluetooth) {
    digitalWrite(LED_RED, HIGH);
    digitalWrite(LED_BLUE, LOW);
    vTaskDelay(500 / portTICK_PERIOD_MS);
    digitalWrite(LED_RED, LOW);
    digitalWrite(LED_BLUE, HIGH);
    vTaskDelay(500 / portTICK_PERIOD_MS);
  }
  digitalWrite(LED_BLUE, LOW);
  digitalWrite(LED_RED, LOW);
  vTaskDelete(NULL);
}

// ==========================================
// 2. LOGIC ĐO NỒNG ĐỘ CỒN
// ==========================================

float getAlcohol() {
  float total_mq3_value = 0;
  int samples = 20; // Số lần lấy mẫu

  // Đọc 20 lần liên tiếp
  for (int i = 0; i < samples; i++) {
    total_mq3_value += analogRead(MQ3_PIN);
    delay(2); // Delay nhỏ giữa các lần đọc
  }

  // Lấy giá trị trung bình
  float mq3_value = total_mq3_value / samples;

  float sensorVoltage = mq3_value * (5.0 / 4095); 
  float RS = ((5.0 * RL) / sensorVoltage) - RL;   
  float ratio = RS / R0;                          
  float mgL = pow(ratio / 1.5, 1.0 / -0.5);
  
  return mgL;
}

void showAlcoholDetail() {
  float mgL = getAlcohol() / 2; 
  if (mgL > MaxAlcohol) MaxAlcohol = mgL;
  
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Alcohol Level:");
  lcd.setCursor(0, 1);
  lcd.print(mgL, 3); 
  lcd.print(" mg/L");

  Serial.print("Current: ");
  Serial.println(mgL, 3);

  if (mgL < 0.2) {
    digitalWrite(LED_BLUE, HIGH);
    digitalWrite(LED_RED, LOW);
  } else {
    digitalWrite(LED_BLUE, LOW);
    digitalWrite(LED_RED, HIGH);
  }
}

void postDelay(int time, int delayTime, void (*func)()) {
  int lastTime = millis();
  int passingTime = 0;
  while (passingTime <= time) {
    int currentTime = millis();
    func(); 
    vTaskDelay(delayTime / portTICK_PERIOD_MS);
    passingTime += currentTime - lastTime;
    lastTime = currentTime;
  }
}

void detectAlcohol() {
  if (!mesuring) {
    mesuring = true;
    
    // Báo cho App biết bắt đầu đo
    if (ESP_BT.hasClient()) {
      ESP_BT.println("StartMesuring");
      Serial.println("Sent: StartMesuring");
    }

    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Stabilizing...");
    lcd.setCursor(0, 1);
    lcd.print("Please wait...");
    
    for(int i = 0; i < 20; i++) {
        analogRead(MQ3_PIN); 
        delay(100); 
    }
    // --------------------------------------------------
    
    MaxAlcohol = 0;
    
    postDelay(5000, 500, showAlcoholDetail);
    
    digitalWrite(LED_BLUE, LOW);
    digitalWrite(LED_RED, LOW);
    resetLCD();
    
    if (ESP_BT.hasClient()) {
      String status = (MaxAlcohol > 0.2 ? "HIGH" : "LOW");
      String json = "GetAlcohol|{\"alcohol_level\":" + String(MaxAlcohol, 3) + ",\"status\":\"" + status + "\"}";
      ESP_BT.println(json);
      Serial.println("Sent Result: " + json);
    }
    
    mesuring = false;
  }
}

void detectingAlcoholTask(void *pvParameters) {
  detectAlcohol();
  vTaskDelete(NULL);
}

// ==========================================
// 3. XỬ LÝ KẾT NỐI
// ==========================================

void connectionTask(void *pvParameters) {
  Serial.println("Connection Task Started");
  
  if (ESP_BT.hasClient()) {
    ESP_BT.println("Confirm Device");
  }
  
  // Vòng lặp chính duy trì kết nối
  while (ESP_BT.hasClient()) {
    // Nếu mất kết nối vật lý bất ngờ -> thoát vòng lặp
    if (!ESP_BT.hasClient()) break;

    if (ESP_BT.available()) {
      String request = ESP_BT.readString();
      request.trim(); 

      // ... (Giữ nguyên phần xử lý chuỗi của bạn) ...
      String eventName = "";
      String data = "";
      int seperatorIndex = request.indexOf('|');
      if (seperatorIndex != -1) {
        eventName = request.substring(0, seperatorIndex);
        data = request.substring(seperatorIndex + 1);
      } else {
        eventName = request;
      }
      // ... 

      Serial.print("Event: "); Serial.println(eventName);

      if (eventName == "CheckConnection") {
        ESP_BT.println("CheckConnection");
      } 
      else if (eventName == "GetAlcohol") {
        // Kiểm tra biến isConnected để tránh lỗi nếu đang ngắt
        if (isConnected) {
            xTaskCreate(detectingAlcoholTask, "Detecting Alcohol", 4096, NULL, 1, NULL);
        }
      } 
      else if (eventName == "Disconnect") {
        // KHI NHẬN LỆNH DISCONNECT -> THOÁT VÒNG LẶP NGAY
        Serial.println("Disconnect command received.");
        break; 
      }
    }
    vTaskDelay(50 / portTICK_PERIOD_MS); 
  }
  
  
  isConnected = false; // Đánh dấu ngắt kết nối để các hàm khác không gửi dữ liệu
  playingBluetooth = false; // Tắt đèn LED nháy (nếu có)

  Serial.println("Performing Disconnect...");
  
  // 1. Ngắt kết nối mềm
  ESP_BT.disconnect();
  vTaskDelay(200 / portTICK_PERIOD_MS); // Chờ Bluetooth xử lý
  
  // 2. Tắt hẳn Bluetooth stack
  ESP_BT.end(); 
  Serial.println("Bluetooth Ended.");

  // 3. Cập nhật LCD
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Disconnected");
  vTaskDelay(1500 / portTICK_PERIOD_MS); 
  
  resetLCD(); // Quay về màn hình chính

  // 4. Tự hủy Task này an toàn
  connectionTaskHandle = NULL;
  Serial.println("Task Deleted.");
  vTaskDelete(NULL); 
}

// ==========================================
// 4. MAIN SETUP & LOOP
// ==========================================

void BTAdvertisingTask(void *pvParameters) {
  if (!ESP_BT.begin("ESP32_Bluetooth_Server")) { 
    Serial.println("Bluetooth initialization failed.");
    return;
  }

  playingBluetooth = true;
  xTaskCreate(BTLEDMode, "BTLEDMode", 4096, NULL, 1, NULL);
  Serial.println("Bluetooth started, waiting for connection...");

  lcd.setCursor(0, 0);
  lcd.print("Bluetooth con...");
  lcd.setCursor(0, 1);
  lcd.print("................");

  unsigned long startTime = millis();
  isConnected = false;

  while (millis() - startTime < 30000) { 
    if (ESP_BT.hasClient()) {           
      isConnected = true;
      Serial.println("Bluetooth client connected!");
      lcd.setCursor(0, 1);
      lcd.print("Connected!    ");
      break; 
    }
    vTaskDelay(100 / portTICK_PERIOD_MS); 
  }
  
  if (isConnected) {
    // SỬA LỖI CRASH: Priority 200 -> 1
    xTaskCreate(connectionTask, "connectionTask", 4096, NULL, 1, &connectionTaskHandle);
  }
  
  if (!isConnected) {
    ESP_BT.end();
    Serial.println("No connection established.");
    lcd.setCursor(0, 1);
    lcd.print("No connection");
    vTaskDelay(1000 / portTICK_PERIOD_MS);
  }
  resetLCD();
  playingBluetooth = false;
  vTaskDelete(NULL);
}

void resetLCD() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Alcohol Detector");
  lcd.setCursor(0, 1);
  lcd.print("Press button...");
}

void setup() {
  Serial.begin(9600);
  pinMode(MQ3_PIN, INPUT);
  pinMode(LED_BLUE, OUTPUT);
  pinMode(LED_RED, OUTPUT);
  pinMode(BUTTON_PIN, INPUT_PULLDOWN); 

  lcd.init();
  lcd.backlight();
  resetLCD();
  
  digitalWrite(LED_BLUE, LOW);
  digitalWrite(LED_RED, LOW);
  delay(2000);
}

void loop() {
  unsigned long pressingTime = 0;
  
  if (digitalRead(BUTTON_PIN) == HIGH) {
    buttonPressTime = millis();
    while (digitalRead(BUTTON_PIN) == HIGH) {
      pressingTime = millis() - buttonPressTime;
      if (pressingTime >= 3000) { 
        xTaskCreate(BTAdvertisingTask, "BTAdvertisingTask", 4096, NULL, 1, NULL);
        break;
      }
    }
    Serial.println((float)(pressingTime / 1000));
  }

  if (!mesuring && !playingBluetooth && pressingTime <= 2000 && pressingTime > 100) {
     detectAlcohol(); 
  }
}