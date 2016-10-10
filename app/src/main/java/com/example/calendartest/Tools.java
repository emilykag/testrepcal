package com.example.calendartest;

import android.content.Context;

public class Tools {

    public static int getDipsFromPixel(float pixels, Context context) {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    public static String getEventColor(MyEvent event) {
        String color = "#9FE1E7";
        if (event.getColor() != null)
            switch (event.getColor()) {
                case "1":
                    color = "#A4BDFC";
                    break;
                case "2":
                    color = "#7AE7BF";
                    break;
                case "3":
                    color = "#DBADFF";
                    break;
                case "4":
                    color = "#FF887C";
                    break;
                case "5":
                    color = "#FBD75B";
                    break;
                case "6":
                    color = "#FFB878";
                    break;
                case "7":
                    color = "#46D6DB";
                    break;
                case "8":
                    color = "#E1E1E1";
                    break;
                case "9":
                    color = "#5484ED";
                    break;
                case "10":
                    color = "#51B749";
                    break;
                case "11":
                    color = "#DC2127";
                    break;
                case "12":
                    color = "#0072C6";
                    break;
            }
        return color;
    }
}
