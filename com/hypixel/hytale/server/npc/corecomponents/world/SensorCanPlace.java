package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorCanPlace;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.CachedPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.BlockPlacementHelper;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class SensorCanPlace extends SensorBase {
   protected static final ComponentType<EntityStore, BoundingBox> BOUNDING_BOX_COMPONENT_TYPE = BoundingBox.getComponentType();
   protected final SensorCanPlace.Direction direction;
   protected final SensorCanPlace.Offset offset;
   protected final double retryDelay;
   protected final boolean allowEmptyMaterials;
   protected final Vector3d transform = new Vector3d();
   protected final CachedPositionProvider positionProvider = new CachedPositionProvider();
   protected final Vector3d cachedPosition = new Vector3d();
   protected boolean cachedResult;
   protected double delay;

   public SensorCanPlace(@Nonnull BuilderSensorCanPlace builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.direction = builder.getDirection(support);
      this.offset = builder.getOffset(support);
      this.retryDelay = builder.getRetryDelay(support);
      this.allowEmptyMaterials = builder.isAllowEmptyMaterials(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (super.matches(ref, role, dt, store) && role.getWorldSupport().getBlockToPlace() != null) {
         BlockType placedBlockType = BlockType.getAssetMap().getAsset(role.getWorldSupport().getBlockToPlace());
         if (placedBlockType == null) {
            this.positionProvider.clear();
            return false;
         } else {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            World world = store.getExternalData().getWorld();
            float yaw = transformComponent.getRotation().getYaw();
            float piQuarter = (float) (Math.PI / 4);
            yaw = Math.round(yaw / piQuarter) * piQuarter;
            this.direction.apply(this.transform, yaw);
            BoundingBox boundingBoxComponent = store.getComponent(ref, BOUNDING_BOX_COMPONENT_TYPE);
            if (boundingBoxComponent != null) {
               Box boundingBox = boundingBoxComponent.getBoundingBox();
               Box blockBox = BlockBoundingBoxes.getAssetMap().getAsset(placedBlockType.getHitboxTypeIndex()).get(0).getBoundingBox();
               boolean xNegative = this.transform.x < 0.0;
               boolean zNegative = this.transform.z < 0.0;
               boolean xPositive = this.transform.x - 1.0E-5 > 0.0;
               boolean zPositive = this.transform.z - 1.0E-5 > 0.0;
               double npcX = xNegative ? boundingBox.min.x : boundingBox.max.x;
               double npcZ = zNegative ? boundingBox.min.z : boundingBox.max.z;
               double blockX = xNegative ? blockBox.max.x : blockBox.min.x;
               double blockZ = zNegative ? blockBox.max.z : blockBox.min.z;
               double transformX = xNegative ? -this.transform.x : this.transform.x;
               double transformZ = zNegative ? -this.transform.z : this.transform.z;
               double magnitude = Math.sqrt(npcX * npcX * transformX + npcZ * npcZ * transformZ)
                  + Math.sqrt(blockX * blockX * transformX + blockZ * blockZ * transformZ);
               this.transform.setLength(magnitude);
               this.transform.add(xPositive ? 1.0 : 0.0, 0.0, zPositive ? 1.0 : 0.0);
            }

            this.offset.apply(this.transform);
            Vector3d position = transformComponent.getPosition();
            this.transform.add(position.getX(), position.getY(), position.getZ()).floor();
            int x = (int)this.transform.getX();
            int y = (int)this.transform.getY();
            int z = (int)this.transform.getZ();
            if (!this.cachedPosition.equals(this.transform)) {
               this.delay = 0.0;
            }

            boolean canPlaceUnitBlock = BlockPlacementHelper.canPlaceUnitBlock(world, placedBlockType, this.allowEmptyMaterials, x, y, z);
            if (canPlaceUnitBlock != this.cachedResult) {
               this.delay = 0.0;
            }

            if (!((this.delay -= dt) > 0.0)) {
               this.cachedResult = canPlaceUnitBlock && BlockPlacementHelper.canPlaceBlock(world, placedBlockType, 0, this.allowEmptyMaterials, x, y, z);
               this.cachedPosition.assign(this.transform);
               this.delay = this.retryDelay;
               if (!this.cachedResult) {
                  this.positionProvider.clear();
                  return false;
               } else {
                  this.positionProvider.setIsFromCache(false);
                  this.positionProvider.setTarget(this.transform);
                  return true;
               }
            } else if (this.cachedResult) {
               this.positionProvider.setIsFromCache(true);
               this.positionProvider.setTarget(this.transform);
               return true;
            } else {
               this.positionProvider.clear();
               return false;
            }
         }
      } else {
         this.positionProvider.clear();
         return false;
      }
   }

   @Override
   public void clearOnce() {
      super.clearOnce();
      this.delay = 0.0;
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }

   public static enum Direction implements Supplier<String> {
      Forward(Vector3d.FORWARD),
      Backward(Vector3d.BACKWARD),
      Left(Vector3d.LEFT),
      Right(Vector3d.RIGHT);

      private final Vector3d direction;

      private Direction(Vector3d direction) {
         this.direction = direction;
      }

      @Nonnull
      public Vector3d apply(@Nonnull Vector3d target, float rotation) {
         return target.assign(this.direction).rotateY(rotation);
      }

      @Nonnull
      public String get() {
         return this.name();
      }
   }

   public static enum Offset implements Supplier<String> {
      HeadPosition(Vector3d.UP),
      BodyPosition(Vector3d.ZERO),
      FootPosition(Vector3d.DOWN);

      private final Vector3d offset;

      private Offset(Vector3d offset) {
         this.offset = offset;
      }

      @Nonnull
      public Vector3d apply(@Nonnull Vector3d target) {
         return target.add(this.offset);
      }

      @Nonnull
      public String get() {
         return this.name();
      }
   }
}
