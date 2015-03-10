package com.dubstin.btslapjack;

import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class MainActivity extends ActionBarActivity {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean isDebugMode = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_SAVE_DEVICE = 6;

    private static final String PREFS_LAST_DEVICE = "LastDevice";
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Save Instance Keys
    private static final String KEY_DECK = "deckCards";
    private static final String KEY_PILECARDS = "pileCards";
    private static final String KEY_TOPCARD = "topCard";
    private static final String KEY_NUMBEROFDECKS = "numberOfDecks";
    private static final String KEY_TIMESTAMP = "timeStamp";
    private static final String KEY_PLAYER1CARDS = "playerOneCards";
    private static final String KEY_PLAYER2CARDS = "playerTwoCards";
    private static final String KEY_PLAYER1NAME = "playerOneName";
    private static final String KEY_PLAYER2NAME = "playerTwoName";

    // Layout Views
    private TextView title;
    private Button startButton;

    private int SCREEN_WIDTH, SCREEN_HEIGHT, numberOfJacksPlayed, numberOfDecks;
    private long dealTimestamp, slapTimestamp;
    private Button mainButton;
    LinearLayout gameContainer, connectContainer;
    private boolean isConnected, isReadyToStart, isConnectedDeviceReadyToStart;
    private TextView deckCountLabel, pileCountLabel, playerOneNameLabel, playerTwoNameLabel,
            playerOneHandCountLabel, playerTwoHandCountLabel, topCardLabel, timestampLabel,
            winnerLabel;
    private ImageButton cardPicture;
    private Deck deck;
    private Card topCard;
    private ArrayList<Card> pile = new ArrayList<Card>();
    private Player playerOne = new Player(), playerTwo = new Player();
    private Handler cardDealer = new Handler();

    private final int SIMPLE_NOTFICATION_ID = 1;
    private String connectedDeviceName = null, bluetoothAddress = null,
            connectedDeviceBlueToothAddress = null;
    private StringBuffer sendStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothChatService chatService = null;
    private NotificationManager notificationManager;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = prefs.edit();
        if (isDebugMode) {
            Log.e(TAG, "Entered onCreate()");
        }
        setContentView(R.layout.activity_main);
        setScreenSize();
        initializeButtons();
        initializeContainers();
        initializeLabels();
        if (savedInstanceState != null) {
            numberOfDecks = savedInstanceState.getInt(KEY_NUMBEROFDECKS);
            topCard = savedInstanceState.getParcelable(KEY_TOPCARD);
            deck = savedInstanceState.getParcelable(KEY_DECK);
            pile = savedInstanceState.getParcelableArrayList(KEY_PILECARDS);
            ArrayList<Card> savedCards = savedInstanceState.getParcelableArrayList(KEY_PLAYER1CARDS);
            playerOne.setCards(savedCards);
            savedCards = savedInstanceState.getParcelableArrayList(KEY_PLAYER2CARDS);
            playerTwo.setCards(savedCards);
            playerOne.setName(savedInstanceState.getString(KEY_PLAYER1NAME));
            playerTwo.setName(savedInstanceState.getString(KEY_PLAYER2NAME));
            timestampLabel.setText(savedInstanceState.getString(KEY_TIMESTAMP));
        } else {
            playerOne.setName("Dustin");
            playerTwo.setName("Bot");
            numberOfDecks = 1;
            deck = new Deck(true, true);
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAddress = bluetoothAdapter.getAddress();
        Log.i(TAG, "Bluetooth Address: " + bluetoothAddress);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.i("FNORD", "notificationManager: " + notificationManager);
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isDebugMode) {
            Log.e(TAG, "Entered onStart()");
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (chatService == null) setupGame();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (isDebugMode) {
            Log.e(TAG, "Entered onResume()");
        }
        notificationManager.cancel(SIMPLE_NOTFICATION_ID);
        Log.i("FNORD", "" + getIntent());
        if (chatService != null) {
            if (chatService.getState() == BluetoothChatService.STATE_NONE) {
                chatService.start();
                String address = prefs.getString(PREFS_LAST_DEVICE, null);
                Log.i(TAG, " Address: " + address);
                if (chatService.getState() != BluetoothChatService.STATE_CONNECTED && address != null) {
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    Log.i(TAG, " DeviceAddress:" + device.getAddress());
                    Log.i(TAG, " DeviceName:" + device.getName());
                    chatService.connect(device);
                }
            }
        }
        if (isConnected) {
            showGameContainer();
            sendBluetoothMessage("id::" + bluetoothAddress);
        }
    }

    private BigInteger addressToInt(String addr) {
        String outString = "";
        String[] separatedVals = addr.split(":");
        for (int i = 0; i < separatedVals.length; i++) {
            outString += separatedVals[i];
        }
        return new BigInteger(outString, 16);
    }

    private void setupGame() {
        if (isDebugMode) {
            Log.d(TAG, "Entered setupGame()");
        }
        startButton.setVisibility(View.VISIBLE);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = "start";
                isReadyToStart= true;
                if (isReadyToStart && isConnectedDeviceReadyToStart) {
                    startDealing();
                }
                sendBluetoothMessage(message);
            }
        });
        chatService = new BluetoothChatService(this, bluetoothMessageHandler);
        sendStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (isDebugMode) {
            Log.e(TAG, "Entered onPause()");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isDebugMode) {
            Log.e(TAG, "Entered onStop()");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isDebugMode) {
            Log.e(TAG, "Entered onDestroy()");
        }
        if (chatService != null) {
            chatService.stop();
        }
    }

    private void makeDiscoverable() {
        if (isDebugMode) {
            Log.d(TAG, "Making device discoverable");
        }
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendBluetoothMessage(String message) {
        if (chatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            showConnectContainer();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            Log.i(TAG, "Sending message: " + message);
            chatService.write(send);
            sendStringBuffer.setLength(0);
        }
    }

    private final Handler bluetoothMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i("FNORD", "handleMessage(): msg: " + msg);
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (isDebugMode) {
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    }
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            title.setText(R.string.title_connected_to);
                            title.append(connectedDeviceName);
                            sendBluetoothMessage("id::" + bluetoothAddress);
                            isConnected = true;
                            showGameContainer();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            title.setText(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            title.setText(R.string.title_not_connected);
                            showConnectContainer();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    String[] separated = readMessage.split("::");
                    switch (separated[0]) {
                        case "start":
                            Log.i(TAG, "Got Start signal");
                            isConnectedDeviceReadyToStart = true;
                            if (isReadyToStart && isConnectedDeviceReadyToStart) {
                                startDealing();
                            }
                            break;
                        case "id":
                            Log.i(TAG, "Got Identification String: " + separated[1]);
                            connectedDeviceBlueToothAddress = separated[1];
                            if (isPrimaryDevice()) {
                                Random rnd = new Random();
                                long seed = rnd.nextLong();
                                sendBluetoothMessage("seed::" + String.valueOf(seed));
                                rnd.setSeed(seed);
                                deck.shuffle(rnd);
                            }

                            break;
                        case "seed":
                            Log.i(TAG, "Got Deck Seed: " + separated[1]);
                            Random rnd = new Random();
                            rnd.setSeed(Long.parseLong(separated[1]));
                            deck.shuffle(rnd);
                            break;
                        case "timestamp":
                            Log.i(TAG, "Got Time Stamp: " + separated[1]);
                            break;
                        default:
                            Log.i(TAG, "Received an unrecognized message: " + readMessage);
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_SAVE_DEVICE:
                    String address = msg.obj.toString();
                    prefsEditor.putString(PREFS_LAST_DEVICE, address);
                    prefsEditor.commit();
                    break;
            }
        }
    };

    private boolean isPrimaryDevice() {
        if (addressToInt(bluetoothAddress).compareTo(addressToInt(connectedDeviceBlueToothAddress)) == 1) {
            if (isDebugMode) {
                Log.i(TAG, "This device is primary.");
            }
            return true;
        } else {
            if (isDebugMode) {
                Log.i(TAG, "This device is secondary.");
            }
            return false;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isDebugMode) {
            Log.d(TAG, "onActivityResult " + resultCode);
        }
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    chatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupGame();
                } else {
                    Log.d(TAG, "ERROR: Bluetooth is not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
              makeDiscoverable();
                return true;
            case R.id.action_toggle_view:
                toggleView();
        }
        return false;
    }

    private void toggleView() {
        if (gameContainer.getVisibility() == View.VISIBLE) {
            showConnectContainer();
        } else {
            showGameContainer();
        }
    }

    private void showGameContainer() {
        if (isDebugMode) {
            Log.i(TAG, "Showing game container");
        }
        gameContainer.setVisibility(View.VISIBLE);
        connectContainer.setVisibility(View.GONE);
        updateScreen();
    }

    private void showConnectContainer() {
        if (isDebugMode) {
            Log.i(TAG, "Showing connect container");
        }
        gameContainer.setVisibility(View.GONE);
        connectContainer.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.INVISIBLE);
    }

    private void initializeContainers() {
        gameContainer = (LinearLayout) findViewById(R.id.gameContainer);
        connectContainer = (LinearLayout) findViewById(R.id.connectContainer);
        if (!isConnected) {
            showConnectContainer();
        } else {
            showGameContainer();
        }
    }

    private void setScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_WIDTH = size.x;
        SCREEN_HEIGHT = size.y;
    }

    private void initializeLabels() {
        deckCountLabel = (TextView) findViewById(R.id.deckCardCount);
        pileCountLabel = (TextView) findViewById(R.id.pileCount);
        playerOneNameLabel = (TextView) findViewById(R.id.playerOneName);
        playerTwoNameLabel = (TextView) findViewById(R.id.playerTwoName);
        playerOneHandCountLabel = (TextView) findViewById(R.id.playerOneHandCount);
        playerTwoHandCountLabel = (TextView) findViewById(R.id.playerTwoHandCount);
        timestampLabel = (TextView) findViewById(R.id.slapTimestamp);
        winnerLabel = (TextView) findViewById(R.id.winner);
        topCardLabel = (TextView) findViewById(R.id.topCard);
        title = (TextView) findViewById(R.id.title_left_text);
        title.setText(R.string.app_name);
        title = (TextView) findViewById(R.id.title_right_text);
    }

    private void initializeButtons() {
        mainButton = (Button) findViewById(R.id.goToMainButton);
        mainButton.setOnClickListener(goToMain);
        cardPicture = (ImageButton) findViewById(R.id.cardPicture);
        cardPicture.setOnClickListener(doSlapCard);
        startButton = (Button) findViewById(R.id.button_start);
    }

    private void updateScreen() {
        updatePileCount();
        updateDeckCount();
        updatePlayerOneLabels();
        updatePlayerTwoLabels();
        showCard(topCard);
    }

    private void updateDeckCount() {
        deckCountLabel.setText("Cards in Deck: " + String.valueOf(deck.getCardCount()));
    }

    private void updatePileCount() {
        pileCountLabel.setText("Cards in Pile: " + String.valueOf(pile.size()));
    }

    private void updatePlayerOneLabels() {
        playerOneNameLabel.setText(playerOne.getName());
        playerOneHandCountLabel.setText(" Cards: " + playerOne.getHandCount());
    }

    private void updatePlayerTwoLabels() {
        playerTwoNameLabel.setText(playerTwo.getName());
        playerTwoHandCountLabel.setText("Cards: " + playerTwo.getHandCount());
    }

    private void givePileToPlayer(ArrayList<Card> cards, Player p) {
        if (cards.size() > 0) {
            p.grabPile(cards);
        }
    }

    private View.OnClickListener doSlapCard = new View.OnClickListener() {
        public void onClick(View v) {
            cardPicture.setBackgroundResource(R.drawable._card_back);
            slapCard();
            updateScreen();
            if (isDebugMode) {
                sendBluetoothMessage("Default Test Message");
            }
        }
    };

    public void startGame() {

    }

    public void startDealing() {
        cardDealer.postDelayed(new Runnable() {
            public void run() {
                if (deck.getCardCount() > 0 && !isGameOver()) {
                    dealCard();
                    updateScreen();
                    cardDealer.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    public void pauseDealing() {
        try {
            cardDealer.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resumeDealing() {
        cardDealer.notify();
    }

    public void stopDealing() {
        //cardDealer.removeCallbacks();
    }

    public void dealCard() {
        if (deck.getCardCount() > 0 && !isGameOver()) {
            topCard = new Card(deck.dealCard());
            if (topCard.getValue() == 11) {
                numberOfJacksPlayed++;
                Log.i(TAG, "Dealt Jack: #" + String.valueOf(numberOfJacksPlayed));
            }
            pile.add(topCard);
        } else {
            doGameOver();
        }
    }

    private void showCard(Card c) {
        String cardName = "";
        if (c != null) {
            if (c.getSuit() == 4) {
                cardName = c.suitToString();
                topCardLabel.setText(cardName);
                cardPicture.setBackgroundResource(R.drawable._black_joker);
            } else {
                cardName = c.valueToString().toLowerCase() + "_of_" + c.suitToString().toLowerCase();
                topCardLabel.setText(c.valueToString() + " of " + c.suitToString());
                String res_string = "_" + cardName;
                Resources res = getResources();
                int resourceId = res.getIdentifier(res_string, "drawable", getPackageName());
                cardPicture.setBackgroundResource(resourceId);
                dealTimestamp = new Date().getTime();
            }
        } else {
            cardPicture.setBackgroundResource(R.drawable._card_back);
        }

    }

    private View.OnClickListener goToMain = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

    public void slapCard() {
        slapTimestamp = new Date().getTime();
        if (topCard != null) {
            if (topCard.getValue() == 11) {
                givePileToPlayer(pile, playerOne);
                long timeStamp = slapTimestamp - dealTimestamp;
                timestampLabel.setText(String.valueOf(timeStamp/1000.00) + " seconds");
                sendBluetoothMessage("timestamp::" + String.valueOf(timeStamp));
            //} else {
            //    givePileToPlayer(pile, playerTwo);
            }
            topCard = null;
            showCard(topCard);
            if (isGameOver()) {
                Player winner = determineWinner();
                winnerLabel.setText(winner.getName() + " wins!");
            }
            pile.clear();
        }
    }

    private boolean isGameOver() {
        if (numberOfJacksPlayed == numberOfDecks * 4) {
            return true;
        } else if (Math.max(playerOne.getHandCount(), playerTwo.getHandCount()) >= numberOfDecks * 27) {
            return true;
        }
        return false;
    }

    private Player determineWinner() {
        return (playerOne.getHandCount() > playerTwo.getHandCount()) ? playerOne : playerTwo;
    }

    private void doGameOver() {
        cardPicture.setBackgroundResource(R.drawable._card_back);
    }

}
