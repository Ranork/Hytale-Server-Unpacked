package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterStandingOnBlock;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityFilterStandingOnBlock extends EntityFilterBase {
   public static final int COST = 300;
   @Nullable
   protected static final ComponentType<EntityStore, NPCEntity> NPC_COMPONENT_TYPE = NPCEntity.getComponentType();
   @Nonnull
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected final int blockSet;

   public EntityFilterStandingOnBlock(@Nonnull BuilderEntityFilterStandingOnBlock builder, @Nonnull BuilderSupport support) {
      this.blockSet = builder.getBlockSet(support);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      NPCEntity targetNpcComponent = store.getComponent(targetRef, NPC_COMPONENT_TYPE);
      if (targetNpcComponent != null) {
         Role targetRole = targetNpcComponent.getRole();

         assert targetRole != null;

         MotionController motionController = targetRole.getActiveMotionController();
         return motionController.standingOnBlockOfType(this.blockSet);
      } else {
         TransformComponent transformComponent = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         Ref<ChunkStore> chunkRef = transformComponent.getChunkRef();
         if (chunkRef != null && chunkRef.isValid()) {
            World world = store.getExternalData().getWorld();
            Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
            BlockChunk blockChunkComponent = chunkStore.getComponent(chunkRef, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            Vector3d pos = transformComponent.getPosition();
            int blockId = blockChunkComponent.getBlock(MathUtil.floor(pos.x), MathUtil.floor(pos.y - 1.0), MathUtil.floor(pos.z));
            return BlockSetModule.getInstance().blockInSet(this.blockSet, blockId);
         } else {
            return false;
         }
      }
   }

   @Override
   public int cost() {
      return 300;
   }
}
