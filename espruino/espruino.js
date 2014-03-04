// -- Globals

var ACCESS_POINT_NAME = "";
var WPA2_KEY = "";
var PORT_NUMBER = 8080;

// This flag gets set if init was successful
var gInit = 0;


// -- LED functionality

var gLED = function() {
  var mRed = 0;
  var mGreen = 0;
  var mBlue = 0;
  
  var toggleRed = function() {
    mRed = !mRed;
    digitalWrite(LED1, mRed);
  };
  
  var toggleGreen = function() {
    mGreen = !mGreen;
    digitalWrite(LED2, mGreen);
  };
  
  var toggleBlue = function() {
    mBlue = !mBlue;
    digitalWrite(LED3, mBlue);
  };
  
  var clear = function() {
    mRed = mGreen = mBlue = 0;
    digitalWrite(LED1, mRed);
    digitalWrite(LED2, mGreen);
    digitalWrite(LED3, mBlue);
  };
  
  var pulseRed = function() {
    toggleRed();

    setTimeout(function() {
      gLED.toggleRed();
    }, 1000);
  };
  
  var pulseGreen = function() {
    toggleGreen();

    setTimeout(function() {
      gLED.toggleGreen();
    }, 1000);
  };
  
  var pulseBlue = function() {
    toggleBlue();

    setTimeout(function() {
      gLED.toggleBlue();
    }, 1000);
  };
  
  return {
    toggleRed: toggleRed,
    toggleGreen: toggleGreen,
    toggleBlue: toggleBlue,
    clear: clear,
    pulseRed: pulseRed,
    pulseGreen: pulseGreen,
    pulseBlue: pulseBlue
  }
}();


// -- Outlet functionality

var gOutlets = function() {
  return {
    set0: function(enable) {
      digitalWrite(B14, enable);
    },
    set1: function(enable) {
      digitalWrite(A1, enable);
    },
    set2: function(enable) {
      digitalWrite(B13, enable);
    },
    set3: function(enable) {
      digitalWrite(A0, enable);
    },
    clear: function() {
      digitalWrite(A0, 1);
      digitalWrite(A1, 1);
      digitalWrite(B13, 1);
      digitalWrite(B14, 1);
    }
  }
}();


// -- Demo

var gDemo = function() {
  var mInterval;
  
  var go = function() {
    gOutlets.set0(0);
    
    setTimeout(function() {
      gOutlets.set0(1);
    }, 1000);
    
    setTimeout(function() {
      gOutlets.set1(0);
    }, 2000);
    
    setTimeout(function() {
      gOutlets.set1(1);
    }, 3000);

    setTimeout(function() {
      gOutlets.set2(0);
    }, 4000);

    setTimeout(function() {
      gOutlets.set2(1);
    }, 5000);

    setTimeout(function() {
      gOutlets.set3(0);
    }, 6000);

    setTimeout(function() {
      gOutlets.set3(1);
    }, 7000);

    setTimeout(function() {
      gOutlets.set0(0);
      gOutlets.set3(0);
    }, 8000);

    setTimeout(function() {
      gOutlets.set0(1);
      gOutlets.set3(1);
    }, 9000);

    setTimeout(function() {
      gOutlets.set1(0);
      gOutlets.set2(0);
    }, 10000);

    setTimeout(function() {
      gOutlets.set1(1);
      gOutlets.set2(1);
    }, 11000);

    setTimeout(function() {
      gOutlets.set0(0);
    }, 11250);

    setTimeout(function() {
      gOutlets.set1(0);
    }, 11500);

    setTimeout(function() {
      gOutlets.set3(0);
    }, 11750);

    setTimeout(function() {
      gOutlets.set2(0);
    }, 12000);

    setTimeout(function() {
      gOutlets.set0(1);
    }, 12250);

    setTimeout(function() {
      gOutlets.set1(1);
    }, 12500);

    setTimeout(function() {
      gOutlets.set3(1);
    }, 12750);

    setTimeout(function() {
      gOutlets.set2(1);
    }, 13000);

    setTimeout(function() {
      gOutlets.set0(0);
    }, 13250);

    setTimeout(function() {
      gOutlets.set1(0);
    }, 13500);

    setTimeout(function() {
      gOutlets.set3(0);
    }, 13750);

    setTimeout(function() {
      gOutlets.set2(0);
    }, 14000);

    setTimeout(function() {
      gOutlets.set0(1);
      gOutlets.set1(1);
      gOutlets.set2(1);
      gOutlets.set3(1);
    }, 14500);

    setTimeout(function() {
      gOutlets.set0(0);
      gOutlets.set1(0);
      gOutlets.set2(0);
      gOutlets.set3(0);
    }, 15000);

    setTimeout(function() {
      gOutlets.set0(1);
      gOutlets.set1(1);
      gOutlets.set2(1);
      gOutlets.set3(1);
    }, 15500);

    setTimeout(function() {
      gOutlets.set0(0);
      gOutlets.set1(0);
      gOutlets.set2(0);
      gOutlets.set3(0);
    }, 16000);

    setTimeout(function() {
      gOutlets.set1(1);
    }, 16250);

    setTimeout(function() {
      gOutlets.set0(1);
    }, 16500);

    setTimeout(function() {
      gOutlets.set2(1);
    }, 16750);

    setTimeout(function() {
      gOutlets.set3(1);
    }, 17000);

    setTimeout(function() {
      gOutlets.set1(0);
    }, 17250);

    setTimeout(function() {
      gOutlets.set0(0);
    }, 17500);

    setTimeout(function() {
      gOutlets.set2(0);
    }, 17750);

    setTimeout(function() {
      gOutlets.set3(0);
    }, 18000);

    setTimeout(function() {
      gOutlets.set1(1);
    }, 18250);

    setTimeout(function() {
      gOutlets.set0(1);
    }, 18500);

    setTimeout(function() {
      gOutlets.set2(1);
    }, 18750);

    setTimeout(function() {
      gOutlets.set3(1);
    }, 19000);
    
    setTimeout(function() {
      var toggle = 1;

      mInterval = setInterval(function() {
        toggle = !toggle;
        gOutlets.set0(toggle);
        gOutlets.set1(toggle);
        gOutlets.set2(toggle);
        gOutlets.set3(toggle);
      }, 200);
    }, 19250);
  };
  
  var stop = function() {
    clearInterval(mInterval);
  };
  
  return {
    go:go,
    stop:stop
  }
}();


