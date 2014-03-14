// -- Hardware Unit Object

var hardwareUnit = {
  
  sockets: [
    
    // socket0 "NW"
    {
      name: "socket0",
      pin: B14,
      state: 0,
    },

    // socket1 "NE"  
    {
      name: "socket1",
      pin: A1,
      state: 0,
    },

    // socket2 "SW"
    {
      name: "socket2",
      pin: B13,
      state: 0,
    },

    // socket3 "SE"
    {
      name: "socket3"
      pin: A0,
      state: 0,
    }

  ]
};

function toggleSocket(socketNumber, state) {
  var pin = hardwareUnit.sockets[socketNumber].pin;
  digitalWrite(pin, state);
  hardwareUnit.sockets[socketNumber].state = state;
}


// -- Web Server

var ACCESS_POINT_NAME = "";
var WPA2_KEY = "";
var PORT_NUMBER = 80;

var wlan;  // WiFi network adapter
var isWebServerSetUp = false;

/**
 * Expects urls of the form:
 *   http://host:port/hack/on?socket=n
 *   http://host:port/hack/off?socket=n
 */
function webHandler(req, res) {

  // handle request

  /* url.parse returns an object containing properties method, host, path,
   * pathname, search, port, and query */
  var urlObj = url.parse(req.url, true);
  
  var pathname = urlObj.pathname;
  var socketNumber = parseInt(urlObj.query.socket, 10);
  var state;

  if (pathname == "/hack/on") {
    state = 1;
  } else if (pathname == "/hack/off") {
    state = 0;
  }

  toggleSocket(socketNumber, state);

  // write response header
  res.writeHead(200 /* OK */, 
                {'Content-Type': 'application/json'} /* send back JSON */
               );

  // write response body  
  var responseBody = JSON.stringify(hardwareUnit.sockets[socketNumber]);
  res.write(responseBody);

  res.end();
}


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
      // console.log("IP address: " + wlan.getIP().ip);

      // create web server
      require("http").createServer(webHandler).listen(PORT_NUMBER);
      
      digitalWrite(LED3, 1);
      
      // turn off all lights
      setTimeout(function(e) {
        digitalWrite(LED1, 0);
        digitalWrite(LED2, 0);
        digitalWrite(LED3, 0);
      }, 3000);
      
    }
  });
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
    if (e.data == " ") {
      command = "";
    } else {
      command += e.data;
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
        isWebServerSetUp = true;
        save();
        command = "";
      }
        }
  } else if (btMode == 1) {  //  receive ACCESS_POINT_NAME
    if (e.data == "|") {
      ACCESS_POINT_NAME = command;
      digitalWrite(LED1, 1);
      command = "";
      btMode = 0;
    } else {
      command += e.data;
    }
  } else if (btMode == 2) {  //  receive WPA2_KEY 
    if (e.data == "|") {
      WPA2_KEY = command;
      digitalWrite(LED2, 1);
      command = "";
      btMode = 0;
    } else {
      command += e.data;
    }
  } else if (btMode == 3) {  // receive PORT NUMBER
    if (e.data == "|") {
      PORT_NUMBER = command;
      digitalWrite(LED3, 1);
      command = "";
      btMode = 0;
      setTimeout(function(e) {
        digitalWrite(LED1, 0);
        digitalWrite(LED2, 0);
        digitalWrite(LED3, 0);
      }, 50);
    } else {
      command += e.data;
    }
  }
}


// -- Util

/**
 * Special function called automatically when Espruino is powered on.
 */
function onInit() {
  if (isWebServerSetUp) {
    webServerInit();
  }
}

/**
 * When a character is received on Serial1, the function supplied to onData
 * (btHandler) gets called.
 */
Serial1.onData(btHandler);