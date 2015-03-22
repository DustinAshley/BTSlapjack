package com.dubstin.btslapjack;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
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
    private ImageButton cardPicture;
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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
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
            Log.i(TAG, "setting cardContainer" + String.valueOf(i));                int cardButtonID = getResources().getIdentifier("card" + String.valueOf(i), "id", getPackageName());
            ImageButton button = (ImageButton) findViewById(cardButtonID);
            if (i <= 54 - unusedCardsAmount) {
                int layoutID = getResources().getIdentifier("cardContainer" + String.valueOf(i), "id", getPackageName());
                LinearLayout layout = (LinearLayout) findViewById(layoutID);
                LayoutParams params = layout.getLayoutParams();
                params.height = Integer.valueOf((SCREEN_HEIGHT - 32) / 12);
                params.width = Integer.valueOf((SCREEN_WIDTH - 32) / 6);
                int padding = ((SCREEN_WIDTH / 6) - ((params.height * 76) / 100)) / 2;
                layout.setPadding(padding, 2, padding, 2);
                button.setOnClickListener(viewCardTime);
                if (!mySlapTimes.get(i - 1).equals(DEFAULTSLAPTIME)
                        || !connectedDeviceSlapTimes.get(i - 1).equals(DEFAULTSLAPTIME)) {
                    String cardName = cards.get(i - 1).valueToString().toLowerCase() + "_of_" + cards.get(i - 1).suitToString().toLowerCase();
                    int resourceId = getResources().getIdentifier("_" + cardName, "drawable", getPackageName());
                    button.setBackgroundResource(resourceId);
                    Log.i(TAG, "DID SLAP: setting cardContainer" + String.valueOf(i));
                } else {
                    button.setBackgroundResource(R.drawable._card_back);
                    Log.i(TAG, "NO SLAP: setting cardContainer" + String.valueOf(i));
                }
            } else {
                button.setVisibility(View.GONE);
            }
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
            Log.i(TAG, "cardID: " + v.getResources().getResourceName(v.getId()).replaceAll(".*card", ""));
            int cardID = Integer.parseInt(v.getResources().getResourceName(v.getId()).replaceAll(".*card", ""));
            String[] separated = mySlapTimes.get(cardID - 1).split("::");
            setTimeLabel(playerOneTime, separated[0]);
            separated = connectedDeviceSlapTimes.get(cardID - 1).split("::");
            setTimeLabel(playerTwoTime, separated[0]);
            Log.i(TAG, "my time: " + mySlapTimes.get(cardID - 1));
            Log.i(TAG, "connected device time: " + connectedDeviceSlapTimes.get(cardID - 1));
        }
    };

    private void setTimeLabel(TextView v, String text) {
        Log.i(TAG, text + "==" + String.valueOf(Integer.MAX_VALUE + "?"));
        if (!text.equals(String.valueOf(Integer.MAX_VALUE))) {
            v.setText(String.valueOf((Double.parseDouble(text) / 1000)) + " seconds");
        } else {
            v.setText("No contest");
        }

    }

}
