package org.motadata.Utils;

import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.motadata.Main;

import static org.motadata.services.ProxyHandler.*;

public class WebClientUtils
{
    private static WebClient CLIENT;

    public static WebClient getCLIENT()
    {
        return CLIENT;
    }

    public static void initialize()
    {
        if (CLIENT == null)
        {
            CLIENT = WebClient.create(Main.VERTX, new WebClientOptions()
                    .setDefaultHost(Main.CONFIG.getString(DEFAULT_HOST))
                    .setDefaultPort(Main.CONFIG.getInteger(DEFAULT_WEB_CLIENT_PORT))
                    .setConnectTimeout(Main.CONFIG.getInteger(WEBCLIENT_CONNECTION_TIMEOUT)));
        }

    }


}