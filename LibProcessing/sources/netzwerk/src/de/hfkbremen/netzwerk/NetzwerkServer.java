package de.hfkbremen.netzwerk;

import netP5.NetAddress;
import netP5.NetAddressList;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;

import static de.hfkbremen.netzwerk.Netzwerk.SERVER_DEFAULT_SERVER_LISTENING_PORT;
import static de.hfkbremen.netzwerk.Netzwerk.SERVER_PATTERN_CONNECT;
import static de.hfkbremen.netzwerk.Netzwerk.SERVER_PATTERN_CONNECT_SERVER;
import static de.hfkbremen.netzwerk.Netzwerk.SERVER_PATTERN_CONNECT_SERVER_TO_SERVER;
import static de.hfkbremen.netzwerk.Netzwerk.SERVER_PATTERN_DISCONNECT;
import static de.hfkbremen.netzwerk.Netzwerk.SERVER_PATTERN_PING;
import static de.hfkbremen.netzwerk.Netzwerk.SERVER_PATTERN_SERVER_TO_SERVER_COMM;

public class NetzwerkServer {

    private static final int LOG_TYPE_MESSAGE = 0;
    private static final int LOG_TYPE_WARNING = 1;
    private static final int LOG_TYPE_ERROR = 2;
    public static boolean SHOW_LOG = true;
    private final NetAddressList mNetAddressListClients = new NetAddressList();
    private final NetAddressList mNetAddressListServers = new NetAddressList();
    private final String mConnectPattern;
    private final String mDisconnectPattern;
    private final OscMessage[] mMessages;
    //    private final HashMap<String, String> mAddressMap = new HashMap<>();
    private final String mIP;
    private final int mListeningPort;
    private int mMessagePtr;
    private OscP5 mOSC;

    public NetzwerkServer() {
        this(SERVER_DEFAULT_SERVER_LISTENING_PORT);
    }

    public NetzwerkServer(int pListeningPort) {
        mListeningPort = pListeningPort;
        mConnectPattern = SERVER_PATTERN_CONNECT;
        mDisconnectPattern = SERVER_PATTERN_DISCONNECT;

        final int mMaxMessages = 32;
        mMessagePtr = 0;
        mMessages = new OscMessage[mMaxMessages];
        for (int i = 0; i < mMessages.length; i++) {
            mMessages[i] = new OscMessage("");
        }

        mOSC = new OscP5(this, mListeningPort);

        mIP = mOSC.ip();
        log(LOG_TYPE_MESSAGE, "server is @ " + mIP + " + listening on port " + mListeningPort);
        // todo write logfile to HD / once a second
    }

    public String ip() {
        return mIP;
    }

    public synchronized void purge_clients() {
//        mAddressMap.clear();
        for (int i = 0; i < mNetAddressListClients.size(); i++) {
            disconnect_client(mNetAddressListClients.get(i).address(), mNetAddressListClients.get(i).port());
        }
        for (int i = 0; i < mNetAddressListServers.size(); i++) {
            disconnect_server(mNetAddressListServers.get(i).address(), mNetAddressListServers.get(i).port());
        }
    }

    public synchronized OscMessage[] messages() {
        OscMessage[] mMessages = new OscMessage[this.mMessages.length];
        for (int i = 0; i < this.mMessages.length; i++) {
            int j = (i + mMessagePtr) % this.mMessages.length;
            mMessages[i] = this.mMessages[j];
        }
        return mMessages;
    }

    public NetAddressList clients() {
        return mNetAddressListClients;
    }

