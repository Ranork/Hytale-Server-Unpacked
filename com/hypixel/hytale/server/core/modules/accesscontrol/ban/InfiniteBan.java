package com.hypixel.hytale.server.core.modules.accesscontrol.ban;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hypixel.hytale.server.core.Message;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class InfiniteBan extends AbstractBan {
   @Nonnull
   public static InfiniteBan fromJsonObject(@Nonnull JsonObject object) throws JsonParseException {
      try {
         UUID target = UUID.fromString(object.get("target").getAsString());
         UUID by = UUID.fromString(object.get("by").getAsString());
         Instant timestamp = Instant.ofEpochMilli(object.get("timestamp").getAsLong());
         String reason = null;
         if (object.has("reason")) {
            reason = object.get("reason").getAsString();
         }

         return new InfiniteBan(target, by, timestamp, reason);
      } catch (Throwable var5) {
         throw new JsonParseException(var5);
      }
   }

   public InfiniteBan(UUID target, UUID by, Instant timestamp, String reason) {
      super(target, by, timestamp, reason);
   }

   @Override
   public boolean isInEffect() {
      return true;
   }

   @Nonnull
   @Override
   public String getType() {
      return "infinite";
   }

   @Nonnull
   @Override
   public CompletableFuture<Optional<Message>> getDisconnectReason(@Nonnull UUID uuid) {
      Message message = this.reason.isPresent()
         ? Message.translation("client.general.disconnect.banned.permanent.withReason").param("reason", this.reason.get())
         : Message.translation("client.general.disconnect.banned.permanent");
      return CompletableFuture.completedFuture(Optional.of(message));
   }
}
