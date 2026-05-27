package com.hypixel.hytale.builtin.worldgen.modifier;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.op.Op;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.worldgen.util.ListPool;
import java.util.EnumMap;
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
      .<Op[]>append(new KeyedCodec<>("Operations", Op.ARRAY_CODEC), (instance, array) -> instance.operations = array, instance -> instance.operations)
      .documentation("List of operations to perform on the target")
      .add()
      .afterDecode(WorldGenModifier::init)
      .build();
   public static final AssetBuilderCodec<String, WorldGenModifier> ASSET_CODEC = AssetBuilderCodec.wrap(
      CODEC, Codec.STRING, (instance, id) -> instance.id = id, instance -> instance.id, (instance, data) -> instance.data = data, instance -> instance.data
   );
   public static final DefaultAssetMap<String, WorldGenModifier> ASSET_MAP = new DefaultAssetMap<>();
   private static final String UNKNOWN_ID = "Unknown";
   private static final ListPool<Op> POOL = new ListPool<>(10, Op.EMPTY_ARRAY);
   @Nonnull
   protected String id = "Unknown";
   @Nullable
   protected AssetExtraInfo.Data data = null;
   @Nonnull
   protected EventPriority priority = EventPriority.NORMAL;
   @Nonnull
   protected Target target = new Target();
   @Nonnull
   protected Op[] operations = Op.EMPTY_ARRAY;
   protected final transient EnumMap<Content.Type, Op[]> opsLookup = new EnumMap<>(Content.Type.class);

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

   public Op[] getOperations(Content.Type type) {
      return this.opsLookup.getOrDefault(type, Op.EMPTY_ARRAY);
   }

   protected static void init(WorldGenModifier modifier) {
      modifier.opsLookup.clear();

      for (Content.Type type : Content.Type.VALUES) {
         try (ListPool.Resource<Op> list = POOL.acquire(modifier.operations.length)) {
            for (Op op : modifier.operations) {
               if (op.type() == type) {
                  list.add(op);
               }
            }

            modifier.opsLookup.put(type, (Op[])list.toArray(Op.EMPTY_ARRAY));
         }
      }
   }
}
