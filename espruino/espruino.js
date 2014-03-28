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
    states: {
      LISTEN: 0,
      VERIFY: 1,
      RECEIVING: 2,
    },
    currentState: 0,
    currentRecord: "",
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

  currentTime: 0,
  
  outlets: [
    
    // outlet0 "NW"
    {
      name: "outlet0",
      pin: B14,
      state: 1,
      totalTimeOn: 0,
      onSinceTime: -1,
      lastTimeUpdated: 0,
      isTimer: false
    },

    // outlet1 "NE"  
    {
      name: "outlet1",
      pin: A1,
      state: 1,
      totalTimeOn: 0,
      onSinceTime: -1,
      lastTimeUpdated: 0,
      isTimer: false
    },

    // outlet2 "SW"
    {
      name: "outlet2",
      pin: B13,
      state: 1,
      totalTimeOn: 0,
      onSinceTime: -1,
      lastTimeUpdated: 0,
      isTimer: false
    },

    // outlet3 "SE"
    {
      name: "outlet3",
      pin: A0,
      state: 1,
      totalTimeOn: 0,
      onSinceTime: -1,
      lastTimeUpdated: 0,
      isTimer: false
    }

  ]
};

function setOutlet(outletNumber, state) {
  var pin = hardwareUnit.outlets[outletNumber].pin;
  var oldState = hardwareUnit.outlets[outletNumber].state;
  var now = getTime();
  
  if (oldState !== state) {
    
    digitalWrite(pin, state);
    hardwareUnit.outlets[outletNumber].state = state;
    
    if (state === 0) {  // turn on from off
      hardwareUnit.outlets[outletNumber].onSinceTime = now;
      hardwareUnit.outlets[outletNumber].lastTimeUpdated = now;
    } else {  // turn off from on
      hardwareUnit.outlets[outletNumber].onSinceTime = -1;
      hardwareUnit.outlets[outletNumber].totalTimeOn += (now - hardwareUnit.outlets[outletNumber].lastTimeUpdated);
      hardwareUnit.outlets[outletNumber].lastTimeUpdated = now;
    }

  } else {
    
    if (state === 0) {  // turn on from on
      hardwareUnit.outlets[outletNumber].totalTimeOn += (now - hardwareUnit.outlets[outletNumber].lastTimeUpdated);
      hardwareUnit.outlets[outletNumber].lastTimeUpdated = now;
    } else {  // turn off from off
      hardwareUnit.outlets[outletNumber].lastTimeUpdated = now;
    }

  }
}

function resetAllOutlets() {
  for (var i = 0; i < 4; i++) {
    hardwareUnit.outlets[i].state = 1;
    setOutlet(i, 1);
    hardwareUnit.outlets[i].totalTimeOn = 0;
    hardwareUnit.outlets[i].onSinceTime = -1;
    hardwareUnit.outlets[i].lastTimeUpdated = 0;
    hardwareUnit.outlets[i].isTimer = false;
  }
}

function resetOutlet(outletNumber) {
    hardwareUnit.outlets[outletNumber].state = 1;
    setOutlet(outletNumber, 1);
    hardwareUnit.outlets[outletNumber].totalTimeOn = 0;
    hardwareUnit.outlets[outletNumber].onSinceTime = -1;
    hardwareUnit.outlets[outletNumber].lastTimeUpdated = 0;
    hardwareUnit.outlets[outletNumber].isTimer = false;
}

function refreshAllOutlets() {
  for (var i = 0; i < 4; i++) {
    var state = hardwareUnit.outlets[i].state;
    var now = getTime();
    if (state === 0) {  // on?
      hardwareUnit.outlets[i].totalTimeOn += (now - hardwareUnit.outlets[i].lastTimeUpdated);
    }    
    hardwareUnit.outlets[i].lastTimeUpdated = now;
  }  
}

function setTimer(outletNumber) {
  hardwareUnit.outlets[outletNumber].isTimer = true;
}


// -- Command Object

var commandDict;

/**
 * Define commands here
 */

function initCommands() {
  commandDict = {};

  commandDict["on"] = function(args) {
    setOutlet(parseInt(args.socket, 10), 0);
  };

  commandDict["off"] = function(args) {
    setOutlet(parseInt(args.socket, 10), 1);
  };

  commandDict["refresh"] = function(args) {
    refreshAllOutlets();
  };

  commandDict["delete"] = function(args) {
    resetOutlet(parseInt(args.socket, 10));
  };

  commandDict["setTimer"] = function(args) {
    setTimer(parseInt(args.socket, 10));
  };

  commandDict["sync"] = function(args) {
    settings.wifi.ssid = args["ap"];
    settings.wifi.wpa2key = args["key"];
    settings.wifi.port = args["port"];
    safeSave();
  };
}

