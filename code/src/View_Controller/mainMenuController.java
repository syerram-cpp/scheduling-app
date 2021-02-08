package View_Controller;

import functionalInterfaces.UpdateAppCustomer;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import model.Customer;
import model.Appointment;
import model.Lists;
import model.User;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javafx.stage.Stage;

public class mainMenuController<TableList> implements Initializable
{
    Stage stage;
    Connection conn = null;

    private User user = null;
    Lists lists = null;

    public mainMenuController(User user, Connection conn, Lists lists) {
        this.user = user; this.conn=conn; this.lists = lists;}

    @FXML TableView<Customer> customerTable;
    @FXML private TableColumn<Customer,Integer> cusIdCol;
    @FXML private TableColumn<Customer,String> cusNameCol;
    @FXML private TableColumn<Customer,String> cusAddressCol;
    @FXML private TableColumn<Customer,String> cusPhoneCol;

    @FXML TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment,Integer> appIdCol;
    @FXML private TableColumn<Appointment,String> appTypeCol;
    @FXML private TableColumn<Appointment,String> appCusNameCol;
    @FXML private TableColumn<Appointment,Timestamp> appStartTimeCol;
    @FXML private TableColumn<Appointment,Timestamp> appEndTimeCol;

    @FXML RadioButton allRadioButton;
    @FXML Label customerErrorLabel;
    @FXML Label appointmentErrorLabel;
    @FXML Label timeZoneLabel;

    // Initializes error labels to empty strings
    // Generates customer and appointment tables with lists from Lists class
    // Sets specific columns on customer table to be editable by end user
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        timeZoneLabel.setText("*" + TimeZone.getDefault().getDisplayName());
        customerErrorLabel.setText("");
        appointmentErrorLabel.setText("");

        allRadioButton.fire();

