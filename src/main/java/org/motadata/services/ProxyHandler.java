package org.motadata.services;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.RoutingContext;
import org.motadata.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyHandler.class);

    public static void handleRequest(RoutingContext context, Vertx vertx, String origin, String cacheKey)
    {
        LOGGER.info("Sending proxy request to {}", origin + context.request().uri());

        WebClient client = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("dummyjson.com")
                .setDefaultPort(80)
                .setConnectTimeout(10000));

        client.get(origin + context.request().uri()).send(result ->
                {
                    if (result.succeeded())
                    {
                        handleResponse(result.result(), context, cacheKey);
                    }
                    else
                    {
                        LOGGER.error("Error proxying request: {}", result.cause().getMessage());

                        context.response().setStatusCode(Constants.SC_500).end("Error proxying request");
                    }
                });
    }

    private static void handleResponse(HttpResponse<io.vertx.core.buffer.Buffer> response, RoutingContext context, String cacheKey)
    {
        LOGGER.debug("Received response with status code {} for cache key: {}", response.statusCode(), cacheKey);

        if (response.statusCode() == Constants.SC_200)
        {
            var responseJson = response.bodyAsJsonObject();

            // Cache the response
            CacheService.putCache(cacheKey, responseJson);

            LOGGER.info("Proxying request to origin and caching response for {}", cacheKey);

            // Respond to the client with the cached data
            context.response().putHeader(Constants.X_CACHE, "MISS").end(responseJson.encodePrettily());
        }
        else
        {
            LOGGER.warn("Received non-OK response from origin: {} - {}", response.statusCode(), response.statusMessage());

            context.response().setStatusCode(response.statusCode()).end("Error response from origin: " + response.statusMessage());
        }
    }
}