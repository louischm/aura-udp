package fr.eip.aura.udp.db.listener;

import fr.eip.aura.udp.server.Server;
import fr.eip.aura.udp.proto.SurveyProto;
import org.postgresql.PGNotification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListenerSurvey extends Thread {
    private Connection  connection;
    private org.postgresql.PGConnection pgConnection;
    private Server  server;

    public ListenerSurvey(Connection connection, Server server) {
        try {
            this.server = server;
            this.connection = connection;
            this.pgConnection = (org.postgresql.PGConnection)connection;
            Statement statement = connection.createStatement();
            statement.execute("LISTEN newSurvey");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
    }

    public void run() {
        List<SurveyProto.Survey>    surveyList = new ArrayList<>();

        while (true) {
            try {
                surveyList.clear();
                PreparedStatement   stmt = connection.prepareStatement("SELECT * FROM survey WHERE answer = false AND send = false");
                ResultSet   rsSurvey = stmt.executeQuery();
                org.postgresql.PGNotification   notifications[] = pgConnection.getNotifications();

                if (notifications != null) {
                    for (PGNotification notification: notifications) {
                        while (rsSurvey.next()) {
                            SurveyProto.Survey.Builder  survey = SurveyProto.Survey.newBuilder();

                            survey.setId(rsSurvey.getInt("id"));
                            survey.setUserId(rsSurvey.getInt("user_id"));
                            survey.setType("survey");
                            PreparedStatement   statementExhange = connection.prepareStatement("SELECT * FROM exchange WHERE survey_id = " +
                                    survey.getId());
                            ResultSet   rsExhange = statementExhange.executeQuery();
                            while (rsExhange.next()) {
                                SurveyProto.SurveyQuestion.Builder  exhange = SurveyProto.SurveyQuestion.newBuilder();

                                exhange.setAnswer("");
                                exhange.setId(rsExhange.getInt("id"));
                                exhange.setQuestion(rsExhange.getString("question"));
                                survey.addQuestions(exhange);
                            }
                            rsExhange.close();
                            surveyList.add(survey.build());
                            statementExhange.close();
                        }
                        if (!surveyList.isEmpty()) {
                            server.sendSurveys(surveyList);
                        }
                    }
                    rsSurvey.close();
                }
                stmt.close();
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
