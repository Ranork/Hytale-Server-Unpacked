package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.modules.collision.WorldUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import java.time.Instant;
import javax.annotation.Nonnull;

public class SuffocatingCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<SuffocatingCondition> CODEC = BuilderCodec.builder(
         SuffocatingCondition.class, SuffocatingCondition::new, Condition.BASE_CODEC
      )
      .build();

   protected SuffocatingCondition() {
   }

   public SuffocatingCondition(boolean inverse) {
      super(inverse);
   }

   @Override
   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      if (EntityUtils.getEntity(ref, componentAccessor) instanceof LivingEntity livingEntity) {
         World var16 = componentAccessor.getExternalData().getWorld();
         Transform lookVec = TargetUtil.getLook(ref, componentAccessor);
         Vector3d position = lookVec.getPosition();
         ChunkStore chunkStore = var16.getChunkStore();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(position.x, position.z);
         Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
         if (chunkRef != null && chunkRef.isValid()) {
            long packed = WorldUtil.getPackedMaterialAndFluidAtPosition(chunkRef, chunkStore.getStore(), position.x, position.y, position.z);
            BlockMaterial material = BlockMaterial.VALUES[MathUtil.unpackLeft(packed)];
            int fluidId = MathUtil.unpackRight(packed);
            return !livingEntity.canBreathe(ref, material, fluidId, componentAccessor);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "SuffocatingCondition{} " + super.toString();
   }
}
