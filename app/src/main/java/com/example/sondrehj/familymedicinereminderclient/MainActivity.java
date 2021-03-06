package com.example.sondrehj.familymedicinereminderclient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.sondrehj.familymedicinereminderclient.api.MyCyFAPPServiceAPI;
import com.example.sondrehj.familymedicinereminderclient.api.RestService;
import com.example.sondrehj.familymedicinereminderclient.bus.BusService;
import com.example.sondrehj.familymedicinereminderclient.bus.DataChangedEvent;
import com.example.sondrehj.familymedicinereminderclient.bus.ForgotReminderEvent;
import com.example.sondrehj.familymedicinereminderclient.bus.LinkingRequestEvent;
import com.example.sondrehj.familymedicinereminderclient.database.DatabaseReceiver;
import com.example.sondrehj.familymedicinereminderclient.dialogs.CreateReminderForMedicationDialogFragment;
import com.example.sondrehj.familymedicinereminderclient.dialogs.ForgotReminderDialogFragment;
import com.example.sondrehj.familymedicinereminderclient.dialogs.SetAliasDialog;
import com.example.sondrehj.familymedicinereminderclient.dialogs.DeleteMedicationDialogFragment;
import com.example.sondrehj.familymedicinereminderclient.dialogs.DeleteReminderDialogFragment;
import com.example.sondrehj.familymedicinereminderclient.fragments.AccountAdministrationFragment;
import com.example.sondrehj.familymedicinereminderclient.dialogs.DatePickerFragment;
import com.example.sondrehj.familymedicinereminderclient.fragments.DashboardListFragment;
import com.example.sondrehj.familymedicinereminderclient.fragments.LinkingFragment;
import com.example.sondrehj.familymedicinereminderclient.fragments.MedicationListFragment;
import com.example.sondrehj.familymedicinereminderclient.fragments.MedicationStorageFragment;
import com.example.sondrehj.familymedicinereminderclient.fragments.NewReminderFragment;
import com.example.sondrehj.familymedicinereminderclient.fragments.ReminderListFragment;
import com.example.sondrehj.familymedicinereminderclient.fragments.WelcomeFragment;
import com.example.sondrehj.familymedicinereminderclient.dialogs.EndDatePickerFragment;
import com.example.sondrehj.familymedicinereminderclient.dialogs.LinkingDialogFragment;
import com.example.sondrehj.familymedicinereminderclient.dialogs.MedicationPickerFragment;
import com.example.sondrehj.familymedicinereminderclient.dialogs.SelectDaysDialogFragment;
import com.example.sondrehj.familymedicinereminderclient.dialogs.SelectUnitDialogFragment;
import com.example.sondrehj.familymedicinereminderclient.dialogs.TimePickerFragment;
import com.example.sondrehj.familymedicinereminderclient.jobs.DeleteMedicationJob;
import com.example.sondrehj.familymedicinereminderclient.jobs.DeleteReminderJob;
import com.example.sondrehj.familymedicinereminderclient.jobs.JobManagerService;
import com.example.sondrehj.familymedicinereminderclient.models.Medication;
import com.example.sondrehj.familymedicinereminderclient.models.Reminder;
import com.example.sondrehj.familymedicinereminderclient.models.User;
import com.example.sondrehj.familymedicinereminderclient.models.User2;
import com.example.sondrehj.familymedicinereminderclient.notification.NotificationScheduler;
import com.example.sondrehj.familymedicinereminderclient.playservice.RegistrationIntentService;
import com.example.sondrehj.familymedicinereminderclient.database.MySQLiteHelper;
import com.example.sondrehj.familymedicinereminderclient.sync.ServerStatusChangeReceiver;
import com.example.sondrehj.familymedicinereminderclient.sync.SyncReceiver;
import com.example.sondrehj.familymedicinereminderclient.utility.UserSpinnerToggle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        NewReminderFragment.OnNewReminderInteractionListener,
        ReminderListFragment.OnReminderListFragmentInteractionListener,
        MedicationListFragment.OnListFragmentInteractionListener,
        WelcomeFragment.OnWelcomeListener,
        TimePickerFragment.TimePickerListener,
        DatePickerFragment.DatePickerListener,
        SelectUnitDialogFragment.OnUnitDialogResultListener,
        SelectDaysDialogFragment.OnDaysDialogResultListener,
        EndDatePickerFragment.EndDatePickerListener,
        MedicationPickerFragment.OnMedicationPickerDialogResultListener,
        SetAliasDialog.OnSetAliasDialogListener,
        DeleteMedicationDialogFragment.DeleteMedicationDialogListener,
        DeleteReminderDialogFragment.DeleteReminderDialogListener,
        CreateReminderForMedicationDialogFragment.CreateReminderForMedicationDialogListener{

    private static String TAG = "MainActivity";
    private SyncReceiver syncReceiver;
    public NotificationManager manager;
    private NotificationScheduler notificationScheduler;
    private User2 currentUser;
    public UserSpinnerToggle userSpinnerToggle;


    public String getCurrentFragmentName() {
        return currentFragmentName;
    }

    private String currentFragmentName;

    // Global Variables
    public static final String AUTHORITY = "com.example.sondrehj.familymedicinereminderclient";

    /**
     * Main entry point of the application. When onCreate is run, view is filled with the
     * layout activity_main in res. The fragment container which resides in the contentView is
     * changed to "MedicationListFragment()" with the changeFragment() function call.
     *
     * In addition, the Sidebar/Drawer is instantiated.
     *
     * Portrait mode is enforced because if the screen is rotated you loose a lot of references
     * when the instance is redrawn.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        //get the accountmanager
        Account account = MainActivity.getAccount(this);

        //Checks if there are accounts on the device. If there aren't, the user is redirected to the welcomeFragment.
        if (account == null) {
            changeFragment(WelcomeFragment.newInstance());
            //disables drawer and navigation in welcomeFragment.
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            //hides ActionBarDrawerToggle
            toggle.setDrawerIndicatorEnabled(false);
        } else {
            changeFragment(DashboardListFragment.newInstance());
            ContentResolver.setIsSyncable(account, "com.example.sondrehj.familymedicinereminderclient.content", 1);
            ContentResolver.setSyncAutomatically(account, "com.example.sondrehj.familymedicinereminderclient.content", true);
            //Enables drawer and menu-button
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            toggle.setDrawerIndicatorEnabled(true);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            //TODO: get main account from database
            String id = AccountManager.get(this).getUserData(getAccount(this), "userId");
            currentUser = new User2(id, "User");
        }

        // Sets repeating creation of a Job Manager that will check for upload jobs
        this.manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // Creates a polling service that checks for server health
        Intent intent = new Intent(this, ServerStatusChangeReceiver.class);
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(this, -2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, new GregorianCalendar().getTimeInMillis(),
                60000, pendingIntent);

        // Creates a service to reset the TIME_TAKEN_COLUMN for REMINDER in the database every day at 00:00.
        Intent resetTimeTakenIntent = new Intent(this, DatabaseReceiver.class);
        PendingIntent pendingResetTimeTakenIntent = PendingIntent
                .getBroadcast(this, -3, resetTimeTakenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the database to be updated at 00:00
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingResetTimeTakenIntent);

        // NotificationScheduler
        this.notificationScheduler = new NotificationScheduler(this);

        // Default account settings
        SharedPreferences sharedPrefs = getSharedPreferences("AccountSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor;
        if (!sharedPrefs.contains("initialized")) {
            editor = sharedPrefs.edit();
            // Indicate that the default shared prefs have been set
            editor.putBoolean("initialized", true);
            // Set default values
            editor.putInt("snoozeDelay", 5); // 5 minutes
            editor.putInt("gracePeriod", 30);// 30 minutes
            editor.putBoolean("reminderSwitch", true);
            editor.putBoolean("notificationSwitch", true);
            editor.putString("accountType", "patient");
            editor.putString("create_user_secret", "createSecretToChangeLater");
            editor.apply();
        }

        // The items inside the grey area of the drawer.
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Enforce rotation-lock.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //TODO: Look more at how database changes can be broadcasted to the system
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent != null) {
            onNewIntent(intent);
            setIntent(null);
        }
    }

    /**
     * Registering the SyncReceiver to receive intents from the SyncAdapter, we need this
     * because the Bus cannot register to the SyncAdapter (it is another process altogether).
     * Registering the activity to the event bus.
     */
    @Override
    public void onResume() {
        super.onResume();
        BusService.getBus().register(this);
        syncReceiver = new SyncReceiver();
        IntentFilter intentFilter = new IntentFilter("mycyfapp");
        registerReceiver(syncReceiver, intentFilter);
    }

    /**
     * Unregister the activity from the bus.
     * Unregister the receiver so that intents aren't received when the application is paused.
     */
    @Override
    public void onPause() {
        super.onPause();
        BusService.getBus().unregister(this);
        unregisterReceiver(syncReceiver);
    }

    /**
     * Gets the instantiazed account of the system, used with the SyncAdapter and
     * ContentResolver, might have to be moved sometime.
     *
     * @return account
     */
    public static Account getAccount(Context context) {
        Account[] accountArray = AccountManager.get(context).getAccountsByType("com.example.sondrehj.familymedicinereminderclient");
        if (accountArray.length >= 1) {
            return accountArray[0];
        }
        return null;
    }

    /**
     * Handle a LinkingRequest sent to the patient on the bus. Opens a LinkingDialogFragment
     * which asks the patient wether it wants to link itself with a guardian account upon request.
     * <p>
     * Flow:
     * 1. Guardian presses link
     * 2. Guardian sends rest call to the server
     * 3. Server sends notification to patient
     * 4. MyGCMListener asks for sync with notificationType in extras bundle
     * 5. SyncAdapter sends a intent to the SyncReceiver.
     * 6. SyncReceiver sends a LinkingRequestEvent out on the event bus which MainActivity is registered to.
     * 7. MainActivity of patient handles incoming linking request
     *
     * @param event
     */
    @Subscribe
    public void handleLinkingRequest(LinkingRequestEvent event) {
        Log.d(TAG, "Handled linking request by opening Linking Dialog.");
        FragmentManager fm = getSupportFragmentManager();
        LinkingDialogFragment linkingDialogFragment = new LinkingDialogFragment();
        linkingDialogFragment.show(fm, "linking_request_fragment");
    }

    @Subscribe
    public void handleMedicationPostedRequest(DataChangedEvent event) {
        if (event.type.equals(DataChangedEvent.MEDICATIONSENT)) {
            Medication medication = (Medication) event.data;
            new MySQLiteHelper(this).updateMedication(medication);
            BusService.getBus().post(new DataChangedEvent(DataChangedEvent.MEDICATIONS));
        }
    }

    @Subscribe
    public void handleReminderPostedRequest(DataChangedEvent event) {
        if (event.type.equals(DataChangedEvent.REMINDERSENT)) {
            Reminder reminder = (Reminder) event.data;
            new MySQLiteHelper(this).updateReminder(reminder);
            BusService.getBus().post(new DataChangedEvent(DataChangedEvent.REMINDERS));
        }
    }

    @Subscribe
    public void handleScheduleRequest(DataChangedEvent event) {
        if(event.type.equals(DataChangedEvent.SCHEDULE_REMINDER)) {
            Reminder reminder = (Reminder) event.data;
            if(reminder.getOwnerId().equals(MainActivity.getAccount(this).name)){
                if(reminder.getIsActive()) {
                    NotificationScheduler ns = new NotificationScheduler(this);
                    ns.scheduleNotification(ns.getNotification("", reminder), reminder);
                }
            }
        }
    }

    @Subscribe
    public void handleChildrenForgotReminderNotification(ForgotReminderEvent event) {
        FragmentManager fm = getSupportFragmentManager();
        ForgotReminderDialogFragment forgotReminderDialogFragment = new ForgotReminderDialogFragment();
        forgotReminderDialogFragment.show(fm, "forgot_reminder_dialog");
    }

    /**
     * If the drawer is open it will be closed when pressing the back button, elsewise it will pop
     * backstates so that you can navigate backwards.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) { //close app before empty container is shown
            supportFinishAfterTransition();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Inflate the options menu. This adds items to the action bar if it is present. The one with
     * the three buttons, which resides physically on Samsung phones.
     *
     * @param menu The application menu
     * @return success
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        View view = menu.findItem(R.id.action_user).getActionView();
        ImageView userIcon = (ImageView) view.findViewById(R.id.user_icon);
        Spinner userSpinner = (Spinner) view.findViewById(R.id.action_user_spinner);
        this.userSpinnerToggle = new UserSpinnerToggle(this, userSpinner);
        userSpinnerToggle.setUserIcon(userIcon);
        userSpinnerToggle.toggle();
        if(getAccount(this) != null) {
            String userRole = AccountManager.get(this).getUserData(getAccount(this), "userRole");
            if(userRole.equals("patient")){
                userSpinnerToggle.showUserActionBar(false);
            }
        }
        if(getAccount(this) == null) {
            userSpinnerToggle.showUserActionBar(false);
        }
        return true;
    }

    /**
     * Takes in a {@link Fragment} which is to replace the fragment which is already in the fragmentcontainer
     * of MainActivity.
     *
     * @param fragment The {@link Fragment} to be shown.
     */
    public void changeFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getName();
        Log.d(TAG, "Navigated to: " + fragment.getClass().getSimpleName());

        if(backStateName.equals(currentFragmentName))   return;

        boolean fragmentPopped = getSupportFragmentManager().popBackStackImmediate(backStateName, 0);
        if(!fragmentPopped) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
            transaction.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
            transaction.addToBackStack(backStateName);

            //Commit the transaction
            transaction.commit();
        }
        currentFragmentName = backStateName;
    }

    /**
     * Executes methods based on the intent received. This method is called when the activity
     * receives intents from notifications (such as when a user clicks a notification, or an action)
     * or when the data is updated from a sync.
     *
     * @param intent the intent sent to the Activity.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Reminder reminder = (Reminder) intent.getSerializableExtra("notification-reminder");
        String notificationAction = intent.getStringExtra("notification-action");
        //String currentUserId = intent.getStringExtra("currentUserId");

        if (notificationAction != null) {
            switch (notificationAction) {
                case "notificationStandard":
                    //notificationScheduler.handleNotificationStandardClick(reminder);
                    break;
                case "notificationTake":
                    notificationScheduler.handleNotificationTakenClick(reminder);
                    break;
                case "notificationSnooze":
                    notificationScheduler.handleNotificationSnoozeClick(reminder);
                    break;
                case "notificationMarkAsDone":
                    notificationScheduler.handleNotificationMarkAsDoneClick(reminder);
                case "medicationChanged":
                    Bundle extras = new Bundle();
                    extras.putString("notificationType", notificationAction);
                    //extras.putString("currentUserId", currentUserId);
                    extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                    extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                    extras.putString("currentUserId", intent.getStringExtra("currentUserId"));

                    ContentResolver.requestSync(
                            MainActivity.getAccount(getApplicationContext()),
                            "com.example.sondrehj.familymedicinereminderclient.content",
                            extras);
                    break;
            }
        }
        setIntent(null);
    }

    /**
     * Deletes local application data and accounts. This function is called when the user
     * presses the delete data button in {@link AccountAdministrationFragment}.
     */
    public void deleteAllApplicationData() {
        boolean deleted = false;
        // Wipe the local database
        this.deleteDatabase("familymedicinereminderclient.db");
        // Wipe account settings stored by SharedPreferences
        this.getSharedPreferences("AccountSettings", 0).edit().clear().commit();

        // Remove children from guardian on the server.
        final MyCyFAPPServiceAPI api = RestService.createRestService();
        Call<User> call = api.removeChildrenFromGuardian(getAccount(this).name);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                //empty
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                //empty
            }
        });

        // TODO: wipe the rest of the server data


        // Wipe all accounts in AccountManager
        AccountManager accountManager = (AccountManager) this.getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccounts();
        for (Account account : accounts) {
            if (account.type.intern().equals(AUTHORITY))
                accountManager.removeAccount(account, null, null);  //TODO: Find alternative to this, deprecated
        }

        // TODO: clear all pendingIntents in AlarmManager

        try {
            Thread.sleep(3000, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    /**
     * Method called by {@link WelcomeFragment} to save/add account to the AccountManager and
     * fetch a gcm token which is sent to the server and associated with the user.
     *
     * @param userId the id of the user
     * @param password the user password
     * @param userRole role of the user. Guardian or patient
     */
    @Override
    public void OnNewAccountCreated(String userId, String password, String userRole, String jwtToken) {
        Account newAccount = new Account(userId, "com.example.sondrehj.familymedicinereminderclient");
        AccountManager manager = AccountManager.get(this);
        boolean saved = manager.addAccountExplicitly(newAccount, password, null);
        if (saved) {
            manager.setUserData(newAccount, "passtoken", password);
            manager.setUserData(newAccount, "userId", userId);
            manager.setUserData(newAccount, "userRole", userRole);
            manager.setUserData(newAccount, "authToken", jwtToken);
            Log.d(TAG, "New account" + newAccount.toString());
        }

        //Enables drawer and menu-button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        toggle.setDrawerIndicatorEnabled(true);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        ContentResolver.setIsSyncable(newAccount, "com.example.sondrehj.familymedicinereminderclient.content", 1);
        ContentResolver.setSyncAutomatically(newAccount, "com.example.sondrehj.familymedicinereminderclient.content", true);

        //check if google play services are enabled (required for GCM).
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM and
            // associate user's token with the user on the back-end server.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        changeFragment(new DashboardListFragment());
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     * <p>
     * TODO: Disallow usage of the application before the user fixes this error.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Set the current user of the application. This called from {@link UserSpinnerToggle#toggle()}
     * when the guardian changes the user from the drop-down menu.
     *
     * @param user The {@link User2} to be set as current user.
     */
    public void setCurrentUser(User2 user) {
        this.currentUser = user;
    }

    /**
     * Retrieves the current user of the application.
     *
     * @return current user
     */
    public User2 getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Set selectedDays in {@link NewReminderFragment} based on the days selected in
     * {@link SelectDaysDialogFragment}
     *
     * @param selectedDays The days selected by the user in {@link SelectDaysDialogFragment}
     */
    @Override
    public void onPositiveDaysDialogResult(ArrayList selectedDays) {
        NewReminderFragment nrf = (NewReminderFragment) getSupportFragmentManager().findFragmentByTag("NewReminderFragment");
        nrf.setDaysOnLayout(selectedDays);
    }

    /**
     * Sets the medication in {@link NewReminderFragment} based on the medication selected by the user
     * in {@link MedicationPickerFragment}. You can only attach a medication if it's synchronized
     * with the server.
     *
     * @param med The days selected by the user in {@link SelectDaysDialogFragment}
     */
    @Override
    public void onPositiveMedicationPickerDialogResult(Medication med) {
        if(med.getServerId() != -1) {
            NewReminderFragment nrf = (NewReminderFragment) getSupportFragmentManager().findFragmentByTag("NewReminderFragment");
            nrf.setMedicationOnLayout(med);
        } else {
            Toast.makeText(this, "This medication is not synchronized. Please synchronize it with the server before attaching it.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * Sets the time in {@link NewReminderFragment} based on the time selected by the user
     * in {@link TimePickerFragment}.
     *
     * @param hourOfDay selected hour
     * @param minute selected minute
     */
    @Override
    public void setTime(int hourOfDay, int minute) {
        NewReminderFragment newReminderFragment = (NewReminderFragment) getSupportFragmentManager().findFragmentByTag("NewReminderFragment");
        newReminderFragment.setTimeOnLayout(hourOfDay, minute);
    }

    /**
     * Sets the date in {@link NewReminderFragment} based on the date selected by the user
     * in {@link DatePickerFragment}.
     *
     * @param year selected year
     * @param month selected month
     * @param day selected day
     */
    @Override
    public void setDate(int year, int month, int day) {
        NewReminderFragment newReminderFragment = (NewReminderFragment) getSupportFragmentManager().findFragmentByTag("NewReminderFragment");
        newReminderFragment.setDateOnLayout(year, month, day);
    }

    /**
     * Sets the end date in {@link NewReminderFragment} based on the date selected by the user
     * in {@link DatePickerFragment}.
     *
     * @param year selected year
     * @param month selected month
     * @param day selected day
     */
    @Override
    public void setEndDate(int year, int month, int day) {
        NewReminderFragment newReminderFragment = (NewReminderFragment) getSupportFragmentManager().findFragmentByTag("NewReminderFragment");
        newReminderFragment.setEndDateOnLayout(year, month, day);
    }

    /**
     * Handles the selection of items in the drawer and replaces the fragment container of
     * MainActivity with the fragment corresponding to the Item selected. The drawer is then closed.
     *
     * @param item The selected {@link MenuItem}
     * @return success
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_reminders) {
            changeFragment(ReminderListFragment.newInstance());
        } else if (id == R.id.nav_medication) {
            changeFragment(MedicationListFragment.newInstance());
        } else if (id == R.id.nav_settings) {
            changeFragment(AccountAdministrationFragment.newInstance());
        } else if (id == R.id.nav_dashboard) {
            changeFragment(new DashboardListFragment());
        } else if (id == R.id.nav_linking) {
            changeFragment(LinkingFragment.newInstance());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Instantiates {@link MedicationStorageFragment} with a medication for editing purposes.
     * The method is called when the user clicks an existing medication
     * in {@link MedicationListFragment}.
     *
     * @param medication The {@link Medication} to be edited.
     */
    @Override
    public void onMedicationListFragmentInteraction(Medication medication) {
        Fragment fragment = MedicationStorageFragment.newInstance(medication);
        changeFragment(fragment);
    }

    /**
     * Schedules a notification and updates the view when a reminder is
     * edited or created in {@link NewReminderFragment}.
     *
     * @param r The newly edited or created {@link Reminder}
     */
    @Override
    public void onSaveNewReminder(Reminder r) {
        if (r.getIsActive()) {
            // Schedule the notification
            notificationScheduler.scheduleNotification(
                    notificationScheduler.getNotification("Take your medication", r), r);
        }
        changeFragment(ReminderListFragment.newInstance());
        BusService.getBus().post(new DataChangedEvent(DataChangedEvent.REMINDERS));
       // BusService.getBus().post(new DataChangedEvent(DataChangedEvent.DASHBOARDCHANGED));
    }

    /**
     * Sets the unit of a medication in {@link MedicationStorageFragment} based on the
     * selection of the user in {@link SelectUnitDialogFragment}.
     *
     * @param unit the index of the selected unit.
     */
    @Override
    public void onPositiveUnitDialogResult(int unit) {
        String[] units = getResources().getStringArray(R.array.unit_items);
        MedicationStorageFragment sf = (MedicationStorageFragment) getSupportFragmentManager().findFragmentByTag("MedicationStorageFragment");
        sf.setUnitText(units[unit]);
    }

    /**
     * Instantiates {@link NewReminderFragment} with a reminder for editing purposes.
     * The method is called when the user clicks an existing reminder
     * in {@link ReminderListFragment}.
     *
     * @param reminder The {@link Reminder} to be edited.
     */
    @Override
    public void onReminderListItemClicked(Reminder reminder) {
        changeFragment(NewReminderFragment.newInstance(reminder));
    }

    /**
     * Sets the isActive variable of a {@link Reminder} based on the status of the
     * ReminderSwitch in {@link ReminderListFragment}. A notification is scheduled
     * if the switch is checked, and unscheduled if the switch is unchecked.
     * The method is called when the user interacts with the ReminderSwitch.
     *
     * @param reminder The {@link Reminder} to be updated.
     */
    @Override
    public void onReminderListSwitchClicked(Reminder reminder) {

        if (reminder.getIsActive()) {
            // Cancel the scheduled reminder
            notificationScheduler.cancelNotification(reminder.getReminderId());
            reminder.setIsActive(false);
        } else {
            // Activate the reminder
            notificationScheduler.scheduleNotification(
                    notificationScheduler.getNotification("Take your medication", reminder), reminder);
            reminder.setIsActive(true);
        }

        // Updates the DB
        MySQLiteHelper db = new MySQLiteHelper(this);
        db.updateReminder(reminder);
        //BusService.getBus().post(new DataChangedEvent(DataChangedEvent.DASHBOARDCHANGED));
    }

    /**
     * Saves the alias of a linked user based on the given alias in {@link SetAliasDialog}.
     * If no alias is specified, it will default to the userId of the patient.
     *
     * @param alias The alias specified by the user.
     * @param userId The userId of the newly linked patient.
     */
    @Override
    public void onPositiveSetAliasDialog(String alias, String userId) {

        User2 user;
        if (!alias.equals("")) {
            user = new User2(userId, alias);
        } else {
            user = new User2(userId, userId);
        }
        new MySQLiteHelper(this).addUser(user);
        userSpinnerToggle.updateSpinnerContent();
        userSpinnerToggle.toggle();
    }

    /**
     * Deletes a {@link Medication} from the local database and the server, and updates the view.
     * The method is called when the user clicks the "x" of a list item in
     * {@link MedicationListFragment}, and agrees to delete the medication in
     * {@link DeleteMedicationDialogFragment}.
     *
     * @param medication The {@link Medication} to be deleted.
     * @param position The position of the list item in {@link MedicationListFragment}.
     */
    public void onPositiveDeleteMedicationDialogResult(Medication medication, int position) {
        if(medication.getServerId() != -1) {
            String authToken = AccountManager.get(this).getUserData(MainActivity.getAccount(this), "authToken");
            new MySQLiteHelper(this).deleteMedication(medication);
            BusService.getBus().post(new DataChangedEvent(DataChangedEvent.MEDICATIONS));
            JobManagerService.getJobManager(this).addJobInBackground(new DeleteMedicationJob(medication, getCurrentUser().getUserId(), authToken));
        }
        else {
            Toast.makeText(this, "This medication is not synchronized. Please synchronize it with the server before deleting.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes a {@link Reminder} from the local database and the server, and updates the view.
     * The method is called when the user clicks the "x" of a list item in
     * {@link ReminderListFragment}, and agrees to delete the reminder in
     * {@link DeleteReminderDialogFragment}.
     *
     * @param reminder The {@link Reminder} to be deleted.
     * @param position The position of the list item in {@link ReminderListFragment}.
     */
    @Override
    public void onPositiveDeleteReminderDialogResult(Reminder reminder, int position) {
        if(reminder.getServerId() != -1) {
            if(reminder.getIsActive()){
                notificationScheduler.cancelNotification(reminder.getReminderId());
            }
            String authToken = AccountManager.get(this).getUserData(MainActivity.getAccount(this), "authToken");
            AccountManager.get(this).getUserData(MainActivity.getAccount(this), "authToken");
            new MySQLiteHelper(this).deleteReminder(reminder);
            BusService.getBus().post(new DataChangedEvent(DataChangedEvent.REMINDERS));
            JobManagerService.getJobManager(this).addJobInBackground(new DeleteReminderJob(reminder, getCurrentUser().getUserId(), authToken));
        } else {
            Toast.makeText(this, "This reminder is not synchronized. Please synchronize it with the server before deleting.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * NOT CURRENTLY IN USE
     * Instantiates {@link NewReminderFragment} with a newly created {@link Medication}.
     * The method is called when the user creates a new medication
     * in {@link MedicationStorageFragment}
     *
     * @param medication The {@link Medication} to be attached to the reminder.
     */
    @Override
    public void onPositiveCreateReminderForMedicationDialogResult(Medication medication) {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("NewReminderFragment");
        if(fragment != null) {
            getFragmentManager().popBackStackImmediate(fragment.getClass().getName(), 0);
        }
        changeFragment(NewReminderFragment.newInstance(medication));
    }
}