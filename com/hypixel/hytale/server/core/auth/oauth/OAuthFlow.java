package com.hypixel.hytale.server.core.auth.oauth;

import java.util.concurrent.CompletableFuture;

abstract class OAuthFlow {
   private OAuthClient.TokenResponse tokenResponse = null;
   private final CompletableFuture<OAuthResult> future = new CompletableFuture<>();
   private OAuthResult result = OAuthResult.UNKNOWN;
   private String errorMessage = null;

   final void onSuccess(OAuthClient.TokenResponse tokenResponse) {
      if (!this.future.isDone()) {
         this.tokenResponse = tokenResponse;
         this.result = OAuthResult.SUCCESS;
         this.future.complete(this.result);
      }
   }

   final void onFailure(String errorMessage) {
      if (!this.future.isDone()) {
         this.errorMessage = errorMessage;
         this.result = OAuthResult.FAILED;
      }
   }

   public OAuthClient.TokenResponse getTokenResponse() {
      return this.tokenResponse;
   }

   public OAuthResult getResult() {
      return this.result;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public CompletableFuture<OAuthResult> getFuture() {
      return this.future;
   }
}
