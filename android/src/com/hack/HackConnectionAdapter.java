package com.hack;

public abstract class HackConnectionAdapter {

    abstract public String submitRequest(HackCommand command);

    public String fail(String msg) {
        return "{'success': 0, 'data': {}, 'message': '" + msg + "'}";
    }

}
