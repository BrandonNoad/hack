package com.hack;

import android.provider.BaseColumns;

public class HackDbContract {

    /* To prevent someone from accidentally instantiating the contract class,
     * give it an empty constructor.*/
    public HackDbContract() {}

    /* Inner classes that define the table contents */

    /* All tables have an _ID column */

    // hardwareUnits Table
    public static abstract class HackHardwareUnits implements BaseColumns {
        public static final String TABLE_NAME = "hardwareUnits";
        public static final String COLUMN_NAME_HARDWARE_UNIT_NAME = "hardwareUnitName";
        public static final String COLUMN_NAME_HARDWARE_UNIT_BASE_PATH = "hardwareUnitBasePath";
        public static final String COLUMN_NAME_HARDWARE_UNIT_PORT_NUMBER = "hardwareUnitPortNumber";
        public static final String COLUMN_NAME_HARDWARE_UNIT_ACCESS_POINT_NAME = "hardwareUnitAccessPointName";
        public static final String COLUMN_NAME_HARDWARE_UNIT_WPA2_KEY = "hardwareUnitWpa2Key";
        public static final String COLUMN_NAME_HARDWARE_UNIT_BT_MAC = "hardwareUnitBtMac";
    }

    // sockets Table
    public static abstract class HackSockets implements BaseColumns {
        public static final String TABLE_NAME = "sockets";
        public static final String COLUMN_NAME_SOCKET_DESCRIPTION = "socketDescription";
    }

    // devices Table
    public static abstract class HackDevices implements BaseColumns {
        public static final String TABLE_NAME = "devices";
        public static final String COLUMN_NAME_HARDWARE_UNIT_ID = "hardwareUnitId";
        public static final String COLUMN_NAME_SOCKET_ID = "socketId";
        public static final String COLUMN_NAME_DEVICE_NAME = "deviceName";
        public static final String COLUMN_NAME_DEVICE_STATE = "deviceState";
        public static final String COLUMN_NAME_DEVICE_TYPE_ID = "deviceTypeId";
        public static final String COLUMN_NAME_DEVICE_TOTAL_TIME_ON = "deviceTotalTimeOn";
        public static final String COLUMN_NAME_DEVICE_ON_SINCE_TIME = "deviceOnSinceTime";
    }

    // deviceTypes Table
    public static abstract class HackDeviceTypes implements BaseColumns {
        public static final String TABLE_NAME = "deviceTypes";
        public static final String COLUMN_NAME_DEVICE_TYPE_NAME = "deviceTypeName";
    }

    // timers Table
    public static abstract class HackTimers implements BaseColumns {
        public static final String TABLE_NAME = "timers";
        public static final String COLUMN_NAME_DEVICE_ID = "deviceId";
        public static final String COLUMN_NAME_TIMER_TIME_ON = "timeOn";
        public static final String COLUMN_NAME_TIMER_TIME_OFF = "timeOff";
        public static final String COLUMN_NAME_TIMER_IS_REPEATED = "isRepeated";
    }

}
