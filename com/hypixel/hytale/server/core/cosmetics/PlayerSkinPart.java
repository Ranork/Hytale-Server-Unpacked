package com.hypixel.hytale.server.core.cosmetics;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class PlayerSkinPart {
   private final String id;
   private final String name;
   private String model;
   private String greyscaleTexture;
   private String gradientSet;
   private Map<String, PlayerSkinPartTexture> textures;
   private Map<String, PlayerSkinPart.Variant> variants;
   private boolean isDefaultAsset;
   private String[] tags;
   private PlayerSkinPart.HaircutType hairType;
   private boolean requiresGenericHaircut;
   @Nonnull
   private PlayerSkinPart.HeadAccessoryType headAccessoryType = PlayerSkinPart.HeadAccessoryType.Simple;

   protected PlayerSkinPart(@Nonnull BsonDocument doc) {
      this.id = doc.getString("Id").getValue();
      this.name = doc.getString("Name").getValue();
      if (doc.containsKey("Model")) {
         this.model = doc.getString("Model").getValue();
      }

      if (doc.containsKey("GradientSet")) {
         this.gradientSet = doc.getString("GradientSet").getValue();
      }

      if (doc.containsKey("GreyscaleTexture")) {
         this.greyscaleTexture = doc.getString("GreyscaleTexture").getValue();
      }

      if (doc.containsKey("Variants")) {
         BsonDocument mapping = doc.getDocument("Variants");
         this.variants = new Object2ObjectOpenHashMap();

         for (Entry<String, BsonValue> set : mapping.entrySet()) {
            this.variants.put(set.getKey(), new PlayerSkinPart.Variant(set.getValue().asDocument()));
         }
      } else if (doc.containsKey("Textures")) {
         BsonDocument mapping = doc.getDocument("Textures");
         this.textures = new Object2ObjectOpenHashMap();

         for (Entry<String, BsonValue> set : mapping.entrySet()) {
            this.textures.put(set.getKey(), new PlayerSkinPartTexture(set.getValue().asDocument()));
         }
      }

      if (doc.containsKey("IsDefaultAsset")) {
         this.isDefaultAsset = doc.getBoolean("IsDefaultAsset").getValue();
      }

      if (doc.containsKey("Tags")) {
         BsonArray bsonArray = doc.getArray("Tags");
         this.tags = new String[bsonArray.size()];

         for (int i = 0; i < bsonArray.size(); i++) {
            this.tags[i] = bsonArray.get(i).asString().getValue();
         }
      }

      if (doc.containsKey("HairType")) {
         this.hairType = PlayerSkinPart.HaircutType.valueOf(doc.getString("HairType").getValue());
      }

      if (doc.containsKey("RequiresGenericHaircut")) {
         this.requiresGenericHaircut = doc.getBoolean("RequiresGenericHaircut").getValue();
      }

      if (doc.containsKey("HeadAccessoryType")) {
         this.headAccessoryType = PlayerSkinPart.HeadAccessoryType.valueOf(doc.getString("HeadAccessoryType").getValue());
      }
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getModel() {
      return this.model;
   }

   public Map<String, PlayerSkinPartTexture> getTextures() {
      return this.textures;
   }

   public Map<String, PlayerSkinPart.Variant> getVariants() {
      return this.variants;
   }

   public boolean isDefaultAsset() {
      return this.isDefaultAsset;
   }

   public String[] getTags() {
      return this.tags;
   }

   public PlayerSkinPart.HaircutType getHairType() {
      return this.hairType;
   }

   public boolean doesRequireGenericHaircut() {
      return this.requiresGenericHaircut;
   }

   @Nonnull
   public PlayerSkinPart.HeadAccessoryType getHeadAccessoryType() {
      return this.headAccessoryType;
   }

   public String getGreyscaleTexture() {
      return this.greyscaleTexture;
   }

   public String getGradientSet() {
      return this.gradientSet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerSkinPart{id='"
         + this.id
         + "', name='"
         + this.name
         + "', model='"
         + this.model
         + "', greyscaleTexture='"
         + this.greyscaleTexture
         + "', gradientSet='"
         + this.gradientSet
         + "', textures="
         + this.textures
         + ", variants="
         + this.variants
         + ", isDefaultAsset="
         + this.isDefaultAsset
         + ", tags="
         + Arrays.toString((Object[])this.tags)
         + ", hairType="
         + this.hairType
         + ", requiresGenericHaircut="
         + this.requiresGenericHaircut
         + ", headAccessoryType="
         + this.headAccessoryType
         + "}";
   }

   public static enum HaircutType {
      Short,
      Medium,
      Long;
   }

   public static enum HeadAccessoryType {
      Simple,
      HalfCovering,
      FullyCovering;
   }

   public static class Variant {
      private final String model;
      private String greyscaleTexture;
      private Map<String, PlayerSkinPartTexture> textures;

      protected Variant(@Nonnull BsonDocument doc) {
         this.model = doc.getString("Model").getValue();
         if (doc.containsKey("GreyscaleTexture")) {
            this.greyscaleTexture = doc.getString("GreyscaleTexture").getValue();
         }

         if (doc.containsKey("Textures")) {
            BsonDocument texturesDoc = doc.getDocument("Textures");
            this.textures = new Object2ObjectOpenHashMap();

            for (Entry<String, BsonValue> set : texturesDoc.entrySet()) {
               this.textures.put(set.getKey(), new PlayerSkinPartTexture(set.getValue().asDocument()));
            }
         }
      }

      public String getModel() {
         return this.model;
      }

      public String getGreyscaleTexture() {
         return this.greyscaleTexture;
      }

      public Map<String, PlayerSkinPartTexture> getTextures() {
         return this.textures;
      }

      @Nonnull
      @Override
      public String toString() {
         return "CharacterPartVariant{model='" + this.model + "'greyscaleTexture='" + this.greyscaleTexture + "', textures=" + this.textures + "}";
      }
   }
}
