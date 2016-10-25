package com.kunalgrover05.travelplanner;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by grokunal on 22/10/16.
 */
public class MainLoginFragment extends Fragment {
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private String location;
    private String groupId;
    private String name;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.main_login, container, false);
        Button button = (Button) v.findViewById(R.id.submit);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupId = ((EditText)v.findViewById(R.id.group_id)).getText().toString();
                name = ((EditText)v.findViewById(R.id.name)).getText().toString();

                Intent intent = null;
                // Check if exists
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                // Check if Place exists, else send an intent
                databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(groupId)) {
                            // New SelectedListFragment
                            Bundle bundle = new Bundle();
                            bundle.putString("name", name);
                            bundle.putString("groupId", groupId);
                            bundle.putString("location", location);
                            SelectedListFragment fragment = new SelectedListFragment();
                            fragment.setArguments(bundle);
                            getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                        } else {
                            // New TouristAttractionFragment
                            Bundle bundle = new Bundle();
                            bundle.putString("name", name);
                            bundle.putString("groupId", groupId);
                            bundle.putString("location", location);
                            TouristAttractionsSearchFragment fragment = new TouristAttractionsSearchFragment();
                            fragment.setArguments(bundle);
                            getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        return v;
    }
}