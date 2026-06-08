CC = gcc
CFLAGS = -Wall -Wextra -O2 -pthread -I./src

SRC = src/server.c src/crypto_core.c src/db_core.c
OBJ = $(SRC:.c=.o)
OUT = auth-service-c
LIBS = -lpq -pthread

all: $(OUT)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

$(OUT): $(OBJ)
	$(CC) $(CFLAGS) -o $(OUT) $(OBJ) $(LIBS)

# Tests
tests: test_jwt test_db

test_jwt: src/crypto_core.o tests/test_jwt.c
	$(CC) $(CFLAGS) -o tests/test_jwt tests/test_jwt.c src/crypto_core.o

test_db: src/crypto_core.o src/db_core.o tests/test_db.c
	$(CC) $(CFLAGS) -o tests/test_db tests/test_db.c src/crypto_core.o src/db_core.o -lpq

clean:
	rm -f src/*.o $(OUT) tests/test_jwt tests/test_db

.PHONY: all tests clean
