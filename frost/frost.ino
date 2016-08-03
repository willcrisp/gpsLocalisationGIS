#include <Adafruit_GPS.h>
#include <SoftwareSerial.h>
#include <Wire.h>
#include <Math.h>
#include <LSM303.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BMP085_U.h>

LSM303 compass;


SoftwareSerial mySerial(6, 5); //gps green,blue
//SoftwareSerial XBee(2, 3); // RX, TX
Adafruit_GPS GPS(&mySerial);
Adafruit_BMP085_Unified bmp = Adafruit_BMP085_Unified(10085);

// Set GPSECHO to 'false' to turn off echoing the GPS data to the Serial console
#define GPSECHO  false

// this keeps track of whether we're using the interrupt
// off by default!
boolean usingInterrupt = true;
void useInterrupt(boolean); // Func prototype keeps Arduino 0023 happy

//Sensor data

float currentLat = -32.00560;
float currentLong = 115.75990;
float currentAlt = 35.00;


float newLat = 0.0;
float newLong = 0.0;
double newAlt = 0.0;
double deltaAlt = 0.0;

float pointBearing = 0;
double pointDistance = 500;
double kmConvertedDistance = pointDistance/1000;
double straightLineDistance = 0;

float temperature;
float seaLevelPressure;

float pitchX;
float pitchY;
float pitchZ;
float pitch;


float earthRadius = 6378.1;
boolean goodData = false;
const int buttonPin = 12; 
int buttonState = 0;

void setup()  
{

  pinMode(buttonPin, INPUT);
  Serial.begin(115200);
  //xbee.begin(9600);
   
  //Altitude Stuff ***************************************************************
  sensor_t sensor;
  bmp.getSensor(&sensor);
  bmp.begin();
  //****************************************************************************************
  
  //compass stuff ****************************************************************
  Wire.begin();
  compass.init();
  compass.enableDefault();
//min: { -3798,  -4809,  -3885}    max: { +3952,  +2521,  +3773}

  compass.m_min = (LSM303::vector<int16_t>){-3798,  -4809,  -3885};
  compass.m_max = (LSM303::vector<int16_t>){+3952,  +2521,  +3773};
  //****************************************************************************************
  
  //GPS stuff ******************************************************************
  // 9600 NMEA is the default baud rate
  GPS.begin(9600);
  GPS.sendCommand(PMTK_SET_NMEA_OUTPUT_RMCONLY);
  GPS.sendCommand(PMTK_SET_NMEA_UPDATE_1HZ);   // 1 Hz update rate
  useInterrupt(true);
  //****************************************************************************************

  
  delay(1000);
  // Ask for firmware version
  mySerial.println(PMTK_Q_RELEASE);
}


// Interrupt is called once a millisecond, looks for any new GPS data, and stores it
SIGNAL(TIMER0_COMPA_vect) {
  char c = GPS.read();
  // if you want to debug, this is a good time to do it!
#ifdef UDR0
  if (GPSECHO)
    if (c) UDR0 = c;  
    // writing direct to UDR0 is much much faster than Serial.print 
    // but only one character can be written at a time. 
#endif
}

void useInterrupt(boolean v) {
  if (v) {
    // Timer0 is already used for millis() - we'll just interrupt somewhere
    // in the middle and call the "Compare A" function above
    OCR0A = 0xAF;
    TIMSK0 |= _BV(OCIE0A);
    usingInterrupt = true;
  } else {
    // do not call the interrupt function COMPA anymore
    TIMSK0 &= ~_BV(OCIE0A);
    usingInterrupt = false;
  }
}

