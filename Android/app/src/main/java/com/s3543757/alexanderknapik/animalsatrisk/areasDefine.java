package com.s3543757.alexanderknapik.animalsatrisk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

public class areasDefine extends AppCompatActivity {

    private RecyclerView recAreaList;
    private RecyclerView.Adapter areaListAdapter;
    private RecyclerView.LayoutManager areaListManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_areas_define);

        recAreaList = (RecyclerView) findViewById(R.id.recAreaList);

        //Optimisation
        //Set so contents do not change layout size of RecyclerView
        recAreaList.setHasFixedSize(true);

        //Use linear layout manager
        areaListManager = new LinearLayoutManager(this);
        recAreaList.setLayoutManager(areaListManager);

        //Specify adapter
    }
}






