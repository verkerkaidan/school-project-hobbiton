#include <microhttpd.h>
#include <string.h>
#include <stdio.h>

#define PORT 21527

static enum MHD_Result request_handler(
    void *cls,
    struct MHD_Connection *connection,
    const char *url,
    const char *method,
    const char *version,
    const char *upload_data,
    size_t *upload_data_size,
    void **con_cls)
{
    (void)cls;
    (void)version;
    (void)upload_data;
    (void)upload_data_size;
    (void)con_cls;

    if (strcmp(method, "GET") != 0)
        return MHD_NO;

    const char *response_text;

    if (strcmp(url, "/") == 0) {
        response_text = "{\"message\":\"Hello World\"}";
    } else {
        response_text = "{\"error\":\"Not Found\"}";
    }

    struct MHD_Response *response =
        MHD_create_response_from_buffer(
            strlen(response_text),
            (void *)response_text,
            MHD_RESPMEM_PERSISTENT);

    MHD_add_response_header(
        response,
        "Content-Type",
        "application/json");

    int status =
        (strcmp(url, "/") == 0)
            ? MHD_HTTP_OK
            : MHD_HTTP_NOT_FOUND;

    enum MHD_Result ret =
        MHD_queue_response(connection, status, response);

    MHD_destroy_response(response);

    return ret;
}

int main(void)
{
    struct MHD_Daemon *daemon;

    daemon = MHD_start_daemon(
        MHD_USE_INTERNAL_POLLING_THREAD,
        PORT,
        NULL,
        NULL,
        &request_handler,
        NULL,
        MHD_OPTION_END);

    if (daemon == NULL)
        return 1;

    printf("Server listening on port %d\n", PORT);

    while (1)
    {
        sleep(60);
    }

    MHD_stop_daemon(daemon);

    return 0;
}
