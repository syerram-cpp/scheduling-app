package View_Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Connection conn = DBConnection.startConnection();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/login.fxml"));
        loginController loginController = new loginController();
        loader.setController(loginController);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) throws SQLException {
        launch(args);
        DBConnection.closeConnection();
    }
}

