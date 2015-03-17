package com.dubstin.btslapjack;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class StatisticsActivity extends ActionBarActivity {

    ArrayList<String[]> mySlapTimes,
        connectedDeviceSlapTimes;

    public static final int VIEW_STATS = 1;

    private static final String TAG = "Statistics Activity";

    private ImageButton cardPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_statistics);

        cardPicture = (ImageButton) findViewById(R.id.card1);
        cardPicture.setOnClickListener(viewCardTimes);
        cardPicture.setBackgroundResource(R.drawable._card_back);


        Intent in = getIntent();

        mySlapTimes = (ArrayList<String[]>) in.getSerializableExtra("mySlapTimes");
        connectedDeviceSlapTimes = (ArrayList<String[]>) in.getSerializableExtra("connectedDeviceSlapTimes");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private View.OnClickListener viewCardTimes = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "my time: " + mySlapTimes.get(1));
            Log.i(TAG, "my time: " + connectedDeviceSlapTimes.get(1));
        }
    };



}
