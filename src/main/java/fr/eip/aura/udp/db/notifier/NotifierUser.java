package fr.eip.aura.udp.db.notifier;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class NotifierUser extends Thread {
    private Connection  connection;

    public NotifierUser(Connection connection) { this.connection = connection; }

    public void run() {
        while (true) {
            try {
                Statement statement = connection.createStatement();
                statement.execute("NOTIFY newUser");
                statement.close();
                Thread.sleep(2000);
            } catch (SQLException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
