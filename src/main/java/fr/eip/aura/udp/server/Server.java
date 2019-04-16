package fr.eip.aura.udp.server;

import fr.eip.aura.udp.db.*;
import fr.eip.aura.udp.db.listener.ListenerMessage;
import fr.eip.aura.udp.db.listener.ListenerSurvey;
import fr.eip.aura.udp.db.listener.ListenerUser;
import fr.eip.aura.udp.db.notifier.NotifierMessage;
import fr.eip.aura.udp.db.notifier.NotifierSurvey;
import fr.eip.aura.udp.db.notifier.NotifierUser;
import fr.eip.aura.udp.proto.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private DatagramSocket dsocket;
    private PostgreSQLJDBC  psql = new PostgreSQLJDBC();
    // Create a buffer to read datagrams into. If a
    // packet is larger than this buffer, the
    // excess will simply be discarded!
    private byte[]  bufferReceiver = new byte[2048];
    private byte[]  bufferSender = new byte[2048];

    private void init() throws Exception {
        // Allow logger to print on console
        ConsoleHandler  handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.FINE);

        int port = 9090;
        // Create a socket to listen on the port.
        this.dsocket = new DatagramSocket(port);
        // Open db
        this.psql.connection();
    }

    public void sendMessages(List<MessageProto.Message> listMessage) throws Exception {
        String  hostAI = "localhost";
        int     portAI = 6005;
        DatagramSocket dsocket = new DatagramSocket();

        for (MessageProto.Message newMessage: listMessage) {
            byte[] message = newMessage.toByteArray();
            InetAddress address = InetAddress.getByName(hostAI);

            // Initialize a datagram packet with data and address
            DatagramPacket packet = new DatagramPacket(message, message.length,
                    address, portAI);

            // Create a datagram socket, send the packet through it, close it.

            dsocket.send(packet);
            LOGGER.log(Level.FINE, "Message sended to chatbot:\n {0}\n", newMessage);
            psql.updateSendMessage(newMessage);
        }
        dsocket.close();
    }

    public void sendSurveys(List<SurveyProto.Survey> listSurveys) throws Exception {
        String  hostAI = "localhost";
        int     portAI = 6005;
        DatagramSocket  dsocket = new DatagramSocket();

        for (SurveyProto.Survey newSurvey: listSurveys) {
            byte[]  survey = newSurvey.toByteArray();
            InetAddress address = InetAddress.getByName(hostAI);

            DatagramPacket  packet = new DatagramPacket(survey, survey.length, address, portAI);

            dsocket.send(packet);
            LOGGER.log(Level.FINE, "Survey sended to chatbot:\n {0}\n", newSurvey);
            psql.updateSendSurvey(newSurvey);
        }
        dsocket.close();
    }

    public  void sendUser(List<UserProto.User>  listUsers) throws Exception {
        String  hostAI = "localhost";
        int     portAI = 6005;
        DatagramSocket  dsocket = new DatagramSocket();

        for (UserProto.User newUser: listUsers) {
            byte[]  user = newUser.toByteArray();
            InetAddress address = InetAddress.getByName(hostAI);

            DatagramPacket  packet = new DatagramPacket(user, user.length, address, portAI);

            dsocket.send(packet);
            LOGGER.log(Level.FINE, "User sended to chatbot:\n {0}\n", newUser);
            psql.updateUser(newUser);
        }
        dsocket.close();
    }

    private void receive() throws Exception {
        // Create a packet to receive data into the buffer
        DatagramPacket packet = new DatagramPacket(bufferReceiver, bufferReceiver.length);
        while (true) {
            dsocket.receive(packet);
            byte[]                  buf = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            TypeProto.Type          type = TypeProto.Type.parseFrom(buf);

            switch (type.getType()) {
                case "message":
                    MessageProto.Message    message = MessageProto.Message.parseFrom(buf);
                    LOGGER.log(Level.FINE, "New message received from chatbot: \n{0}\n", message);
                    psql.insertMessage(message);
                    psql.updateAnsweredMessage(message);
                    break;
                case "survey":
                    SurveyProto.Survey  survey = SurveyProto.Survey.parseFrom(buf);
                    LOGGER.log(Level.FINE, "Survey's answered received from chatbot: \n{0}\n", survey);
                    psql.updateAnsweredSurvey(survey);
                    break;
                case "score":
                    ScoreProto.Score    score = ScoreProto.Score.parseFrom(buf);
                    LOGGER.log(Level.FINE, "Score received from chatbot: \n{0}\n", score);
                    psql.insertOrUpdateScore(score);
                    break;
                default:
                    LOGGER.log(Level.FINE, "Couldn't parse this packet\n");
                    break;
            }
            packet.setLength(bufferReceiver.length);
        }
    }

    public static void main(String args[]) {
        Server server = new Server();

        try {
            Class.forName("org.postgresql.Driver");
            String  url = "jdbc:postgresql://localhost:5432/aura";
            Connection  lConnMessage = DriverManager.getConnection(url,"adminaura","admin4ura");
            Connection  nConnMessage = DriverManager.getConnection(url,"adminaura","admin4ura");
            Connection  lConnSurvey = DriverManager.getConnection(url,"adminaura","admin4ura");
            Connection  nConnSurvey = DriverManager.getConnection(url,"adminaura","admin4ura");
            Connection  lConnUser = DriverManager.getConnection(url,"adminaura","admin4ura");
            Connection  nConnUser = DriverManager.getConnection(url,"adminaura","admin4ura");

            server.init();
            ListenerMessage listenerMessage = new ListenerMessage(lConnMessage, server);
            NotifierMessage notifierMessage = new NotifierMessage(nConnMessage);
            ListenerSurvey listenerSurvey = new ListenerSurvey(lConnSurvey, server);
            NotifierSurvey notifierSurvey = new NotifierSurvey(nConnSurvey);
            ListenerUser listenerUser = new ListenerUser(lConnUser, server);
            NotifierUser notifierUser = new NotifierUser(nConnUser);

            listenerMessage.start();
            notifierMessage.start();
            listenerSurvey.start();
            notifierSurvey.start();
            listenerUser.start();
            notifierUser.start();
            server.receive();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

}