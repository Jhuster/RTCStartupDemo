package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io/ioutil"

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

	router.AddMonitorService()
	router.AddSignalService()
	router.Run(cfg.ListenPort)
}
