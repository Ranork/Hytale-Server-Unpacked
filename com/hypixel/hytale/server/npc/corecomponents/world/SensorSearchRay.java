package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorSearchRay;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.hypixel.hytale.server.npc.util.RayBlockHitTest;
import javax.annotation.Nonnull;

public class SensorSearchRay extends SensorBase {
   protected final int id;
   protected final float angle;
   protected final double range;
   protected final int blockSet;
   protected final float minRetestAngle;
   protected final double minRetestMoveSquared;
   protected final double throttleTime;
   protected final PositionProvider positionProvider = new PositionProvider();
   protected final Vector3d lastCheckedPosition = new Vector3d();
   protected float lastCheckedYaw = Float.MAX_VALUE;
   protected short lastBlockRevision;
   protected double throttleTimeRemaining;

   public SensorSearchRay(@Nonnull BuilderSensorSearchRay builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.id = builder.getId(support);
      this.angle = -builder.getAngle(support);
      this.range = builder.getRange(support);
      this.blockSet = builder.getBlockSet(support);
      this.minRetestAngle = builder.getMinRetestAngle(support);
      double minRetestMove = builder.getMinRetestMove(support);
      this.minRetestMoveSquared = minRetestMove * minRetestMove;
      this.throttleTime = builder.getThrottleTime(support);
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

         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3d position = transformComponent.getPosition();
         Vector3f headRotation = headRotationComponent.getRotation();
         Vector3d cachedPosition = role.getWorldSupport().getCachedSearchRayPosition(this.id);
         if (!cachedPosition.equals(Vector3d.MIN)) {
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(cachedPosition.x, cachedPosition.z));
            if (chunk != null) {
               BlockSection section = chunk.getBlockChunk().getSectionAtBlockY(MathUtil.floor(cachedPosition.y));
               if (section.getLocalChangeCounter() == this.lastBlockRevision) {
                  this.positionProvider.setTarget(cachedPosition);
                  return true;
               }

               cachedPosition.assign(Vector3d.MIN);
               this.positionProvider.clear();
            }
         } else if ((this.throttleTimeRemaining -= dt) > 0.0
            && Math.abs(headRotation.getYaw() - this.lastCheckedYaw) <= this.minRetestAngle
            && position.distanceSquaredTo(this.lastCheckedPosition) <= this.minRetestMoveSquared) {
            this.positionProvider.clear();
            return false;
         }

         RayBlockHitTest blockRaySearch = RayBlockHitTest.THREAD_LOCAL.get();
         if (!blockRaySearch.init(ref, this.blockSet, this.angle, store)) {
            cachedPosition.assign(Vector3d.MIN);
            this.positionProvider.clear();
            blockRaySearch.clear();
            return false;
         } else {
            this.lastCheckedPosition.assign(position);
            this.lastCheckedYaw = headRotation.getYaw();
            this.throttleTimeRemaining = this.throttleTime;
            boolean result = blockRaySearch.run(this.range);
            if (result) {
               this.lastBlockRevision = blockRaySearch.getLastBlockRevision();
               Vector3d targetPosition = blockRaySearch.getHitPosition();
               cachedPosition.assign(targetPosition.x + 0.5, targetPosition.y + 0.5, targetPosition.z + 0.5);
               this.positionProvider.setTarget(cachedPosition);
            } else {
               cachedPosition.assign(Vector3d.MIN);
               this.positionProvider.clear();
            }

            blockRaySearch.clear();
            return result;
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
