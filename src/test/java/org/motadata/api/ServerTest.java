package org.motadata.api;

import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.motadata.Main;
import org.motadata.Utils.WebClientUtils;
import org.motadata.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class ServerTest
{
    public static final int PORT = 8000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerTest.class);

    @BeforeAll
    static void setUp()
    {
        // Explicitly call the main method
        try
        {
            Main.initialize(new String[]{"--port", "8000", "--origin", "http://dummyjson.com"}).onComplete(result ->
            {
                if (result.succeeded())
                {
                    LOGGER.info("Application setup completed successfully.");
                }
                else
                {
                    LOGGER.error("Application setup failed.", result.cause());
                }

                LOGGER.info("Main method executed successfully.");
            });
        }

        catch (Exception exception)
        {
            LOGGER.error("Error executing main method", exception);
        }

        LOGGER.info("Configuration loaded: {} ", Main.CONFIG);
    }

    @Test
    @Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
    void testServerStart(VertxTestContext testContext)
    {
       LOGGER.info("Inside testServerStart function ...");

        //      Check webclient before using it
        if (WebClientUtils.getCLIENT() == null)
        {
            LOGGER.error("WebClientUtils.CLIENT is not initialized");

            testContext.failNow(new IllegalStateException("WebClientUtils.CLIENT not initialized"));
        }

        // Perform GET request
        WebClientUtils.getCLIENT().get(PORT, "localhost", "/products/2")
                .send(asyncResult ->
                {
                    if (asyncResult.succeeded())
                    {
                        LOGGER.info("Got result {}", asyncResult.result());

                        // Assert that the status code is 200
                        assertEquals(Constants.SC_200, asyncResult.result().statusCode());

                        // Additional assertions can be added to check response body if needed
                        testContext.completeNow();
                    }
                    else
                    {
                        LOGGER.info("Failed response {}", asyncResult.result());

                        // Fail the test if the request fails
                        testContext.failNow(asyncResult.cause());
                    }
                });
    }
}
