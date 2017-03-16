package de.hfkbremen.netzwerk;

public class Netzwerk {

    public static String SERVER_DEFAULT_BROADCAST_IP = "localhost";
    public static int SERVER_DEFAULT_SERVER_LISTENING_PORT = 32000;
    public static String SERVER_PATTERN_CONNECT = "/server/connect";
    public static String SERVER_PATTERN_DISCONNECT = "/server/disconnect";
    public static String SERVER_PATTERN_PING = "/server/ping";
    public static String SERVER_PATTERN_CONNECT_SERVER = "/server/connect-server";
    public static String SERVER_PATTERN_CONNECT_SERVER_TO_SERVER = "/server/connect-server-to-server";
    public static String SERVER_PATTERN_SERVER_TO_SERVER_COMM = "/*";

    public static boolean match(String s1, String equals) {
        return s1.equals(equals);
    }
}
