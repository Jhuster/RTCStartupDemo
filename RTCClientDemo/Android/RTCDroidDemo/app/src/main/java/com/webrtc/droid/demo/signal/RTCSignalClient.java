package com.webrtc.droid.demo.signal;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class RTCSignalClient {

    private static final String TAG = "RTCSignalClient";

    public static final int MESSAGE_TYPE_OFFER = 0x01;
    public static final int MESSAGE_TYPE_ANSWER = 0x02;
    public static final int MESSAGE_TYPE_CANDIDATE = 0x03;
    public static final int MESSAGE_TYPE_HANGUP = 0x04;

    private static RTCSignalClient mInstance;
    private OnSignalEventListener mOnSignalEventListener;

    private Socket mSocket;
    private String mUserId;
    private String mRoomName;

    public interface OnSignalEventListener {
        void onConnected();
        void onConnecting();
        void onDisconnected();
        void onRemoteUserJoined(String userId);
        void onRemoteUserLeaved(String userId);
        void onBroadcastReceived(JSONObject message);
    }

    public static RTCSignalClient getInstance() {
        synchronized (RTCSignalClient.class) {
            if (mInstance == null) {
                mInstance = new RTCSignalClient();
            }
        }
        return mInstance;
    }

    public void setSignalEventListener(final OnSignalEventListener listener) {
        mOnSignalEventListener = listener;
    }

    public String getUserId() {
        return mUserId;
    }

    public void joinRoom(String url, String roomName) {
        Log.i(TAG, "joinRoom: " + url + ", " + roomName);
        try {
            mSocket = IO.socket(url);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mRoomName = roomName;
        listenSignalEvents();
        mSocket.emit("join-room", roomName);
    }

    public void leaveRoom() {
        Log.i(TAG, "leaveRoom: " + mRoomName);
        if (mSocket == null) {
            return;
        }
        mSocket.emit("leave-room", mRoomName);
        mSocket.close();
        mSocket = null;
    }

    public void sendMessage(JSONObject message) {
        Log.i(TAG, "broadcast: " + message);
        if (mSocket == null) {
            return;
        }
        mSocket.emit("broadcast", message);
    }

    private void listenSignalEvents() {
        if (mSocket == null) {
            return;
        }
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "onConnectError: " + args);
            }
        });
        mSocket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "onError: " + args);
            }
        });
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String sessionId = mSocket.id();
                mUserId = sessionId.substring(sessionId.indexOf("#")+1);
                Log.i(TAG, "onConnected, Local userId = " + mUserId);
                if (mOnSignalEventListener != null) {
                    mOnSignalEventListener.onConnected();
                }
            }
        });
        mSocket.on(Socket.EVENT_CONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "onConnecting");
                if (mOnSignalEventListener != null) {
                    mOnSignalEventListener.onConnecting();
                }
            }
        });
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "onDisconnected");
                if (mOnSignalEventListener != null) {
                    mOnSignalEventListener.onDisconnected();
                }
            }
        });
        mSocket.on("user-joined", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String userId = (String) args[0];
                if (!mUserId.equals(userId) && mOnSignalEventListener != null) {
                    mOnSignalEventListener.onRemoteUserJoined(userId);
                }
                Log.i(TAG, "onRemoteUserJoined: " + userId);
            }
        });
        mSocket.on("user-leaved", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String userId = (String) args[0];
                if (!mUserId.equals(userId) && mOnSignalEventListener != null) {
                    mOnSignalEventListener.onRemoteUserLeaved(userId);
                }
                Log.i(TAG, "onRemoteUserLeaved: " + userId);
            }
        });
        mSocket.on("broadcast", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject msg = (JSONObject) args[0];
                try {
                    String userId = msg.getString("userId");
                    if (!mUserId.equals(userId) && mOnSignalEventListener != null) {
                        mOnSignalEventListener.onBroadcastReceived(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
