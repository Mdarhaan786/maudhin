package com.muadhinpro.app;

public class PrayerCalculator {
    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double RAD_TO_DEG = 180.0 / Math.PI;

    // Prayer angle constants
    private static final double FAJR_ANGLE = -18.0;
    private static final double ISHA_ANGLE = -17.0;
    private static final double SUNRISE_ANGLE = -0.833333;

    public static double[] calculatePrayerTimes(double latitude, double longitude, 
                                              double timezone, int year, int month, int day) {
        // Array to store prayer times [Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha]
        double[] prayerTimes = new double[6];

        // Calculate Julian Date
        double julianDate = calculateJulianDate(year, month, day);
        
        // Calculate solar position
        double solarPosition = calculateSolarPosition(julianDate);
        
        // Calculate equation of time
        double equationOfTime = calculateEquationOfTime(solarPosition);
        
        // Calculate solar declination
        double solarDeclination = calculateSolarDeclination(solarPosition);

        // Calculate Dhuhr time
        double dhuhrTime = calculateDhuhrTime(longitude, timezone, equationOfTime);
        prayerTimes[2] = dhuhrTime;

        // Calculate Fajr time
        prayerTimes[0] = calculateTimeByAngle(latitude, solarDeclination, FAJR_ANGLE, 
                                            true, dhuhrTime);

        // Calculate Sunrise time
        prayerTimes[1] = calculateTimeByAngle(latitude, solarDeclination, SUNRISE_ANGLE, 
                                            true, dhuhrTime);

        // Calculate Asr time (Shafi'i method - shadow length factor = 1)
        prayerTimes[3] = calculateAsrTime(latitude, solarDeclination, dhuhrTime, 1);

        // Calculate Maghrib time
        prayerTimes[4] = calculateTimeByAngle(latitude, solarDeclination, SUNRISE_ANGLE, 
                                            false, dhuhrTime);

        // Calculate Isha time
        prayerTimes[5] = calculateTimeByAngle(latitude, solarDeclination, ISHA_ANGLE, 
                                            false, dhuhrTime);

        return prayerTimes;
    }

    private static double calculateJulianDate(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor(year / 100.0);
        double B = 2 - A + Math.floor(A / 4.0);
        
        return Math.floor(365.25 * (year + 4716)) + 
               Math.floor(30.6001 * (month + 1)) + 
               day + B - 1524.5;
    }

    private static double calculateSolarPosition(double julianDate) {
        return 2 * Math.PI * (julianDate - 2451545) / 365.25;
    }

    private static double calculateEquationOfTime(double solarPosition) {
        double U = (solarPosition - 2451545) / 36525;
        double L0 = 280.46607 + 36000.7698 * U;
        
        return -1 * (1789 + 237 * U) * Math.sin(L0 * DEG_TO_RAD) - 
               (7146 - 62 * U) * Math.cos(L0 * DEG_TO_RAD) / 60.0;
    }

    private static double calculateSolarDeclination(double solarPosition) {
        return 0.37877 + 23.264 * Math.sin((57.297 * solarPosition - 79.547) * DEG_TO_RAD) + 
               0.3812 * Math.sin((2 * 57.297 * solarPosition - 82.682) * DEG_TO_RAD) + 
               0.17132 * Math.sin((3 * 57.297 * solarPosition - 59.722) * DEG_TO_RAD);
    }

    private static double calculateDhuhrTime(double longitude, double timezone, 
                                           double equationOfTime) {
        return 12 + timezone - longitude / 15.0 - equationOfTime / 60.0;
    }

    private static double calculateTimeByAngle(double latitude, double solarDeclination, 
                                             double angle, boolean isBefore, double dhuhrTime) {
        double G = (Math.sin(angle * DEG_TO_RAD) - 
                   Math.sin(latitude * DEG_TO_RAD) * Math.sin(solarDeclination * DEG_TO_RAD)) / 
                  (Math.cos(latitude * DEG_TO_RAD) * Math.cos(solarDeclination * DEG_TO_RAD));
        
        if (G > 1 || G < -1) return Double.NaN;  // Time doesn't exist for this date
        
        double T = RAD_TO_DEG * Math.acos(G) / 15.0;
        return isBefore ? dhuhrTime - T : dhuhrTime + T;
    }

    private static double calculateAsrTime(double latitude, double solarDeclination, 
                                         double dhuhrTime, double shadowFactor) {
        double asrAngle = RAD_TO_DEG * Math.atan(1 / (shadowFactor + 
                         Math.tan(Math.abs(latitude - solarDeclination) * DEG_TO_RAD)));
        return calculateTimeByAngle(latitude, solarDeclination, 90 - asrAngle, false, dhuhrTime);
    }

    // Convert double time to formatted string (HH:mm)
    public static String formatTime(double time) {
        if (Double.isNaN(time)) return "-----";
        
        int hours = (int) time;
        int minutes = (int) ((time - hours) * 60);
        
        // Ensure 24-hour format
        hours = hours % 24;
        if (hours < 0) hours += 24;
        
        return String.format("%02d:%02d", hours, minutes);
    }
}
