package router

import (
    "fmt"
    "net/http"
    "github.com/googollee/go-socket.io"
)

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
        fmt.Printf("error: %s\n", err)
    }
    server.On("connection", func(so socketio.Socket) {
        fmt.Printf("new connection, id: %s\n", so.Id())
        so.On("join-room", func(room string) {
            fmt.Printf("join-room, id: %s, room: %s\n", so.Id(), room)
            so.Join(room)
            broadcastTo(server, so.Rooms(), "user-joined", so.Id())
        })
        so.On("leave-room", func(room string) {
            fmt.Printf("leave-room, id: %s, room: %s\n", so.Id(), room)
            so.Leave(room)
            broadcastTo(server, so.Rooms(), "user-leaved", so.Id())
        })
        so.On("broadcast", func(msg interface{}) {
            fmt.Printf("broadcast, id: %s \n", so.Id())
            broadcastTo(server, so.Rooms(), "broadcast", msg)
        })
        so.On("disconnection", func() {
            fmt.Printf("disconnection, id: %s \n", so.Id())
        })
    })
    server.On("error", func(so socketio.Socket, err error) {
        fmt.Printf("error: %s\n", err)
    })

    http.Handle("/socket.io/", server)
    fmt.Printf("Handle /socket.io\n")
    return nil
}

func Run(port int) {
    fmt.Printf("Listen Port: %d \n", port)
    http.ListenAndServe(fmt.Sprintf(":%d", port), nil)
}

func broadcastTo(server *Server, rooms []string, event string, msg interface{}) {
    fmt.Printf("broadcastTo: \n\n%s\n\n", msg)
    if len(rooms) == 0 {
        fmt.Printf("error: not join room !\n")
        return
    }
    for _, room := range rooms {
        server.BroadcastTo(room, event, msg)
    }
}
