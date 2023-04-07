package com.example.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout player1Layout, player2Layout;
    private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9;
    private TextView player1NameTv, player2NameTv;

    private final List<int[]> combinationsList = new ArrayList<>();
    private final List<String> doneBoxes = new ArrayList<String>();

    private String playerUniqueId = "0";

    ;
    private String opponentUniqueId = "0";
    private String status = "matching";

    private String connectionId = "0";

    ValueEventListener turnsEventListener, wonEventListener;

    private final String[] boxesSelectedBy ={"","","","","","","","",""};

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tictactoe2-87b20-default-rtdb.firebaseio.com/");

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        player1Layout = findViewById(R.id.player1Layout);
        player2Layout = findViewById(R.id.player2Layout);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        image5 = findViewById(R.id.image5);
        image6 = findViewById(R.id.image6);
        image7 = findViewById(R.id.image7);
        image8 = findViewById(R.id.image8);
        image9 = findViewById(R.id.image9);

        player1NameTv = findViewById(R.id.player1NameTv);
        player2NameTv = findViewById(R.id.player2NameTv);

        final String playerName = getIntent().getStringExtra("playerName");


        combinationsList.add(new int[]{0, 1, 2});
        combinationsList.add(new int[]{3, 4, 5});
        combinationsList.add(new int[]{6, 7, 8});
        combinationsList.add(new int[]{0, 3, 6});
        combinationsList.add(new int[]{1, 4, 7});
        combinationsList.add(new int[]{2, 5, 8});
        combinationsList.add(new int[]{0, 4, 8});
        combinationsList.add(new int[]{2, 4, 6});

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Raqibingiz kirishi kutilmoqda...");
        progressDialog.show();

        playerUniqueId = String.valueOf(System.currentTimeMillis());
        player1NameTv.setText(playerName);

        databaseReference.child("connections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!getOpponentFound()) {
                    if (snapshot.hasChildren()) {
                        for (DataSnapshot connections : snapshot.getChildren()) {
                            long conId = Long.parseLong(connections.getKey());
                            int getPlayersCount = (int) connections.getChildrenCount();
                            if (status.equals("waiting")) {
                                if (getPlayersCount == 2) {
                                    setPlayerTurn(playerUniqueId);
                                    applyPlayerTurn(playerUniqueId);
                                    boolean playerFound = false;
                                    for (DataSnapshot players : connections.getChildren()) {
                                        String getPlayerUniqueId = players.getKey();
                                        if (getPlayerUniqueId.equals(playerUniqueId)) {
                                            playerFound = true;
                                        } else {
                                            if (playerFound) {
                                                String getOpponentPlayerName = players.child("player_name").getValue(String.class);
                                                opponentUniqueId = players.getKey();
                                                player2NameTv.setText(getOpponentPlayerName);
                                                connectionId = String.valueOf(conId);
                                                setOpponentFound(true);
                                                databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                                databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                                if (progressDialog.isShowing()) {
                                                    progressDialog.dismiss();
                                                }
                                                databaseReference.child("connections").removeEventListener(this);

                                            }
                                        }
                                    }
                                }
                            } else {
                                if (getPlayersCount == 1) {
                                    connections.child(playerUniqueId).child("player_name").getRef().setValue(playerName);
                                    for (DataSnapshot players : connections.getChildren()) {
                                        String getOpponentPlayerName = players.child("player_name").getValue(String.class);

                                        opponentUniqueId = players.getKey();
                                        setPlayerTurn(opponentUniqueId);
                                        applyPlayerTurn(opponentUniqueId);
                                        player2NameTv.setText(getOpponentPlayerName);
                                        connectionId = String.valueOf(conId);
                                        setOpponentFound(true);
                                        databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                        databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                        databaseReference.child("connections").removeEventListener(this);
                                        break;

                                    }

                                }
                            }
                        }
                    }
                    if (!getOpponentFound() && !status.equals("waiting")) {
                        String connectionUniqueId = String.valueOf(System.currentTimeMillis());
                        snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(playerName);
                        setPlayerTurn(playerUniqueId);
                        status = "waiting";
                    }
                } else {
                    String connectionUniqueId = String.valueOf(System.currentTimeMillis());
                    snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(playerName);
                    setPlayerTurn(playerUniqueId);
                    status = "waiting";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        turnsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if (dataSnapshot.getChildrenCount()==2){
                        final int getBoxPosition = Integer.parseInt(dataSnapshot.child("box_position").getValue(String.class));
                        final String getPlayerId=dataSnapshot.child("player_id").getValue(String.class);
                        if (!doneBoxes.contains(String.valueOf(getBoxPosition))){
                                 doneBoxes.add(String.valueOf(getBoxPosition));
                                 if (getBoxPosition==1){
                                     selectBox(image1,getBoxPosition,getPlayerId);
                                 }else if(getBoxPosition==2){
                                     selectBox(image2,getBoxPosition,getPlayerId);

                                 }else if (getBoxPosition==3){
                                     selectBox(image3,getBoxPosition,getPlayerId);

                                 }else if (getBoxPosition==4){
                                     selectBox(image4,getBoxPosition,getPlayerId);

                                 }else if (getBoxPosition==5){
                                     selectBox(image5,getBoxPosition,getPlayerId);

                                 }else if (getBoxPosition==6){
                                     selectBox(image6,getBoxPosition,getPlayerId);

                                 }else if (getBoxPosition==7){
                                     selectBox(image7,getBoxPosition,getPlayerId);

                                 }else if (getBoxPosition==8){
                                     selectBox(image8,getBoxPosition,getPlayerId);

                                 }else if (getBoxPosition==9){
                                     selectBox(image9,getBoxPosition,getPlayerId);

                                 }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        wonEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("player_id")){
                    String getWinnerPlayerId=snapshot.child("player_id").getValue(String.class);
                    final WinDialog winDialog;
                    if (getWinnerPlayerId.equals(playerUniqueId)) {
                        winDialog = new WinDialog(MainActivity.this, "Siz yutdingiz");
                    }else {
                        winDialog=new WinDialog(MainActivity.this,"Siz yutqazdingiz");
                    }
                    winDialog.setCancelable(false);
                    winDialog.show();
                    databaseReference.child("won").child(connectionId).removeEventListener(wonEventListener);
                    databaseReference.child("turns").child(connectionId).removeEventListener(turnsEventListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, getPlayerTurn(), Toast.LENGTH_SHORT).show();
                if(!doneBoxes.contains("1")){
                    if(getPlayerTurn().equals(playerUniqueId)){
                        ((ImageView)view).setImageResource(R.drawable.cross_icon);
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("1");
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                        setPlayerTurn(opponentUniqueId);

                    }else {
                        Toast.makeText(MainActivity.this, "Hozir raqibingizni yurish navbati, iltimos kuting", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Bu joy band", Toast.LENGTH_SHORT).show();
                }
            }
        });
        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!doneBoxes.contains("2")){
                    if(getPlayerTurn().equals(playerUniqueId)){
                        ((ImageView)view).setImageResource(R.drawable.cross_icon);
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("2");
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                        setPlayerTurn(opponentUniqueId);

                    }else {
                        Toast.makeText(MainActivity.this, "Hozir raqibingizni yurish navbati, iltimos kuting", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Bu joy band", Toast.LENGTH_SHORT).show();
                }
            }
        });
        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!doneBoxes.contains("3")){
                    if(getPlayerTurn().equals(playerUniqueId)){
                        ((ImageView)view).setImageResource(R.drawable.cross_icon);
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("3");
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                        setPlayerTurn(opponentUniqueId);

                    }else {
                        Toast.makeText(MainActivity.this, "Hozir raqibingizni yurish navbati, iltimos kuting", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Bu joy band", Toast.LENGTH_SHORT).show();
                }
            }
        });
        image4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!doneBoxes.contains("4")){
                    if(getPlayerTurn().equals(playerUniqueId)){
                        ((ImageView)view).setImageResource(R.drawable.cross_icon);
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("4");
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                        setPlayerTurn(opponentUniqueId);

                    }else {
                        Toast.makeText(MainActivity.this, "Hozir raqibingizni yurish navbati, iltimos kuting", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Bu joy band", Toast.LENGTH_SHORT).show();
                }
            }
        });
        image5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!doneBoxes.contains("5")){
                    if(getPlayerTurn().equals(playerUniqueId)){
                        ((ImageView)view).setImageResource(R.drawable.cross_icon);
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("5");
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                        setPlayerTurn(opponentUniqueId);

                    }else {
                        Toast.makeText(MainActivity.this, "Hozir raqibingizni yurish navbati, iltimos kuting", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Bu joy band", Toast.LENGTH_SHORT).show();
                }
            }
        });
        image6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!doneBoxes.contains("6")){
                    if(getPlayerTurn().equals(playerUniqueId)){
                        ((ImageView)view).setImageResource(R.drawable.cross_icon);
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("6");
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                        setPlayerTurn(opponentUniqueId);

                    }else {
                        Toast.makeText(MainActivity.this, "Hozir raqibingizni yurish navbati, iltimos kuting", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Bu joy band", Toast.LENGTH_SHORT).show();
                }
            }
        });
        image7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!doneBoxes.contains("7")){
                    if(getPlayerTurn().equals(playerUniqueId)){
                        ((ImageView)view).setImageResource(R.drawable.cross_icon);
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("7");
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                        setPlayerTurn(opponentUniqueId);

                    }else {
                        Toast.makeText(MainActivity.this, "Hozir raqibingizni yurish navbati, iltimos kuting", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Bu joy band", Toast.LENGTH_SHORT).show();
                }
            }
        });
        image8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!doneBoxes.contains("8")){
                    if(getPlayerTurn().equals(playerUniqueId)){
                        ((ImageView)view).setImageResource(R.drawable.cross_icon);
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("8");
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                        setPlayerTurn(opponentUniqueId);

                    }else {
                        Toast.makeText(MainActivity.this, "Hozir raqibingizni yurish navbati, iltimos kuting", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Bu joy band", Toast.LENGTH_SHORT).show();
                }
            }
        });
        image9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!doneBoxes.contains("9")){
                    if(getPlayerTurn().equals(playerUniqueId)){
                        ((ImageView)view).setImageResource(R.drawable.cross_icon);
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("box_position").setValue("9");
                        databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                        setPlayerTurn(opponentUniqueId);

                    }else {
                        Toast.makeText(MainActivity.this, "Hozir raqibingizni yurish navbati, iltimos kuting", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Bu joy band", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void applyPlayerTurn(String playerUniqueId2) {
        if (playerUniqueId2.equals(playerUniqueId)) {
            player1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            player2Layout.setBackgroundResource(R.drawable.round_dark_blue_20);

        } else {
            player2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            player1Layout.setBackgroundResource(R.drawable.round_dark_blue_20);
        }

    }
    private void selectBox(ImageView imageView, int selectedBoxPosition,String selectedByPlayer){
        boxesSelectedBy[selectedBoxPosition-1]=selectedByPlayer;
        if (selectedByPlayer.equals(playerUniqueId)){
            imageView.setImageResource(R.drawable.cross_icon);
            setPlayerTurn(opponentUniqueId);
        }else {
            imageView.setImageResource(R.drawable.zero_icon);
        }
        applyPlayerTurn(getPlayerTurn());
        if (checkPlayerWin(selectedByPlayer)){
            databaseReference.child("won").child(connectionId).child("player_id").setValue(selectedByPlayer);
        }else if(doneBoxes.size()==9){
            final WinDialog winDialog= new WinDialog(MainActivity.this,"Durrang !");
            winDialog.setCancelable(false);
            winDialog.show();

        }
    }
    private boolean checkPlayerWin(String playerId){

        for (int i=0;i<combinationsList.size();i++){
            final int[]combination=combinationsList.get(i);
            if (boxesSelectedBy[combination[0]].equals(playerId) && boxesSelectedBy[combination[1]].equals(playerId) && boxesSelectedBy[combination[2]].equals(playerId) ){
                return true;
            }
        }
        return false;
    }
    private void setOpponentFound(boolean opponentFound) {
        databaseReference.child("connections").child(connectionId).child("opponent_found").setValue(opponentFound);
    }
    private boolean getOpponentFound() {
        return databaseReference.child("connections").child(connectionId).child("opponent_found").getRef().equals("true");
    }
    private String getPlayerTurn() {
        return databaseReference.child("connections").child(connectionId).child("player_turn").getKey();
    }

    private void setPlayerTurn(String playerTurn) {
        databaseReference.child("connections").child(connectionId).child("player_turn").setValue(playerTurn);
    }

}