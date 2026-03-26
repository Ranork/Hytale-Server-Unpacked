package com.hypixel.hytale.server.core.modules.accesscontrol.ban;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hypixel.hytale.server.core.Message;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class TimedBan extends AbstractBan {
   private final Instant expiresOn;

   @Nonnull
   public static TimedBan fromJsonObject(@Nonnull JsonObject object) throws JsonParseException {
      try {
         UUID target = UUID.fromString(object.get("target").getAsString());
         UUID by = UUID.fromString(object.get("by").getAsString());
         Instant timestamp = Instant.ofEpochMilli(object.get("timestamp").getAsLong());
         Instant expiresOn = Instant.ofEpochMilli(object.get("expiresOn").getAsLong());
         String reason = null;
         if (object.has("reason")) {
            reason = object.get("reason").getAsString();
         }

         return new TimedBan(target, by, timestamp, expiresOn, reason);
      } catch (Throwable var6) {
         throw new JsonParseException(var6);
      }
   }

   public TimedBan(UUID target, UUID by, Instant timestamp, Instant expiresOn, String reason) {
      super(target, by, timestamp, reason);
      this.expiresOn = expiresOn;
   }

   @Override
   public boolean isInEffect() {
      return this.expiresOn.isAfter(Instant.now());
   }

   @Nonnull
   @Override
   public String getType() {
      return "timed";
   }

   public Instant getExpiresOn() {
      return this.expiresOn;
   }

   @Nonnull
   @Override
   public CompletableFuture<Optional<Message>> getDisconnectReason(@Nonnull UUID uuid) {
      long length = Duration.between(Instant.now(), this.expiresOn).toMillis();
      long days = length / 86400000L;
      long hours = (length - days * 86400000L) / 3600000L;
      long mins = (length - (days * 86400000L + hours * 3600000L)) / 60000L;
      Message message = this.reason.isPresent()
         ? Message.translation("client.general.disconnect.banned.timed.withReason")
            .param("days", days)
            .param("hours", hours)
            .param("mins", mins)
            .param("reason", this.reason.get())
         : Message.translation("client.general.disconnect.banned.timed").param("days", days).param("hours", hours).param("mins", mins);
      return CompletableFuture.completedFuture(Optional.of(message));
   }

   @Nonnull
   @Override
   public JsonObject toJsonObject() {
      JsonObject object = super.toJsonObject();
      object.addProperty("expiresOn", this.expiresOn.toEpochMilli());
      return object;
   }
}
