package org.motadata.api;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.motadata.Main;
import org.motadata.constants.Constants;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class ServerTest
{
    public static final int PORT = 8000;
    public static final String URL = " http://dummyjson.com";
    private WebClient webClient;

    @BeforeEach
    void setUp(VertxTestContext testContext)
    {
        ConfigRetriever.create(Main.VERTX, new ConfigRetrieverOptions()
                .addStore(new ConfigStoreOptions()
                        .setType(Constants.FILE)
                        .setFormat(Constants.JSON)
                        .setConfig(new JsonObject().put(Constants.PATH, "src/main/resources/config.json"))
                )).getConfig(result ->
        {
            if (result.failed())
            {
                testContext.failNow(result.cause());
            }
            else
            {
                Main.CONFIG = result.result();

                System.out.println("Configuration loaded: " + Main.CONFIG);

                // Initialize WebClient after configuration is loaded
                webClient = WebClient.create(Main.VERTX, new WebClientOptions().setDefaultPort(80).setConnectTimeout(60000).setDefaultHost("localhost"));

                // Deploy server after configuration is loaded
                Main.VERTX.deployVerticle(new Server(PORT, URL), res ->
                {
                    if (res.succeeded())
                    {
                        System.out.println("Server deployed successfully with ID: " + res.result());

                        testContext.completeNow();
                    }
                    else
                    {
                        System.err.println("Failed to deploy server: " + res.cause());

                        testContext.failNow(res.cause());
                    }
                });
            }
        });
    }


    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void testServerStart(VertxTestContext testContext)
    {
        System.out.println("Inside testServerStart function ...");
        // Perform GET request
        webClient.get(PORT, "localhost", "/products/2")
                .send(asyncResult ->
                {
                    if (asyncResult.succeeded())
                    {
                        System.out.println("Got result " + asyncResult.result());

                        // Assert that the status code is 200
                        assertEquals(Constants.SC_200, asyncResult.result().statusCode());

                        // Additional assertions can be added to check response body if needed
                        testContext.completeNow();
                    }
                    else
                    {
                        System.out.println("Failed response " + asyncResult.cause());

                        // Fail the test if the request fails
                        testContext.failNow(asyncResult.cause());
                    }
                });
    }
}
