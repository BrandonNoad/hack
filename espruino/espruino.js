// -- Web Server

var ACCESS_POINT_NAME = "";
var WPA2_KEY = "";
var PORT_NUMBER = 80;

var isWebServerSetUp = false;
var lightStatus = "OFF";

function webHandler(req, res) {

  // handle request

  // we can use url.parse(req.url) to do more complicated things
  if (req.url == "/on") {
    digitalWrite(LED3, 1);
  } else if (req.url == "/off") {
    digitalWrite(LED3, 0);
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
  if (isWebServerSetUp) {
    webServerInit();
  }
}

// -- Bluetooth

var command = "";

/* Changes the behaviour of the btHandler()
 *   0 - receive command
 *   1 - receive ACCESS_POINT_NAME
 *   2 - receive WPA2_KEY
 *   3 - receive PORT_NUMBER
 */
var btMode = 0;

function btHandler(e) {
  if (btMode == 0) {  // receive command
    print("mode 0\n");
    if (e.data == " ") {
      command = "";
    } else {
      command += e.data;
      print("command: " + command + "\n");
      // handle event
      if (command == "red_on") {
        digitalWrite(LED1, 1);
        command = "";
      } else if (command == "red_off") {
        digitalWrite(LED1, 0);
        command = "";
      } else if (command == "set_ap") {
        btMode = 1;
        command = "";
      } else if (command == "set_wpa") {
        btMode = 2;  
        command = "";
      } else if (command == "set_port") {
        btMode = 3;
        command = "";
      } else if (command == "save") {
        print("Access Point: " + ACCESS_POINT_NAME + ", WPA2 key: " + WPA2_KEY + ", Port Number: " + PORT_NUMBER + "\n");
        isWebServerSetUp = true;
        save();
        command = "";
      }
        }
  } else if (btMode == 1) {  //  receive ACCESS_POINT_NAME
    print("mode 1\n");
    if (e.data == "|") {
      ACCESS_POINT_NAME = command;
      digitalWrite(LED1, 1);
      command = "";
      btMode = 0;
    } else {
      command += e.data;
      print("command: " + command + "\n");
    }
  } else if (btMode == 2) {  //  receive WPA2_KEY 
    print("mode 2\n");
    if (e.data == "|") {
      WPA2_KEY = command;
      digitalWrite(LED2, 1);
      command = "";
      btMode = 0;
    } else {
      command += e.data;
      print("command: " + command + "\n");
    }
  } else if (btMode == 3) {  // receive PORT NUMBER
    print("mode 3\n");
    if (e.data == "|") {
      PORT_NUMBER = command;
      digitalWrite(LED3, 1);
      command = "";
      btMode = 0;
      setTimeout(function(e) {
        digitalWrite(LED1, 0);
        digitalWrite(LED2, 0);
        digitalWrite(LED3, 0);
      }, 500);
    } else {
      command += e.data;
      print("command: " + command + "\n");
    }
  }
}

Serial1.onData(btHandler);