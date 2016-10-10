package com.example.calendartest;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    // google calendar events
    public static String formatDateTime(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatAllDayDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatEventDateTime(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                SimpleDateFormat output = new SimpleDateFormat("ddMMyyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatAllDayEventDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat output = new SimpleDateFormat("ddMMyyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatEventDateTimeToTime(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                SimpleDateFormat output = new SimpleDateFormat("HH:mm");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatDateToFullDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                SimpleDateFormat output = new SimpleDateFormat("EEEE, dd MMMM");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatEventDateToFullDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                SimpleDateFormat output = new SimpleDateFormat("EEEE, dd MMMM");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatAllDayEventDateToFullDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat output = new SimpleDateFormat("EEEE, dd MMMM");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    // outlook calendar events
    public static String getLocalTime(String time) {
        String newTime = "";
        try {
            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = utcFormat.parse(time);

            DateFormat pstFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            pstFormat.setTimeZone(TimeZone.getDefault());

            newTime = pstFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newTime;
    }

    public static String formatEventOutlookDateAllDay(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static Date formatEventDateToDate(String dateString) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /////////////////////////////////////////////////////////////////////////
    public static String formatJsonDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatDateForAPI(String date) {
        if (!date.isEmpty()) {
            String newDate = null;
            if (date.contains("/")) {
                newDate = date.replace("/", "");
            } else if (date.contains("-")) {
                newDate = date.replace("-", "");
            }
            return newDate;
        }
        return date;
    }

    public static boolean checkDate(String startDateStr, String endDateStr) {
        if (!endDateStr.isEmpty()) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date startDate = simpleDateFormat.parse(startDateStr);
                Date endDate = simpleDateFormat.parse(endDateStr);
                if (startDate.after(endDate)) {
                    return false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static String formatDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatDemographicDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat output = new SimpleDateFormat("ddMMyyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatReverseDemographicDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatDateWithDashesToDateWithSlashes(String date) {
        String newDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
                Date d = sdf.parse(date);
                newDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return newDate;
    }

    public static String formatNoSlashDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static Date formatStringToDate(String dateString) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formatDateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        return sdf.format(date);
    }

    public static String getDecimalValue(String decimalFormat, int number) {
        if (decimalFormat.isEmpty()) {
            decimalFormat = "00";
        }
        DecimalFormat format = new DecimalFormat(decimalFormat);
        return format.format(Double.valueOf(number));
    }

    public static String formatSHealthDate(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatSHealthDateForAPI(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                SimpleDateFormat output = new SimpleDateFormat("ddMMyyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static Calendar formatDateToCalendar(Date date) {
        Calendar maxDate = Calendar.getInstance();
        maxDate.setTime(date);
        maxDate.set(Calendar.HOUR_OF_DAY, 23);
        maxDate.set(Calendar.MINUTE, 59);
        maxDate.set(Calendar.SECOND, 59);
        return maxDate;
    }

    public static String getExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 10);
        return DateUtils.formatDateToString(calendar.getTime());
    }

    public static String formatDateToDayOfWeek(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                SimpleDateFormat output = new SimpleDateFormat("EEE, dd/MM/yyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static String formatDateFromDayOfWeek(String date) {
        String formattedDate = "";
        if (!date.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MM/yyyy");
                SimpleDateFormat output = new SimpleDateFormat("ddMMyyyy");
                Date d = sdf.parse(date);
                formattedDate = output.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return formattedDate;
    }

    public static Date formatStringDateWeekToDate(String dateString) {
        Date date = null;
        if ((!dateString.isEmpty())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                date = sdf.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }
}