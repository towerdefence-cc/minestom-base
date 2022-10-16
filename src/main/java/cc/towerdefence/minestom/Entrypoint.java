package cc.towerdefence.minestom;

import java.util.Arrays;

public final class Entrypoint {
    private static final String DEFAULT_ADDRESS = "0.0.0.0";
    private static final String DEFAULT_PORT = "25565";

    public static void main(String[] args) {
        String address = getValue("minestom.address", args, DEFAULT_ADDRESS);
        int port = Integer.parseInt(getValue("minestom.port", args, DEFAULT_PORT));

        for (String arg : args) {
            if (arg.startsWith("minestom.address")) address = arg.split("=")[1];
            else if (arg.startsWith("minestom.port")) port = Integer.parseInt(arg.split("=")[1]);
        }

        new MinestomServer(address, port);
    }

    public static String getValue(String key, String[] args, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null) return value;

        value = System.getenv(key);
        if (value != null) return value;

        for (String arg : args) {
            if (arg.startsWith(key)) return arg.split("=")[1];
        }
        return defaultValue;
    }
}