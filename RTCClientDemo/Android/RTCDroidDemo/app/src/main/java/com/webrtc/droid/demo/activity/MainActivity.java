package com.webrtc.droid.demo.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.webrtc.droid.demo.R;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText serverEditText = findViewById(R.id.ServerEditText);
        final EditText roomEditText = findViewById(R.id.RoomEditText);
        findViewById(R.id.JoinRoomBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addr = serverEditText.getText().toString();
                String roomName = roomEditText.getText().toString();
                if (!"".equals(roomName)) {
                    Intent intent = new Intent(MainActivity.this, CallActivity.class);
                    intent.putExtra("ServerAddr", addr);
                    intent.putExtra("RoomName", roomName);
                    startActivity(intent);
                }
            }
        });

        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "Need permissions for camera & microphone", 0, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    protected void onDestroy() {
        super.onDestroy();
    }
}
