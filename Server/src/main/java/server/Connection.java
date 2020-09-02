package server;

import java.nio.channels.SelectionKey;

public class Connection {
    private SelectionKey key;
    private final Integer UDPport;

    public Connection(SelectionKey key, Integer UDPport) {
        this.key = key;
        this.UDPport = UDPport;
    }

    /**
     * @return the key
     */
    public SelectionKey getKey() {
        return key;
    }

    /**
     * @return the uDPport
     */
    public Integer getUDPport() {
        return UDPport;
    }
}

