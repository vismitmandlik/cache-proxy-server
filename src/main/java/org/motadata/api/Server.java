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
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final int PORT;

    private final String ORIGIN;

    @Override
    public void start(Promise<Void> Promise) throws Exception
    {
        super.start(Promise);

        var server = vertx.createHttpServer();

        var router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.route().handler(context ->
        {
            var cacheKey = context.request().uri();

            logger.info("Cache key {}", cacheKey);

            if (CacheService.isCacheHit(cacheKey))
            {
                logger.info("Cache HIT for request: {}", cacheKey);

                context.response().putHeader(Constants.X_CACHE, Constants.HIT).end(Objects.requireNonNull(CacheService.getCache(cacheKey)).encodePrettily());
            }

            else
            {
                logger.info("Cache MISS for request: {}", cacheKey);

                ProxyHandler.handleRequest(context, vertx, ORIGIN, cacheKey);
            }
        });

        server.requestHandler(router).listen(PORT, response ->
        {
            if (response.succeeded())
            {
                logger.info("Server started on port {}", PORT);
            }

            else
            {
                logger.error("Failed to start server", response.cause());
            }
        });
    }

    public Server(int PORT, String ORIGIN)
    {
        this.PORT = PORT;

        this.ORIGIN = ORIGIN;
    }
}
