package fr.eip.aura.udp.db.listener;

import fr.eip.aura.udp.proto.MessageProto;
import fr.eip.aura.udp.server.Server;
import org.postgresql.PGNotification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListenerMessage extends Thread {
    private Connection  connection;
    private org.postgresql.PGConnection pgConnection;
    private Server  server;

    public ListenerMessage(Connection connection, Server server) {
        try {
            this.server = server;
            this.connection = connection;
            this.pgConnection = (org.postgresql.PGConnection)connection;
            Statement statement = connection.createStatement();
            statement.execute("LISTEN newMessage");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
    }

    public void run() {
        List<MessageProto.Message> listMessage = new ArrayList<>();
        try {

            while (true) {
                listMessage.clear();
                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM message WHERE answered = false AND human = true AND send = false;");
                ResultSet rs = stmt.executeQuery();

                org.postgresql.PGNotification notifications[] = pgConnection.getNotifications();
                if (notifications != null) {
                    for (PGNotification notification : notifications) {
                        while (rs.next()) {
                            MessageProto.Message.Builder message = MessageProto.Message.newBuilder();

                            message.setId(rs.getInt("id"));
                            message.setUserId(rs.getInt("user_id"));
                            message.setText(rs.getString("content"));
                            message.setTimestamp(rs.getTimestamp("timestamp").getTime());
                            message.setType("message");
                            listMessage.add(message.build());
                        }
                        if (!listMessage.isEmpty()) {
                            server.sendMessages(listMessage);
                        }
                    }
                    rs.close();
                }
                stmt.close();
                // wait a while before checking again for new
                // notifications
                Thread.sleep(500);
            }
        } catch (Exception sqle) {
            sqle.printStackTrace();
        }
    }
}
