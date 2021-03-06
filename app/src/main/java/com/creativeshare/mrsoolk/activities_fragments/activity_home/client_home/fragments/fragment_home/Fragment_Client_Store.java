package com.creativeshare.mrsoolk.activities_fragments.activity_home.client_home.fragments.fragment_home;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.creativeshare.mrsoolk.R;
import com.creativeshare.mrsoolk.activities_fragments.activity_home.client_home.activity.ClientHomeActivity;
import com.creativeshare.mrsoolk.adapters.NearbyAdapter;
import com.creativeshare.mrsoolk.adapters.QueryAdapter;
import com.creativeshare.mrsoolk.adapters.SliderAdapter;
import com.creativeshare.mrsoolk.models.NearbyModel;
import com.creativeshare.mrsoolk.models.NearbyStoreDataModel;
import com.creativeshare.mrsoolk.models.PlaceModel;
import com.creativeshare.mrsoolk.models.QuerySearchModel;
import com.creativeshare.mrsoolk.models.SliderModel;
import com.creativeshare.mrsoolk.remote.Api;
import com.creativeshare.mrsoolk.tags.Tags;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Client_Store extends Fragment {
    private ClientHomeActivity activity;
    private LinearLayout ll_search,ll_no_store;
    private CardView cardView;
    private ProgressBar progBar,progBarSlider;
    private RecyclerView recView,recViewQueries;
    private RecyclerView.LayoutManager manager,managerQueries;
    private NearbyAdapter adapter;
    private List<PlaceModel> nearbyModelList,mainNearbyModelList;
    private ViewPager pager;
    private TabLayout tab;
    private FrameLayout fl_slider;
    private Location location;
    private QueryAdapter queryAdapter;
    private List<String> queriesList;
    private List<QuerySearchModel> en_ar_queriesList;
    private Timer timer;
    private TimerTask timerTask;
    private SliderAdapter sliderAdapter;
    private String current_language;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_store, container, false);
        initView(view);
        return view;
    }

    public static Fragment_Client_Store newInstance() {
        return new Fragment_Client_Store();
    }

    private void initView(View view) {
        activity = (ClientHomeActivity) getActivity();
        Paper.init(activity);
        current_language = Paper.book().read("lang", Locale.getDefault().getLanguage());
        nearbyModelList = new ArrayList<>();
        mainNearbyModelList = new ArrayList<>();
        queriesList = new ArrayList<>();
        queriesList.add("restaurant");
        queriesList.add("bakery");
        queriesList.add("supermarket");
        queriesList.add("cafe");
        queriesList.add("store");
        queriesList.add("florist");

        en_ar_queriesList = new ArrayList<>();
        en_ar_queriesList.add(new QuerySearchModel(getString(R.string.restaurant),R.drawable.ic_restaurant));
        en_ar_queriesList.add(new QuerySearchModel(getString(R.string.bakery),R.drawable.ic_sweet));
        en_ar_queriesList.add(new QuerySearchModel(getString(R.string.supermarket),R.drawable.ic_nav_store));
        en_ar_queriesList.add(new QuerySearchModel(getString(R.string.cafe),R.drawable.ic_cup));
        en_ar_queriesList.add(new QuerySearchModel(getString(R.string.store),R.drawable.ic_store));
        en_ar_queriesList.add(new QuerySearchModel(getString(R.string.florist),R.drawable.ic_gift));




        ll_search = view.findViewById(R.id.ll_search);
        ll_no_store = view.findViewById(R.id.ll_no_store);


        cardView = view.findViewById(R.id.cardView);
        pager = view.findViewById(R.id.pager);
        tab = view.findViewById(R.id.tab);
        tab.setupWithViewPager(pager);

        fl_slider = view.findViewById(R.id.fl_slider);
        progBarSlider = view.findViewById(R.id.progBarSlider);
        progBarSlider.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(activity,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        progBar = view.findViewById(R.id.progBar);
        progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(activity,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        recView = view.findViewById(R.id.recView);
        manager = new LinearLayoutManager(activity);
        recView.setLayoutManager(manager);
        recView.setDrawingCacheEnabled(true);
        recView.setItemViewCacheSize(20);
        recView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        recView.setNestedScrollingEnabled(false);
        recViewQueries = view.findViewById(R.id.recViewQueries);
        managerQueries = new LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false);
        recViewQueries.setLayoutManager(managerQueries);
        queryAdapter = new QueryAdapter(en_ar_queriesList,activity,this);
        recViewQueries.setAdapter(queryAdapter);

        ll_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.DisplayFragmentSearch();
            }
        });





    }

    private void getAds() {

        Api.getService(Tags.base_url)
                .getAds()
                .enqueue(new Callback<SliderModel>() {
                    @Override
                    public void onResponse(Call<SliderModel> call, Response<SliderModel> response) {
                        progBarSlider.setVisibility(View.GONE);

                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            updateSliderData(response.body());
                        }else
                            {

                                try {
                                    Log.e("error_code",response.code()+"_"+response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                fl_slider.setVisibility(View.GONE);

                            }
                    }

                    @Override
                    public void onFailure(Call<SliderModel> call, Throwable t) {


                        try {
                            progBarSlider.setVisibility(View.GONE);

                            fl_slider.setVisibility(View.GONE);
                            Log.e("Error",t.getMessage());

                        }catch (Exception e){}
                    }
                });
    }

    private void updateSliderData(SliderModel sliderModel) {

        if (sliderModel.getData().size()>0)
        {
            fl_slider.setVisibility(View.VISIBLE);

            if (sliderModel.getData().size()>1)
            {
                timer = new Timer();
                timerTask = new MyTimerTask();
                timer.scheduleAtFixedRate(timerTask,6000,6000);
            }
            sliderAdapter = new SliderAdapter(sliderModel.getData(),activity);
            pager.setAdapter(sliderAdapter);

            for (int i=0;i<tab.getTabCount()-1;i++)
            {
                View view = ((ViewGroup)tab.getChildAt(0)).getChildAt(i);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                params.setMargins(5,0,5,0);
                tab.requestLayout();
            }


        }else
            {
                fl_slider.setVisibility(View.GONE);

            }



    }


    @SuppressLint("MissingPermission")
    public void getNearbyPlaces(final Location location,String query)
    {
        getAds();
        progBar.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.VISIBLE);
        ll_no_store.setVisibility(View.GONE);
        nearbyModelList.clear();
        if (adapter!=null)
        {
            adapter.notifyDataSetChanged();
        }

        if (location!=null)
        {
            this.location = location;
            String loc = location.getLatitude()+","+location.getLongitude();

            Api.getService("https://maps.googleapis.com/maps/api/")
                    .getNearbyStores(loc,15000,query,current_language,getString(R.string.map_api_key))
                    .enqueue(new Callback<NearbyStoreDataModel>() {
                        @Override
                        public void onResponse(Call<NearbyStoreDataModel> call, Response<NearbyStoreDataModel> response) {
                            if (response.isSuccessful()&&response.body()!=null)
                            {
                                progBar.setVisibility(View.GONE);
                                if (response.body().getResults().size()>0)
                                {
                                    ll_no_store.setVisibility(View.GONE);
                                    updateUi(response.body(),location);
                                }else
                                {
                                    ll_no_store.setVisibility(View.VISIBLE);

                                }
                            }else
                                {

                                    progBar.setVisibility(View.GONE);

                                    try {
                                        Log.e("error_code",response.errorBody().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }


                        }

                        @Override
                        public void onFailure(Call<NearbyStoreDataModel> call, Throwable t) {
                            try {

                                Log.e("Error",t.getMessage());
                                progBar.setVisibility(View.GONE);
                                Toast.makeText(activity, getString(R.string.something), Toast.LENGTH_LONG).show();
                            }catch (Exception e)
                            {

                            }
                        }
                    });
        }

    }

    private void updateUi(NearbyStoreDataModel nearbyStoreDataModel, Location location) {




        if (mainNearbyModelList.size()==0)
        {
            mainNearbyModelList.clear();
            mainNearbyModelList.addAll(getPlaceModelFromResult(nearbyStoreDataModel.getResults()));

        }

        nearbyModelList.addAll(getPlaceModelFromResult(nearbyStoreDataModel.getResults()));


        recViewQueries.setVisibility(View.VISIBLE);
        if (adapter == null)
        {
            adapter = new NearbyAdapter(nearbyModelList,activity,this,location.getLatitude(),location.getLongitude());
            recView.setAdapter(adapter);

        }else
            {
                adapter.notifyDataSetChanged();
            }

            activity.DisplayFragmentHomeView();

    }

    private List<PlaceModel> getPlaceModelFromResult(List<NearbyModel> nearbyModelList)
    {
        List<PlaceModel> returnedList = new ArrayList<>();
        for (NearbyModel nearbyModel : nearbyModelList)
        {


            PlaceModel placeModel = new PlaceModel(nearbyModel.getId(),nearbyModel.getPlace_id(),nearbyModel.getName(),nearbyModel.getIcon(),nearbyModel.getRating(),nearbyModel.getGeometry().getLocation().getLat(),nearbyModel.getGeometry().getLocation().getLng(),nearbyModel.getVicinity());


            if (nearbyModel.getOpening_hours()!=null)
            {
                placeModel.setOpenNow(nearbyModel.getOpening_hours().isOpen_now());

            }else
                {
                    placeModel.setOpenNow(false);


                }
            returnedList.add(placeModel);
        }
        return returnedList;
    }

    public void setItemData(PlaceModel placeModel) {
        activity.DisplayFragmentStoreDetails(placeModel);

    }

    public void setQueryItemData(int pos)
    {
        if (pos == 0)
        {
            nearbyModelList.clear();
            nearbyModelList.addAll(mainNearbyModelList);
            if (mainNearbyModelList.size()>0)
            {
                ll_no_store.setVisibility(View.GONE);

            }
            if (adapter!=null)
            {
                adapter.notifyDataSetChanged();
            }
        }else
            {
                String query = queriesList.get(pos);
                getNearbyPlaces(location,query);

            }
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (pager.getCurrentItem()<pager.getAdapter().getCount()-1)
                    {
                        pager.setCurrentItem(pager.getCurrentItem()+1,true);
                    }else
                        {
                            pager.setCurrentItem(0);
                        }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer!=null)
        {
            timer.purge();
            timer.cancel();
        }
        if (timerTask!=null)
        {
            timerTask.cancel();
        }
    }
}
