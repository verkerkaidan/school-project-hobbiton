#include "request_queue.h"
#include <stdlib.h>
#include <pthread.h>

#define MAX_QUEUE_SIZE 1024

typedef struct request_job {
    int client_fd;
    struct request_job *next;
} request_job_t;

static request_job_t *head = NULL;
static request_job_t *tail = NULL;

static int queue_size = 0;

static pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t not_empty = PTHREAD_COND_INITIALIZER;
static pthread_cond_t not_full = PTHREAD_COND_INITIALIZER;

void queue_init(void) {
    head = tail = NULL;
    queue_size = 0;
}

void queue_push(int client_fd) {
    request_job_t *job = malloc(sizeof(*job));
    job->client_fd = client_fd;
    job->next = NULL;

    pthread_mutex_lock(&lock);

    while (queue_size >= MAX_QUEUE_SIZE) {
        pthread_cond_wait(&not_full, &lock);
    }

    if (tail) {
        tail->next = job;
    } else {
        head = job;
    }

    tail = job;
    queue_size++;

    pthread_cond_signal(&not_empty);
    pthread_mutex_unlock(&lock);
}

int queue_pop(void) {
    pthread_mutex_lock(&lock);

    while (queue_size == 0) {
        pthread_cond_wait(&not_empty, &lock);
    }

    request_job_t *job = head;
    head = job->next;

    if (!head)
        tail = NULL;

    queue_size--;

    pthread_cond_signal(&not_full);
    pthread_mutex_unlock(&lock);

    int fd = job->client_fd;
    free(job);
    return fd;
}