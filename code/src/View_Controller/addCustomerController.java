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
import java.util.ResourceBundle;

import javafx.stage.Stage;

public class addCustomerController implements Initializable
{
    Connection conn = null;

    User user = null;
    Stage stage;
    ObservableList<String> comboBoxItems = FXCollections.observableArrayList("Brazil", "Canada", "China", "France", "Germany", "India", "Israel", "Japan", "Mexico", "South Korea", "Spain" , "Taiwan", "Turkey", "United Kingdom", "United States");
    Lists lists;

    public addCustomerController(Connection conn, User user, Lists lists) { this.conn=conn; this.user = user; this.lists = lists;}

    @FXML TextField customerNameText;
    @FXML TextField phoneText;
    @FXML TextField addressText;
    @FXML TextField address2Text;
    @FXML TextField cityText;
    @FXML TextField postalCodeText;
    @FXML ComboBox<String> countryComboBox;
    @FXML Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        errorLabel.setText("");
        countryComboBox.setItems(comboBoxItems);
    }

    // Gets input from all text fields and combo boxes
    // If any text fields or combo boxes return empty or blank values,
    // the errorLabel provides a message on scene to fill all fields
    // A new customer is created in the database and locally in the program
    // The customer is added to the customerList from the List class
    // The scene changes to mainMenu.fxml
    @FXML public void addCustomer(MouseEvent event) throws SQLException, IOException {
        try {
            String countryName = countryComboBox.getValue();
            String cityName = cityText.getText();
            String address1 = addressText.getText();
            String address2 = address2Text.getText();
            String postalCode = postalCodeText.getText();
            String phone = phoneText.getText();
            String createdBy = user.getUserName();
            String customerName = customerNameText.getText();

            if (countryName.isEmpty() || countryName.matches(" *") ||
                    cityName.isEmpty() || cityName.matches(" *") ||
                    address1.isEmpty() || address1.matches(" *") ||
                    address2.isEmpty() || address2.matches(" *") ||
                    postalCode.isEmpty() || postalCode.matches(" *") ||
                    phone.isEmpty() || phone.matches(" *") ||
                    customerName.isEmpty() || customerName.matches(" *")) {
                errorLabel.setText("PLEASE FILL IN ALL FIELDS WITH VALID VALUES");
            } else {
                Customer customer = new Customer(conn, countryName, cityName, address1, address2, postalCode, phone, createdBy, customerName, 1);
                lists.addCustomer(customer);
                goToMainMenu(event);
            }
        }
        catch(Exception e) { errorLabel.setText("PLEASE FILL IN ALL FIELDS");}
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
}

