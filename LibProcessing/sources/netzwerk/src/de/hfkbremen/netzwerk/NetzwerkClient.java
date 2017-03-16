package de.hfkbremen.netzwerk;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class NetzwerkClient {

    private static final String mReceiveMethod = "receive";
    private static final String mPingMethod = "ping";
    private static final int MIN_PORT_NUMBER = 12000;
    private static final String DELIMITER = "/";
    private static final String ANONYM = "anonymous";
    private static final String LOCALHOST = "127.0.0.1";
    public static boolean SHOW_LOG = true;
    private final OscP5 mOSC;
    private final NetAddress mBroadcastLocation;
    private final String mSenderName;
    private final Object mClientParent;
    private final NetzwerkClientListener mNetzwerkClientListener;
    private final String mIP;
    private final int mListeningPort;
    private Method mMethodReceive1f;
    private Method mMethodReceive2f;
    private Method mMethodReceive3f;
    private Method mMethodReceiveStr;
    private Method mMethodReceiveRaw;
    private Method mMethodReceivePing;

    private NetzwerkClient(Object pClientParent,
                           NetzwerkClientListener pNetzwerkClientListener,
                           String pServer,
                           String pSenderName,
                           int pServerListeningPort,
                           int pClientListeningPort) {
        mClientParent = pClientParent;
        mNetzwerkClientListener = pNetzwerkClientListener;
        mListeningPort = pClientListeningPort;

        mOSC = new OscP5(this, mListeningPort);
        mIP = mOSC.ip();
        log("+++", "client is @ " + mIP + " + sending on port " + mListeningPort);

        mBroadcastLocation = new NetAddress(pServer, pServerListeningPort);
        mSenderName = pSenderName;
        log("+++", "server is @ " + pServer + " + listening on port " + pServerListeningPort);
    }

    public NetzwerkClient(Object pClientParent, String pServer, String pSenderName) {
        this(pClientParent, pServer, pSenderName, Netzwerk.SERVER_DEFAULT_SERVER_LISTENING_PORT, findAvailablePort());
    }

    public NetzwerkClient(NetzwerkClientListener pNetzwerkClientListener, String pServer, String pSenderName) {
        this(pNetzwerkClientListener,
             pServer,
             pSenderName,
             Netzwerk.SERVER_DEFAULT_SERVER_LISTENING_PORT,
             findAvailablePort());
    }

    public NetzwerkClient(NetzwerkClientListener pNetzwerkClientListener,
                          String pServer,
                          String pSenderName,
                          int pServerListeningPort,
                          int pClientListeningPort) {
        this(null, pNetzwerkClientListener, pServer, pSenderName, pServerListeningPort, pClientListeningPort);
    }

    public NetzwerkClient(Object pClientParent,
                          String pServer,
                          String pSenderName,
                          int pServerListeningPort,
                          int pClientListeningPort) {
        this(pClientParent, null, pServer, pSenderName, pServerListeningPort, pClientListeningPort);

        try {
            mMethodReceive1f = mClientParent.getClass().getDeclaredMethod(mReceiveMethod,
                                                                          String.class,
                                                                          String.class,
                                                                          Float.TYPE);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            mMethodReceive2f = mClientParent.getClass().getDeclaredMethod(mReceiveMethod,
                                                                          String.class,
                                                                          String.class,
                                                                          Float.TYPE,
                                                                          Float.TYPE);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            mMethodReceive3f = mClientParent.getClass().getDeclaredMethod(mReceiveMethod,
                                                                          String.class,
                                                                          String.class,
                                                                          Float.TYPE,
                                                                          Float.TYPE,
                                                                          Float.TYPE);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            mMethodReceiveStr = mClientParent.getClass().getDeclaredMethod(mReceiveMethod,
                                                                           String.class,
                                                                           String.class,
                                                                           String.class);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            mMethodReceiveRaw = mClientParent.getClass().getDeclaredMethod(mReceiveMethod, OscMessage.class);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            mMethodReceivePing = mClientParent.getClass().getDeclaredMethod(mPingMethod);
        } catch (NoSuchMethodException ignored) {
        }

        prepareExitHandler();
        if (pClientParent instanceof PApplet) {
            PApplet p = (PApplet) pClientParent;
            p.registerMethod("dispose", this);
        }
    }

    private static int findAvailablePort() {
        int mPortTemp = MIN_PORT_NUMBER;
        while (!available(mPortTemp)) {
            mPortTemp++;
        }
        return mPortTemp;
    }

    public String ip() {
        return mIP;
    }

    public int port() {
        return mListeningPort;
    }

    private static boolean available(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException ignored) {
                }
            }
        }
        return false;
    }

    public void connect() {
        OscMessage m = new OscMessage(Netzwerk.SERVER_PATTERN_CONNECT);
        m.add(mListeningPort);
        // todo       // System.out.println("### also connect with a name `m.add(mSenderName`); so that IPs can be mapped to names.");
        mOSC.send(m, mBroadcastLocation);
    }

    public void disconnect() {
        OscMessage m = new OscMessage(Netzwerk.SERVER_PATTERN_DISCONNECT);
        m.add(mListeningPort);
        mOSC.send(m, mBroadcastLocation);
    }

    public void ping() {
        OscMessage m = new OscMessage(Netzwerk.SERVER_PATTERN_PING);
        m.add(mListeningPort);
        mOSC.send(m, mBroadcastLocation);
    }

    public void connect_server(String address, int port) {
        OscMessage m = new OscMessage(Netzwerk.SERVER_PATTERN_CONNECT_SERVER);
        m.add(address);
        m.add(port);
        mOSC.send(m, mBroadcastLocation);
    }

    /* send message to server with 1, 2, or 3 floats as data or a string */

    public void send(String tag, float x) {
        OscMessage m = new OscMessage(getAddressPattern(tag));
        m.add(x);
        mOSC.send(m, mBroadcastLocation);
    }

    public void send(String tag, float x, float y) {
        OscMessage m = new OscMessage(getAddressPattern(tag));
        m.add(x);
        m.add(y);
        mOSC.send(m, mBroadcastLocation);
    }

    public void send(String tag, float x, float y, float z) {
        OscMessage m = new OscMessage(getAddressPattern(tag));
        m.add(x);
        m.add(y);
        m.add(z);
        mOSC.send(m, mBroadcastLocation);
    }

    public void send(String tag, String message) {
        OscMessage m = new OscMessage(getAddressPattern(tag));
        m.add(message);
        mOSC.send(m, mBroadcastLocation);
    }

    /* `send_raw` allows the sending of plain OSC messages to any address on the network. more or less direct access to the underlying OSC library */

    public void send_raw(OscMessage pMessage, NetAddress pNetAddress) {
        mOSC.send(pMessage, pNetAddress);
    }

    /* `send_direct` works like `send` except that it sends a message directly to the specified IP */

    public void send_direct(String IP, String tag, float x) {
        OscMessage m = new OscMessage(getAddressPattern(tag));
        m.add(x);
        NetAddress mLocal = new NetAddress(IP, mListeningPort);
        OscP5.flush(m, mLocal);
    }

    public void send_direct(String IP, String tag, float x, float y) {
        OscMessage m = new OscMessage(getAddressPattern(tag));
        m.add(x);
        m.add(y);
        NetAddress mLocal = new NetAddress(IP, mListeningPort);
        OscP5.flush(m, mLocal);
    }

    public void send_direct(String IP, String tag, float x, float y, float z) {
        OscMessage m = new OscMessage(getAddressPattern(tag));
        m.add(x);
        m.add(y);
        m.add(z);
        NetAddress mLocal = new NetAddress(IP, mListeningPort);
        OscP5.flush(m, mLocal);
    }

    public void send_direct(String IP, String tag, String message) {
        OscMessage m = new OscMessage(getAddressPattern(tag));
        m.add(message);
        NetAddress mLocal = new NetAddress(IP, mListeningPort);
        OscP5.flush(m, mLocal);
    }

    /* `spoof` works like `send` except that it use an arbitray sender */

    public void spoof(String sender, String tag, float x) {
        OscMessage m = new OscMessage(getAddressPattern(sender, tag));
        m.add(x);
        mOSC.send(m, mBroadcastLocation);
    }

    public void spoof(String sender, String tag, float x, float y) {
        OscMessage m = new OscMessage(getAddressPattern(sender, tag));
        m.add(x);
        m.add(y);
        mOSC.send(m, mBroadcastLocation);
    }

    public void spoof(String sender, String tag, float x, float y, float z) {
        OscMessage m = new OscMessage(getAddressPattern(sender, tag));
        m.add(x);
        m.add(y);
        m.add(z);
        mOSC.send(m, mBroadcastLocation);
    }

    /* `sneak` works like `send_direct` except that it send a message to `localhost` */

    public void sneak(String tag, float x) {
        send_direct(LOCALHOST, tag, x);
    }

    public void sneak(String tag, float x, float y) {
        send_direct(LOCALHOST, tag, x, y);
    }

    public void sneak(String tag, float x, float y, float z) {
        send_direct(LOCALHOST, tag, x, y, z);
    }

    public void sneak(String tag, String message) {
        send_direct(LOCALHOST, tag, message);
    }

    private String getAddressPattern(String pSenderName, String pTag) {
        return DELIMITER + pSenderName + DELIMITER + pTag;
    }

    private String getAddressPattern(String pTag) {
        return getAddressPattern(mSenderName, pTag);
    }

    public void oscEvent(OscMessage m) {
        if (m.checkAddrPattern(Netzwerk.SERVER_PATTERN_PING)) {
            receive_ping();
        } else if (m.typetag().equalsIgnoreCase("f")) {
            receive(getName(m.addrPattern()), getTag(m.addrPattern()), m.get(0).floatValue());
        } else if (m.typetag().equalsIgnoreCase("ff")) {
            receive(getName(m.addrPattern()), getTag(m.addrPattern()), m.get(0).floatValue(), m.get(1).floatValue());
        } else if (m.typetag().equalsIgnoreCase("fff")) {
            receive(getName(m.addrPattern()),
                    getTag(m.addrPattern()),
                    m.get(0).floatValue(),
                    m.get(1).floatValue(),
                    m.get(2).floatValue());
        } else if (m.typetag().equalsIgnoreCase("s")) {
            receive(getName(m.addrPattern()), getTag(m.addrPattern()), m.get(0).stringValue());
        } else {
            receive_raw(m);
//            log("### ", "client couldn t parse message:");
//            log("### ", theOscMessage.toString());
        }
    }

    private String getTag(String pAddrPattern) {
        String[] mStrings = PApplet.split(pAddrPattern, DELIMITER);
        if (mStrings.length == 3) {
            return mStrings[2];
        } else if (mStrings.length == 2) {
            return mStrings[1];
        } else {
            log("### ", "ERROR-MALFORMED-NAME-TAG: " + pAddrPattern);
            return "ERROR-MALFORMED-NAME-TAG";
        }
    }

    private String getName(String pAddrPattern) {
        String[] mStrings = PApplet.split(pAddrPattern, DELIMITER);
        if (mStrings.length == 3) {
            return mStrings[1];
        } else if (mStrings.length == 2) {
            return ANONYM;
        } else {
            log("### ", "ERROR-MALFORMED-NAME-TAG: " + pAddrPattern);
            return "ERROR-MALFORMED-NAME-TAG";
        }
    }

    private void log(String prefix, String message) {
        if (SHOW_LOG) {
            System.out.println(prefix + "\t" + message);
        }
    }

    public void dispose() {
        log("###", "disconnecting client*");
        disconnect();
    }

    private void prepareExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                log("###", "disconnecting client*");
                disconnect();
            }
        }));
    }

    /* --- callback methods --- */

    private void receive(String name, String tag, float x) {
        if (mMethodReceive1f != null) {
            try {
                mMethodReceive1f.invoke(mClientParent, name, tag, x);
            } catch (Exception ignored) {
            }
        }
        if (mNetzwerkClientListener != null) {
            mNetzwerkClientListener.receive(name, tag, x);

        }
    }

    private void receive(String name, String tag, float x, float y) {
        if (mMethodReceive2f != null) {
            try {
                mMethodReceive2f.invoke(mClientParent, name, tag, x, y);
            } catch (Exception ignored) {
            }
        }
        if (mNetzwerkClientListener != null) {
            mNetzwerkClientListener.receive(name, tag, x, y);
        }
    }

    private void receive(String name, String tag, float x, float y, float z) {
        if (mMethodReceive3f != null) {
            try {
                mMethodReceive3f.invoke(mClientParent, name, tag, x, y, z);
            } catch (Exception ignored) {
            }
        }
        if (mNetzwerkClientListener != null) {
            mNetzwerkClientListener.receive(name, tag, x, y, z);

        }
    }

    private void receive(String name, String tag, String message) {
        if (mMethodReceiveStr != null) {
            try {
                mMethodReceiveStr.invoke(mClientParent, name, tag, message);
            } catch (Exception ignored) {
            }
        }
        if (mNetzwerkClientListener != null) {
            mNetzwerkClientListener.receive(name, tag, message);
        }
    }

    private void receive_raw(OscMessage m) {
        if (mMethodReceiveRaw != null) {
            try {
                mMethodReceiveRaw.invoke(mClientParent, m);
            } catch (Exception ignored) {
            }
        }
        if (mNetzwerkClientListener != null) {
            mNetzwerkClientListener.receive_raw(m);
        }
    }

    private void receive_ping() {
        if (mMethodReceivePing != null) {
            try {
                mMethodReceivePing.invoke(mClientParent);
            } catch (Exception ignored) {
            }
        }
        if (mNetzwerkClientListener != null) {
            mNetzwerkClientListener.receive_ping();
        }
    }
}
