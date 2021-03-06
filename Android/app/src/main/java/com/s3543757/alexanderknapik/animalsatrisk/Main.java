package com.s3543757.alexanderknapik.animalsatrisk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickMap(View view)
    {
        Intent mapActivity = new Intent(Main.this, Map.class);
        startActivity(mapActivity);
    }

    public void onClickSearch(View view)
    {
        Intent searchActivity = new Intent(Main.this, SearchAnimalListActivity.class);
        startActivity(searchActivity);
    }

    public void onClickAreas(View view)
    {
        Intent areasActivity = new Intent(Main.this, SearchAreasListActivity.class);
        startActivity(areasActivity);
    }

    public void onClickUDP(View view)
    {
        Intent UDPActivity = new Intent(Main.this, UDPTester.class);
        startActivity(UDPActivity);
    }

}
