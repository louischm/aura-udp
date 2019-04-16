package fr.eip.aura.udp.db.notifier;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class NotifierMessage extends Thread {
    private Connection connection;

    public NotifierMessage(Connection connection) {
        this.connection = connection;
    }

    public void run() {
        while (true) {
            try {
                Statement stmt = connection.createStatement();
                stmt.execute("NOTIFY newMessage");
                stmt.close();
                Thread.sleep(2000);
            } catch (SQLException | InterruptedException sqle) {
                sqle.printStackTrace();
            }
        }
    }
}
