package View_Controller;

import java.net.URL;

import javafx.collections.FXCollections;
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
import java.time.*;
import java.util.ResourceBundle;
import java.util.TimeZone;


import javafx.stage.Stage;



public class updateAppointmentController implements Initializable
{
    Connection conn = null;
    Stage stage;

    User user = null;
    Customer customer = null;
    Appointment selectedAppointment = null;
    ObservableList<LocalTime> times = FXCollections.observableArrayList();
    Lists lists = null;

    public updateAppointmentController(Connection conn, User user, Appointment selectedAppointment, Lists lists) {
        this.conn = conn;
        this.user = user;
        this.selectedAppointment = selectedAppointment;
        this.customer = selectedAppointment.getCustomer();
        this.lists = lists;
        this.times = lists.getTimes();}

    @FXML DatePicker datePicker;
    @FXML ComboBox<String> typeComboBox;
    @FXML ComboBox<LocalTime> startTimeComboBox;
    @FXML ComboBox<LocalTime> endTimeComboBox;
    @FXML Label timeZoneLabel;
    @FXML Label errorLabel;
    @FXML Label customerNameLabel;
    @FXML TableView<Customer> customerTable;
    @FXML TableColumn<Customer, Integer> cusIdCol;
    @FXML TableColumn<Customer, String> cusNameCol;
    @FXML TableColumn<Customer, String> cusPhoneCol;
    @FXML TableColumn<Customer, String> cusAddress1Col;

    // Filters datePicker so no weekends or days that have already passed are allowed to be chosen.
    // Initializes text fields and combo boxes with selectedAppointment's information
    // Sets type, start time, and end time combo box items
    // Generates customer table
    @Override public void initialize(URL location, ResourceBundle resources)
    {
        addAppointmentController.filterDatePicker(datePicker);

        LocalDate localDate = selectedAppointment.getStart().toLocalDateTime().toLocalDate();
        LocalTime localStartTime = selectedAppointment.getStart().toLocalDateTime().toLocalTime();
        LocalTime localEndTime = selectedAppointment.getEnd().toLocalDateTime().toLocalTime();

        errorLabel.setText("");
        timeZoneLabel.setText("*" + TimeZone.getDefault().getDisplayName());
        customerNameLabel.setText(customer.getCustomerName());
        typeComboBox.setValue(selectedAppointment.getType());
        datePicker.setValue(localDate);
        startTimeComboBox.setValue(localStartTime);
        endTimeComboBox.setValue(localEndTime);

        typeComboBox.setItems(lists.getTypes());
        startTimeComboBox.setItems(times);
        endTimeComboBox.setItems(times);

        generateCustomerTable(lists.getCustomerList());
    }

    // Generates customerTable
    public void generateCustomerTable(ObservableList<Customer> list){
        customerTable.setItems(list);
        cusIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        cusNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        cusPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        cusAddress1Col.setCellValueFactory(new PropertyValueFactory<>("address1"));
    }

    // Gets selected customer from customerTable
    // If no customer is selected, the original customer for the appointment is taken as the customer
    // Gets all inputted fields
    // Checks if inputted date and times overlap with any of the user's other appointments
    // Checks if start time is before end time
    // Updates appointment in database and locally
    // Goes to mainMenu.fxml
    public void updateAppointment(MouseEvent event) throws SQLException, IOException {
        try {
            Customer customer1 = null;
            if(!customerTable.getSelectionModel().isEmpty()){ customer1 = customerTable.getSelectionModel().getSelectedItem(); }
            else { customer1 = this.customer;}
            String type = typeComboBox.getValue();
            String date = datePicker.getValue().toString();
            LocalTime startTime = startTimeComboBox.getValue();
            LocalTime endTime = endTimeComboBox.getValue();
            LocalDateTime ss = Timestamp.valueOf(date + " " + startTime + ":00").toLocalDateTime();
            LocalDateTime ee = Timestamp.valueOf(date + " " + endTime + ":00").toLocalDateTime();
            if (user.checkOverlappingTimes(conn, selectedAppointment.getAppointmentId(), ss, ee)) {
                errorLabel.setText("PLEASE SELECT DIFFERENT TIMES TO AVOID OVERLAPPING APPOINTMENTS");
                return;
            }
            if(ss.isAfter(ee) || ss.isEqual(ee)) { errorLabel.setText("PLEASE SELECT VALID START AND END TIMES"); return;}
            Timestamp start = Timestamp.valueOf(date + " " + startTime + ":00");
            Timestamp end = Timestamp.valueOf(date + " " + endTime + ":00");
            selectedAppointment.updateAppointment(conn, customer1, type, start, end);
            goToMainMenu(event);
        }
        catch(Exception e) {errorLabel.setText("PLEASE FILL IN ALL FIELDS"); }
    }

    // Changes scene to mainMenu.fxml
    public void goToMainMenu(MouseEvent event) throws IOException
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


}


