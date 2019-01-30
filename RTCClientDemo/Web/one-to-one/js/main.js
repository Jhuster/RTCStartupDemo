'use strict';

const MESSAGE_TYPE_OFFER = 0x01;
const MESSAGE_TYPE_ANSWER = 0x02;
const MESSAGE_TYPE_CANDIDATE = 0x03;
const MESSAGE_TYPE_HANGUP = 0x04;

var localUserId = Math.random().toString(36).substr(2); // store local userId
var localStream; // local video stream object
var pc = null; // webrtc RTCPeerConnection

/////////////////////////////////////////////

var room = prompt('Enter room name:');

// var socket = io('http://localhost:8080/socket.io');
var socket = io('http://rtc-signal.jhuster.com:8080/socket.io');

if (room !== '') {
    console.log('Attempted to join room:', localUserId, room);
    var args = {
        'userId': localUserId,
        'roomName': room
    };
    socket.emit('join-room', JSON.stringify(args));
}

socket.on('connect', function() {
    console.log("Signal server connected !");
});

socket.on('user-joined', function(userId) {
    if (localUserId == userId) {
        return;
    }  
    console.log('Peer joined room: ', userId);
});

socket.on('user-left', function(userId) {
    if (localUserId == userId) {
        return;
    }
    console.log('Peer left room: ', userId); 
});

socket.on('broadcast', function(msg) {
    console.log('Broadcast Received: ', msg); 
    if (localUserId == msg.userId) {
        return;
    }
    console.log('Broadcast Received: ', msg.userId); 
    switch (msg.msgType) {
        case MESSAGE_TYPE_OFFER:
            handleRemoteOffer(msg);
            break;
        case MESSAGE_TYPE_ANSWER:
            handleRemoteAnswer(msg);
            break;
        case MESSAGE_TYPE_CANDIDATE:
            handleRemoteCandidate(msg);
            break;
        case MESSAGE_TYPE_HANGUP:
            handleRemoteHangup();
            break;
        default:
            break;
    }
});

function handleRemoteOffer(msg) {
    console.log('Remote offer received: ', msg.sdp);
    if (pc == null) {
        createPeerConnection()
    }
    var sdp = new RTCSessionDescription({
        'type': 'offer',
        'sdp': msg.sdp
    });
    pc.setRemoteDescription(sdp);
    doAnswer();
}

function handleRemoteAnswer(msg) {
    console.log('Remote answer received: ', msg.sdp);
    var sdp = new RTCSessionDescription({
        'type': 'answer',
        'sdp': msg.sdp
    });
    pc.setRemoteDescription(sdp);
}

function handleRemoteCandidate(msg) {
    console.log('Remote candidate received: ', msg.candidate);
    var candidate = new RTCIceCandidate({
        sdpMLineIndex: msg.label,
        candidate: msg.candidate
    });
    pc.addIceCandidate(candidate);    
}

function handleRemoteHangup() {
    console.log('Remote hangup received');
    hangup();
}

////////////////////////////////////////////////////

var localVideo = document.querySelector('#localVideo');
var remoteVideo = document.querySelector('#remoteVideo');

navigator.mediaDevices.getUserMedia({
    audio: false,
    video: true
})
.then(openLocalStream)
.catch(function(e) {
    alert('getUserMedia() error: ' + e.name);
});

function openLocalStream(stream) {
    console.log('Open local video stream');    
    localVideo.srcObject = stream;
    localStream = stream;
}

function createPeerConnection() {
    try {
        pc = new RTCPeerConnection(null);
        pc.onicecandidate = handleIceCandidate;
        pc.onaddstream = handleRemoteStreamAdded;
        pc.onremovestream = handleRemoteStreamRemoved;
        pc.addStream(localStream);
        console.log('RTCPeerConnnection Created');
    } catch (e) {
        console.log('Failed to create PeerConnection, exception: ' + e.message);
        alert('Cannot create RTCPeerConnection object.');
        return;
    }
}

/////////////////////////////////////////////////////////

function doCall() {
    console.log('Starting call: Sending offer to remote peer');
    if (pc == null) {
        createPeerConnection()
    }
    pc.createOffer(createOfferAndSendMessage, handleCreateOfferError);
}

function doAnswer() {
    console.log('Answer call: Sending answer to remote peer');
    if (pc == null) {
        createPeerConnection()
    }
    pc.createAnswer().then(createAnswerAndSendMessage, handleCreateAnswerError);
}

function createOfferAndSendMessage(sessionDescription) {
    console.log('CreateOfferAndSendMessage sending message', sessionDescription);
    pc.setLocalDescription(sessionDescription);
    var message = {
        'userId': localUserId,
        'msgType': MESSAGE_TYPE_OFFER,
        'sdp': sessionDescription.sdp
    };
    socket.emit('broadcast', message);
    console.log('Broadcast Offer:', message);
}

function createAnswerAndSendMessage(sessionDescription) {
    console.log('CreateAnswerAndSendMessage sending message', sessionDescription);
    pc.setLocalDescription(sessionDescription);
    var message = {
        'userId': localUserId,
        'msgType': MESSAGE_TYPE_ANSWER,
        'sdp': sessionDescription.sdp
    };
    socket.emit('broadcast', message);
    console.log('Broadcast Answer:', message);
}

function handleCreateOfferError(event) {
    console.log('CreateOffer() error: ', event);
}

function handleCreateAnswerError(error) {
    console.log('CreateAnswer() error: ', error);
}

function handleIceCandidate(event) {
    console.log('Handle ICE candidate event: ', event);
    if (event.candidate) {
        var message = {
            'userId': localUserId,
            'msgType': MESSAGE_TYPE_CANDIDATE,
            'id': event.candidate.sdpMid,
            'label': event.candidate.sdpMLineIndex,
            'candidate': event.candidate.candidate
        };
        socket.emit('broadcast', message);
        console.log('Broadcast Candidate:', message);
    } else {
        console.log('End of candidates.');
    }
}

function handleRemoteStreamAdded(event) {
    console.log('Handle remote stream added.');
    remoteVideo.srcObject = event.stream;
}

function handleRemoteStreamRemoved(event) {
    console.log('Handle remote stream removed. Event: ', event);
    remoteVideo.srcObject = null;
}

function hangup() {
    console.log('Hanging up !');
    remoteVideo.srcObject = null;
    if (pc != null) {
        pc.close();
        pc = null;
    }
}

/////////////////////////////////////////////////////////

document.getElementById('startCall').onclick = function() {
    console.log('Start call');
    doCall();
};

document.getElementById('endCall').onclick = function() {
    console.log('End call');
    hangup();
    var message = {
        'userId': localUserId,
        'msgType': MESSAGE_TYPE_HANGUP,
    };
    socket.emit('broadcast', message);
    console.log('Broadcast Hangup:', message);
};


