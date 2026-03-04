package com.hypixel.hytale.server.core.cosmetics;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class PlayerSkinGradientSet {
   private final String id;
   private final Map<String, PlayerSkinPartTexture> gradients;

   protected PlayerSkinGradientSet(@Nonnull BsonDocument doc) {
      this.id = doc.getString("Id").getValue();
      BsonDocument gradients = doc.getDocument("Gradients");
      this.gradients = new Object2ObjectOpenHashMap();

      for (Entry<String, BsonValue> gradient : gradients.entrySet()) {
         this.gradients.put(gradient.getKey(), new PlayerSkinPartTexture(gradient.getValue().asDocument()));
      }
   }

   public String getId() {
      return this.id;
   }

   public Map<String, PlayerSkinPartTexture> getGradients() {
      return this.gradients;
   }
}
