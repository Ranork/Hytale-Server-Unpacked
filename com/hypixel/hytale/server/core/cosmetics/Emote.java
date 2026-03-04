package com.hypixel.hytale.server.core.cosmetics;

import javax.annotation.Nonnull;
import org.bson.BsonDocument;

public class Emote {
   protected String id;
   protected String name;
   protected String animation;

   protected Emote(@Nonnull BsonDocument bson) {
      this.id = bson.getString("Id").getValue();
      this.name = bson.getString("Name").getValue();
      this.animation = bson.getString("Animation").getValue();
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getAnimation() {
      return this.animation;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Emote{id='" + this.id + "', name='" + this.name + "', animation='" + this.animation + "'}";
   }
}
