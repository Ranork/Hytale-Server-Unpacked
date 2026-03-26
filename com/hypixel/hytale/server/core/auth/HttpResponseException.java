package com.hypixel.hytale.server.core.auth;

import java.io.IOException;

public class HttpResponseException extends IOException {
   private final int statusCode;
   private final String responseBody;

   public HttpResponseException(int statusCode, String responseBody) {
      super("HTTP " + statusCode + ": " + truncateBody(responseBody));
      this.statusCode = statusCode;
      this.responseBody = responseBody;
   }

   public int getStatusCode() {
      return this.statusCode;
   }

   public String getResponseBody() {
      return this.responseBody;
   }

   private static String truncateBody(String body) {
      return body != null && body.length() > 200 ? body.substring(0, 200) + "..." : body;
   }
}
