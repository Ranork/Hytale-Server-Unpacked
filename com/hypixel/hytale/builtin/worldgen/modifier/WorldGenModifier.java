package com.hypixel.hytale.builtin.worldgen.modifier;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.worldgen.modifier.event.EventType;
import com.hypixel.hytale.builtin.worldgen.modifier.op.Op;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.event.EventPriority;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldGenModifier implements JsonAssetWithMap<String, DefaultAssetMap<String, WorldGenModifier>> {
   public static final BuilderCodec<WorldGenModifier> CODEC = BuilderCodec.builder(WorldGenModifier.class, WorldGenModifier::new)
      .documentation("Asset type used to data-drive user modifications to world-gen-v1 assets")
      .<EventPriority>append(
         new KeyedCodec<>("Priority", new EnumCodec<>(EventPriority.class)),
         (instance, priority) -> instance.priority = priority,
         instance -> instance.priority
      )
      .documentation("The order this modifier will be applied relative to others")
      .add()
      .<Target>append(new KeyedCodec<>("Target", Target.CODEC), (instance, target) -> instance.target = target, instance -> instance.target)
      .documentation("The target world-gen configuration to modify")
      .add()
      .<Map<EventType, Op[]>>append(
         new KeyedCodec<>("Content", new EnumMapCodec<>(EventType.class, Op.ARRAY_CODEC)),
         (instance, map) -> instance.content = map,
         instance -> instance.content
      )
      .documentation("The operations to perform on the target configuration")
      .add()
      .build();
   public static final AssetBuilderCodec<String, WorldGenModifier> ASSET_CODEC = AssetBuilderCodec.wrap(
      CODEC, Codec.STRING, (instance, id) -> instance.id = id, instance -> instance.id, (instance, data) -> instance.data = data, instance -> instance.data
   );
   public static final DefaultAssetMap<String, WorldGenModifier> ASSET_MAP = new DefaultAssetMap<>();
   private static final String UNKNOWN_ID = "Unknown";
   @Nonnull
   protected String id = "Unknown";
   @Nullable
   protected AssetExtraInfo.Data data = null;
   @Nonnull
   protected EventPriority priority = EventPriority.NORMAL;
   @Nonnull
   protected Target target = new Target();
   @Nonnull
   protected Map<EventType, Op[]> content = new EnumMap<>(EventType.class);

   public WorldGenModifier() {
   }

   public WorldGenModifier(@Nonnull String id) {
      this.id = id;
   }

   @Nonnull
   public String getId() {
      return this.id;
   }

   @Nonnull
   public Target getTarget() {
      return this.target;
   }

   @Nonnull
   public Op[] getOperations(@Nonnull EventType type) {
      return this.content.getOrDefault(type, Op.EMPTY_ARRAY);
   }
}
