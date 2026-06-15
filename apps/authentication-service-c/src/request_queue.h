#pragma once

void queue_init(void);
void queue_push(int client_fd);
int queue_pop(void);