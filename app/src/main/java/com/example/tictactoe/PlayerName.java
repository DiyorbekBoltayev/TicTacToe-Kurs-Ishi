package com.example.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name);
        final EditText playerNameEt = findViewById(R.id.playerNameEt);
        final AppCompatButton startGameBtn = findViewById(R.id.startGameBtn);
        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playerName = playerNameEt.getText().toString();
                if (playerName.isEmpty()) {
                    Toast.makeText(PlayerName.this, "Iltimos ismingizni kiriting", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(PlayerName.this, MainActivity.class);
                    intent.putExtra("playerName", playerName);
                    startActivity(intent);
                    finish();
                }


            }
        });
    }
}