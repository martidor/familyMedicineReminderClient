package com.example.sondrehj.familymedicinereminderclient.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sondrehj.familymedicinereminderclient.utility.DividerItemDecoration;
import com.example.sondrehj.familymedicinereminderclient.MainActivity;
import com.example.sondrehj.familymedicinereminderclient.adapters.MedicationRecyclerViewAdapter;
import com.example.sondrehj.familymedicinereminderclient.R;
import com.example.sondrehj.familymedicinereminderclient.database.MedicationListContent;
import com.example.sondrehj.familymedicinereminderclient.models.Medication;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MedicationListFragment extends android.app.Fragment{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";

    public static List<Medication> medications = new ArrayList<>();

    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MedicationListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MedicationListFragment newInstance() {
        MedicationListFragment fragment = new MedicationListFragment();
        return fragment;
    }

    public void notifyChanged() {
        RecyclerView recView = (RecyclerView) getActivity().findViewById(R.id.medication_list);
        medications.addAll(mListener.onGetMedications());
        if (recView != null) {
            recView.getAdapter().notifyDataSetChanged();
            System.out.println("notifychanged called");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        medications.addAll(mListener.onGetMedications());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medication_list, container, false);
        RecyclerView recView = (RecyclerView) view.findViewById(R.id.medication_list);

        // Set the adapter
        if (recView != null) {
            Context context = view.getContext();
            if (mColumnCount <= 1) {
                recView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
            recView.setAdapter(new MedicationRecyclerViewAdapter(getActivity(), medications, mListener));
        }

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.new_medication_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).changeFragment(new MedicationStorageFragment());
            }
        });
    return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnListFragmentInteractionListener {
        void onMedicationListFragmentInteraction(Medication medication);
        List<Medication> onGetMedications();
        }
    }
