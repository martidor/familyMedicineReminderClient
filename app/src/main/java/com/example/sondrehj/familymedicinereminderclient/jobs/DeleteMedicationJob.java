package com.example.sondrehj.familymedicinereminderclient.jobs;

import android.util.Log;

import com.example.sondrehj.familymedicinereminderclient.api.MyCyFAPPServiceAPI;
import com.example.sondrehj.familymedicinereminderclient.api.RestService;
import com.example.sondrehj.familymedicinereminderclient.bus.BusService;
import com.example.sondrehj.familymedicinereminderclient.bus.DataChangedEvent;
import com.example.sondrehj.familymedicinereminderclient.models.Medication;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import retrofit2.Call;
import retrofit2.Response;

public class DeleteMedicationJob extends Job {


    private static String TAG = "DeleteMedicationJob";
    private static final int PRIORITY = 1;
    private Medication medication;
    private String userId;
    private String authToken;

    public DeleteMedicationJob(Medication medication, String userId, String authToken) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.

        super(new Params(PRIORITY)
                .requireNetwork()
                .persist());
        Log.d(TAG, "New delete medication job posted.");
        this.medication = medication;
        this.userId = userId;
        this.authToken = authToken;
    }

    @Override
    public void onAdded() {
        Log.d(TAG, "Medication added.");
        // Job has been saved to disk.
        // This is a good place to dispatch a UI event to indicate the job will eventually run.
        // In this example, it would be good to update the UI with the newly posted tweet.
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(TAG, "Job is running in background.");
        Log.d(TAG, Thread.currentThread().toString());
        MyCyFAPPServiceAPI api = RestService.createRestService(authToken);
        Call<Boolean> call = api.deleteMedication(userId, medication.getServerId() + ""); //Will get problems here due if serverID = -1
        Response res = call.execute(); //medication retrieved from server
        Log.d(TAG, "Delete response: " + res.raw().toString());
        if(res.isSuccessful()) {
            Log.d(TAG, "deleted: ");
            //BusService.getBus().post(new DataChangedEvent(DataChangedEvent.MEDICATIONS));
        }
        else {
            Log.d(TAG, "Server did not respond to deletion request");
        }
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        throwable.printStackTrace();
        return true;

        // An error occurred in onRun.
        // Return value determines whether this job should retry running (true) or abort (false).
    }

    @Override
    protected void onCancel() {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has returned false.
    }
}
