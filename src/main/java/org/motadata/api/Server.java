package org.motadata.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.motadata.constants.Constants;
import org.motadata.services.CacheService;
import org.motadata.services.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Server extends AbstractVerticle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final int PORT;
    private final String ORIGIN;

    public Server(int PORT, String ORIGIN)
    {
        this.PORT = PORT;

        this.ORIGIN = ORIGIN;
    }

    @Override
    public void start(Promise<Void> promise)
    {
        var server = vertx.createHttpServer();

        var router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.route().handler(context ->
        {
            var cacheKey = context.request().uri();

            LOGGER.debug("Cache key {}", cacheKey);

            if (CacheService.isCacheHit(cacheKey))
            {
                LOGGER.info("Cache HIT for request: {}", cacheKey);

                context.response().putHeader(Constants.X_CACHE, Constants.HIT).end(Objects.requireNonNull(CacheService.getCache(cacheKey)).encodePrettily());
            }

            else
            {
                LOGGER.info("Cache MISS for request: {}", cacheKey);

                ProxyHandler.handleRequest(context, ORIGIN, cacheKey);
            }
        });

        server.requestHandler(router).listen(PORT, response ->
        {
            if (response.succeeded())
            {
                LOGGER.info("Server started on port {}", PORT);

                promise.complete();
            }

            else
            {
                LOGGER.error("Failed to start server", response.cause());

                promise.fail(response.cause());
            }
        });
    }
}
