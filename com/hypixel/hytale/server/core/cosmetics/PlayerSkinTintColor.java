package com.hypixel.hytale.server.core.cosmetics;

import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonDocument;

public class PlayerSkinTintColor {
   protected String id;
   protected String[] baseColor;

   protected PlayerSkinTintColor(@Nonnull BsonDocument doc) {
      this.id = doc.getString("Id").getValue();
      BsonArray baseColor = doc.getArray("BaseColor");
      this.baseColor = new String[baseColor.size()];

      for (int i = 0; i < baseColor.size(); i++) {
         this.baseColor[i] = baseColor.get(i).asString().getValue();
      }
   }

   public String getId() {
      return this.id;
   }

   public String[] getBaseColor() {
      return this.baseColor;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerSkinTintColor{id='" + this.id + "', baseColor='" + this.baseColor + "'}";
   }
}
