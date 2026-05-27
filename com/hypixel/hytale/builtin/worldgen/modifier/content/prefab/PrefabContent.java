package com.hypixel.hytale.builtin.worldgen.modifier.content.prefab;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.BlockMask;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.HeightMask;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.NoiseMask;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabLoader;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.util.function.ConstantCoordinateDoubleSupplier;
import com.hypixel.hytale.server.worldgen.util.function.ICoordinateDoubleSupplier;
import javax.annotation.Nonnull;

public abstract class PrefabContent implements Content {
   public static final BuilderCodec<PrefabContent> CODEC = BuilderCodec.abstractBuilder(PrefabContent.class)
      .append(new KeyedCodec<>("Prefabs", PrefabContent.Entry.ARRAY_CODEC), (t, v) -> t.prefabs = v, t -> t.prefabs)
      .documentation("Weighted list of prefab paths to randomly choose from")
      .add()
      .<BlockMask>append(new KeyedCodec<>("BlockMask", BlockMask.CODEC), (t, v) -> t.blockMask = v, t -> t.blockMask)
      .documentation("The types of blocks that the prefab's blocks can replace")
      .add()
      .<NoiseMask>append(new KeyedCodec<>("NoiseMask", NoiseMask.CODEC), (t, v) -> t.noiseMask = v, t -> t.noiseMask)
      .documentation("The horizontal noise mask for the prefab placement position")
      .add()
      .<HeightMask>append(new KeyedCodec<>("HeightMask", HeightMask.CODEC), (t, v) -> t.heightMask = v, t -> t.heightMask)
      .documentation("The height range mask for the prefab placement position")
      .add()
      .<Integer>append(new KeyedCodec<>("OffsetY", Codec.INTEGER), (t, v) -> t.offsetY = v, t -> t.offsetY)
      .documentation("The vertical offset for the prefab placement position")
      .add()
      .<PrefabRotation[]>append(
         new KeyedCodec<>("Rotations", new ArrayCodec<>(new EnumCodec<>(PrefabRotation.class), PrefabRotation[]::new)),
         (t, v) -> t.rotations = v,
         t -> t.rotations
      )
      .documentation("The rotation directions that the prefab can be randomly placed in")
      .add()
      .<Environment>append(new KeyedCodec<>("Environment", Environment.CODEC), (t, v) -> t.environment = v, t -> t.environment)
      .documentation("The environment to place where the prefab is placed")
      .add()
      .afterDecode(PrefabContent::rebuild)
      .build();
   protected PrefabContent.Entry[] prefabs = PrefabContent.Entry.EMPTY_ARRAY;
   protected NoiseMask noiseMask = NoiseMask.DEFAULT;
   protected HeightMask heightMask = HeightMask.DEFAULT;
   protected BlockMask blockMask = BlockMask.REPLACE_ANY;
   protected int offsetY = 0;
   protected PrefabRotation[] rotations = PrefabRotation.VALUES;
   protected Environment environment = Environment.UNKNOWN;
   protected transient ICoordinateDoubleSupplier offset = ConstantCoordinateDoubleSupplier.DEFAULT_ZERO;

   @Override
   public Content.Type type() {
      return Content.Type.BIOME_PREFAB;
   }

   @Override
   public abstract <T> void applyTo(ModifyEvent<T> var1) throws Exception;

   protected void rebuild() {
      this.offset = new ConstantCoordinateDoubleSupplier(this.offsetY);
   }

   protected IWeightedMap<WorldGenPrefabSupplier> buildPrefabs(@Nonnull SeedString<SeedStringResource> seed) {
      WorldGenPrefabLoader loader = seed.get().getLoader();
      WeightedMap.Builder<WorldGenPrefabSupplier> builder = WeightedMap.builder(WorldGenPrefabSupplier.EMPTY_ARRAY);

      for (PrefabContent.Entry entry : this.prefabs) {
         WorldGenPrefabSupplier[] prefabs = loader.get(entry.path);
         if (prefabs != null) {
            for (WorldGenPrefabSupplier prefab : prefabs) {
               builder.put(prefab, entry.weight);
            }
         }
      }

      return builder.build();
   }

   public static class Entry {
      public static final BuilderCodec<PrefabContent.Entry> CODEC = BuilderCodec.builder(PrefabContent.Entry.class, PrefabContent.Entry::new)
         .append(new KeyedCodec<>("Path", Codec.STRING), (t, v) -> t.path = v, t -> t.path)
         .documentation("The path of the prefab")
         .add()
         .<Double>append(new KeyedCodec<>("Weight", Codec.DOUBLE), (t, v) -> t.weight = v, t -> t.weight)
         .addValidator(Validators.range(0.0, 100.0))
         .documentation("The random chance of the path being chosen")
         .add()
         .build();
      public static final ArrayCodec<PrefabContent.Entry> ARRAY_CODEC = new ArrayCodec<>(CODEC, PrefabContent.Entry[]::new);
      public static final PrefabContent.Entry[] EMPTY_ARRAY = new PrefabContent.Entry[0];
      private String path = "";
      private double weight = 1.0;
   }
}
