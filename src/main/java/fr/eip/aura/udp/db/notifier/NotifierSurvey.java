package fr.eip.aura.udp.db.notifier;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class NotifierSurvey extends Thread {
    private Connection connection;

    public NotifierSurvey(Connection connection) { this.connection = connection; }

    public void run() {
        while (true) {
            try {
                Statement   statement = connection.createStatement();
                statement.execute("NOTIFY newSurvey");
                statement.close();
                Thread.sleep(2000);
            } catch (SQLException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
