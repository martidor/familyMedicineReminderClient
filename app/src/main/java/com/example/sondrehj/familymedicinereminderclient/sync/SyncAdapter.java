package com.example.sondrehj.familymedicinereminderclient.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.example.sondrehj.familymedicinereminderclient.MainActivity;
import com.example.sondrehj.familymedicinereminderclient.api.MyCyFAPPServiceAPI;
import com.example.sondrehj.familymedicinereminderclient.api.RestService;
import com.example.sondrehj.familymedicinereminderclient.database.MySQLiteHelper;
import com.example.sondrehj.familymedicinereminderclient.models.User2;

/**
 * The SyncAdapter class is part of the Android synchronization framework. This implementation uses
 * a stub ContentProvider, and instantiates a Synchronizer that performs all the logic related to
 * synchronization.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static String TAG = "SyncAdapter";
    private Context context;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
    }

    /**
     * This function runs when requestSync is called from anywhere in the
     * android system with the right authority. The functions performs a sync of the content provider.
     * In our use case, the content provider is a stub, and we perform custom syncronization instead.
     *
     *
     * @param account
     * @param extras
     * @param authority
     * @param provider
     * @param syncResult
     */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        Log.d(TAG, "Sync is performing");


        String authToken = AccountManager.get(context).getUserData(MainActivity.getAccount(context), "authToken");
        String notificationType = extras.getString("notificationType");
        String optionalData = extras.getString("optionalData");
        String currentUserId = extras.getString("currentUserId");

        MyCyFAPPServiceAPI api = RestService.createRestService(authToken);
        MySQLiteHelper db = new MySQLiteHelper(getContext());

        Synchronizer synchronizer = new Synchronizer(currentUserId, api, db, context);
        Intent intent = new Intent();
        intent.setAction("mycyfapp"); //not action, is a filter
        if (notificationType != null) {
            switch (notificationType) {
                case "remindersChanged":
                    synchronizer.syncReminders();
                    break;
                case "medicationsChanged":
                    synchronizer.syncMedications();
                    break;
                case "linkingRequest":
                    Log.d(TAG, "in switch -> linkingRequest");
                    //incoming linking request from push notification
                    intent.putExtra("action", "openLinkingRequestDialog");
                    context.sendBroadcast(intent);
                    break;
                case "positiveLinkingResponse":
                    Log.d(TAG, "in switch -> positiveLinkingResponse");
                    intent.putExtra("action", "notifyPositiveResultToLinkingFragment");
                    intent.putExtra("patientID", optionalData);
                    context.sendBroadcast(intent);
                    break;
                case "negativeLinkingResponse":
                    Log.d(TAG, "in switch -> negativeLinkingResponse");
                    intent.putExtra("action", "notifyNegativeResultToLinkingFragment");
                    context.sendBroadcast(intent);
                    break;
                case "childForgotReminder":
                    Log.d(TAG, "in switch -> childForgotReminder");
                    intent.putExtra("action", "childForgotReminder");
                    context.sendBroadcast(intent);
                    break;
            }
        } else {
            Log.d("SyncAdapter", "notificationType == null");
        }
    }
}