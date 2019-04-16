package fr.eip.aura.udp.db.listener;

import fr.eip.aura.udp.proto.UserProto;
import fr.eip.aura.udp.server.Server;
import org.postgresql.PGNotification;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ListenerUser extends Thread {
    private Connection  connection;
    private org.postgresql.PGConnection pgConnection;
    private Server  server;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

    public ListenerUser(Connection connection, Server server) {
        try {
            this.server = server;
            this.connection = connection;
            this.pgConnection = (org.postgresql.PGConnection)connection;
            Statement statement = connection.createStatement();
            statement.execute("LISTEN newUser");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
    }

    public void run() {
        List<UserProto.User>    userList = new ArrayList<>();

        while (true) {
            try {
                userList.clear();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE send = false;");
                ResultSet   rs = statement.executeQuery();
                org.postgresql.PGNotification   notifications[] = pgConnection.getNotifications();

                if(notifications != null) {
                    for (PGNotification notification: notifications) {
                        while (rs.next()) {
                            UserProto.User.Builder  user = UserProto.User.newBuilder();

                            user.setType("user");
                            user.setId(rs.getInt("id"));
                            user.setDoctorId(rs.getInt("doctor_id"));
                            user.setFirstname(rs.getString("firstname"));
                            user.setLastname(rs.getString("lastname"));
                            user.setGender(rs.getString("gender"));
                            user.setBirthdayDate(sdf.format(rs.getTimestamp("birthday")));
                            userList.add(user.build());
                        }
                        if (!userList.isEmpty()) {
                            server.sendUser(userList);
                        }
                    }
                    rs.close();
                }
                statement.close();
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