/**
 * Receive commands and invoke their callback
 */

function doCommand(obj) {
  var pathList = obj.pathname.split("/");
  var lastItem = "";
  var command = "";
  var args = obj.query;

  if (pathList.length > 2) {
    // Use "2" because leading "/" creates a blank first element
    command = pathList[2];
  } else {
    pulseLED("red", 100);
    console.log("doCommand got blank command");
    return;
  }

  if (typeof commandDict[command] === "function") {
    pulseLED("green", 100);
    console.log("doCommand recognizes " + command);
    commandDict[command](args);
  } else {
    pulseLED("red", 100);
    console.log("doCommand discards " + command);
  }

  // update current time 
  hardwareUnit.currentTime = getTime();
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
  
  var result = doCommand(urlObj);

  // write response header
  res.writeHead(200 /* OK */, 
                {'Content-Type': 'application/json'} /* send back JSON */
               );

  // write response body  
  var response = {
    "success": 0,
    "data": {},
    "message": "Error. Invalid command. Please try again."
  };

  if (result) {
    response["success"] = 1;
    response["data"] = hardwareUnit;
    response["message"] = "Success!";
  }

  var responseBody = JSON.stringify(response);
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

  // skip extra connect if blank credentials
  if (settings.wifi.ssid === "") {
    console.log("Skipping 2nd wifi connect since no credentials");

    // Indicate bad wifi state
    resetLEDs();
    repeatLED("red");
    settings.wifi.status = "disconnect";
  }
     
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

        // Indicate bad wifi state
        resetLEDs();
        repeatLED("red");

        // Override normal status behaviour
        settings.wifi.status = "expired";
        return;
      }

      // create web server
      settings.wifi.server = require("http").createServer(webHandler).listen(settings.wifi.port);

      // Indicate good wifi state
      resetLEDs();
      repeatLED("blue");
    } else if (status == "disconnect" && settings.wifi.status == "dhcp") {
      console.log("Got disconnect");
      console.log("I lost my connection");

      // Indicate bad wifi state
      resetLEDs();
      repeatLED("red");
      settings.wifi.status = status;
    } else if (status == "disconnect" && settings.wifi.status != "expired") {
      console.log("Got disconnect");
      console.log("I lost my connection in a different way");

      // Indicate bad wifi state
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

function BluetoothRequest(s) {
  this.pathname = s.substring(0, s.indexOf("?"));
  this.query = {};
  var queryString = s.substring(s.indexOf("?") + 1, s.length);
  var queryDefs = queryString.split("&");

  for (var i = 0; i < queryDefs.length; i++) {
    var def = queryDefs[i];
    this.query[def.split("=")[0]] = def.split("=")[1];
  }
}

function btHandler(e) {
  if (settings.bluetooth.currentState == settings.bluetooth.states.LISTEN) {  // receive command
    if (e.data == " ") {
      console.log("Bluetooth got space, start recording (entering VERIFY)");

      settings.bluetooth.currentRecord = "";
      settings.bluetooth.currentState = settings.bluetooth.states.VERIFY;
    }
  } else if (settings.bluetooth.currentState == settings.bluetooth.states.VERIFY) {
    if (settings.bluetooth.currentRecord == "/hack/") {
      console.log("Bluetooth verified /hack/, commit recording entire command (entering RECORD)");

      settings.bluetooth.currentState = settings.bluetooth.states.RECEIVING;
    } else if (settings.bluetooth.currentRecord.length > 6) {
      console.log("Bluetooth verification failed, got <" + settings.bluetooth.currentRecord + "> instead (entering LISTEN)");

      settings.bluetooth.currentState = settings.bluetooth.states.LISTEN;
    }

    settings.bluetooth.currentRecord += e.data;
  } else if (settings.bluetooth.currentState == settings.bluetooth.states.RECEIVING) {
    if (e.data == "$") {
      console.log("Bluetooth got $, the full command is: " + settings.bluetooth.currentRecord + " (entering LISTEN)");

      var command = new BluetoothRequest(settings.bluetooth.currentRecord);
      doCommand(command);

      settings.bluetooth.currentState = settings.bluetooth.states.LISTEN;
    }

    settings.bluetooth.currentRecord += e.data;
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
  resetAllOutlets();

  save();
}

/**
 * Special function called automatically when Espruino is powered on.
 */
function onInit() {
  // Init commands
  if (typeof commandDict !== "object") {
    console.log("Writing out commands...");
    initCommands();
  }
  

  // Always want to try the webserver
  webServerInit();
}

/**
 * When a character is received on Serial1, the function supplied to onData
 * (btHandler) gets called.
 */
Serial1.onData(btHandler);
