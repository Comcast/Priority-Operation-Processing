package com.theplatform.dfh.modules.http.api;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface HttpURLConnectionFactory
{
    /**
     * Generates a new HttpURLConnection for the indicated url
     * @param url The url to get the connection for
     * @return The resulting HttpURLConnection
     * @throws IOException If there is an issue creating the HttpURLConnection
     */
    HttpURLConnection getHttpURLConnection(String url) throws IOException;

    /**
     * Generates a new HttpURLConnection for the indicated url (with consideration of the post data)
     * @param url The url to get the connection for
     * @param contentType The content type to indicate in the header
     * @param postData The data to be posted (for considering in header generation)
     * @return The resulting HttpURLConnection
     * @throws IOException If there is an issue creating the HttpURLConnection
     */
    HttpURLConnection getHttpURLConnection(String url, String contentType, byte[] postData) throws IOException;
}
