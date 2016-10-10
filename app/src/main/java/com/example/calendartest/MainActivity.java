package com.example.calendartest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.services.orc.auth.AuthenticationCredentials;
import com.microsoft.services.orc.core.*;
import com.microsoft.services.orc.http.Credentials;
import com.microsoft.services.orc.http.impl.LoggingInterceptor;
import com.microsoft.services.orc.http.impl.OAuthCredentials;
import com.microsoft.services.orc.http.impl.OkHttpTransport;
import com.microsoft.services.orc.serialization.impl.GsonSerializer;
import com.microsoft.services.outlook.DateTimeTimeZone;
import com.microsoft.services.outlook.Event;
import com.microsoft.services.outlook.fetchers.OutlookClient;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.crypto.NoSuchPaddingException;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, GEventCallback {

    private GoogleAccountCredential credential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    private CaldroidFragment caldroidFragment;
    private CoordinatorLayout coordinatorLayout;
    private FrameLayout frameLayoutCalendar;
    private BottomSheetBehavior bottomSheetBehavior;
    private ListView listViewEvents;
    private TextView textViewTodayDate;
    private TextView textViewNoEvents;

    private int calHeight;
    private int screenHeight;

    private Date selectedDate = null;

    private HashMap<Date, Drawable> backgroundForDateMap;

    // outlook variables
    private static final int OUTLOOK_RESULT_OK = 2003;
    private AuthenticationContext authContext;
    private DependencyResolver resolver;
    private OutlookClient outlookClient;
    private String[] outlookScopes = new String[]{"https://outlook.office.com/calendars.readwrite"};

    //private ProgressDialog progressDialogOutlook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        frameLayoutCalendar = (FrameLayout) findViewById(R.id.frameLayoutCalendar);
        listViewEvents = (ListView) findViewById(R.id.listViewEvents);
        textViewTodayDate = (TextView) findViewById(R.id.textViewTodayDate);
        textViewNoEvents = (TextView) findViewById(R.id.textViewNoEvents);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        onLoad();

        listViewEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showEventDetailsDialog((MyEvent) adapterView.getAdapter().getItem(i));
            }
        });
    }

    private void onLoad() {
        // Initialize credentials and service object.
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        setCaldroidCalendar();
    }

    private void setCaldroidCalendar() {
        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
        caldroidFragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutCalendar, caldroidFragment)
                .commit();

        frameLayoutCalendar.post(new Runnable() {
            @Override
            public void run() {
                calHeight = frameLayoutCalendar.getHeight();
                screenHeight = coordinatorLayout.getHeight();
                int bottomSheetHeight = screenHeight - calHeight;
                bottomSheetBehavior.setPeekHeight(bottomSheetHeight);
            }
        });

        caldroidFragment.setCaldroidListener(new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                if (selectedDate != null)
                    caldroidFragment.clearBackgroundDrawableForDate(selectedDate);
                caldroidFragment.setBackgroundDrawableForDates(backgroundForDateMap);
                caldroidFragment.setBackgroundDrawableForDate(getResources().getDrawable(R.drawable.selected_date_calendar), date);
                caldroidFragment.refreshView();
                selectedDate = date;
                populateEventsList(DateUtils.formatDateToString(date));
            }
        });
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) { // check if google play services are available
            acquireGooglePlayServices();
        } else if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) { // check network connection
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            new GetGoogleCalendarAsyncTask(this, this, credential).execute();
        }
    }

    @Override
    public void setEventView(List<MyEvent> events) {
        Global.allEvents.removeAll(Global.googleEvents);
        Global.googleEvents.clear();
        Global.googleEvents = events;
        Global.allEvents.addAll(Global.googleEvents);
        Calendar cal = Calendar.getInstance();
        populateEventsList(DateUtils.formatDateToString(cal.getTime()));
        setDrawableForCalendarEvents();
    }

    private void populateEventsList(String comparingDate) {
        Collections.sort(Global.allEvents);
        textViewTodayDate.setText(DateUtils.formatDateToFullDate(comparingDate));
        List<MyEvent> todayEvents = new ArrayList<>();
        for (MyEvent event : Global.allEvents) {
            if (!event.getEnd().isEmpty()) {
                if (DateUtils.formatEventDateTime(event.getStart()).equals(comparingDate)) {
                    todayEvents.add(event);
                }
            } else {
                if (DateUtils.formatAllDayEventDate(event.getStart()).equals(comparingDate)) {
                    todayEvents.add(event);
                }
            }
        }
        EventListAdapter adapter = new EventListAdapter(this, todayEvents);
        listViewEvents.setAdapter(adapter);
        if (!todayEvents.isEmpty()) {
            if (textViewNoEvents.isShown()) {
                textViewNoEvents.setVisibility(View.GONE);
            }
        } else {
            textViewNoEvents.setVisibility(View.VISIBLE);
        }
    }

    private void setDrawableForCalendarEvents() {
        Calendar cal = Calendar.getInstance();
        backgroundForDateMap = new HashMap<>();
        for (MyEvent event : Global.allEvents) {
            if (!event.getEnd().isEmpty()) {
                //if (!DateUtils.formatEventDateTime(event.getStart()).equals(DateUtils.formatDateToString(cal.getTime()))) {
                backgroundForDateMap.put(DateUtils.formatStringToDate(DateUtils.formatEventDateTime(event.getStart())), getResources().getDrawable(R.drawable.calendar_has_event));
                //}
            } else {
                //if (!DateUtils.formatAllDayEventDate(event.getStart()).equals(DateUtils.formatDateToString(cal.getTime()))) {
                backgroundForDateMap.put(DateUtils.formatStringToDate(DateUtils.formatAllDayEventDate(event.getStart())), getResources().getDrawable(R.drawable.calendar_has_event));
                //}
            }
        }

        caldroidFragment.setBackgroundDrawableForDates(backgroundForDateMap);
        caldroidFragment.refreshView();
    }

    private void showEventDetailsDialog(MyEvent event) {
        Dialog dialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dialog = new Dialog(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            dialog = new Dialog(this);
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.events_dialog_layout);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
        dialog.show();

        FrameLayout frameLayoutEventColor = (FrameLayout) dialog.findViewById(R.id.frameLayoutEventColor);
        TextView textViewEventTitle = (TextView) dialog.findViewById(R.id.textViewEventTitle);
        TextView textViewEventDate = (TextView) dialog.findViewById(R.id.textViewEventDate);
        TextView textViewEventTime = (TextView) dialog.findViewById(R.id.textViewEventTime);
        TextView textViewEventDescription = (TextView) dialog.findViewById(R.id.textViewEventDescription);
        TextView textViewEventLocation = (TextView) dialog.findViewById(R.id.textViewEventLocation);

        GradientDrawable shape = (GradientDrawable) frameLayoutEventColor.getBackground();
        shape.setColor(Color.parseColor(Tools.getEventColor(event)));

        textViewEventTitle.setText(event.getSummary());
        if (!event.getEnd().isEmpty()) {
            textViewEventDate.setText(DateUtils.formatEventDateToFullDate(event.getStart()));
            textViewEventTime.setText(DateUtils.formatEventDateTimeToTime(event.getStart()) + " - " + DateUtils.formatEventDateTimeToTime(event.getEnd()));
        } else {
            textViewEventDate.setText(DateUtils.formatAllDayEventDateToFullDate(event.getStart()));
            textViewEventTime.setVisibility(View.GONE);
        }
        textViewEventDescription.setText(event.getDescription());
        textViewEventLocation.setText(event.getLocation());
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                credential.setSelectedAccountName(accountName);
                getResultsFromApi();
                //createGoogleEvents();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        credential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();
                } else {
                    getResultsFromApi();
                    //createGoogleEvents();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        credential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                        //createGoogleEvents();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                    //createGoogleEvents();
                } else if (resultCode == OUTLOOK_RESULT_OK) {
                    authContext.onActivityResult(requestCode, resultCode, data);
//                    progressDialogOutlook = new ProgressDialog(this);
//                    progressDialogOutlook.setMessage("Retrieving events");
//                    progressDialogOutlook.show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private void createGoogleEvents() {
        if (!isGooglePlayServicesAvailable()) { // check if google play services are available
            acquireGooglePlayServices();
        } else if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) { // check network connection
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            new CreateEventGoogleCalendarAsyncTask(this, this, credential).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_connect_google) {
            getResultsFromApi();
            return true;
        } else if (id == R.id.action_connect_outlook) {
            Futures.addCallback(logon(), new FutureCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    outlookClient = new OutlookClient(GlobalStr.BASE_URL, resolver);
                    getOutlookEvents();
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });

            return true;
        } else if (id == R.id.action_create_outlook_event) {
            Futures.addCallback(logon(), new FutureCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    outlookClient = new OutlookClient(GlobalStr.BASE_URL, resolver);
                    createOutlookEvent();
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });
            return true;
        } else if (id == R.id.action_create_google_event) {
            createGoogleEvents();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    // methods for google play services
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    // connect with outlook
    public SettableFuture<Boolean> logon() {
        final SettableFuture<Boolean> result = SettableFuture.create();
        try {
            authContext = new AuthenticationContext(this, GlobalStr.AUTHORITY_URL, true);
        } catch (Exception e) {
            System.out.println("Failed to initialize Authentication Context with error: " + e.getMessage());
            authContext = null;
            result.setException(e);
        }

        if (authContext != null) {
            authContext.acquireToken(
                    this,
                    outlookScopes,
                    null,
                    GlobalStr.CLIENT_ID,
                    GlobalStr.REDIRECT_URI,
                    PromptBehavior.Auto,
                    new AuthenticationCallback<AuthenticationResult>() {
                        @Override
                        public void onSuccess(final AuthenticationResult authenticationResult) {
                            if (authenticationResult != null && authenticationResult.getStatus() == AuthenticationResult.AuthenticationStatus.Succeeded) {
                                resolver = new DependencyResolver.Builder(
                                        new OkHttpTransport().setInterceptor(new LoggingInterceptor()), new GsonSerializer(),
                                        new AuthenticationCredentials() {
                                            @Override
                                            public Credentials getCredentials() {
                                                return new OAuthCredentials(authenticationResult.getAccessToken());
                                            }
                                        }).build();

                                result.set(true);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            result.setException(e);
                        }
                    }
            );
        }
        return result;
    }

    private void createOutlookEvent() {
        Event event = getSampleEvent();
        Futures.addCallback(outlookClient.getMe().getCalendars().getById("Calendar").getEvents().add(event), new FutureCallback<Event>() {
            @Override
            public void onSuccess(@Nullable Event result) {
                System.out.println(result.getSubject());
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "An error occurred during fetching events", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private Event getSampleEvent() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String formattedTime = formatter.format(new Date());

        DateTimeTimeZone dtz = new DateTimeTimeZone();
        dtz.setDateTime(formattedTime);
        dtz.setTimeZone("UTC");

        Event event = new Event();
        event.setSubject("Appointment");
        event.setStart(dtz);
        event.setEnd(dtz);

        return event;
    }

    public void getOutlookEvents() {
        Calendar dateStart = java.util.Calendar.getInstance();
        Calendar dateEnd = java.util.Calendar.getInstance();
        dateEnd.add(Calendar.MONTH, 6);

        Futures.addCallback(outlookClient.getMe().getCalendarView()
                .addParameter("startDateTime", dateStart)
                .addParameter("endDateTime", dateEnd)
                .read(), new FutureCallback<List<Event>>() {
            @Override
            public void onSuccess(final List<Event> result) {
                Global.allEvents.removeAll(Global.outlookEvents);
                Global.outlookEvents.clear();
                List<MyEvent> myEvents = new ArrayList<>();
                for (Event event : result) {
                    String start;
                    String end;
                    Date dateStart;
                    if (event.getIsAllDay()) {
                        start = DateUtils.formatEventOutlookDateAllDay(event.getStart().getDateTime());
                        end = "";
                        dateStart = DateUtils.formatStringToDate(DateUtils.formatAllDayEventDate(start));
                    } else {
                        start = DateUtils.getLocalTime(event.getStart().getDateTime());
                        end = DateUtils.getLocalTime(event.getEnd().getDateTime());
                        dateStart = DateUtils.formatEventDateToDate(start);
                    }
                    myEvents.add(new MyEvent(event.getSubject(), start, end, "12", event.getBodyPreview(), event.getLocation().getDisplayName(), dateStart));
                }
                //progressDialogOutlook.cancel();
                Global.outlookEvents = myEvents;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Calendar cal = Calendar.getInstance();
                        Global.allEvents.addAll(Global.outlookEvents);
                        setDrawableForCalendarEvents();
                        populateEventsList(DateUtils.formatDateToString(cal.getTime()));
                    }
                });
            }

            @Override
            public void onFailure(final Throwable t) {
                //progressDialogOutlook.cancel();
                Toast.makeText(getApplicationContext(), "An error occurred during fetching events", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}