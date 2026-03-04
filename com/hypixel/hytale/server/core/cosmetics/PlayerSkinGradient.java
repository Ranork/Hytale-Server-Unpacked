package com.hypixel.hytale.server.core.cosmetics;

import javax.annotation.Nonnull;
import org.bson.BsonDocument;

public class PlayerSkinGradient extends PlayerSkinTintColor {
   private String texture;

   protected PlayerSkinGradient(@Nonnull BsonDocument doc) {
      super(doc);
      if (doc.containsKey("Texture")) {
         this.texture = doc.getString("Texture").getValue();
      }
   }

   public String getTexture() {
      return this.texture;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerSkinGradient{texture='" + this.texture + "', id='" + this.id + "', baseColor='" + this.baseColor + "'}";
   }
}
