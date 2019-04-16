package fr.eip.aura.udp.db;

import fr.eip.aura.udp.proto.ScoreProto;
import fr.eip.aura.udp.proto.SurveyProto;
import fr.eip.aura.udp.proto.MessageProto;
import fr.eip.aura.udp.proto.UserProto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgreSQLJDBC {

    private Connection c = null;

    private static final Logger LOGGER = Logger.getLogger(PostgreSQLJDBC.class.getName());

    public void connection(){
        try {
            ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(Level.FINE);
            LOGGER.addHandler(handler);
            LOGGER.setLevel(Level.FINE);
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/aura",
                            "adminaura", "admin4ura");
            c.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        LOGGER.log(Level.FINE, "Database successfully opened\n");
    }

    // Message query
    public void insertMessage(MessageProto.Message message) {
        Statement   statement = null;
        Timestamp time = new Timestamp(System.currentTimeMillis());

        LOGGER.log(Level.FINE, "Try to insert new chatbot's message:\n {0}\n", message);
        try {
            String messageCheched = message.getText().replace("\'", "\'\'");
            statement = c.createStatement();
            String sql = "INSERT INTO message(content, timestamp, human, user_id, answered, send) " +
                    "VALUES(\'" + messageCheched + "\', \'" + time + "\', " +
                    false + ", " + message.getUserId() + ", " + true + ", " + true + ");";
            statement.executeUpdate(sql);
            statement.close();
            c.commit();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
        LOGGER.log(Level.FINE, "Chatbot's message inserted\n");
    }

    public void updateSendMessage(MessageProto.Message message) {
        Statement statement = null;

        LOGGER.log(Level.FINE, "Try to update sended message:\n {0}\n", message);
        try {
            statement = c.createStatement();
            statement.executeUpdate("UPDATE message SET send = true WHERE user_id = " + message.getUserId() +
                    " AND id = " + message.getId() +";");
            c.commit();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
        LOGGER.log(Level.FINE, "Sended message updated\n");
    }

    public void updateAnsweredMessage(MessageProto.Message message) {
        Statement statement = null;
        Timestamp time = new Timestamp(System.currentTimeMillis());

        LOGGER.log(Level.FINE, "Try to update answered message:\n {0}\n", message);
        try {
            statement = c.createStatement();
            statement.executeUpdate("UPDATE message SET answered = true WHERE user_id = " + message.getUserId() +
                    " AND timestamp <= \'" + time + "\';");
            c.commit();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
        LOGGER.log(Level.FINE, "Answered message updated\n");
    }

    // Survey query
    public void updateSendSurvey(SurveyProto.Survey survey) {
        Statement   statement = null;

        LOGGER.log(Level.FINE, "Try to update sended survey:\n {0}\n", survey);
        try {
            statement = c.createStatement();
            statement.executeUpdate("UPDATE survey SET send = true WHERE user_id = " + survey.getUserId() +
                    " AND id = " + survey.getId() + ";");
            c.commit();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
        LOGGER.log(Level.FINE, "Sended survey updated");
    }

    public void updateAnsweredSurvey(SurveyProto.Survey survey) {
        Statement   statement = null;

        LOGGER.log(Level.FINE, "Try to update answered survey:\n {0}", survey);
        try {
            statement = c.createStatement();
            statement.executeUpdate("UPDATE survey SET answer = true WHERE id = " + survey.getId());
            for (SurveyProto.SurveyQuestion surveyQuestion: survey.getQuestionsList()) {
                statement.executeUpdate("UPDATE exchange SET answer = \'" + surveyQuestion.getAnswer() +
                        "\' WHERE id = " + surveyQuestion.getId() + ";");
                c.commit();
            }
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
        LOGGER.log(Level.FINE, "Answered survey updated\n");
    }

    // User
    public void insertOrUpdateScore(ScoreProto.Score score) {
        Statement   statement = null;

        LOGGER.log(Level.FINE, "Try to update user's score:\n {0}\n", score);
        try {
            statement = c.createStatement();
            statement.executeUpdate("UPDATE users SET score = " + score.getScore() + " WHERE id = " +
                    score.getUserId() + ";");
            c.commit();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
    }

    public void updateUser(UserProto.User user) {
        Statement   statement = null;

        LOGGER.log(Level.FINE, "Try to update user:\n {0}\n", user);
        try {
            statement = c.createStatement();
            statement.executeUpdate("UPDATE users SET send = true WHERE id = " + user.getId() + ";");
            c.commit();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
    }
}
