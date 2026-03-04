package com.hypixel.hytale.builtin.blockspawner;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.store.StoredCodec;
import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.prefab.config.SelectionPrefabSerializer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class BlockSpawnerEntry implements IWeightedElement {
   @Nonnull
   public static BuilderCodec<BlockSpawnerEntry> CODEC = BuilderCodec.builder(BlockSpawnerEntry.class, BlockSpawnerEntry::new)
      .append(new KeyedCodec<>("Name", Codec.STRING), (entry, key) -> entry.blockName = key, entry -> entry.blockName)
      .addValidatorLate(() -> BlockType.VALIDATOR_CACHE.getValidator().late())
      .add()
      .append(new KeyedCodec<>("RotationMode", BlockSpawnerEntry.RotationMode.CODEC), (entry, b) -> entry.rotationMode = b, entry -> entry.rotationMode)
      .add()
      .append(new KeyedCodec<>("Weight", Codec.DOUBLE), (entry, d) -> entry.weight = d, entry -> entry.weight)
      .add()
      .append(
         new KeyedCodec<>("State", Codec.BSON_DOCUMENT),
         (entry, wrapper, extraInfo) -> entry.blockComponents = SelectionPrefabSerializer.legacyStateDecode(wrapper),
         (entry, extraInfo) -> null
      )
      .add()
      .append(
         new KeyedCodec<>("Components", new StoredCodec<>(ChunkStore.HOLDER_CODEC_KEY)),
         (entry, holder) -> entry.blockComponents = holder,
         entry -> entry.blockComponents
      )
      .add()
      .build();
   @Nonnull
   public static final BlockSpawnerEntry[] EMPTY_ARRAY = new BlockSpawnerEntry[0];
   private String blockName;
   private Holder<ChunkStore> blockComponents;
   private double weight;
   private BlockSpawnerEntry.RotationMode rotationMode = BlockSpawnerEntry.RotationMode.INHERIT;

   public String getBlockName() {
      return this.blockName;
   }

   public Holder<ChunkStore> getBlockComponents() {
      return this.blockComponents;
   }

   public BlockSpawnerEntry.RotationMode getRotationMode() {
      return this.rotationMode;
   }

   @Override
   public double getWeight() {
      return this.weight;
   }

   public static enum RotationMode {
      NONE,
      RANDOM,
      INHERIT;

      @Nonnull
      public static final Codec<BlockSpawnerEntry.RotationMode> CODEC = new EnumCodec<>(BlockSpawnerEntry.RotationMode.class);
   }
}
