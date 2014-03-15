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
      name: "socket3",
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

/**
 * 
 */
function doCommand(obj) {
  var pathname = obj.pathname;
  var socketNumber = parseInt(obj.query.socket, 10);
  var state;

  if (pathname == "/hack/on") {
    state = 1;
  } else if (pathname == "/hack/off") {
    state = 0;
  }

  toggleSocket(socketNumber, state);

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
  
  doCommand(urlObj);

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

var LISTEN = 0;
var PARSE_CMD = 1;
var SET_APN = 2;
var SET_WPA = 3;
var SET_PORT = 4;

/* Changes the behaviour of the btHandler()
 *   LISTEN - receive command
 *   SET_APN - receive ACCESS_POINT_NAME
 *   SET_WPA - receive WPA2_KEY
 *   SET_PORT - receive PORT_NUMBER
 */
var btMode = LISTEN;

function btHandler(e) {
  if (btMode == LISTEN) {  // receive command
    if (e.data == " ") {
      command = "";
    } else {
      command += e.data;
      // handle event
      if (command == "_toggle") {
        btMode = PARSE_CMD;
        command = "";
      } else if (command == "_set_ap") {
        btMode = SET_APN;
        command = "";
      } else if (command == "_set_wpa") {
        btMode = SET_WPA;  
        command = "";
      } else if (command == "_set_port") {
        btMode = SET_PORT;
        command = "";
      } else if (command == "_save") {
        isWebServerSetUp = true;
        save();
        command = "";
      }
    }
  } else if (btMode == PARSE_CMD) {
    if (e.data == " ") {
      var btObj = url.parse(command, true);
      doCommand(btObj);
      command = "";
      btMode = LISTEN;
    } else {
      command += e.data;
    }
  } else if (btMode == SET_WPA) {  //  receive ACCESS_POINT_NAME
    if (e.data == "|") {
      ACCESS_POINT_NAME = command;
      command = "";
      btMode = LISTEN;
    } else {
      command += e.data;
    }
  } else if (btMode == SET_WPA) {  //  receive WPA2_KEY 
    if (e.data == "|") {
      WPA2_KEY = command;
      command = "";
      btMode = LISTEN;
    } else {
      command += e.data;
    }
  } else if (btMode == SET_PORT) {  // receive PORT NUMBER
    if (e.data == "|") {
      PORT_NUMBER = command;
      command = "";
      btMode = LISTEN;
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

/**
 * Turns on all the LEDs, then turns them off.
 */
function cascadeLEDs() {
  digitalWrite(LED1, 1);
  digitalWrite(LED2, 1);
  digitalWrite(LED3, 1);
  digitalWrite(LED1, 0);
  digitalWrite(LED2, 0);
  digitalWrite(LED3, 0);
}