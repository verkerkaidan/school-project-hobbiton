CC=gcc
CFLAGS=-Wall -Wextra -O2
LIBS=-lmicrohttpd

TARGET=microservice

all:
	$(CC) $(CFLAGS) src/main.c -o $(TARGET) $(LIBS)

clean:
	rm -f $(TARGET)
