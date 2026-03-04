package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionPlaceBlock;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.CachedPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.BlockPlacementHelper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionPlaceBlock extends ActionBase {
   protected static final ComponentType<EntityStore, BoundingBox> BOUNDING_BOX_COMPONENT_TYPE = BoundingBox.getComponentType();
   protected final double range;
   protected final boolean allowEmptyMaterials;
   protected final Vector3d target = new Vector3d();

   public ActionPlaceBlock(@Nonnull BuilderActionPlaceBlock builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.range = builder.getRange(support);
      this.allowEmptyMaterials = builder.isAllowEmptyMaterials(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      if (super.canExecute(ref, role, sensorInfo, dt, store) && sensorInfo != null && sensorInfo.hasPosition()) {
         String blockToPlace = role.getWorldSupport().getBlockToPlace();
         if (blockToPlace == null) {
            return false;
         } else {
            BlockType placedBlockType = BlockType.getAssetMap().getAsset(blockToPlace);
            if (placedBlockType == null) {
               return false;
            } else {
               sensorInfo.getPositionProvider().providePosition(this.target);
               World world = store.getExternalData().getWorld();
               TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

               assert transformComponent != null;

               double maxDistance = this.range;
               BoundingBox hitBox = store.getComponent(ref, BOUNDING_BOX_COMPONENT_TYPE);
               if (hitBox != null) {
                  maxDistance += hitBox.getBoundingBox().getMaximumExtent();
               }

               int x = MathUtil.floor(this.target.getX());
               int y = MathUtil.floor(this.target.getY());
               int z = MathUtil.floor(this.target.getZ());
               if (transformComponent.getPosition().distanceSquaredTo(x, y, z) > maxDistance * maxDistance) {
                  return false;
               } else if (sensorInfo instanceof CachedPositionProvider && !((CachedPositionProvider)sensorInfo).isFromCache()) {
                  return true;
               } else {
                  return !BlockPlacementHelper.canPlaceUnitBlock(world, placedBlockType, this.allowEmptyMaterials, x, y, z)
                     ? false
                     : BlockPlacementHelper.canPlaceBlock(world, placedBlockType, 0, this.allowEmptyMaterials, x, y, z);
               }
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      World world = store.getExternalData().getWorld();
      WorldChunk chunk = world.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(this.target.getX(), this.target.getZ()));
      chunk.setBlock(
         MathUtil.floor(this.target.getX()), MathUtil.floor(this.target.getY()), MathUtil.floor(this.target.getZ()), role.getWorldSupport().getBlockToPlace()
      );
      return true;
   }
}
