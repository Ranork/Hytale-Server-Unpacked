package com.hypixel.hytale.server.core.universe.world.worldgen.provider;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.RangeRefValidator;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockStateChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedEntityChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenTimingsCollector;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlatWorldGenProvider implements IWorldGenProvider {
   public static final String ID = "Flat";
   public static final BuilderCodec<FlatWorldGenProvider> CODEC = BuilderCodec.builder(FlatWorldGenProvider.class, FlatWorldGenProvider::new)
      .documentation("A world generation provider that generates a flat world with defined layers.")
      .<Color>append(new KeyedCodec<>("Tint", ProtocolCodecs.COLOR), (config, o) -> config.tint = o, config -> config.tint)
      .documentation("The tint to set for all chunks that are generated.")
      .add()
      .<FlatWorldGenProvider.Layer[]>append(
         new KeyedCodec<>("Layers", new ArrayCodec<>(FlatWorldGenProvider.Layer.CODEC, FlatWorldGenProvider.Layer[]::new)),
         (config, o) -> config.layers = o,
         config -> config.layers
      )
      .documentation("The list of layers to add to the world.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   public static final Color DEFAULT_TINT = new Color((byte)91, (byte)-98, (byte)40);
   private Color tint = DEFAULT_TINT;
   private FlatWorldGenProvider.Layer[] layers;

   public FlatWorldGenProvider() {
      this.layers = new FlatWorldGenProvider.Layer[]{new FlatWorldGenProvider.Layer(0, 1, Environment.UNKNOWN.getId(), "Soil_Grass")};
   }

   public FlatWorldGenProvider(Color tint, FlatWorldGenProvider.Layer[] layers) {
      this.tint = tint;
      this.layers = layers;
   }

   @Nonnull
   @Override
   public IWorldGen getGenerator() throws WorldGenLoadException {
      int tintId = ColorParseUtil.colorToARGBInt(this.tint);

      for (FlatWorldGenProvider.Layer layer : this.layers) {
         if (layer.from >= layer.to) {
            throw new WorldGenLoadException("Failed to load 'Flat' WorldGen config, 'To' must be greater than 'From': " + layer);
         }

         layer.from = Math.max(layer.from, 0);
         layer.to = Math.min(layer.to, 320);
         if (layer.environment != null) {
            int index = Environment.getAssetMap().getIndex(layer.environment);
            if (index == Integer.MIN_VALUE) {
               throw new WorldGenLoadException("Unknown key! " + layer.environment);
            }

            layer.environmentId = index;
         } else {
            layer.environmentId = 0;
         }

         if (layer.blockType != null) {
            int index = BlockType.getAssetMap().getIndex(layer.blockType);
            if (index == Integer.MIN_VALUE) {
               throw new WorldGenLoadException("Unknown key! " + layer.blockType);
            }

            layer.blockId = index;
         } else {
            layer.blockId = 0;
         }
      }

      return new FlatWorldGenProvider.FlatWorldGen(this.layers, tintId);
   }

   @Nonnull
   @Override
   public String toString() {
      return "FlatWorldGenProvider{tint=" + this.tint + ", layers=" + Arrays.toString((Object[])this.layers) + "}";
   }

   private static class FlatWorldGen implements IWorldGen {
      private final FlatWorldGenProvider.Layer[] layers;
      private final int tintId;

      public FlatWorldGen(FlatWorldGenProvider.Layer[] layers, int tintId) {
         this.layers = layers;
         this.tintId = tintId;
      }

      @Nullable
      @Override
      public WorldGenTimingsCollector getTimings() {
         return null;
      }

      @Nonnull
      @Override
      public Transform[] getSpawnPoints(int seed) {
         return new Transform[]{new Transform(0.0, 81.0, 0.0)};
      }

      @Nonnull
      @Override
      public CompletableFuture<GeneratedChunk> generate(int seed, long index, int cx, int cz, LongPredicate stillNeeded) {
         GeneratedBlockChunk generatedBlockChunk = new GeneratedBlockChunk(index, cx, cz);

         for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
               generatedBlockChunk.setTint(x, z, this.tintId);
            }
         }

         for (FlatWorldGenProvider.Layer layer : this.layers) {
            for (int x = 0; x < 32; x++) {
               for (int z = 0; z < 32; z++) {
                  for (int y = layer.from; y < layer.to; y++) {
                     generatedBlockChunk.setBlock(x, y, z, layer.blockId, 0, 0);
                     generatedBlockChunk.setEnvironment(x, y, z, layer.environmentId);
                  }

                  generatedBlockChunk.setTint(x, z, this.tintId);
               }
            }
         }

         return CompletableFuture.completedFuture(
            new GeneratedChunk(generatedBlockChunk, new GeneratedBlockStateChunk(), new GeneratedEntityChunk(), GeneratedChunk.makeSections())
         );
      }
   }

   public static class Layer {
      public static final BuilderCodec<FlatWorldGenProvider.Layer> CODEC = BuilderCodec.builder(
            FlatWorldGenProvider.Layer.class, FlatWorldGenProvider.Layer::new
         )
         .documentation("A layer of blocks for a given range.")
         .<Integer>append(new KeyedCodec<>("From", Codec.INTEGER), (layer, i) -> layer.from = i, layer -> layer.from)
         .documentation("The Y coordinate (inclusive) to start placing blocks at.")
         .add()
         .<Integer>append(new KeyedCodec<>("To", Codec.INTEGER), (layer, i) -> layer.to = i, layer -> layer.to)
         .documentation("The Y coordinate (exclusive) to stop placing blocks at.")
         .addValidator(new RangeRefValidator<>("1/From", null, false))
         .add()
         .<String>append(new KeyedCodec<>("BlockType", Codec.STRING), (layer, s) -> layer.blockType = s, layer -> layer.blockType)
         .documentation("The type of block that will be used for all blocks placed at this layer.")
         .addValidator(BlockType.VALIDATOR_CACHE.getValidator())
         .add()
         .<String>append(new KeyedCodec<>("Environment", Codec.STRING), (layer, s) -> layer.environment = s, layer -> layer.environment)
         .documentation("The environment to set for every block placed.")
         .addValidator(Environment.VALIDATOR_CACHE.getValidator())
         .add()
         .build();
      public int from = Integer.MIN_VALUE;
      public int to = Integer.MAX_VALUE;
      public String environment;
      public String blockType;
      public int environmentId;
      public int blockId;

      public Layer() {
      }

      public Layer(int from, int to, String environment, String blockType) {
         this.from = from;
         this.to = to;
         this.environment = environment;
         this.blockType = blockType;
      }

      @Nonnull
      @Override
      public String toString() {
         return "Layer{from=" + this.from + ", to=" + this.to + ", environment='" + this.environment + "', blockType=" + this.blockType + "}";
      }
   }
}
