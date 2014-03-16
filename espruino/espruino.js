// -- Global Object

var settings = {
  wifi: {
    wlan: {}, // WiFi network adapter
    status: "",
    ssid: "",
    wpa2key: "",
    port: 80,
    server: {},
  },

  bluetooth: {
    LISTEN: 0,
    PARSE_CMD: 1,
    SET_APN: 2,
    SET_WPA: 3,
    SET_PORT: 4,
    state: 0,
    current: "",
  },
};


// -- LED functions

/**
 * Turns off all the LEDs.
 */
function resetLEDs() {
  clearInterval(); // This clears all intervals when no argument
  digitalWrite(LED1, 0);
  digitalWrite(LED2, 0);
  digitalWrite(LED3, 0);
}

/**
 * Repeatedly flash red LED
 */

function repeatRed() {
  var enable = 0;

  setInterval(function() {
    enable = !enable;
    digitalWrite(LED1, enable);
  }, 1000);
}

/**
 * Repeatedly flash green LED
 */

function repeatGreen() {
  var enable = 0;

  setInterval(function() {
    enable = !enable;
    digitalWrite(LED2, enable);
  }, 1000);
}

/**
 * Repeatedly flash blue LED
 */

function repeatBlue() {
  var enable = 0;

  setInterval(function() {
    enable = !enable;
    digitalWrite(LED3, enable);
  }, 1000);
}

/**
 * Pulse red LED
 */

function pulseRed() {
  digitalWrite(LED1, 1);

  setTimeout(function() {
    digitalWrite(LED1, 0);
  }, 750);
}

/**
 * Pulse green LED
 */

function pulseGreen() {
  digitalWrite(LED2, 1);

  setTimeout(function() {
    digitalWrite(LED2, 0);
  }, 750);
}

/**
 * Flash red LED
 */

function flashRed() {
  digitalWrite(LED1, 1);

  setTimeout(function() {
    digitalWrite(LED1, 0);
  }, 100);
}

/**
 * Flash green LED
 */

function flashGreen() {
  digitalWrite(LED2, 1);

  setTimeout(function() {
    digitalWrite(LED2, 0);
  }, 100);
}


// -- Util

function safeSave() {
  // Reset wifi hardware
  if (settings.wifi.status !== "") {
    settings.wifi.wlan.disconnect();
  }

  // Reset LEDs
  resetLEDs();

  save();
}


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

  flashGreen();

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
  resetLEDs();
  digitalWrite(LED1, 1);

  settings.wifi.wlan = require("CC3000").connect();
  digitalWrite(LED2, 1);
     
  // connect to wireless network
  settings.wifi.wlan.connect(settings.wifi.ssid, settings.wifi.wpa2key, function(status) {
    if (status == "dhcp") {
      var ip = settings.wifi.wlan.getIP();
      console.log("Got dhcp");

      if (typeof ip === "object") {
        // print IP address to console
        console.log("IP address: " + ip.ip);
        digitalWrite(LED3, 1);
      } else {
        console.log("I don't have an IP address");

        // Indicate bad state
        resetLEDs();
        repeatRed();

        // Override normal status behaviour
        settings.wifi.status = "expired";
        return;
      }

      // create web server
      settings.wifi.server = require("http").createServer(webHandler).listen(settings.wifi.port);

      // Indicate good state
      resetLEDs();
      repeatBlue();
    } else if (status == "disconnect" && settings.wifi.status == "dhcp") {
      console.log("Got disconnect");
      console.log("I lost my connection");

      // Indicate bad state
      resetLEDs();
      repeatRed();
      settings.wifi.status = status;
    } else if (status == "disconnect" && settings.wifi.status != "expired") {
      console.log("Got disconnect");
      console.log("I lost my connection in a different way");

      // Indicate bad state
      resetLEDs();
      repeatRed();
      settings.wifi.status = status;
    } else if (status == "connect") {
      console.log("Got connect");
    } else {
      console.log("Got post-expire disconnect");
    }
  });
}


// -- Bluetooth

/* Changes the behaviour of the btHandler()
 *   LISTEN - receive command
 *   SET_APN - receive ACCESS_POINT_NAME
 *   SET_WPA - receive WPA2_KEY
 *   SET_PORT - receive PORT_NUMBER
 */

function btHandler(e) {
  if (settings.bluetooth.state == settings.bluetooth.LISTEN) {  // receive command
    if (e.data == " ") {
      settings.bluetooth.current = "";
    } else {
      settings.bluetooth.current = settings.bluetooth.current + e.data;
      // handle event
      if (settings.bluetooth.current == "_toggle") {
        settings.bluetooth.state = settings.bluetooth.PARSE_CMD;
        settings.bluetooth.current = "";
      } else if (settings.bluetooth.current == "_set_apn") {
        settings.bluetooth.state = settings.bluetooth.SET_APN;
        settings.bluetooth.current = "";
      } else if (settings.bluetooth.current == "_set_wpa") {
        settings.bluetooth.state = settings.bluetooth.SET_WPA;
        settings.bluetooth.current = "";
      } else if (settings.bluetooth.current == "_set_port") {
        settings.bluetooth.state = settings.bluetooth.SET_PORT;
        settings.bluetooth.current = "";
      } else if (settings.bluetooth.current == "_save") {
        settings.bluetooth.current = "";
        safeSave();
      }

    }
  } else if (settings.bluetooth.state == settings.bluetooth.PARSE_CMD) {
    if (e.data == " ") {
      var btObj = url.parse(settings.bluetooth.current, true);
      doCommand(btObj);
      settings.bluetooth.current = "";
      settings.bluetooth.state = settings.bluetooth.LISTEN;
    } else {
      settings.bluetooth.current += e.data;
    }
  } else if (settings.bluetooth.state == settings.bluetooth.SET_APN) {  //  receive ACCESS_POINT_NAME
    if (e.data == "|") {
      settings.wifi.ssid = settings.bluetooth.current;
      settings.bluetooth.current = "";
      settings.bluetooth.state = settings.bluetooth.LISTEN;
    } else {
      settings.bluetooth.current += e.data;
    }
  } else if (settings.bluetooth.state == settings.bluetooth.SET_WPA) {  //  receive WPA2_KEY
    if (e.data == "|") {
      settings.wifi.wpa2key = settings.bluetooth.current;
      settings.bluetooth.current = "";
      settings.bluetooth.state = settings.bluetooth.LISTEN;
    } else {
      settings.bluetooth.current += e.data;
    }
  } else if (settings.bluetooth.state == settings.bluetooth.SET_PORT) {  // receive PORT NUMBER
    if (e.data == "|") {
      settings.wifi.port = settings.bluetooth.current;
      settings.bluetooth.current = "";
      settings.bluetooth.state = settings.bluetooth.LISTEN;
    } else {
      settings.bluetooth.current += e.data;
    }
  }
}


// -- Init

/**
 * Special function called automatically when Espruino is powered on.
 */
function onInit() {
  // Always want to try the webserver
  webServerInit();
}

/**
 * When a character is received on Serial1, the function supplied to onData
 * (btHandler) gets called.
 */
Serial1.onData(btHandler);
