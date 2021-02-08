package View_Controller;

import java.net.URL;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import model.*;

import java.io.IOException;
import java.sql.*;
import java.util.ResourceBundle;

import javafx.stage.Stage;


public class reportsController implements Initializable
{
    Connection conn = null;
    Stage stage;

    User user;
    Lists lists;

    public reportsController(Connection conn, Lists lists, User user) { this.conn = conn; this.lists = lists; this.user=user;}

    @FXML TableView<Appointment> userAppointmentTable;
    @FXML TableColumn<Appointment, String> userNameCol;
    @FXML TableColumn<Appointment, String> customerNameCol;
    @FXML TableColumn<Appointment, String> typeCol;
    @FXML TableColumn<Appointment, Timestamp> startCol;
    @FXML TableColumn<Appointment, Timestamp> endCol;

    @FXML TableView<Appointment> typesByMonthTable;
    @FXML TableColumn<Appointment, String> monthCol;
    @FXML TableColumn<Appointment, Integer> noOfTypesCol;

    @FXML Label upcomingAppLabel;

    @Override public void initialize(URL location, ResourceBundle resources)
    {
        upcomingAppLabel.setText("");
    }

    // Generates userAppointmentTable
    public void generateUserAppointmentTable(ObservableList<Appointment> list){
        userAppointmentTable.setItems(list);
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("end"));
    }

    // Generates typesByMonthTable
    public void generateTypesByMonthTable(ObservableList<Appointment> list) {
        typesByMonthTable.setItems(list);
        monthCol.setCellValueFactory(new PropertyValueFactory<>("monthName"));
        noOfTypesCol.setCellValueFactory(new PropertyValueFactory<>("noOfTypes"));
    }

    // Gets all appointments and sorts them by user
    public void getUserSchedules(MouseEvent event)
    {
        generateUserAppointmentTable(lists.getAppointmentList());
        userAppointmentTable.getSortOrder().add(userNameCol);
    }

    // Gets number of appointment types by month from Lists class
    public void getNoAppTypes(MouseEvent event) throws SQLException {
        generateTypesByMonthTable(lists.getNoOfTypesByMonth());
    }

    // Gets the number of upcoming appointments (not ones that have already passed)
    public void getUpcomingApps(MouseEvent event)
    {
        upcomingAppLabel.setText(Integer.toString(lists.getNoOfUpcomingAppointments()));
    }

    // Changes scene to mainMenu.fxml
    public void goToMain(MouseEvent event) throws IOException {
        stage = (Stage)((Button)event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/mainMenu.fxml"));
        mainMenuController mainMenuController = new mainMenuController(user, conn, lists);
        loader.setController(mainMenuController);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


}

