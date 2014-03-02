package com.hack;

import android.provider.BaseColumns;

public class HackDbContract {
    
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public HackDbContract() {}

    /* Inner classes that define the table contents */
    
    public static abstract class HackHardwareUnits implements BaseColumns {
        public static final String TABLE_NAME = "hardwareUnits";
        public static final String COLUMN_NAME_HARDWARE_UNIT_NAME = "hardwareUnitName";
        public static final String COLUMN_NAME_HARDWARE_UNIT_BASE_PATH = "hardwareBasePath";
        public static final String COLUMN_NAME_HARDWARE_UNIT_PORT_NUMBER = "hardwarePortNumber";
    }
    
    public static abstract class HackSockets implements BaseColumns {
        public static final String TABLE_NAME = "sockets";
        public static final String COLUMN_NAME_SOCKET_DESCRIPTION = "socketDescription";
    }
    
    public static abstract class HackDevices implements BaseColumns {
        public static final String TABLE_NAME = "devices";
        public static final String COLUMN_NAME_HARDWARE_UNIT_ID = "hardwareUnitId";
        public static final String COLUMN_NAME_SOCKET_ID = "socketId";
        public static final String COLUMN_NAME_DEVICE_NAME = "deviceName";
        public static final String COLUMN_NAME_DEVICE_STATE = "deviceState";
        
    }

}
