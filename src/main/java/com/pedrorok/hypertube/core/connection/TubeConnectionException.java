package com.pedrorok.hypertube.core.connection;

import com.pedrorok.hypertube.core.connection.interfaces.IConnection;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/06/2025
 * @project Create Hypertube
 */
public class TubeConnectionException extends RuntimeException {

    public TubeConnectionException(String message, IConnection... connections) {
        super(message + getConnectionsString(connections));
    }

    private static String getConnectionsString(IConnection... connections) {
        StringBuilder sb = new StringBuilder(" | Connections: ");
        int i = 0;

        for (IConnection connection : connections) {
            i++;
            sb.append("[").append(i).append("] ").append(connection).append(" ");
        }
        return sb.toString();
    }
}
