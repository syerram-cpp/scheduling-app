package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import functionalInterfaces.MonthOrByWeek;
import functionalInterfaces.UpdateAppCustomer;
import util.DBQuery;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;

public class Lists
{
    private Connection conn;
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    private ObservableList<Appointment> appByMonth = FXCollections.observableArrayList();
    private ObservableList<Appointment> appByWeek = FXCollections.observableArrayList();
    private ObservableList<LocalTime> times = FXCollections.observableArrayList();
    ObservableList<String> types = FXCollections.observableArrayList("Standard", "Bronze", "Silver" , "Gold", "Carwash");

    // FOR REPORT
    private ObservableList<Appointment> noOfTypesByMonth = FXCollections.observableArrayList();
    private int noOfUpcomingAppointments;

    // Initializes customerList and appointmentList and times
    // Creates list of local times that are in business hours UTC
    public Lists(Connection conn, User user) throws IOException, SQLException
    {
        this.conn = conn;
        customerList = Customer.getAllCustomers(conn);
        appointmentList = Appointment.getAllAppointments(conn);
        this.noOfUpcomingAppointments = Appointment.getInitialNoOfUpcomingApp();

        int offset = OffsetDateTime.now().getOffset().getTotalSeconds()/3600;
        setLocalTimes(times, offset);
    }

    public ObservableList<Customer> getCustomerList()
    { return customerList;}

    public ObservableList<Appointment> getAppointmentList()
    { return appointmentList;}

    // Generates appointments by month list every time it's called
    public ObservableList<Appointment> getAppByMonth()
    {
        appByMonth.clear();
        generateAppByMonthOrWeek(appByMonth, testByMonth);
        return appByMonth;
    }

    // Generates appointments by week list every time it's called
    public ObservableList<Appointment> getAppByWeek()
    {
        appByWeek.clear();
        generateAppByMonthOrWeek(appByWeek, testByWeek);
        return appByWeek;
    }

    // Generates number of types by month list every time it's called
    public ObservableList<Appointment> getNoOfTypesByMonth() throws SQLException
    {
        noOfTypesByMonth.clear();
        generateNoOfAppTypes(noOfTypesByMonth);
        return noOfTypesByMonth;
    }

    public ObservableList<String> getTypes()
    { return types; }

    public ObservableList<LocalTime> getTimes()
    { return times;}

    // Returns number of upcoming appointments
    public int getNoOfUpcomingAppointments()
    { return noOfUpcomingAppointments;}

    // Adds customer to customerList
    public void addCustomer(Customer customer)
    { customerList.add(customer);
    }

    // Removes customer from customerList
    public void deleteCustomer(Customer customer)
    { customerList.remove(customer);}

    // Adds appointment to appointmentList and
    // adds 1 to noOfUpcomingAppointments if the new appointment's
    // start time is after the current time
    public void addAppointment(Appointment appointment)
    { appointmentList.add(appointment);
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime date = appointment.getStart().toLocalDateTime();
        if(date.isAfter(currentDate)) { noOfUpcomingAppointments++; }
    }

    // Removes appointment from appointmentList and
    // subtracts 1 from noOfUpcomingAppointments if the appointment's
    // start time was after the current time
    public void deleteAppointment(Appointment appointment)
    { appointmentList.remove(appointment);
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime date = appointment.getStart().toLocalDateTime();
        if(date.isAfter(currentDate)) { noOfUpcomingAppointments--; }

       try { appByMonth.remove(appointment); } catch(Exception e) {}
       try{ appByWeek.remove(appointment);} catch(Exception e) {}
    }

    // Takes in lambda that updates an Appointment's customer fields
    // The lambda decides which fields are updated
    public void updateAppointmentCustomer(int customerId, String customerInfo, UpdateAppCustomer update) throws SQLException {
        for(int i=0; i<appointmentList.size(); i++)
        {
            int cusId = appointmentList.get(i).getCustomerId();

            if(cusId==customerId)
            {
                update.setInfo(appointmentList.get(i), customerInfo);
            }
        }
    }

    //Business Hours are 2pm to 3am UTC time
    public void setLocalTimes(ObservableList<LocalTime> times, long offset)
    {
        LocalTime startTime = LocalTime.of(14, 0, 0).plusHours(offset);
        times.add(startTime);
        for(int i=0; i<52; i++)
        {
            startTime = startTime.plusMinutes(15l);
            times.add(startTime);
        }
    }

    //LAMBDAS
    // Allows the generateAppByMonthOrWeek() method to filter appointments
    // by month and week rather than creating two separate functions for
    // generating appointments by month and generating appointments by week

    // Returns true if inputted month and year equal the current month and year
    MonthOrByWeek testByMonth = (currentYear, year, month, week) ->
    {
        int currentMonth = LocalDate.now().getMonthValue();
        return(month == currentMonth && year == currentYear);
    };

    //LAMBDA
    // Returns true if inputted week and year equal the current week and year
    MonthOrByWeek testByWeek = (currentYear, year, month, week) ->
    {
        Calendar currentCalendar = Calendar.getInstance();
        int currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        return(week == currentWeek && year == currentYear);
    };

    // Takes in a lambda and generates list of appointments
    // either in the current month or the current week depending on the lambda
    public void generateAppByMonthOrWeek(ObservableList<Appointment> appByMonthOrWeek, MonthOrByWeek monthOrWeek) {
        int currentYear = LocalDate.now().getYear();

        for(int i=0; i<appointmentList.size(); i++) {
            int month = appointmentList.get(i).getStart().toLocalDateTime().toLocalDate().getMonthValue();
            int year = appointmentList.get(i).getStart().toLocalDateTime().toLocalDate().getYear();
            Calendar appCalendar = Calendar.getInstance();
            Date date = new Date(appointmentList.get(i).getStart().getTime());
            appCalendar.setTime(date);
            int week = appCalendar.get(Calendar.WEEK_OF_YEAR);

            if(monthOrWeek.test(currentYear, year, month, week)) {
                appByMonthOrWeek.add(appointmentList.get(i)); }
        }
    }

    // Generates number of appointment types by month list that stores appointment objects
    // Appointments are created with an overloaded constructor that takes in monthName and the numberOfTypes
    public void generateNoOfAppTypes(ObservableList<Appointment> noOfTypesByMonth) throws SQLException {
        String getNoAppTypes = "SELECT MONTHNAME(start), MONTH(start), COUNT(DISTINCT type) FROM appointment " +
                "GROUP BY MONTHNAME(start), MONTH(start) " +
                "ORDER BY MONTH(start)";
        DBQuery.setPreparedStatement(conn, getNoAppTypes);
        PreparedStatement ps = DBQuery.getPreparedStatement();
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while(rs.next())
        {
            String month = rs.getString(1);
            int noOfTypes = rs.getInt(3);
            noOfTypesByMonth.add(new Appointment(month, noOfTypes));
        }

    }
}

