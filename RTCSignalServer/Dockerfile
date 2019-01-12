FROM alpine:3.4

MAINTAINER jhuster <lujun.hust@gmail.com>

WORKDIR /deploy

RUN apk add --no-cache ca-certificates

COPY bin/linux/server.conf /deploy/
COPY bin/linux/app /deploy/

CMD ["./app", "server.conf"]