uint32_t timer = millis();
void loop() // run over and over again
{
  //heading
  compass.read();
  pointBearing = radians(compass.heading());
  sensors_event_t event;
  
  //alt
  bmp.getEvent(&event);
  bmp.getTemperature(&temperature);
  seaLevelPressure = SENSORS_PRESSURE_SEALEVELHPA;
  currentAlt = bmp.pressureToAltitude(seaLevelPressure, event.pressure);

  //pitch
  pitchX = compass.a.x; pitchY = compass.a.y; pitchZ = compass.a.z;
  pitch = degrees(atan(pitchX/sqrt(sq(pitchY) + sq(pitchZ))));

  // in case you are not using the interrupt above, you'll
  // need to 'hand query' the GPS, not suggested :(
  if (! usingInterrupt) {
    // read data from the GPS in the 'main loop'
    char c = GPS.read();
    // if you want to debug, this is a good time to do it!
    if (GPSECHO)
      if (c) Serial.print(c);
  }
  
  // if a sentence is received, we can check the checksum, parse it...
  if (GPS.newNMEAreceived()) {
    // a tricky thing here is if we print the NMEA sentence, or data
    // we end up not listening and catching other sentences! 
    // so be very wary if using OUTPUT_ALLDATA and trytng to print out data
    //Serial.println(GPS.lastNMEA());   // this also sets the newNMEAreceived() flag to false
  
    if (!GPS.parse(GPS.lastNMEA()))   // this also sets the newNMEAreceived() flag to false
      return;  // we can fail to parse a sentence in which case we should just wait for another
  }

  // if millis() or timer wraps around, we'll just reset it
  if (timer > millis())  timer = millis();

  //Lets save our parsed values
  if(GPS.fix){
  currentLat = radians(GPS.latitudeDegrees);
  currentLong = radians(GPS.longitudeDegrees);

  buttonState = digitalRead(buttonPin);
  if(buttonState == HIGH){
   newLat = asin(sin(currentLat)*cos(kmConvertedDistance/earthRadius) + cos(currentLat)*sin(kmConvertedDistance/earthRadius)*cos(pointBearing));
   newLong = currentLong + atan2(sin(pointBearing)*sin(kmConvertedDistance/earthRadius)*cos(newLat), cos(kmConvertedDistance/earthRadius)-sin(currentLat)*sin(newLat));
   newLat = degrees(newLat);
   newLong = degrees(newLong);

   
   deltaAlt = sin(radians(pitch)) * pointDistance;
   straightLineDistance = sqrt(sq(pointDistance) - sq(deltaAlt));
   newAlt = currentAlt + deltaAlt;

  }else{
    newLat = 0.0;
    newLong = 0.0;
    newAlt = 0.0;
    deltaAlt = 0.0;
    straightLineDistance = 0.0;
  }
  
  }

  // approximately every 2 seconds or so, print out the current stats
  if (millis() - timer > 2000) { 
    timer = millis(); // reset the timer
    
    //XBee.print(currentLat);
   // XBee.print(", ");
   // XBee.println(currentLong);
   
//  Serial.print("\nTime: ");
//  Serial.print(GPS.hour, DEC); Serial.print(':');
//  Serial.print(GPS.minute, DEC);Serial.print(':');
//  Serial.print(GPS.seconds, DEC);Serial.print('.');
//  Serial.println(GPS.milliseconds);
//  Serial.print("Date: ");
//  Serial.print(GPS.day, DEC); Serial.print('/');
//  Serial.print(GPS.month, DEC); Serial.print("/20");
//  Serial.println(GPS.year, DEC);
  Serial.print("\n-----------------------------------------------");
  Serial.print("\nFix: "); Serial.print((int)GPS.fix);
  Serial.print(" quality: "); Serial.println((int)GPS.fixquality); 
    if (GPS.fix) {      
//      Serial.print("Speed (knots): "); Serial.println(GPS.speed);
//      Serial.print("Angle: "); Serial.println(GPS.angle);
      Serial.print("GPS Altitude: "); Serial.print(GPS.altitude);
      Serial.print("  Sensor Altitude: "); Serial.println(currentAlt); 
      Serial.print("Pitch: "); Serial.println(pitch);
//      Serial.print("Satellites: "); Serial.println((int)GPS.satellites);

   Serial.print("Bearing to point: "); Serial.println(degrees(pointBearing),3);
   Serial.print("Your position: "); Serial.print(degrees(currentLat), 5);
   Serial.print(", "); 
   Serial.println(degrees(currentLong), 5);

   Serial.print("\nTarget position: "); Serial.print(newLat, 5);
   Serial.print(", "); 
   Serial.println(newLong, 5);
   Serial.print("Straight line :"); Serial.println(straightLineDistance, 2);
   Serial.print("Target altitude:"); Serial.println(newAlt, 2);
   Serial.print("Target delta altitude:"); Serial.println(deltaAlt, 2);
    }
    else{
     Serial.println("No Fix, Hold tight");
    }
  Serial.print("\n-----------------------------------------------");
  }
}
