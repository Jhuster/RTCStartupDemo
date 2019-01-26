package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io/ioutil"
	"net"
	"rtc.signal.com/config"
	"rtc.signal.com/router"
)

func load(filename string) ([]byte, error) {
	f, err := ioutil.ReadFile(filename)
	if err != nil {
		return nil, err
	}
	return f, nil
}

func main() {
	flag.Parse()
	args := flag.Args()

	if len(args) < 1 {
		fmt.Println("no config file provided !")
		return
	}

	f := args[0]
	c, err := load(f)
	if err != nil {
		fmt.Println("failed to load config !", err)
		return
	}

	var cfg config.Config
	err = json.Unmarshal(c, &cfg)
	if err != nil {
		fmt.Println("can't find Port in config file !", err)
		return
	}

	addrs, err := net.InterfaceAddrs()
	if err != nil {
		fmt.Println(err)
		return
	}

	for _, address := range addrs {
		if ipnet, ok := address.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if ipnet.IP.To4() != nil {
				fmt.Printf("Listen on: %s:%d\n", ipnet.IP.String(), cfg.ListenPort)
			}
		}
	}

	router.AddMonitorService()
	router.AddSignalService()
	router.Run(cfg.ListenPort)
}
