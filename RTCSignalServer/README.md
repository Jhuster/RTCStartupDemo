# RTCSignalServer

A simple signal server written in Golang based on  [socket.io](https://socket.io). Support simple room management and message exchange.

## 1. Build

```shell
$ cd RTCSignalServer
$ source env.sh 
$ make
```
## 2. Run

```shell
$ cd bin/{platform}
$ ./app server.conf
```

## 3. API Guide

All of the signal API are besed on [socket.io](https://socket.io). You should use a socket.io client library to connect & exchange message with the server: 

- [javascript api](https://socket.io/docs/client-api/)
- [Android/java](https://github.com/socketio/socket.io-client-java)
- [Swift](https://github.com/socketio/socket.io-client-swift)
- [C++](https://github.com/socketio/socket.io-client-cpp)

#### 3.1 Request（from client）: join room & leave room

```shell
event: 'join-room'
message: json string
"{\"userId\": <UserId>,\"roomName\": <RoomName>}\"

event: 'leave-room'
message: json string
"{\"userId\": <UserId>,\"roomName\": <RoomName>}\"
```

**Client Examples**：

```html
<script src="https://cdnjs.cloudflare.com/ajax/libs/socket.io/2.2.0/socket.io.js"></script>
<script>
    var socket = io('http://localhost:8080/socket.io');
    var args = {
        'userId': 'blob',
        'roomName': 'Room1'
    };
    socket.emit('join-room', JSON.stringify(args));
    socket.emit('leave-room', JSON.stringify(args));
</script>
```

#### 3.2 Broadcast（from server）: user joined & user leaved

```shell
event: 'user-joined'
message: string  // The userId who joined

event: 'user-left'
message: string  // The userId who left
```

**Client Examples**：

```html
<script src="https://cdnjs.cloudflare.com/ajax/libs/socket.io/2.2.0/socket.io.js"></script>
<script>
    var socket = io('http://localhost:8080/socket.io');
	socket.on('user-joined', function(userId) {
            console.log('Peer joined room: ' + userId);
	});
	socket.on('user-left', function(userId) {
            console.log('Peer leaved room: ' + userId);
    });
</script>
```

#### 3.3 Request（from client）:  message

```shell
event: 'broadcast'
message: json object
```

**Client Examples**：

```html
<script src="https://cdnjs.cloudflare.com/ajax/libs/socket.io/2.2.0/socket.io.js"></script>
<script>
    var socket = io('http://localhost:8080/socket.io');
    var message = {
        'userId': <userId>
        'msgType': <MsgType>, // OFFER: 0x01, ANSWER: 0x02, CANDIDATE: 0x03, HANGUP: 0x04
        'sdp': <sdp description>
    };
    socket.emit('broadcast', message);
</script>
```

#### 3.4  Broadcast（from server）：message

```shell
event: 'broadcast'
message: json object
```

**Client Examples**：

```html
<script src="https://cdnjs.cloudflare.com/ajax/libs/socket.io/2.2.0/socket.io.js"></script>
<script>
    var socket = io('http://localhost:8080/socket.io');
    socket.on('broadcast', function(msg) {
	console.log('Broadcast received, userId = ', msg.userId); 
    	switch (msg.msgType) {
            case 0x01:
                //handleRemoteOffer(msg);
                break;
    	    case 0x02:
                //handleRemoteAnswer(msg);
                break;
	        case 0x03:
                //handleRemoteCandidate(msg);
                break;
	        case 0x04:
                //handleRemoteHangup(msg);
                break;
	    default:
    	        break;
    	}
    });
</script>
```

### 4. Contact

Email：[lujun.hust@gmail.com](mailto:lujun.hust@gmail.com)


