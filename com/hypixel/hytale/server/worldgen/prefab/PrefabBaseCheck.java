package com.hypixel.hytale.server.worldgen.prefab;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabBaseCheck {
   public static final String KEY_BASE_SCALE = "BaseScale";
   public static final String KEY_PREFAB_SIZE = "PrefabSize";
   public static final String KEY_HEIGHT_TOLERANCE = "HeightTolerance";
   public static final float DEFAULT_BASE_SCALE = 1.0F;
   public static final int DEFAULT_PREFAB_SIZE = 20;
   public static final int DEFAULT_HEIGHT_TOLERANCE = 2;
   public static final BuilderCodec<PrefabBaseCheck> CODEC = BuilderCodec.builder(PrefabBaseCheck.class, PrefabBaseCheck::new)
      .documentation("Define additional height checks around the base of the prefab to prevent it placing on uneven ground")
      .<Float>append(new KeyedCodec<>("BaseScale", Codec.FLOAT), (self, v) -> self.baseScale = v, self -> self.baseScale)
      .addValidator(Validators.range(0.0F, 1.0F))
      .documentation("The size of the prefab's base as a proportion of its horizontal dimensions")
      .add()
      .<Integer>append(new KeyedCodec<>("PrefabSize", Codec.INTEGER), (self, v) -> self.prefabSize = v, self -> self.prefabSize)
      .addValidator(Validators.greaterThan(0))
      .documentation("The minimum size of the prefab that this base check applies to")
      .add()
      .<Integer>append(new KeyedCodec<>("HeightTolerance", Codec.INTEGER), (self, v) -> self.heightTolerance = v, self -> self.heightTolerance)
      .documentation("The amount of terrain height difference allowed around the prefab's base before the location is deemed too uneven")
      .add()
      .build();
   public static final ArrayCodec<PrefabBaseCheck> ARRAY_CODEC = new ArrayCodec<>(CODEC, PrefabBaseCheck[]::new);
   public static final PrefabBaseCheck[] EMPTY_ARRAY = new PrefabBaseCheck[0];
   protected float baseScale = 1.0F;
   protected int prefabSize = 20;
   protected int heightTolerance = 2;

   public float getBaseScale() {
      return this.baseScale;
   }

   public int getPrefabSize() {
      return this.prefabSize;
   }

   public int getHeightTolerance() {
      return this.heightTolerance;
   }

   public static class Loader<T extends SeedStringResource> extends JsonLoader<T, PrefabBaseCheck> {
      public Loader(SeedString<T> seed, Path dataFolder, @Nullable JsonElement json) {
         super(seed, dataFolder, json);
      }

      @Nullable
      public PrefabBaseCheck load() {
         PrefabBaseCheck check = new PrefabBaseCheck();
         check.baseScale = this.mustGetNumber("BaseScale", 1.0F).floatValue();
         check.prefabSize = this.mustGetNumber("PrefabSize", 20).intValue();
         check.heightTolerance = this.mustGetNumber("HeightTolerance", 2).intValue();
         return check;
      }

      public static <T extends SeedStringResource> PrefabBaseCheck[] loadAll(@Nonnull JsonLoader<T, ?> parent, @Nullable JsonElement element) {
         if (element != null && element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.isEmpty()) {
               return PrefabBaseCheck.EMPTY_ARRAY;
            } else {
               PrefabBaseCheck[] checks = new PrefabBaseCheck[array.size()];

               for (int i = 0; i < array.size(); i++) {
                  checks[i] = new PrefabBaseCheck.Loader<>(parent.getSeed(), parent.getDataFolder(), array.get(i)).load();
               }

               Arrays.sort(checks, Comparator.comparingInt(PrefabBaseCheck::getPrefabSize).reversed());
               return checks;
            }
         } else {
            return PrefabBaseCheck.EMPTY_ARRAY;
         }
      }
   }
}
