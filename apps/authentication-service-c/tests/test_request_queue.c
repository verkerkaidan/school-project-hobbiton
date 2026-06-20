#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>
#include "../src/request_queue.h"

static volatile int tests_run = 0;
static volatile int tests_passed = 0;
static volatile int tests_failed = 0;
static pthread_mutex_t test_lock = PTHREAD_MUTEX_INITIALIZER;

#define ASSERT(cond, msg) do { \
    __sync_add_and_fetch(&tests_run, 1); \
    if (!(cond)) { \
        pthread_mutex_lock(&test_lock); \
        fprintf(stderr, "FAIL: %s\n", msg); \
        pthread_mutex_unlock(&test_lock); \
        __sync_add_and_fetch(&tests_failed, 1); \
    } else { \
        __sync_add_and_fetch(&tests_passed, 1); \
    } \
} while(0)

static void test_single_push_pop(void) {
    queue_init();
    queue_push(42);
    int val = queue_pop();
    ASSERT(val == 42, "popped value should match pushed value");
}

static void test_fifo_order(void) {
    queue_init();
    queue_push(1);
    queue_push(2);
    queue_push(3);

    int v1 = queue_pop();
    int v2 = queue_pop();
    int v3 = queue_pop();

    ASSERT(v1 == 1, "first pop should be 1");
    ASSERT(v2 == 2, "second pop should be 2");
    ASSERT(v3 == 3, "third pop should be 3");
}

static void test_push_pop_alternating(void) {
    queue_init();
    queue_push(10);
    int a = queue_pop();
    ASSERT(a == 10, "first alternating pop");
    queue_push(20);
    queue_push(30);
    int b = queue_pop();
    int c = queue_pop();
    ASSERT(b == 20, "second alternating pop");
    ASSERT(c == 30, "third alternating pop");
}

static int produced_count = 0;
static int consumed_count = 0;
static pthread_mutex_t count_lock = PTHREAD_MUTEX_INITIALIZER;

static void *producer(void *arg) {
    int n = *(int *)arg;
    for (int i = 0; i < n; i++) {
        queue_push(i);
        pthread_mutex_lock(&count_lock);
        produced_count++;
        pthread_mutex_unlock(&count_lock);
    }
    return NULL;
}

static void *consumer(void *arg) {
    int n = *(int *)arg;
    for (int i = 0; i < n; i++) {
        int val = queue_pop();
        (void)val;
        pthread_mutex_lock(&count_lock);
        consumed_count++;
        pthread_mutex_unlock(&count_lock);
    }
    return NULL;
}

static void test_concurrent_push_pop(void) {
    queue_init();
    produced_count = 0;
    consumed_count = 0;

    int ops_per = 500;
    pthread_t p, c;
    pthread_create(&p, NULL, producer, &ops_per);
    pthread_create(&c, NULL, consumer, &ops_per);

    pthread_join(p, NULL);
    pthread_join(c, NULL);

    ASSERT(produced_count == ops_per, "all items produced");
    ASSERT(consumed_count == ops_per, "all items consumed");
}

int main(void) {
    printf("=== Request queue tests ===\n");

    test_single_push_pop();
    test_fifo_order();
    test_push_pop_alternating();
    test_concurrent_push_pop();

    printf("Results: %d/%d passed, %d failed\n",
           tests_passed, tests_run, tests_failed);

    return tests_failed > 0 ? 1 : 0;
}
