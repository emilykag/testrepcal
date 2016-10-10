package com.example.calendartest;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;

public class CreateEventGoogleCalendarAsyncTask extends AsyncTask<String, Void, String> {

    private GoogleAccountCredential credential;
    private ProgressDialog progressDialog;
    private GEventCallback callback;

    public CreateEventGoogleCalendarAsyncTask(GEventCallback callback, Context context, GoogleAccountCredential credential) {
        this.callback = callback;
        this.credential = credential;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("Creating event");
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        String str = "";
        try {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            Calendar calService = new Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();

            DateTime now = new DateTime(System.currentTimeMillis());
            Event event = new Event()
                    .setSummary("Google I/O 2016")
                    .setLocation("800 Howard St., San Francisco, CA 94103")
                    .setDescription("A chance to hear more about Google's developer products.");
            DateTime startDateTime = new DateTime("2016-10-12T09:00:00-07:00");
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setStart(start);

            DateTime endDateTime = new DateTime("2016-10-12T17:00:00-07:00");
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("America/Los_Angeles");
            event.setEnd(end);


            Event result = calService.events().insert("primary", event).execute();
            str = result.getSummary();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    @Override
    protected void onPostExecute(String s) {
        System.out.println(s);
        progressDialog.cancel();
    }
}
