package com.example.sondrehj.familymedicinereminderclient.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sondrehj.familymedicinereminderclient.MainActivity;
import com.example.sondrehj.familymedicinereminderclient.R;
import com.example.sondrehj.familymedicinereminderclient.dialogs.DeleteAllDataDialogFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.example.sondrehj.familymedicinereminderclient.jobs.JobManagerService;
import com.example.sondrehj.familymedicinereminderclient.jobs.PutGracePeriodJob;
import com.example.sondrehj.familymedicinereminderclient.jobs.PutReceiveChangeNotificationJob;

public class AccountAdministrationFragment extends android.support.v4.app.Fragment {

    //private boolean busIsRegistered;
    private static String TAG = "AccountAdministrationFragment";
    private Context context;

    @Bind(R.id.account_group_personal) LinearLayout personalGroup;
    @Bind(R.id.account_group_guardian) LinearLayout guardianGroup;
    @Bind(R.id.account_group_sync) LinearLayout syncGroup;

    @Bind(R.id.account_personal_reminder_switch) Switch reminderSwitch;
    @Bind(R.id.account_general_notification_switch) Switch notificationSwitch; //receiveChangeNotification

    @Bind(R.id.account_general_snooze_minutes_value) TextView snoozeMinuteValue;
    @Bind(R.id.account_general_snooze_minutes_text) TextView snoozeMinuteText;
    @Bind(R.id.account_general_snooze_seekbar) SeekBar snoozeSeekBar;

    @Bind(R.id.account_guardian_grace_minutes_value) TextView graceMinuteValue;
    @Bind(R.id.account_guardian_grace_minutes_text) TextView graceMinuteText;
    @Bind(R.id.account_guardian_grace_seekbar) SeekBar graceSeekBar;

    public AccountAdministrationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccountAdministrationFragment.
     */
    public static AccountAdministrationFragment newInstance() {
        AccountAdministrationFragment fragment = new AccountAdministrationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_administration, container, false);
        ButterKnife.bind(this, view);
        getActivity().setTitle("Settings");

        Account account = MainActivity.getAccount(context); //Gets a reference to the account.
        String userRole = AccountManager.get(context)
                .getUserData(account, "userRole"); // Gets the userRole of the specified account.

        Log.d(TAG, "The user (" + account.name + ") role equals: " + userRole +
                ". Hiding elements belonging to the opposite role.");

        snoozeSeekBar.setMax(29); //set maximum value of seek bar.
        graceSeekBar.setMax(59);

        snoozeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress + 1 == 1) {
                    snoozeMinuteText.setText("min");
                } else {
                    snoozeMinuteText.setText("mins");
                }
                snoozeMinuteValue.setText((progress + 1) + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        graceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress + 1 == 1) {
                    graceMinuteText.setText("min");
                } else {
                    graceMinuteText.setText("mins");
                }
                graceMinuteValue.setText((progress + 1) + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        personalGroup.setVisibility(View.GONE);
        syncGroup.setVisibility(View.GONE); //hides syncing options.

        if (userRole.equals("patient")) {
            guardianGroup.setVisibility(View.GONE); // hides guardian options if user is a patient.
        }
        fillTextFields(); //fill the text fields with data saved in sharedprefs.
        return view;
    }

    @OnClick(R.id.account_settings_save_button)
    public void onSaveButtonClick() {
        updateAccountInformation();
        Toast.makeText(getActivity(), "Settings updated.", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.account_delete_data_button)
    public void onDeleteButtonClick() {
        FragmentManager fm = getActivity().getFragmentManager();
        DeleteAllDataDialogFragment daf = new DeleteAllDataDialogFragment();
        daf.show(fm, "delete");
    }

    /**
     * Persist user settings in SharedPreferences and add a Job that guarantees that the gracePeriod
     * will be sent to the server sometime when there is internet.
     *
     * @return boolean
     */
    public boolean updateAccountInformation() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("AccountSettings",
                Context.MODE_PRIVATE).edit();
        Account account = MainActivity.getAccount(getActivity());
        //editor.putBoolean("reminderSwitch", reminderSwitch.isChecked()) ;
        editor.putBoolean("notificationSwitch", notificationSwitch.isChecked()); //receiveChangeNotification
        if (notificationSwitch.isChecked()) {
            JobManagerService
                    .getJobManager(getActivity())
                    .addJobInBackground(
                            new PutReceiveChangeNotificationJob(account.name, "true")
                    );
        } else {
            JobManagerService
                    .getJobManager(getActivity())
                    .addJobInBackground(
                            new PutReceiveChangeNotificationJob(account.name, "false")
                    );
        }
        editor.putInt("snoozeDelay", Integer.parseInt(snoozeMinuteValue.getText().toString()));
        if (!(guardianGroup.getVisibility() == View.GONE)) {
            editor.putInt("gracePeriod", Integer.parseInt(graceMinuteValue.getText().toString()));
            JobManagerService
                    .getJobManager(getActivity())
                    .addJobInBackground( //guarantees that this job will be done sometime when there is internet.
                            new PutGracePeriodJob(account.name, graceMinuteValue.getText().toString())
                    );
        }
        editor.apply(); //save shared preferences
        return true;
    }

    /**
     * Fills the field with the settings from SharedPreferences. Default settings are
     * set in MainActivity.
     */
    public void fillTextFields() {
        SharedPreferences prefs = getActivity().getSharedPreferences("AccountSettings",
                Context.MODE_PRIVATE);
        //reminderSwitch.setChecked(prefs.getBoolean("reminderSwitch", true));
        notificationSwitch.setChecked(prefs.getBoolean("notificationSwitch", true));
        snoozeSeekBar.setProgress(prefs.getInt("snoozeDelay", 5) - 1);
        snoozeMinuteValue.setText(prefs.getInt("snoozeDelay", 5) + "");
        if (prefs.getInt("snoozeDelay", 5) == 1){
            snoozeMinuteText.setText("min");
        }
        graceSeekBar.setProgress(prefs.getInt("gracePeriod", 5) - 1);
        graceMinuteValue.setText(prefs.getInt("gracePeriod", 5) + "");
        if (prefs.getInt("gracePeriod", 5) == 1){
            graceMinuteText.setText("min");
        }
    }

    /**
     * Register this fragment to the event bus.
     * Saves a reference to the application context.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (!busIsRegistered) {
            BusService.getBus().register(this);
            busIsRegistered = true;
        }*/
        this.context = context;
    }

    /**
     * Provides compatibility for api levels below 23
     *
     * @param context
     */
    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        /*if (!busIsRegistered) {
            BusService.getBus().register(this);
            busIsRegistered = true;
        }*/
        this.context = context;
    }

    /**
     * Unregisters the bus when the fragment is detached.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        /*if (busIsRegistered) {
            BusService.getBus().unregister(this);
            busIsRegistered = false;
        }*/
    }

    /**
     * Unbinds references to objects in the view.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
