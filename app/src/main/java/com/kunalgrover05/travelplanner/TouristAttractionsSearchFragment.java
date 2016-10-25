package com.kunalgrover05.travelplanner;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kunalgrover05.travelplanner.JavaGen.Result;
import com.kunalgrover05.travelplanner.JavaGen.Schema;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by grokunal on 21/10/16.
 */
public class TouristAttractionsSearchFragment extends Fragment {
    private String mName, mGroupId;

    private RxJavaCallAdapterFactory rxAdapter;
    private Retrofit retrofit;
    private GoogleAPI googleAPI;
    private List<Result> resultList;
    private Adapter adapter;
    private String mLocation;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                mLocation = String.format("%s,%s", place.getLatLng().latitude, place.getLatLng().longitude);

                // Save this
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference key = databaseReference.child("places").child(mGroupId);
                key.setValue(mLocation);
                makeCall();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void makeCall() {
        String apiKey = null;
        ApplicationInfo ai = null;
        try {
            ai = getActivity().getPackageManager().getApplicationInfo(
                    getActivity().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            apiKey = bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Observable<Schema> call = googleAPI.nearbyLocations(apiKey, mLocation);
        call.subscribeOn(Schedulers.io()) // optional if you do not wish to override the default behavior
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Schema>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        // cast to retrofit.HttpException to get the response code
                        if (e instanceof HttpException) {
                            HttpException response = (HttpException)e;
                            int code = response.code();
                        }
                    }

                    @Override
                    public void onNext(Schema response) {
                        resultList = response.getResults();
                        adapter.notifyDataSetChanged();
                    }
                });

    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        // Call something using Retrofit
        rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
        retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(rxAdapter)
                .build();
        googleAPI = retrofit.create(GoogleAPI.class);
        adapter = new Adapter();
        mName = getArguments().getString("name");
        mGroupId = getArguments().getString("groupId");
        mLocation = getArguments().getString("location");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Check if Place exists, else send an intent
        databaseReference.child("places").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mGroupId)) {

                    mLocation = (String) dataSnapshot.child(mGroupId).getValue();

                    Log.d("Firebase", mLocation);
                    makeCall();
                } else {
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(getActivity());
                        // Check if Place exists, else send an intent
                        startActivityForResult(intent, 1);
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_places, container, false);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.places_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.setAdapter(adapter);

        Button mButton = (Button) v.findViewById(R.id.goto_selected);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                SelectedListFragment fragment = new SelectedListFragment();
                Bundle args = new Bundle();
                args.putString("name", mName);
                args.putString("groupId", mGroupId);
                args.putString("location", mLocation);

                fragment.setArguments(args);

                fm.beginTransaction().replace(R.id.container, fragment).commit();
            }
        });
        return v;
    }

    private class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTextView;
        ImageView mImageView;
        MyPlace myPlace;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTextView = (TextView) itemView.findViewById(R.id.label);

            mImageView = (ImageView) itemView.findViewById(R.id.image_view);
            mImageView.setImageDrawable(null);
        }

        public void bind(String placeID, String name, Integer price) {
            String textOutput = name;
            if (price != null) {
                textOutput += String.format("Cost %d/4", price);
            }
            mTextView.setText(textOutput);
            getPhotoTask(placeID);
            myPlace = new MyPlace(placeID, name, mName);
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

        @Override
        public void onClick(View v) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference key = databaseReference.child("users").child(mGroupId).push();
            key.setValue(myPlace);
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Result result = resultList.get(position);
            holder.bind(result.getPlaceId(), result.getName(), result.getPriceLevel());
        }


        @Override
        public int getItemCount() {
            if (resultList == null)
                return 0;
            else
                return resultList.size();
        }
    }
}