// -- BlueTooth (TODO: update this)

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

var lightStatus = "OFF";

function webHandler(req, res) {

  // handle request

  // we can use url.parse(req.url) to do more complicated things
  if (req.url == "/0/on") {
    gOutlets.set0(0);
    gLED.pulseGreen();
  } else if (req.url == "/0/off") {
    gOutlets.set0(1);
    gLED.pulseGreen();
  } else if (req.url == "/1/on") {
    gOutlets.set1(0);
    gLED.pulseGreen();
  } else if (req.url == "/1/off") {
    gOutlets.set1(1);
    gLED.pulseGreen();
  } else if (req.url == "/2/on") {
    gOutlets.set2(0);
    gLED.pulseGreen();
  } else if (req.url == "/2/off") {
    gOutlets.set2(1);
    gLED.pulseGreen();
  } else if (req.url == "/3/on") {
    gOutlets.set3(0);
    gLED.pulseGreen();
  } else if (req.url == "/3/off") {
    gOutlets.set3(1);
    gLED.pulseGreen();
  } else if (req.url == "/go") {
    gDemo.go();
    gLED.pulseGreen();
  } else if (req.url == "/stop") {
    gDemo.stop();
    gLED.pulseGreen();
  } else {
    gLED.pulseRed();
  }

  // write response header
  res.writeHead(200 /* OK */, 
                {'Content-Type': 'application/json'} /* send back JSON */
               );

  // write response body
  var responseBody = { success: 1, 
                       message: "Success! You did " + req.url
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

  gLED.toggleRed();
  
  // load CC3000 module and get an instance of a WiFi network adapter
  wlan = require("CC3000").connect();

  gLED.toggleGreen();
     
  // connect to wireless network
  wlan.connect(ACCESS_POINT_NAME, WPA2_KEY, function(status) {
    if (status == "dhcp") {

      // print IP address to console
      console.log("IP address: " + wlan.getIP().ip);

      // create web server
      require("http").createServer(webHandler).listen(PORT_NUMBER);
      
      gLED.toggleBlue();
      
      // turn off all lights
      setTimeout(function(e) {
        gLED.toggleRed();
        gLED.toggleGreen();
        gLED.toggleBlue();
      }, 2000);

      gInit = 1;      
    }
  });
}

/**
 * Special function called automatically when Espruino is powered on.
 */
function onInit() {
  gOutlets.clear();
  gLED.clear();
  webServerInit();

  // This function checks afterwards if init was successful
  setTimeout(function() {
    if (gInit !== 0) {
      setInterval(function() {
        gLED.toggleBlue();
      }, 1000);
    } else {
      setInterval(function() {
        gLED.toggleRed();
      }, 1000);
    }
  }, 4000);
}
