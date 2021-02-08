package functionalInterfaces;

// Allows the generateAppByMonthOrWeek() method to filter appointments
// by month and week rather than creating two separate functions for
// generating appointments by month and generating appointments by week

@FunctionalInterface
public interface MonthOrByWeek
{
    boolean test(int currentYear, int year, int month, int week);
}