    public synchronized void oscEvent(OscMessage m) {
        /* store copy of message */
        mMessages[mMessagePtr] = new OscMessage(m);
        mMessagePtr++;
        mMessagePtr %= mMessages.length;

        /* check if the address pattern fits any of our patterns, accepting `int` and `float` as typetag */
        if (m.checkAddrPattern(SERVER_PATTERN_PING) && m.checkTypetag("i")) {
            NetAddress mNetAddress = new NetAddress(m.netAddress().address(), m.get(0).intValue());
            mOSC.send(m, mNetAddress);
        } else if (m.checkAddrPattern(SERVER_PATTERN_CONNECT_SERVER_TO_SERVER) && m.checkTypetag("i")) {
            int mRemoteServerPort = m.get(0).intValue();
            String mRemoteServerIP = m.netAddress().address();
            log(LOG_TYPE_MESSAGE,
                "remote server requesting to connect from " + mRemoteServerIP + ":" + mRemoteServerPort);
        } else if (m.checkAddrPattern(SERVER_PATTERN_CONNECT_SERVER) && m.checkTypetag("si")) {
            String mServerIP = m.get(0).stringValue();
            int mServerPort = m.get(1).intValue();
            log(LOG_TYPE_MESSAGE, "trying to connect to remote server " + mServerIP + ":" + mServerPort);
            if (mServerIP.equals(mIP) && mServerPort == mListeningPort) {
                log(LOG_TYPE_ERROR, "should not connect server to itself ( same IP:port ).");
            } else {
                OscMessage mMessage = new OscMessage(SERVER_PATTERN_CONNECT_SERVER_TO_SERVER);
                mMessage.add(mListeningPort);
                NetAddress mNetAddress = new NetAddress(mServerIP, mServerPort);
                mOSC.send(mMessage, mNetAddress);
                connect_server(mServerIP, mServerPort);
            }
        } else if (m.checkAddrPattern(mConnectPattern) && m.checkTypetag("i")) {
            connect_client(m.netAddress().address(), m.get(0).intValue());
        } else if (m.checkAddrPattern(mDisconnectPattern) && m.checkTypetag("i")) {
            disconnect_client(m.netAddress().address(), m.get(0).intValue());
        } else {
            /*
             * if pattern matching was not successful, then broadcast the incoming
             * message to all addresses in the netAddresList.
             */
            /* if message from remote server then chop off pattern else send to server */
            if (m.addrPattern().startsWith(SERVER_PATTERN_SERVER_TO_SERVER_COMM)) {
                m.setAddrPattern(m.addrPattern().substring(SERVER_PATTERN_SERVER_TO_SERVER_COMM.length()));
            } else {
                if (mNetAddressListServers.size() > 0) {
                    OscMessage mMessageS2S = new OscMessage(m);
                    mMessageS2S.setAddrPattern(SERVER_PATTERN_SERVER_TO_SERVER_COMM + m.addrPattern());
                    mOSC.send(mMessageS2S, mNetAddressListServers);
                }
            }
            mOSC.send(m, mNetAddressListClients);
        }
    }

    private void connect_client(String theIPaddress, int pBroadcastPort) {
        if (!mNetAddressListClients.contains(theIPaddress, pBroadcastPort)) {
            mNetAddressListClients.add(new NetAddress(theIPaddress, pBroadcastPort));
            log(LOG_TYPE_MESSAGE, "adding client " + theIPaddress + ":" + pBroadcastPort + " to list.");
        } else {
            log(LOG_TYPE_ERROR, theIPaddress + " is already connected.");
        }
        log(LOG_TYPE_MESSAGE,
            "currently there are " + mNetAddressListClients.list().size() + " remote locations connected.");
        log(LOG_TYPE_MESSAGE, mNetAddressListClients.list().toString());
    }

    private void disconnect_client(String theIPaddress, int pBroadcastPort) {
        if (mNetAddressListClients.contains(theIPaddress, pBroadcastPort)) {
            mNetAddressListClients.remove(theIPaddress, pBroadcastPort);
            log(LOG_TYPE_MESSAGE, "removing client " + theIPaddress + ":" + pBroadcastPort + " from list.");
        } else {
            log(LOG_TYPE_ERROR, theIPaddress + " is not connected.");
        }
        log(LOG_TYPE_MESSAGE,
            "currently there are " + mNetAddressListClients.list().size() + " remote locations connected.");
    }

    private void connect_server(String theIPaddress, int pBroadcastPort) {
        if (!mNetAddressListServers.contains(theIPaddress, pBroadcastPort)) {
            mNetAddressListServers.add(new NetAddress(theIPaddress, pBroadcastPort));
            log(LOG_TYPE_MESSAGE, "adding server " + theIPaddress + " to list.");
        } else {
            log(LOG_TYPE_ERROR, theIPaddress + " is already connected.");
        }
        log(LOG_TYPE_MESSAGE,
            "currently there are " + mNetAddressListServers.list().size() + " remote servers connected.");
        log(LOG_TYPE_MESSAGE, mNetAddressListServers.list().toString());
    }

    private void disconnect_server(String theIPaddress, int pBroadcastPort) {
        if (mNetAddressListServers.contains(theIPaddress, pBroadcastPort)) {
            mNetAddressListServers.remove(theIPaddress, pBroadcastPort);
            log(LOG_TYPE_MESSAGE, "removing server" + theIPaddress + " from list.");
        } else {
            log(LOG_TYPE_ERROR, theIPaddress + " is not connected.");
        }
        log(LOG_TYPE_MESSAGE,
            "currently there are " + mNetAddressListServers.list().size() + " remote servers connected.");
    }

    private void log(int pLogType, String m) {
        if (SHOW_LOG) {
            switch (pLogType) {
                case LOG_TYPE_MESSAGE:
                    System.out.print("###");
                    break;
                case LOG_TYPE_WARNING:
                    System.out.print("+++");
                    break;
                case LOG_TYPE_ERROR:
                    System.out.print("---");
                    break;
            }
            System.out.println("\t" + m);
        }
    }

    public static String getAsString(Object[] theObject) {
        StringBuilder s = new StringBuilder();
        for (Object o : theObject) {
            if (o instanceof Float) {
                String str = PApplet.nfc((Float) o, 2);
                s.append(str).append(" | ");
            } else if (o instanceof Integer) {
                String str = o.toString();
                s.append(str).append(" | ");
            }
        }
        return s.toString();
    }
}
