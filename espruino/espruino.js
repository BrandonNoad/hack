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
  if (typeof settings.LEDs === "object") {
    if (settings.LEDs["red"].interval !== null) {
      clearInterval(settings.LEDs["red"].interval);
    }
    if (settings.LEDs["green"].interval !== null) {
      clearInterval(settings.LEDs["green"].interval);
    }
    if (settings.LEDs["blue"].interval !== null) {
      clearInterval(settings.LEDs["blue"].interval);
    }
  }

  digitalWrite(LED1, 0);
  digitalWrite(LED2, 0);
  digitalWrite(LED3, 0);

  settings.LEDs = {};
  settings.LEDs["red"] = {
    interval: null,
    enable: 0,
    pin: LED1,
  };

  settings.LEDs["green"] = {
    interval: null,
    enable: 0,
    pin: LED2,
  };

  settings.LEDs["blue"] = {
    interval: null,
    enable: 0,
    pin: LED3,
  };
}

/**
 * Repeatedly flash an LED
 */

function repeatLED(colour) {
  if (typeof settings.LEDs[colour] === "object") {
    settings.LEDs[colour].interval = setInterval(function() {
      settings.LEDs[colour].enable = !settings.LEDs[colour].enable;
      digitalWrite(settings.LEDs[colour].pin, settings.LEDs[colour].enable);
    }, 1000);
  }
}

/**
 * Pulse an LED
 */

function pulseLED(colour, time) {
  if (typeof settings.LEDs[colour] === "object") {
    digitalWrite(settings.LEDs[colour].pin, 1);

    setTimeout(function() {
      digitalWrite(settings.LEDs[colour].pin, 0);
    }, time);
  }
}


// -- Outlet Object
// Note: Electric relays use reverse logic of LEDs (0 is on, 1 is off)
var hardwareUnit = {
  
  outlets: [
    
    // outlet0 "NW"
    {
      name: "outlet0",
      pin: B14,
      state: 1,
    },

    // outlet1 "NE"  
    {
      name: "outlet1",
      pin: A1,
      state: 1,
    },

    // outlet2 "SW"
    {
      name: "outlet2",
      pin: B13,
      state: 1,
    },

    // outlet3 "SE"
    {
      name: "outlet3",
      pin: A0,
      state: 1,
    }

  ]
};

function setOutlet(outletNumber, state) {
  var pin = hardwareUnit.outlets[outletNumber].pin;
  digitalWrite(pin, state);
  hardwareUnit.outlets[outletNumber].state = state;
}

function resetOutlets() {
  for (var i = 0; i < 4; i++) {
    hardwareUnit.outlets[i].state = 1;
    setOutlet(i, 1);
  }
}

/**
 * 
 */
function doCommand(obj) {

  pulseLED("green", 100);

  var pathname = obj.pathname;
  var socketNumber = parseInt(obj.query.socket, 10);
  var state;

  if (pathname == "/hack/on") {
    state = 0;
  } else if (pathname == "/hack/off") {
    state = 1;
  }

  setOutlet(socketNumber, state);

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
  var responseBody = JSON.stringify(hardwareUnit);
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
        repeatLED("red");

        // Override normal status behaviour
        settings.wifi.status = "expired";
        return;
      }

      // create web server
      settings.wifi.server = require("http").createServer(webHandler).listen(settings.wifi.port);

      // Indicate good state
      resetLEDs();
      repeatLED("blue");
    } else if (status == "disconnect" && settings.wifi.status == "dhcp") {
      console.log("Got disconnect");
      console.log("I lost my connection");

      // Indicate bad state
      resetLEDs();
      repeatLED("red");
      settings.wifi.status = status;
    } else if (status == "disconnect" && settings.wifi.status != "expired") {
      console.log("Got disconnect");
      console.log("I lost my connection in a different way");

      // Indicate bad state
      resetLEDs();
      repeatLED("red");
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
 * Wraps the built-in save to reset everything nicely before overwriting memory.
 * This way an onInit() after save should behave more like onInit() after power cycle.
 */
function safeSave() {
  // Reset wifi hardware
  if (settings.wifi.status !== "") {
    settings.wifi.wlan.disconnect();
  }

  // Reset LEDs
  resetLEDs();

  // Reset outlets
  resetOutlets();

  save();
}

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
