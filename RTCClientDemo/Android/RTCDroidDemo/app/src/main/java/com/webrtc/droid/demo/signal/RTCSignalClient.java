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
        void onRemoteUserLeft(String userId);
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

    public void joinRoom(String url, String userId, String roomName) {
        Log.i(TAG, "joinRoom: " + url + ", " + userId + ", " + roomName);
        try {
            mSocket = IO.socket(url);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mUserId = userId;
        mRoomName = roomName;
        listenSignalEvents();
        try {
            JSONObject args = new JSONObject();
            args.put("userId", userId);
            args.put("roomName", roomName);
            mSocket.emit("join-room", args.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void leaveRoom() {
        Log.i(TAG, "leaveRoom: " + mRoomName);
        if (mSocket == null) {
            return;
        }
        try {
            JSONObject args = new JSONObject();
            args.put("userId", mUserId);
            args.put("roomName", mRoomName);
            mSocket.emit("leave-room", args.toString());
            mSocket.close();
            mSocket = null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                Log.i(TAG, "onConnected");
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
        mSocket.on("user-left", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String userId = (String) args[0];
                if (!mUserId.equals(userId) && mOnSignalEventListener != null) {
                    mOnSignalEventListener.onRemoteUserLeft(userId);
                }
                Log.i(TAG, "onRemoteUserLeft: " + userId);
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
