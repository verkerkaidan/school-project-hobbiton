#include "request_queue.h"
#include "auth_core.h"
#include <pthread.h>
#include <stdlib.h>

static pthread_t *threads;
static int running = 1;

static void *worker(void *arg) {
    (void)arg;

    while (running) {
        int client = queue_pop();

        if (client >= 0) {
            handle_client_fd(client);  // you refactor handle_client slightly
        }
    }

    return NULL;
}

void worker_pool_start(int n_threads) {
    threads = malloc(sizeof(pthread_t) * n_threads);

    for (int i = 0; i < n_threads; i++) {
        pthread_create(&threads[i], NULL, worker, NULL);
    }
}

void worker_pool_stop(void) {
    running = 0;
    free(threads);
}