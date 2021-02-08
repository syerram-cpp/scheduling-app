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
import javafx.scene.input.MouseEvent;
import model.*;

import java.io.IOException;
import java.sql.*;
import java.time.*;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javafx.stage.Stage;


public class addAppointmentController implements Initializable
{
    Connection conn = null;
    Stage stage;
    Customer selectedCustomer;
    User user;
    ObservableList<LocalTime> times = FXCollections.observableArrayList();
    Lists lists = null;

    public addAppointmentController(Connection conn, User user, Customer selectedCustomer, Lists lists)
    {
        this.conn=conn;
        this.user = user;
        this.selectedCustomer = selectedCustomer;
        this.lists = lists;
        this.times = lists.getTimes();
    }

    @FXML DatePicker datePicker;
    @FXML ComboBox<String> typeComboBox;
    @FXML ComboBox<LocalTime> startTimeComboBox;
    @FXML ComboBox<LocalTime> endTimeComboBox;
    @FXML TextField customerNameText;
    @FXML TextField titleText;
    @FXML TextField descriptionText;
    @FXML TextField locationText;
    @FXML TextField contactText;
    @FXML TextField urlText;
    @FXML Label errorLabel;
    @FXML Label timeZoneLabel;

    // Dates in datePicker are filtered so no weekends or days that have already passed
    // are allowed to be chosen in the date picker.
    // The customerName textField is set to the current appointment's customer's name.
    // The errorLabel is initialized to an empty string
    // The types combo box is initialized with types list from Lists class
    // The times in the start and end time combo boxes are initialized to the local time zone
    // and only contain times that are in business hours UTC time
    @Override public void initialize(URL location, ResourceBundle resources)
    {
        filterDatePicker(datePicker);
        customerNameText.setText(selectedCustomer.getCustomerName());
        customerNameText.setEditable(false);
        errorLabel.setText("");
        timeZoneLabel.setText("*" + TimeZone.getDefault().getDisplayName());

        typeComboBox.setItems(lists.getTypes());
        startTimeComboBox.setItems(times);
        endTimeComboBox.setItems(times);
    }

    // Takes in inputs from all text fields, all combo boxes, and date picker
    // If any of the text fields, combo boxes, or the date picker are empty or blank,
    // the errorLabel prints a message to the scene prompting the user to fill in all fields.
    // Selected times are checked to see if they overlap with any of the user's
    // existing appointments. If there is overlap, the errorLabel prints to the scene prompting
    // the user to select different times.
    // If the start time is equal to or after the end time, the errorLabel prints to the scene
    // prompting the user to select valid times.
    // If none of the catch blocks run, an appointment is created in the database.
    // An appointment is also created locally and added to the appointmentList in the Lists class
    @FXML public void addAppointment(MouseEvent event) throws SQLException, IOException {
        try {
            String title = titleText.getText();
            String description = descriptionText.getText();
            String location = locationText.getText();
            String contact = contactText.getText();
            String url = urlText.getText();
            String type = typeComboBox.getValue();
            if (titleText.getText().isEmpty() || titleText.getText().matches(" *") ||
                    description.isEmpty() || description.matches(" *") ||
                    location.isEmpty() || location.matches(" *") ||
                    contact.isEmpty() || contact.matches(" *") ||
                    url.isEmpty() || url.matches(" *") ||
                    type.isEmpty() || type.matches(" *")) {
                errorLabel.setText("PLEASE FILL IN ALL FIELDS");
                return;
            }
            String date = datePicker.getValue().toString();
            LocalTime startTime = startTimeComboBox.getValue();
            LocalTime endTime = endTimeComboBox.getValue();

            LocalDateTime ss = Timestamp.valueOf(date + " " + startTime + ":00").toLocalDateTime();
            LocalDateTime ee = Timestamp.valueOf(date + " " + endTime + ":00").toLocalDateTime();
            if (user.checkOverlappingTimes(conn, -1, ss, ee)) {
                errorLabel.setText("PLEASE SELECT DIFFERENT TIMES TO AVOID OVERLAPPING APPOINTMENTS");
                return;
            }
            if (ss.isAfter(ee) || ss.isEqual(ee)) {
                errorLabel.setText("PLEASE SELECT VALID START AND END TIMES");
                return;
            }
            Timestamp start = Timestamp.valueOf(date + " " + startTime + ":00");
            Timestamp end = Timestamp.valueOf(date + " " + endTime + ":00");
            Appointment appointment = new Appointment(conn, selectedCustomer, user, title, description, location, contact, url, type, start, end);
            lists.addAppointment(appointment);
            goToMainMenu(event);
        }
        catch (Exception e) { errorLabel.setText("PLEASE FILL IN ALL FIELDS"); }
    }

    // Changes scene to mainMenu.fxml
    @FXML public void goToMainMenu(MouseEvent event) throws IOException
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

    // Filters dates in the date picker so that dates that fall on weekends or
    // days that have already passed can not be chosen
    public static void filterDatePicker(DatePicker dt)
    {
        dt.setDayCellFactory(picker -> new DateCell()
        {
            @Override public void updateItem(LocalDate date, boolean empty)
            {
                super.updateItem(date, empty);
                setDisable(date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        date.getDayOfWeek() == DayOfWeek.SUNDAY ||
                        date.isBefore(LocalDateTime.now().toLocalDate()));
            }
        });
    }
}

