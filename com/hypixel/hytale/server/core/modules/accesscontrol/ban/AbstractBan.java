package com.hypixel.hytale.server.core.modules.accesscontrol.ban;

import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;

abstract class AbstractBan implements Ban {
   protected final UUID target;
   protected final UUID by;
   protected final Instant timestamp;
   @Nonnull
   protected final Optional<String> reason;

   public AbstractBan(UUID target, UUID by, Instant timestamp, String reason) {
      this.target = target;
      this.by = by;
      this.timestamp = timestamp;
      this.reason = Optional.ofNullable(reason);
   }

   @Override
   public UUID getTarget() {
      return this.target;
   }

   @Override
   public UUID getBy() {
      return this.by;
   }

   @Override
   public Instant getTimestamp() {
      return this.timestamp;
   }

   @Nonnull
   @Override
   public Optional<String> getReason() {
      return this.reason;
   }

   @Nonnull
   @Override
   public JsonObject toJsonObject() {
      JsonObject object = new JsonObject();
      object.addProperty("type", this.getType());
      object.addProperty("target", this.target.toString());
      object.addProperty("by", this.by.toString());
      object.addProperty("timestamp", this.timestamp.toEpochMilli());
      this.reason.ifPresent(s -> object.addProperty("reason", s));
      return object;
   }
}
