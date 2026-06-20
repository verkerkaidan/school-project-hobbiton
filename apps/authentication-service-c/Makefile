CC = gcc
CFLAGS = -Wall -Wextra -O2 -pthread -I./src
OUT = auth-service-c
LIBS = -lpq -pthread

# Detect libpq availability
HAVE_LIBPQ := $(shell $(CC) -E -I./src src/db_core.c -o /dev/null 2>/dev/null && echo 1 || echo 0)

all: $(OUT)

# Explicit compile rules (no generic %.o pattern to avoid failing on db_core)
src/crypto_core.o: src/crypto_core.c
	$(CC) $(CFLAGS) -c $< -o $@

src/request_queue.o: src/request_queue.c
	$(CC) $(CFLAGS) -c $< -o $@

src/server.o: src/server.c src/crypto_core.o
	$(CC) $(CFLAGS) -c $< -o $@

ifeq ($(HAVE_LIBPQ),1)
src/db_core.o: src/db_core.c
	$(CC) $(CFLAGS) -c $< -o $@

$(OUT): src/server.o src/crypto_core.o src/db_core.o
	$(CC) $(CFLAGS) -o $(OUT) $^ $(LIBS)
else
$(OUT):
	@echo "WARNING: libpq not found. Skipping server build."
	@echo "Install libpq (e.g. 'brew install libpq' or 'apt install libpq-dev') to build the server."
endif

# Tests
TEST_JWT = tests/test_jwt
TEST_CRYPTO = tests/test_crypto
TEST_QUEUE = tests/test_request_queue

tests: $(TEST_JWT) $(TEST_CRYPTO) $(TEST_QUEUE)
ifeq ($(HAVE_LIBPQ),1)
tests: tests/test_db
endif

test: tests
	@echo "--- Running test_crypto (JWT + validators) ---"
	@$(TEST_CRYPTO) && echo "PASS" || echo "FAIL"
	@echo "--- Running test_jwt (basic JWT) ---"
	@$(TEST_JWT) && echo "PASS" || echo "FAIL"
	@echo "--- Running test_request_queue ---"
	@$(TEST_QUEUE) && echo "PASS" || echo "FAIL"
ifeq ($(HAVE_LIBPQ),1)
	@echo "--- Running test_db (requires Postgres) ---"
	@tests/test_db && echo "PASS" || echo "FAIL (set SPRING_DATASOURCE_URL etc.)"
endif

$(TEST_JWT): src/crypto_core.o tests/test_jwt.c
	$(CC) $(CFLAGS) -o $@ tests/test_jwt.c src/crypto_core.o

$(TEST_CRYPTO): src/crypto_core.o tests/test_crypto.c
	$(CC) $(CFLAGS) -o $@ tests/test_crypto.c src/crypto_core.o

$(TEST_QUEUE): src/request_queue.o tests/test_request_queue.c
	$(CC) $(CFLAGS) -o $@ tests/test_request_queue.c src/request_queue.o

ifeq ($(HAVE_LIBPQ),1)
tests/test_db: src/crypto_core.o src/db_core.o tests/test_db.c
	$(CC) $(CFLAGS) -o $@ tests/test_db.c src/crypto_core.o src/db_core.o -lpq
endif

# Valgrind — rebuilds tests with debug symbols and runs under memcheck
VALGRIND ?= valgrind
VALGRIND_FLAGS = --leak-check=full --show-leak-kinds=all --track-origins=yes --error-exitcode=1 --quiet --suppressions=tests/valgrind.supp
VGCFLAGS = -Wall -Wextra -g -O0 -pthread -I./src

valgrind: valgrind_test_jwt valgrind_test_db

valgrind_test_jwt: tests/test_jwt.c src/crypto_core.c
	$(CC) $(VGCFLAGS) -o tests/test_jwt_valgrind tests/test_jwt.c src/crypto_core.c
	$(VALGRIND) $(VALGRIND_FLAGS) ./tests/test_jwt_valgrind
	@echo "valgrind test_jwt: OK"

valgrind_test_db: tests/test_db.c src/crypto_core.c src/db_core.c
	$(CC) $(VGCFLAGS) -o tests/test_db_valgrind tests/test_db.c src/crypto_core.c src/db_core.c -lpq
	$(VALGRIND) $(VALGRIND_FLAGS) ./tests/test_db_valgrind
	@echo "valgrind test_db: OK"

clean:
	rm -f src/*.o $(OUT) tests/test_jwt tests/test_db tests/test_crypto tests/test_request_queue tests/test_jwt_valgrind tests/test_db_valgrind

.PHONY: all tests clean valgrind valgrind_test_jwt valgrind_test_db
