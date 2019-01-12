package config

var VERSION = "0.0.1"

type Config struct {
	ListenPort int    `json:"port"`
}