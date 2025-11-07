package com.example.ajilore.code.ui.profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ajilore.code.R;

/**
 * Fragment representing a user profile screen.
 * Use the {@link #newInstance(String, String)} factory method to
 * create an instance with the provided parameters.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Default public constructor for {@link ProfileFragment}.
     * Required by the Android system for fragment instantiation.
     */
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of {@link ProfileFragment}
     * using the provided parameters.
     *
     * @param param1 The first initialization parameter.
     * @param param2 The second initialization parameter.
     * @return A new instance of fragment {@link ProfileFragment}.
     */
    // TODO: Rename and change types and number of parameters
    public static com.example.ajilore.code.ui.profile.ProfileFragment newInstance(String param1, String param2) {
        com.example.ajilore.code.ui.profile.ProfileFragment fragment = new com.example.ajilore.code.ui.profile.ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to do initial creation of the fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should attach to.
     * @param savedInstanceState If non-null, this fragment is being re-created from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}