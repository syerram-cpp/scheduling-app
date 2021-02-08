package View_Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import model.Lists;
import model.User;
import util.DBConnection;
import util.DBQuery;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class loginController implements Initializable
{
    Connection conn = null;
    Stage stage;
    ResourceBundle bundle;

    private User user = null;
    private Lists lists = null;

    @FXML Label userNameLabel;
    @FXML Label passwordLabel;
    @FXML TextField usernameText;
    @FXML TextField passwordText;
    @FXML Button loginButton;

    // Locales used: -US: English -Mexico: Spanish
    // Gets system default language and country
    // Sets text on labels and button according to system language and country
    @Override public void initialize(URL location, ResourceBundle resources) {
        this.conn = DBConnection.startConnection();

        String country = System.getProperty("user.country");
        String language = System.getProperty("user.language");
        Locale.setDefault(new Locale(language, country));
        this.bundle = ResourceBundle.getBundle("loginBundle", Locale.getDefault());

        userNameLabel.setText(bundle.getString("username"));
        passwordLabel.setText(bundle.getString("password"));
        loginButton.setText(bundle.getString("login"));
    }

    // Checks if database contains inputted username and password
    // If user exists:
        // Checks all the appointments that the user has and launches an alert if there is one within 15 minutes
        // Creates a new user locally to store within the program
        // Creates an instance of the class Lists and passes the User object to it
        // Appends timestamp and username to "log.txt" file
    // If database does not contain inputted login information, an incorrect login alert is launched
    @FXML
    public void loginButton(MouseEvent event) throws SQLException, IOException {
        String username = usernameText.getText();
        String password = passwordText.getText();

        String loginString = "SELECT userId FROM user WHERE userName = ? AND password = ?";
        DBQuery.setPreparedStatement(conn, loginString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setString(1, username);
        ps.setString(2, password);
        ps.execute();

        ResultSet rs = ps.getResultSet();
        if (rs.next())
        {
            int userId = rs.getInt("userId");

            stage = (Stage)((Button)event.getSource()).getScene().getWindow();
            stage.hide();
            findAppointments(userId, Timestamp.valueOf(LocalDateTime.now()));

            this.user = new User(userId, username, password);
            this.lists = new Lists(conn, user);

            String log = Instant.now().toString() + ": " +username;
            setUpLogFile(log);

            goToMainMenu(event);
        }
        else { loginAlert(); }
    }

    // Changes scene to Main Menu
    @FXML
    private void goToMainMenu(MouseEvent event) throws IOException
    {
        stage = (Stage)((Button)event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/mainMenu.fxml"));
        mainMenuController mainMenuController = new mainMenuController(user, conn, lists);
        loader.setController(mainMenuController);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // Takes in string containing timestamp and username as a parameter
    // Creates "log.txt" file if it does not exist
    // Appends inputted string to "log.txt" file
    public void setUpLogFile(String log) throws IOException {
        File f = new File("log.txt");
        if(!f.exists()) { f.createNewFile(); }
        FileWriter fw = new FileWriter(f.getName(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("\n"+log);
        bw.close();
    }

    // Looks at all of user's appointments
    // Determines time difference between current time and start time of each appointment
    // If there is an appointment within 15 minutes, an appointment alert is displayed
    public void findAppointments(int userId, Timestamp currentTime) throws SQLException {
        String findAppointmentsString = "SELECT start FROM user u " +
                "JOIN appointment a ON (u.userId=a.userId AND u.userId=?)";
        DBQuery.setPreparedStatement(conn, findAppointmentsString);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.setInt(1, userId);
        ps.execute();

        ResultSet rs = ps.getResultSet();
        while(rs.next())
        {
            LocalDateTime startTime = rs.getTimestamp(1).toLocalDateTime();
            LocalDateTime localTime = LocalDateTime.now();
// Used to test login alert for appointment in <= 15 minutes:
// 'localTime' is set to the time of an existing appointment in system timezone
            // LocalDateTime localTime = LocalDateTime.of(2020, 9, 1, 4, 20);
            String nowTime = localTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"Z";
            String start = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"Z";

            Long difference = Duration.between(Instant.parse(nowTime), Instant.parse(start)).getSeconds();
            if(difference <= 900 && difference > 0) { appointmentAlert(); break;}
        }
    }

    // Displays when user has an appointment within 15 minutes of login
    // Changes text displayed on alert depending on location
    private void appointmentAlert()
    {
        Alert a1 = new Alert(Alert.AlertType.INFORMATION);
        a1.setContentText(bundle.getString("appointmentAlert"));
        a1.showAndWait();
    }

    // Displays when user enters incorrect login information
    // Changes text displayed on alert depending on location
    private void loginAlert()
    {
        Alert a1 = new Alert(Alert.AlertType.WARNING);
        a1.setContentText(bundle.getString("loginAlert"));
        a1.showAndWait();
    }
}

