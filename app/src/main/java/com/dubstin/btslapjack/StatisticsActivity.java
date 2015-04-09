package com.dubstin.btslapjack;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StatisticsActivity extends ActionBarActivity {

    ArrayList<String> mySlapTimes,
        connectedDeviceSlapTimes;
    public static final int VIEW_STATS = 1;
    private int unusedCardsAmount;
    private int SCREEN_WIDTH,
            SCREEN_HEIGHT;
    long deckSeed;
    private static final String TAG = "Statistics Activity",
        DEFAULTSLAPTIME = String.valueOf(Integer.MAX_VALUE) + "::false";
    private TextView playerOneName,
            playerTwoName,
            playerOneTime,
            playerTwoTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Entered onCreate()");
        setContentView(R.layout.activity_statistics);
        Intent in = getIntent();
        mySlapTimes = in.getStringArrayListExtra("mySlapTimes");
        connectedDeviceSlapTimes = in.getStringArrayListExtra("connectedDeviceSlapTimes");
        deckSeed = in.getLongExtra("seed", 0);
        unusedCardsAmount = in.getIntExtra("cardsRemaining", 0);
        Log.i(TAG, "seed: " + String.valueOf(deckSeed));
        getScreenSize();
        setupTable();
        playerOneName = (TextView) findViewById(R.id.playerOneName);
        playerTwoName = (TextView) findViewById(R.id.playerTwoName);
        playerOneTime = (TextView) findViewById(R.id.playerOneTime);
        playerTwoTime = (TextView) findViewById(R.id.playerTwoTime);
        playerOneName.setText(in.getStringExtra("p1Name"));
        playerTwoName.setText(in.getStringExtra("p2Name"));
        Button closeButton = (Button) findViewById(R.id.close_button);
        closeButton.setOnClickListener(closeActivity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupTable() {
        Deck deck = new Deck(false, true);
        Random rnd = new Random();
        rnd.setSeed(deckSeed);
        deck.shuffle(rnd);
        List<Card> cards = deck.getCards();
        for (int i = 1; i <= 54; i++) {
            Log.i(TAG, "setting cardContainer" + String.valueOf(i));
            int cardButtonID = getResources().getIdentifier("card" + String.valueOf(i), "id", getPackageName());
            ImageButton button = (ImageButton) findViewById(cardButtonID);
            if (i <= 54 - unusedCardsAmount) {
                button.setOnClickListener(viewCardTime);
                if (!mySlapTimes.get(i - 1).equals(DEFAULTSLAPTIME)
                        || !connectedDeviceSlapTimes.get(i - 1).equals(DEFAULTSLAPTIME)) {
                    if (cards.get(i - 1).suitToString().toLowerCase().equals("joker")) {
                        button.setBackgroundResource(R.drawable._black_joker);
                    } else {
                        String cardName = cards.get(i - 1).valueToString().toLowerCase() + "_of_" + cards.get(i - 1).suitToString().toLowerCase();
                        int resourceId = getResources().getIdentifier("_" + cardName, "drawable", getPackageName());
                        button.setBackgroundResource(resourceId);
                        Log.i(TAG, "DID SLAP: setting cardContainer" + String.valueOf(i));
                    }
                } else {
                    button.setBackgroundResource(R.drawable._card_back);
                    Log.i(TAG, "NO SLAP: setting cardContainer" + String.valueOf(i));
                }
            } else {
                button.setVisibility(View.GONE);
            }
        }

        for (int i = 1; i <= 9; i++) {
            int rowID = getResources().getIdentifier("cardRow" + String.valueOf(i), "id", getPackageName());
            LinearLayout ll = (LinearLayout) findViewById(rowID);
            ll.getLayoutParams().height = (((SCREEN_WIDTH / 6) * 85) / 100);
            ll.requestLayout();
            Log.i(TAG, "ROW" + String.valueOf(i) + ": " + String.valueOf(ll.getLayoutParams().height));
        }

    }

    private void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_WIDTH = size.x;
        SCREEN_HEIGHT = size.y;
        Log.i(TAG, "Screen Width: " + String.valueOf(SCREEN_WIDTH) + " Screen Height: " + String.valueOf(SCREEN_HEIGHT));
    }

    private View.OnClickListener viewCardTime = new View.OnClickListener() {
        public void onClick(View v) {
            int cardID = Integer.parseInt(v.getResources().getResourceName(v.getId()).replaceAll(".*card", ""));
            String[] separated = mySlapTimes.get(cardID - 1).split("::");
            setTimeLabel(playerOneTime, separated[0]);
            separated = connectedDeviceSlapTimes.get(cardID - 1).split("::");
            setTimeLabel(playerTwoTime, separated[0]);
        }
    };

    private View.OnClickListener closeActivity = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };


    private void setTimeLabel(TextView v, String text) {
        if (!text.equals(String.valueOf(Integer.MAX_VALUE))) {
            v.setText(String.valueOf((Double.parseDouble(text) / 1000)) + " seconds");
        } else {
            v.setText("No contest");
        }

    }

}
