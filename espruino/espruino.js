// -- Common functions called by both the Bluetooth and Web Server handlers

function turnRedLightOn() {
  digitalWrite(LED1, 1);
}

function turnRedLightOff() {
  digitalWrite(LED1, 0);
}

function turnGreenLightOn() {
  digitalWrite(LED2, 1);
}

function turnGreenLightOff() {
  digitalWrite(LED2, 0);
}

function turnBlueLightOn() {
  digitalWrite(LED3, 1);
  lightStatus = "ON";
}

function turnBlueLightOff() {
  digitalWrite(LED3, 0);
  lightStatus = "OFF";
}

// -- BlueTooth

var command = "";

function btHandler(e) {
  command += e.data;

  if (e.data == " ") {
    command = "";
  } else {    
    // handle event
    if (command == "red_on") {
      turnRedLightOn();
    } else if (command == "red_off") {
      turnRedLightOff();
    } else if (command == "green_on") {
      turnGreenLightOn();
    } else if (command == "green_off") {
      turnGreenLightOff();
    } else if (command == "blue_on") {
      turnBlueLightOn();
    } else if (command == "blue_off") {
      turnBlueLightOff(); 
    }
  }
}

Serial1.onData(btHandler);


// -- Web Server

var ACCESS_POINT_NAME = "";
var WPA2_KEY = "";
var PORT_NUMBER = 80;

var lightStatus = "OFF";

function webHandler(req, res) {

  // handle request

  // we can use url.parse(req.url) to do more complicated things
  if (req.url == "/on") {
    turnBlueLightOn();
  } else if (req.url == "/off") {
    turnBlueLightOff();
  }

  // write response header
  res.writeHead(200 /* OK */, 
                {'Content-Type': 'application/json'} /* send back JSON */
               );

  // write response body
  var responseBody = { success: 1, 
                       message: "You turned the light " + lightStatus
                     };  
  responseBody = JSON.stringify(responseBody);
  res.write(responseBody);

  res.end();
}

var wlan;
/**
 * This function should turn on the red light, green light, and blue light in 
 * succession if the web serever is initialized successfully.
 */
function webServerInit() {

  digitalWrite(LED1, 1);
  
  // load CC3000 module and get an instance of a WiFi network adapter
  wlan = require("CC3000").connect();

  digitalWrite(LED2, 1);
     
  // connect to wireless network
  wlan.connect(ACCESS_POINT_NAME, WPA2_KEY, function(status) {
    if (status == "dhcp") {

      // print IP address to console
      console.log("IP address: " + wlan.getIP().ip);

      // create web server
      require("http").createServer(webHandler).listen(PORT_NUMBER);
      
      digitalWrite(LED3, 1);
      
      // turn off all lights
      setTimeout(function(e) {
        digitalWrite(LED1, 0);
      	digitalWrite(LED2, 0);
        digitalWrite(LED3, 0);
      }, 5000);
      
    }
  });
}

/**
 * Special function called automatically when Espruino is powered on.
 */
function onInit() {
  webServerInit();
}
