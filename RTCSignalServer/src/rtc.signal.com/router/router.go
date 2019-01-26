package router

import (
    "fmt"
    "net/http"
    "github.com/googollee/go-socket.io"
     "encoding/json"
)

type RoomArgs struct {
    UserId   string `json:"userId"`
    RoomName string `json:"roomName"`
}

type Server struct {
    *socketio.Server
}

func NewServer(transports []string) (*Server, error) {
    s, err := socketio.NewServer(transports)
    if err != nil {
        return nil, err
    }
    return &Server{s}, nil
}

func (s Server) ServeHTTP(w http.ResponseWriter, r *http.Request) {
    w.Header().Set("Access-Control-Allow-Credentials", "true")
    origin := r.Header.Get("Origin")
    w.Header().Set("Access-Control-Allow-Origin", origin)
    s.Server.ServeHTTP(w, r)
}

func AddMonitorService() (err error) {
    http.HandleFunc("/status", func(w http.ResponseWriter, r *http.Request) {
        fmt.Fprintf(w, "Status: OK")
    })
    fmt.Printf("Handle /status\n")
    return nil
}

func AddSignalService() (err error) {
    server, err := NewServer(nil)
    if err != nil {
        fmt.Println(err)
    }
    server.On("connection", func(so socketio.Socket) {
        fmt.Printf("new connection, connection id: %s\n", so.Id())
        so.On("join-room", func(args string) {
            var room RoomArgs
            err := json.Unmarshal([]byte(args), &room)
            if err != nil {
                fmt.Println(err)
                return
            }
            fmt.Printf("join-room, user: %s, room: %s\n", room.UserId, room.RoomName)
            so.Join(room.RoomName)
            broadcastTo(server, so.Rooms(), "user-joined", room.UserId)
        })
        so.On("leave-room", func(args string) {
            var room RoomArgs
            err := json.Unmarshal([]byte(args), &room)
            if err != nil {
                fmt.Println(err)
                return
            }
            fmt.Printf("leave-room, user: %s, room: %s\n", room.UserId, room.RoomName)
            broadcastTo(server, so.Rooms(), "user-left", room.UserId)
            so.Leave(room.RoomName)
        })
        so.On("broadcast", func(msg interface{}) {
            broadcastTo(server, so.Rooms(), "broadcast", msg)
        })
        so.On("disconnection", func() {
            fmt.Printf("disconnection, connection id: %s \n", so.Id())
        })
    })
    server.On("error", func(so socketio.Socket, err error) {
        fmt.Println(err)
    })

    http.Handle("/socket.io/", server)
    fmt.Printf("Handle /socket.io\n")
    return nil
}

func Run(port int) {
    http.ListenAndServe(fmt.Sprintf(":%d", port), nil)
}

func broadcastTo(server *Server, rooms []string, event string, msg interface{}) {
    fmt.Printf("broadcastTo: \n\n%s\n\n", msg)
    if len(rooms) == 0 {
        fmt.Printf("broadcastTo error: not join room !\n")
        return
    }
    for _, room := range rooms {
        server.BroadcastTo(room, event, msg)
    }
}
