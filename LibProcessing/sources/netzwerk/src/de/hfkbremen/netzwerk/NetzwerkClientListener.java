package de.hfkbremen.netzwerk;

import oscP5.OscMessage;

public interface NetzwerkClientListener {

    void receive(String name, String tag, float x);

    void receive(String name, String tag, float x, float y);

    void receive(String name, String tag, float x, float y, float z);

    void receive(String name, String tag, String message);

    void receive_raw(OscMessage m);

    void receive_ping();
}
