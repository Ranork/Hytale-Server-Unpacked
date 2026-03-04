package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnvironmentCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<EnvironmentCondition> CODEC = BuilderCodec.builder(
         EnvironmentCondition.class, EnvironmentCondition::new, Condition.BASE_CODEC
      )
      .append(
         new KeyedCodec<>("Environments", new ArrayCodec<>(Codec.STRING, String[]::new)),
         (condition, value) -> condition.unknownEnvironments = value,
         condition -> condition.unknownEnvironments
      )
      .documentation("The environments to evaluate the condition against.")
      .add()
      .afterDecode(condition -> condition.environments = null)
      .build();
   protected String[] unknownEnvironments;
   @Nullable
   protected int[] environments;

   protected EnvironmentCondition() {
   }

   public int[] getEnvironments() {
      if (this.environments == null && this.unknownEnvironments != null) {
         this.environments = Arrays.stream(this.unknownEnvironments)
            .mapToInt(environment -> Environment.getAssetMap().getIndex(environment))
            .sorted()
            .toArray();
      }

      return this.environments;
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      World world = componentAccessor.getExternalData().getWorld();
      ChunkStore chunkStore = world.getChunkStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(position.getX(), position.getZ());
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
         BlockChunk blockChunkComponent = chunkComponentStore.getComponent(chunkRef, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         int environmentId = blockChunkComponent.getEnvironment(position);
         return Arrays.binarySearch(this.getEnvironments(), environmentId) >= 0;
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "EnvironmentCondition{unknownEnvironments="
         + Arrays.toString((Object[])this.unknownEnvironments)
         + ", environments="
         + Arrays.toString(this.environments)
         + "} "
         + super.toString();
   }
}