        generateCustomerTable(lists.getCustomerList());
        generateAppointmentTable(lists.getAppointmentList());
        customerTable.setEditable(true);
        cusNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        cusAddressCol.setCellFactory(TextFieldTableCell.forTableColumn());
        cusPhoneCol.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    // Generates customerTable
    public void generateCustomerTable(ObservableList<Customer> list){
        customerTable.setItems(list);
        cusIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        cusNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        cusAddressCol.setCellValueFactory(new PropertyValueFactory<>("address1"));
        cusPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    // Generates Appointment Table
    public void generateAppointmentTable(ObservableList<Appointment> list){
        appointmentTable.setItems(list);
        appIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        appTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        appCusNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        appStartTimeCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        appEndTimeCol.setCellValueFactory(new PropertyValueFactory<>("end"));
    }

    // Displays all appointments in appointmentTable
    @FXML public void filterByAll(MouseEvent event)
    {
        generateAppointmentTable(lists.getAppointmentList());
    }

    // Filters appointments in appointmentTable by current month
    @FXML public void filterByMonth(MouseEvent event)
    {
        generateAppointmentTable(lists.getAppByMonth());
    }

    // Filters appointments in appointmentTable by current week of the year
    @FXML public void filterByWeek(MouseEvent event)
    {
        generateAppointmentTable(lists.getAppByWeek());
    }

    // LAMBDAS
    // Updates the appointmentTable when a customer in the customerTable is edited
    // Only 'updateCustomerName' is needed with the current appointmentTable
    // But if the appointmentTable is changed to display customer address and phone as well,
    // this would update the appointmentTable to reflect those updates to the customerTable
    // This allows the Lists class to have only one function called, 'updateAppointmentCustomer'
    // when the customerTable is updated instead of 3 separate functions
    // for customerName, customerAddress, and customerPhone that have overlapping functionality

    // Takes in appointment and string and sets a field (either name, address, or phone)
    // of the appointment equal to the inputted string
    UpdateAppCustomer updateCustomerName = (a, cInfo) -> a.setCustomerName(cInfo);
    UpdateAppCustomer updateCustomerAddress = (a, cInfo) -> a.setCustomerAddress(cInfo);
    UpdateAppCustomer updateCustomerPhone = (a, cInfo) -> a.setCustomerPhone(cInfo);

    // If inputted customerName is not empty or blank, the customer table updates the edited cell to store the inputted value
    // customerName of corresponding customer is updated in the database and locally in the program
    // Updates corresponding appointments (locally in the program) to reflect new customerName
    // Refreshes the appointment table so updated customer names are displayed
    @FXML public void updateCustomerName(TableColumn.CellEditEvent edditedCell) throws SQLException, IOException {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        String newCustomerName = edditedCell.getNewValue().toString();
        if(newCustomerName.isEmpty() || newCustomerName.matches(" *")) {
            int row = edditedCell.getTablePosition().getRow();
            customerTable.getItems().set(row, selectedCustomer);
        }
        else {
            selectedCustomer.setCustomerName(conn, newCustomerName);
            lists.updateAppointmentCustomer(selectedCustomer.getCustomerId(), selectedCustomer.getCustomerName(), updateCustomerName);
            appointmentTable.refresh();
        }
    }

    // If inputted customerAddress is not empty or blank, the customer table updates the edited cell to store the inputted value
    // customerAddress of corresponding customer is updated in the database and locally in the program
    // Updates corresponding appointments (locally in the program) to reflect new customerAddress
    @FXML
    public void updateCustomerAddress(TableColumn.CellEditEvent edditedCell) throws SQLException
    {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        String newCustomerAddress = edditedCell.getNewValue().toString();
        if(newCustomerAddress.isEmpty() || newCustomerAddress.matches(" *")) {
            int row = edditedCell.getTablePosition().getRow();
            customerTable.getItems().set(row, selectedCustomer);
        }
        else { selectedCustomer.setAddress1(conn, edditedCell.getNewValue().toString());
            lists.updateAppointmentCustomer(selectedCustomer.getCustomerId(), selectedCustomer.getCustomerName(), updateCustomerAddress);
        }
    }

    // If inputted customerPhone is not empty or blank, the customer table updates the edited cell to store the inputted value
    // customerPhone of corresponding customer is updated in the database and locally in the program
    // Updates corresponding appointments (locally in the program) to reflect new customerPhone
    @FXML
    public void updateCustomerPhone(TableColumn.CellEditEvent edditedCell) throws SQLException
    {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        String newCustomerPhone = edditedCell.getNewValue().toString();
        if(newCustomerPhone.isEmpty() || newCustomerPhone.matches(" *")) {
            int row = edditedCell.getTablePosition().getRow();
            customerTable.getItems().set(row, selectedCustomer);
        }
        else { selectedCustomer.setPhone(conn, edditedCell.getNewValue().toString());
            lists.updateAppointmentCustomer(selectedCustomer.getCustomerId(), selectedCustomer.getCustomerName(), updateCustomerPhone);
        }
    }

    // Changes scene to addCustomer.fxml
    @FXML public void goToAddCustomer(MouseEvent event) throws IOException, SQLException {
        stage = (Stage)((Button)event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/addCustomer.fxml"));
        addCustomerController addCustomerController = new addCustomerController(conn, user, lists);
        loader.setController(addCustomerController);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // Passes selected customer from customerTable to addAppointmentController
    // If no customer is selected, errorLabel provides a message on scene to select a customer
    // Changes scene to addAppointment.fxml
    @FXML
    public void goToAddAppointment(MouseEvent event) throws IOException
    { try {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/addAppointment.fxml"));
        addAppointmentController addAppointmentController = new addAppointmentController(conn, user, selectedCustomer, lists);
        loader.setController(addAppointmentController);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    catch(Exception e){ appointmentErrorLabel.setText("PLEASE SELECT A CUSTOMER TO ADD AN APPOINTMENT"); }
    }

    // Selected appointment on appointmentTable is retrieved.
    // If no appointment is selected, errorLabel provides message on scene to select an appointment
    // Selected appointment is then input into the updateAppointment controller
    // Scene changes to updateAppointment.fxml
    @FXML
    public void goToUpdateAppointment(MouseEvent event) throws IOException
    { try {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/updateAppointment.fxml"));
        updateAppointmentController updateAppointmentController = new updateAppointmentController(conn, user, selectedAppointment, lists);
        loader.setController(updateAppointmentController);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    catch(Exception e){ appointmentErrorLabel.setText("PLEASE SELECT AN APPOINTMENT TO UPDATE"); }
    }

    // Changes scene to reports.fxml
    @FXML
    public void goToReports(MouseEvent event) throws IOException {
        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View_Controller/reports.fxml"));
        reportsController reportsController = new reportsController(conn, lists, user);
        loader.setController(reportsController);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // Confirmation alert displays when user clicks the delete customer button
    // If user presses 'OK':
        // Selected customer on customerTable is retrieved.
        // If no customer is selected, errorLabel provides message on scene to select a customer
        // If customer has appointments, errorLabel provides message on scene to delete all appointments for said customer
        // Otherwise, Selected customer is removed from customerList in Lists class and
        // is deleted from the database
    // If user presses 'CANCEL': no delete takes place
    @FXML
    public void deleteCustomer(MouseEvent event) throws IOException, SQLException
    {
        if(deleteAlert().equals("OK"))
        {
            try
            {
                Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
                selectedCustomer.deleteCustomer(conn);
                lists.deleteCustomer(selectedCustomer);
                generateCustomerTable(lists.getCustomerList());
            }
            catch(Exception e)
            {
                try { Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();}
                catch(Exception e1) { customerErrorLabel.setText("PLEASE SELECT A CUSTOMER TO DELETE"); return; }

                customerErrorLabel.setText("PLEASE DELETE ALL APPOINTMENTS FOR THIS CUSTOMER FIRST");

            }
        }
    }

    // Confirmation alert displays when user clicks the delete appointment button
    // If user presses 'OK':
        // Selected appointment on customerTable is retrieved.
        // If no appointment is selected, errorLabel provides message on scene to select an appointment
        // Otherwise, Selected appointment is removed from appointmentList in Lists class and
        // is deleted from the database
    // If user presses 'CANCEL': no delete takes place
    @FXML public void deleteAppointment(MouseEvent event) throws IOException, SQLException
    {
        if(deleteAlert().equals("OK"))
        {
            try {
                Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
                selectedAppointment.deleteAppointment(conn);
                lists.deleteAppointment(selectedAppointment);
                generateAppointmentTable(lists.getAppointmentList());
            }
            catch (Exception e) { appointmentErrorLabel.setText("PLEASE SELECT AN APPOINTMENT TO DELETE"); }
        }
    }

    // Launches when a delete button (for either appointment or customer) is pressed
    // If 'OK' button is pressed, delete takes place
    // If 'CANCEL' button is pressed, delete doesn't take place
    private String deleteAlert()
    {
        Alert a1 = new Alert(Alert.AlertType.CONFIRMATION);
        a1.setContentText("Are you sure you want to delete this?");
        Optional<ButtonType> response = a1.showAndWait();
        if(response.get() == ButtonType.OK)
        {
            return "OK";
        }
        return "CANCEL";
    }

}

