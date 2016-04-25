package com.example.sondrehj.familymedicinereminderclient.dialogs;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.example.sondrehj.familymedicinereminderclient.MainActivity;
import com.example.sondrehj.familymedicinereminderclient.api.MyCyFAPPServiceAPI;
import com.example.sondrehj.familymedicinereminderclient.api.RestService;
import com.example.sondrehj.familymedicinereminderclient.models.Message;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by nikolai on 19/04/16.
 *
 * This dialog opens when
 */
public class LinkingDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final MyCyFAPPServiceAPI api = RestService.createRestService();

            final Account account = MainActivity.getAccount(getContext());

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Someone wants to link with your account and become your guardian! " +
                    "Only press yes if you know who this is!" + "Do you want to link your account?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Call<Message> call = api.responseToLinkingRequest(account.name, "accept");
                            call.enqueue(new Callback<Message>() {
                                @Override
                                public void onResponse(Call<Message> call, Response<Message> response) {

                                }

                                @Override
                                public void onFailure(Call<Message> call, Throwable t) {

                                }
                            });
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            Call<Message> call = api.responseToLinkingRequest(account.name, "deny");
                            call.enqueue(new Callback<Message>() {
                                @Override
                                public void onResponse(Call<Message> call, Response<Message> response) {

                                }

                                @Override
                                public void onFailure(Call<Message> call, Throwable t) {

                                }
                            });
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
}