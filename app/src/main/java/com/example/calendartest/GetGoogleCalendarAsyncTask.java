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
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetGoogleCalendarAsyncTask extends AsyncTask<String, Void, List<Event>> {

    private GoogleAccountCredential credential;
    private ProgressDialog progressDialog;
    private GEventCallback callback;

    public GetGoogleCalendarAsyncTask(GEventCallback callback, Context context, GoogleAccountCredential credential) {
        this.callback = callback;
        this.credential = credential;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("Retrieving events");
        progressDialog.show();
    }

    @Override
    protected List<Event> doInBackground(String... strings) {
        List<Event> eventList = null;
        try {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            Calendar calService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();

            DateTime now = new DateTime(System.currentTimeMillis());

            Events events = calService.events().list("primary")
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            eventList = events.getItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eventList;
    }

    @Override
    protected void onPostExecute(List<Event> eventList) {
        List<MyEvent> myEvents = new ArrayList<>();
        if (eventList != null)
            if (!eventList.isEmpty())
                for (Event event : eventList) {
                    DateTime start = event.getStart().getDateTime();
                    DateTime end = event.getEnd().getDateTime();
                    String startDate;
                    String endDate;
                    Date dateStart;
                    if (start == null) {
                        // All-day events don't have start times, so just use
                        // the start date.
                        start = event.getStart().getDate();
                        startDate = DateUtils.formatAllDayDate(start.toString());
                        dateStart = DateUtils.formatStringToDate(DateUtils.formatAllDayEventDate(startDate));
                    } else {
                        startDate = DateUtils.formatDateTime(start.toString());
                        dateStart = DateUtils.formatEventDateToDate(startDate);
                    }
                    if (end == null) {
                        endDate = "";
                    } else {
                        endDate = DateUtils.formatDateTime(end.toString());
                    }
                    myEvents.add(new MyEvent(event.getSummary(), startDate, endDate, event.getColorId(),
                            event.getDescription(), event.getLocation(), dateStart));
                }

        callback.setEventView(myEvents);
        progressDialog.cancel();
    }
}
