package functionalInterfaces;

import model.Appointment;

// Updates the appointmentTable when a customer in the customerTable is edited
// Only 'updateCustomerName' is needed with the current appointmentTable
// But if the appointmentTable is changed to display customer address and phone as well,
// this would update the appointmentTable to reflect those updates to the customerTable
// This allows the Lists class to have only one function called, 'updateAppointmentCustomer'
// when the customerTable is updated instead of 3 separate functions
// for customerName, customerAddress, and customerPhone that have overlapping functionality

@FunctionalInterface
public interface UpdateAppCustomer
{
    void setInfo(Appointment appointment, String customerInfo);
}
