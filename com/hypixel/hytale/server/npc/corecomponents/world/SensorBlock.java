package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions.IBlockPositionData;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.blocktype.BlockTypeView;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import com.hypixel.hytale.server.npc.corecomponents.BlockTarget;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorBlock;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class SensorBlock extends SensorBase {
   protected final double range;
   protected final double yRange;
   protected final int blockSet;
   protected final boolean pickRandom;
   protected final boolean reserveBlock;
   protected final PositionProvider positionProvider = new PositionProvider();

   public SensorBlock(@Nonnull BuilderSensorBlock builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.range = builder.getRange(support);
      this.yRange = builder.getYRange(support);
      this.blockSet = builder.getBlockSet(support);
      this.pickRandom = builder.isPickRandom(support);
      this.reserveBlock = builder.isReserveBlock(support);
      support.requireBlockTypeBlackboard(this.blockSet);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         this.positionProvider.clear();
         return false;
      } else {
         World world = store.getExternalData().getWorld();
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d entityPos = transformComponent.getPosition();
         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         BlockTarget target = role.getWorldSupport().getCachedBlockTarget(this.blockSet);
         Vector3d position = target.getPosition();
         if (!position.equals(Vector3d.MIN)) {
            WorldChunk targetChunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(position.x, position.z));
            if (targetChunk != null) {
               BlockSection section = targetChunk.getBlockChunk().getSectionAtBlockY(MathUtil.floor(position.y));
               if (section.getLocalChangeCounter() == target.getChunkChangeRevision()
                  || section.get(MathUtil.floor(position.x), MathUtil.floor(position.y), MathUtil.floor(position.z)) == target.getFoundBlockType()) {
                  if (!(Math.abs(entityPos.y - position.y) > this.yRange) && !(entityPos.distanceSquaredTo(position) > this.range * this.range)) {
                     this.positionProvider.setTarget(position);
                     return true;
                  } else {
                     this.positionProvider.clear();
                     return false;
                  }
               }
            }
         }

         if (target.isActive()) {
            target.reset(npcComponent);
         }

         BlockTypeView blackboard = npcComponent.getBlockTypeBlackboardView(ref, store);
         IBlockPositionData blockData = blackboard.findBlock(this.blockSet, this.range, this.yRange, this.pickRandom, ref, store);
         if (blockData == null) {
            this.positionProvider.clear();
            return false;
         } else {
            position.assign(blockData.getXCentre(), blockData.getYCentre(), blockData.getZCentre());
            int blockTypeId = blockData.getBlockType();
            target.setFoundBlockType(blockTypeId);
            target.setChunkChangeRevision(blockData.getChunkSection().getLocalChangeCounter());
            BlockType blockType = BlockType.getAssetMap().getAsset(blockTypeId);
            if (this.reserveBlock || !blockType.isAllowsMultipleUsers()) {
               ResourceView resourceView = store.getResource(Blackboard.getResourceType())
                  .getView(ResourceView.class, ResourceView.indexViewFromWorldPosition(position));
               resourceView.reserveBlock(npcComponent, blockData.getX(), blockData.getY(), blockData.getZ());
               target.setReservationHolder(resourceView);
               Blackboard.LOGGER.at(Level.FINE).log("Entity %s reserved block from set %s at %s", npcComponent.getRoleName(), this.blockSet, position);
            }

            Blackboard.LOGGER.at(Level.FINE).log("Entity %s found block from set %s at %s", npcComponent.getRoleName(), this.blockSet, position);
            this.positionProvider.setTarget(position);
            return true;
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
