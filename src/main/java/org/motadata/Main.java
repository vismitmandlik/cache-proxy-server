package org.motadata;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.motadata.Utils.WebClientUtils;
import org.motadata.api.Server;
import org.motadata.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static final Vertx VERTX = Vertx.vertx();

    private static final Object CONFIG_FILE_PATH = "src/main/resources/config.json";

    public static int PORT ;

    public static String ORIGIN = "";

    public static JsonObject CONFIG;

    public static void main(String[] args)
    {
        // Call the asynchronous startup method
        initialize(args).onComplete(result ->
        {
            if (result.succeeded())
            {
                LOGGER.info("Application started successfully.");
            }
            else
            {
                LOGGER.error("Application failed to start.", result.cause());
            }
        });
    }

    public static Future<Object> initialize(String[] args)
    {
        var startupPromise = Promise.promise();

        /* Set config.json path and load configuration from it */
        ConfigRetriever.create(VERTX, new io.vertx.config.ConfigRetrieverOptions()
                .addStore(new ConfigStoreOptions()
                        .setType(Constants.FILE)
                        .setFormat(Constants.JSON)
                        .setConfig(new io.vertx.core.json.JsonObject().put(Constants.PATH, CONFIG_FILE_PATH))
                )).getConfig(result ->
        {
            if (result.failed())
            {
                LOGGER.error("Failed to load configuration: ", result.cause());

                startupPromise.fail(result.cause());

                return;
            }

            CONFIG = result.result();

            LOGGER.info("Configuration loaded successfully: {}", CONFIG);

            if (args.length < 4)
            {
                LOGGER.error("Usage: java -jar caching-proxy.jar --port <port> --origin <url>");

                startupPromise.fail("Invalid arguments");

                return;
            }

            // Iterate through arguments and extract --port and --origin
            for (int i = 0; i < args.length; i++)
            {
                switch (args[i])
                {
                    case "--port":
                        if (i + 1 < args.length)
                        {
                            try
                            {
                                PORT = Integer.parseInt(args[i + 1]);
                            }

                            catch (Exception exception)
                            {
                                LOGGER.error("Invalid port number: {}", args[i + 1], exception);

                                startupPromise.fail(exception);

                                return;
                            }
                        }

                        break;

                    case "--origin":
                        if (i + 1 < args.length)
                        {
                            ORIGIN = args[i + 1];
                        }
                        break;

                    default:
                        break;
                }
            }

            if (ORIGIN.isEmpty())
            {
                LOGGER.error("Origin URL is required. Usage: --origin <url>");

                startupPromise.fail("Missing origin URL");

                return;
            }

            LOGGER.info("Configured origin: {}", ORIGIN);

            WebClientUtils.initialize();

            // Deploy the Server verticle
            VERTX.deployVerticle(new Server(PORT, ORIGIN), response ->
            {
                if (response.succeeded())
                {
                    LOGGER.info("Server verticle deployed successfully.");

                    startupPromise.complete();
                }

                else
                {
                    LOGGER.error("Failed to deploy server verticle.", response.cause());

                    startupPromise.fail(response.cause());
                }
            });
        });

        return startupPromise.future();

    }
}