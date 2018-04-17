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
        Intent searchActivity = new Intent(Main.this, Search.class);
        startActivity(searchActivity);
    }

    public void onClickAreas(View view)
    {
        Intent areasActivity = new Intent(Main.this, Areas.class);
        startActivity(areasActivity);
    }

}
