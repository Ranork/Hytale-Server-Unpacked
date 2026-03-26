package com.hypixel.hytale.server.core.modules.entity.condition;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.modules.collision.WorldUtil;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.time.Instant;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InFluidCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<InFluidCondition> CODEC = BuilderCodec.builder(InFluidCondition.class, InFluidCondition::new, Condition.BASE_CODEC)
      .appendInherited(
         new KeyedCodec<>("FluidIds", new ArrayCodec<>(Codec.STRING, String[]::new)),
         (condition, value) -> condition.fluidIds = value,
         condition -> condition.fluidIds,
         (condition, parent) -> condition.fluidIds = parent.fluidIds
      )
      .addValidatorLate(() -> Fluid.VALIDATOR_CACHE.getArrayValidator().late())
      .documentation("Fluid IDs to match against. Returns true when at least one block within the entity's bounding box contains one of these fluids.")
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("Tags", new ArrayCodec<>(Codec.STRING, String[]::new)),
         (condition, value) -> condition.tags = value,
         condition -> condition.tags,
         (condition, parent) -> condition.tags = parent.tags
      )
      .documentation(
         "Fluid tags to match against. Returns true when at least one block within the entity's bounding box contains a fluid with any of these tags."
      )
      .add()
      .afterDecode(condition -> {
         if (condition.fluidIds != null) {
            condition.fluidIndexes = Arrays.stream(condition.fluidIds).mapToInt(id -> Fluid.getAssetMap().getIndex(id)).sorted().toArray();
         }

         if (condition.tags != null) {
            condition.tagIndexes = Arrays.stream(condition.tags).mapToInt(AssetRegistry::getOrCreateTagIndex).sorted().toArray();
         }
      })
      .build();
   @Nullable
   protected String[] fluidIds;
   @Nullable
   protected String[] tags;
   @Nullable
   private transient int[] fluidIndexes;
   @Nullable
   private transient int[] tagIndexes;

   protected InFluidCondition() {
   }

   private boolean isMatchingFluid(int fluidId) {
      if (this.fluidIndexes != null && this.fluidIndexes.length > 0 && Arrays.binarySearch(this.fluidIndexes, fluidId) >= 0) {
         return true;
      } else {
         if (this.tagIndexes != null && this.tagIndexes.length > 0) {
            Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
            if (fluid != null) {
               AssetExtraInfo.Data data = fluid.getData();
               if (data != null) {
                  Int2ObjectMap<IntSet> fluidTags = data.getTags();

                  for (int tagIndex : this.tagIndexes) {
                     if (fluidTags.containsKey(tagIndex)) {
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      BoundingBox boundingBoxComponent = componentAccessor.getComponent(ref, BoundingBox.getComponentType());
      if (boundingBoxComponent == null) {
         return false;
      } else {
         World world = componentAccessor.getExternalData().getWorld();
         ChunkStore chunkStore = world.getChunkStore();
         Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
         Vector3d position = transformComponent.getPosition();
         Box box = boundingBoxComponent.getBoundingBox();
         return !box.forEachBlock(position.x, position.y, position.z, 0.0, (bx, by, bz) -> {
            Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(bx, bz));
            if (chunkRef != null && chunkRef.isValid()) {
               long packed = WorldUtil.getPackedMaterialAndFluidAtPosition(chunkRef, chunkComponentStore, bx, by, bz);
               int fluidId = MathUtil.unpackRight(packed);
               return fluidId == 0 || !this.isMatchingFluid(fluidId);
            } else {
               return true;
            }
         });
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "InFluidCondition{fluidIds="
         + Arrays.toString((Object[])this.fluidIds)
         + ", tags="
         + Arrays.toString((Object[])this.tags)
         + "} "
         + super.toString();
   }
}
