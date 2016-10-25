package com.kunalgrover05.travelplanner;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by grokunal on 22/10/16.
 */
public class SelectedListFragment extends Fragment{
    private Adapter adapter;
    private List<MyPlace> places = new ArrayList<>();
    private String mName;
    private String mGroupId;
    private String mLocation;


    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        adapter = new Adapter();
        mGroupId = getArguments().getString("groupId");
        mName = getArguments().getString("name");
        mLocation = getArguments().getString("location");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.layout_selected_places, container, false);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.selected_places_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        Button mButton = (Button) v.findViewById(R.id.goto_search);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                TouristAttractionsSearchFragment fragment = new TouristAttractionsSearchFragment();
                Bundle args = new Bundle();
                args.putString("groupId", mGroupId);
                args.putString("name", mName);
                args.putString("location", mLocation);

                fragment.setArguments(args);

                fm.beginTransaction().replace(R.id.container, fragment).commit();
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(mGroupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                places.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    MyPlace place = dataSnapshot.getValue(MyPlace.class);
                    places.add(place);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;
    }

    private class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        TextView mTextView, mAdderView;
        ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.label);
            mAdderView = (TextView) itemView.findViewById(R.id.adder);
            mImageView = (ImageView) itemView.findViewById(R.id.image_view);
            mImageView.setImageDrawable(null);
        }

        public void bind(@Nullable String placeID, @Nullable String name, @Nullable String adder) {
            mTextView.setText(name);
            mAdderView.setText(adder);

            getPhotoTask(placeID);
        }

        private void getPhotoTask(String placeId) {
            // Create a new AsyncTask that displays the bitmap and attribution once loaded.
            new PhotoTask(100, 100, ((MainActivity)getActivity()).mGoogleApiClient) {
                @Override
                protected void onPreExecute() {
                    // Display a temporary image to show while bitmap is loading.
                    mImageView.setImageResource(R.drawable.cast_album_art_placeholder);
                }

                @Override
                protected void onPostExecute(AttributedPhoto attributedPhoto) {
                    if (attributedPhoto != null) {
                        // Photo has been loaded, display it.
                        mImageView.setImageBitmap(attributedPhoto.bitmap);
                    }
                }
            }.execute(placeId);
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_selected, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MyPlace myPlace = places.get(position);
            holder.bind(myPlace.getPlaceId(), myPlace.getName(), myPlace.getAdder());
        }


        @Override
        public int getItemCount() {
            return places.size();
        }
    }
}
