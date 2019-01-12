.PHONY: all build clean install test

all:
	@echo GOPATH = $(GOPATH)
	@make mac
	@make linux

mac:
	@echo making for mac
	@CGO_ENABLED=0 GOOS=darwin GOARCH=amd64 go build -o bin/darwin/app ./src/rtc.signal.com/app/...
	@cp server.conf bin/darwin/server.conf

linux:
	@echo making for linux
	@CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o bin/linux/app ./src/rtc.signal.com/app/...
	@cp server.conf bin/linux/server.conf

test:
	@go test ./...

clean:
	rm -rf ./pkg ./bin