package com.hypixel.hytale.server.core.cosmetics;

import java.util.Arrays;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonDocument;

public class PlayerSkinPartTexture {
   private String texture;
   private String[] baseColor;

   protected PlayerSkinPartTexture(@Nonnull BsonDocument doc) {
      this.texture = doc.getString("Texture").getValue();
      if (doc.containsKey("BaseColor")) {
         BsonArray baseColor = doc.getArray("BaseColor");
         this.baseColor = new String[baseColor.size()];

         for (int i = 0; i < baseColor.size(); i++) {
            this.baseColor[i] = baseColor.get(i).asString().getValue();
         }
      }
   }

   public String getTexture() {
      return this.texture;
   }

   public String[] getBaseColor() {
      return this.baseColor;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerSkinPartTexture{texture='" + this.texture + "', baseColor=" + Arrays.toString((Object[])this.baseColor) + "}";
   }
}
