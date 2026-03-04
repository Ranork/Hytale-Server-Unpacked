package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.spatial.KDTree;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.collision.commands.HitboxCommand;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CollisionModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(CollisionModule.class).build();
   public static final int VALIDATE_INVALID = -1;
   public static final int VALIDATE_OK = 0;
   public static final int VALIDATE_ON_GROUND = 1;
   public static final int VALIDATE_TOUCH_CEIL = 2;
   private static CollisionModule instance;
   private ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> tangibleEntitySpatialResourceType;
   private double extentMax;
   private double minimumThickness;
   @Nonnull
   private final Config<CollisionModuleConfig> config = this.withConfig("CollisionModule", CollisionModuleConfig.CODEC);

   public static CollisionModule get() {
      return instance;
   }

   public CollisionModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Nonnull
   public CollisionModuleConfig getConfig() {
      return this.config.get();
   }

   @Override
   protected void setup() {
      this.getCommandRegistry().registerCommand(new HitboxCommand());
      this.getEventRegistry().register(LoadedAssetsEvent.class, BlockBoundingBoxes.class, this::onLoadedAssetsEvent);
      this.tangibleEntitySpatialResourceType = this.getEntityStoreRegistry().registerSpatialResource(() -> new KDTree<>(Ref::isValid));
   }

   public ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> getTangibleEntitySpatialResourceType() {
      return this.tangibleEntitySpatialResourceType;
   }

   private void onLoadedAssetsEvent(@Nonnull LoadedAssetsEvent<String, BlockBoundingBoxes, IndexedLookupTableAssetMap<String, BlockBoundingBoxes>> event) {
      if (event.isInitial()) {
         this.extentMax = 0.0;
         this.minimumThickness = Double.MAX_VALUE;
      }

      for (BlockBoundingBoxes box : event.getLoadedAssets().values()) {
         this.handleLoadedHitbox(box);
      }

      CollisionModuleConfig config = this.config.get();
      if (config.hasMinimumThickness()) {
         this.minimumThickness = config.getMinimumThickness();
      }

      this.getLogger().at(Level.INFO).log("Block extents for CollisionSystem is Max=" + this.extentMax + ", Min=" + this.minimumThickness);
   }

   private void handleLoadedHitbox(@Nonnull BlockBoundingBoxes box) {
      BlockBoundingBoxes.RotatedVariantBoxes defaultBox = box.get(Rotation.None, Rotation.None, Rotation.None);
      double maximumExtent = defaultBox.getBoundingBox().getMaximumExtent();
      double blockExtent = 0.0;
      if (maximumExtent > blockExtent) {
         blockExtent = maximumExtent;
      }

      if (blockExtent > 1.0) {
         this.getLogger()
            .at(Level.FINE)
            .log("Block Hitbox %s protrudes more than 1 unit (%s units) out of standard block and degrades performance", box.getId(), blockExtent);
      }

      if (blockExtent > this.extentMax) {
         this.extentMax = blockExtent;
      }

      double thickness;
      if (defaultBox.hasDetailBoxes()) {
         thickness = Double.MAX_VALUE;

         for (Box boundingBox : defaultBox.getDetailBoxes()) {
            thickness = Math.min(thickness, boundingBox.getThickness());
         }
      } else {
         thickness = defaultBox.getBoundingBox().getThickness();
      }

      if (thickness < 0.0) {
         this.getLogger().at(Level.SEVERE).log("Hitbox for " + box.getId() + " has a negative size!");
      } else {
         if (thickness < this.minimumThickness) {
            this.minimumThickness = thickness;
         }
      }
   }

   public static boolean findCollisions(
      @Nonnull Box collider,
      @Nonnull Vector3d pos,
      @Nonnull Vector3d v,
      @Nonnull CollisionResult result,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return findCollisions(collider, pos, v, true, result, componentAccessor);
   }

   public static boolean findCollisions(
      @Nonnull Box collider,
      @Nonnull Vector3d pos,
      @Nonnull Vector3d v,
      boolean stopOnCollisionFound,
      @Nonnull CollisionResult result,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      result.reset();
      boolean isFarDistance = !isBelowMovementThreshold(v);
      if (isFarDistance) {
         findBlockCollisionsIterative(world, collider, pos, v, stopOnCollisionFound, result);
      } else {
         findBlockCollisionsShortDistance(world, collider, pos, v, result);
      }

      if (result.isCheckingForCharacterCollisions()) {
         findCharacterCollisions(pos, v, result, componentAccessor);
      }

      result.process();
      return isFarDistance;
   }

   public static void findBlockCollisionsIterative(
      @Nonnull World world, @Nonnull Box collider, @Nonnull Vector3d pos, @Nonnull Vector3d v, boolean stopOnCollisionFound, @Nonnull CollisionResult result
   ) {
      if (result.shouldLog()) {
         result.getLogger()
            .at(Level.INFO)
            .log(
               ">>>>>> Start findBlockCollisionIterative collider=[%s] pos=%s dir=%s", collider, Vector3d.formatShortString(pos), Vector3d.formatShortString(v)
            );
      }

      CollisionConfig coll = result.getConfig();
      coll.setWorld(world);
      result.getMovingBoxBoxCollision().setCollider(collider).setMove(pos, v);
      if (result.shouldLog()) {
         result.getLogger().at(Level.INFO).log(">>>>>> Start collider=[%s] + offset[%s]", collider, v);
      }

      result.acquireCollisionModule();
      collider.forEachBlock(pos, 1.0E-5, result, (x, y, z, aResult) -> aResult.accept(x, y, z));
      if (result.shouldLog()) {
         result.getLogger().at(Level.INFO).log(">>>> line collider=[%s] dir=%s len=%s", collider, Vector3d.formatShortString(v), v.length());
      }

      result.iterateBlocks(collider, pos, v, v.length(), stopOnCollisionFound);
      coll.clear();
   }

   public static void findCharacterCollisions(
      @Nonnull Vector3d pos, @Nonnull Vector3d v, @Nonnull CollisionResult result, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!isBelowMovementThreshold(v)) {
         Vector3d coll = new Vector3d();
         Vector2d minMax = new Vector2d();
         List<Entity> collisionEntities = result.getCollisionEntities();

         for (int i = 0; i < collisionEntities.size(); i++) {
            Entity entity = collisionEntities.get(i);
            Ref<EntityStore> ref = entity.getReference();

            assert ref != null;

            Archetype<EntityStore> archetype = componentAccessor.getArchetype(ref);
            boolean isProjectile = archetype.contains(Projectile.getComponentType()) || archetype.contains(ProjectileComponent.getComponentType());
            if (!isProjectile) {
               if (archetype.contains(DeathComponent.getComponentType())) {
                  return;
               }

               TransformComponent entityTransformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
               if (entityTransformComponent != null) {
                  BoundingBox entityBoundingBoxComponent = componentAccessor.getComponent(ref, BoundingBox.getComponentType());
                  if (entityBoundingBoxComponent != null) {
                     Vector3d position = entityTransformComponent.getPosition();
                     Box boundingBox = entityBoundingBoxComponent.getBoundingBox();
                     if (boundingBox != null
                        && CollisionMath.intersectVectorAABB(pos, v, position.getX(), position.getY(), position.getZ(), boundingBox, minMax)) {
                        coll.assign(pos).addScaled(v, minMax.x);
                        result.allocCharacterCollision().assign(coll, minMax.x, entity.getReference(), entity instanceof Player);
                     }
                  }
               }
            }
         }
      }
   }

   public static void findBlockCollisionsShortDistance(
      @Nonnull World world, @Nonnull Box collider, @Nonnull Vector3d pos, @Nonnull Vector3d v, @Nonnull CollisionResult result
   ) {
      result.reset();
      result.getConfig().setWorld(world);
      result.getConfig().extraData1 = pos;
      BoxBlockIntersectionEvaluator boxBlockIntersectionEvaluator = result.getBoxBlockIntersection();
      boxBlockIntersectionEvaluator.setBox(collider, pos).offsetPosition(v);
      collider.forEachBlock(
         pos.x + v.x,
         pos.y + v.y,
         pos.z + v.z,
         1.0E-5,
         result,
         (x, y, z, aResult) -> {
            CollisionConfig coll = aResult.getConfig();
            if (!coll.canCollide(x, y, z)) {
               if (aResult.shouldLog()) {
                  String name = coll.blockType != null ? coll.blockType.getId().toString() : "null";
                  aResult.getLogger().at(Level.INFO).log("-- Short: Ignoring block at %s/%s/%s blockType=%s", x, y, z, name);
               }

               return true;
            } else {
               Vector3d _pos = (Vector3d)coll.extraData1;
               if (coll.blockId == Integer.MIN_VALUE) {
                  addImmediateCollision(_pos, aResult, coll, 0);
                  if (aResult.shouldLog()) {
                     aResult.getLogger().at(Level.INFO).log("-- Short: Stopping with invalid block at %s/%s/%s blockType=<invalid>", x, y, z);
                  }

                  return true;
               } else {
                  int boundingBoxX = x + coll.getBoundingBoxOffsetX();
                  int boundingBoxY = y + coll.getBoundingBoxOffsetY();
                  int boundingBoxZ = z + coll.getBoundingBoxOffsetZ();
                  int numDetails = coll.getDetailCount();
                  BoxBlockIntersectionEvaluator blockBox = aResult.getBoxBlockIntersection();
                  int code = blockBox.intersectBoxComputeTouch(coll.getBoundingBox(), boundingBoxX, boundingBoxY, boundingBoxZ);
                  boolean haveCollision = !CollisionMath.isDisjoint(code);
                  if (aResult.shouldLog()) {
                     String name = coll.blockType != null ? coll.blockType.getId().toString() : "null";
                     aResult.getLogger()
                        .at(Level.INFO)
                        .log(
                           "?? Block Test at %s/%s/%s numDet=%d haveColl=%s overlap=%s blockType=%s",
                           x,
                           y,
                           z,
                           numDetails,
                           haveCollision,
                           aResult.getBoxBlockIntersection().isOverlapping(),
                           name
                        );
                  }

                  if (numDetails <= 1) {
                     processCollision(aResult, _pos, blockBox, haveCollision, 0);
                  } else {
                     for (int i = 0; i < numDetails; i++) {
                        code = blockBox.intersectBoxComputeTouch(coll.getBoundingBox(i), boundingBoxX, boundingBoxY, boundingBoxZ);
                        haveCollision = !CollisionMath.isDisjoint(code);
                        processCollision(aResult, _pos, blockBox, haveCollision, i);
                     }
                  }

                  return true;
               }
            }
         }
      );
      result.getConfig().clear();
   }

   protected static void processCollision(
      @Nonnull CollisionResult result,
      @Nonnull Vector3d pos,
      @Nonnull BoxBlockIntersectionEvaluator boxBlockIntersectionEvaluator,
      boolean haveCollision,
      int hitboxIndex
   ) {
      CollisionConfig coll = result.getConfig();
      Predicate<CollisionConfig> isWalkable = coll.getBlockCollisionPredicate();
      if (result.shouldLog()) {
         result.getLogger()
            .at(Level.INFO)
            .log(
               "?? Short: Further testing block haveCol=%s hitBoxIndex=%s onGround=%s touching=%s canCollide=%s canTrigger=%s",
               haveCollision,
               hitboxIndex,
               boxBlockIntersectionEvaluator.isOnGround(),
               boxBlockIntersectionEvaluator.isTouching(),
               coll.blockCanCollide,
               coll.blockCanTrigger
            );
      }

      if (boxBlockIntersectionEvaluator.isOnGround() && coll.blockCanCollide) {
         haveCollision = coll.blockType == null || !isWalkable.test(coll);
         if (!haveCollision) {
            result.addSlide(boxBlockIntersectionEvaluator, hitboxIndex);
            if (coll.blockCanTrigger) {
               result.addTrigger(boxBlockIntersectionEvaluator, hitboxIndex);
            }

            if (result.shouldLog()) {
               result.getLogger()
                  .at(Level.INFO)
                  .log(
                     "++ Short: Sliding block start=%s end=%s normal=%s",
                     boxBlockIntersectionEvaluator.getCollisionStart(),
                     boxBlockIntersectionEvaluator.getCollisionEnd(),
                     Vector3d.formatShortString(boxBlockIntersectionEvaluator.getCollisionNormal())
                  );
            }

            return;
         }

         if (result.shouldLog()) {
            result.getLogger()
               .at(Level.INFO)
               .log(
                  "?? Short: Sliding block is unwalkable start=%s end=%s normal=%s",
                  boxBlockIntersectionEvaluator.getCollisionStart(),
                  boxBlockIntersectionEvaluator.getCollisionEnd(),
                  Vector3d.formatShortString(boxBlockIntersectionEvaluator.getCollisionNormal())
               );
         }
      }

      if (haveCollision && coll.blockCanCollide) {
         addImmediateCollision(pos, result, coll, hitboxIndex);
         if (result.shouldLog()) {
            result.getLogger()
               .at(Level.INFO)
               .log(
                  "++ Short: Collision with block start=%s end=%s normal=%s",
                  boxBlockIntersectionEvaluator.getCollisionStart(),
                  boxBlockIntersectionEvaluator.getCollisionEnd(),
                  Vector3d.formatShortString(boxBlockIntersectionEvaluator.getCollisionNormal())
               );
         }
      }

      if (coll.blockCanTrigger && (haveCollision || boxBlockIntersectionEvaluator.isTouching())) {
         if (result.shouldLog()) {
            result.getLogger()
               .at(Level.INFO)
               .log(
                  "++ Short: Trigger block start=%s end=%s normal=%s",
                  boxBlockIntersectionEvaluator.getCollisionStart(),
                  boxBlockIntersectionEvaluator.getCollisionEnd(),
                  Vector3d.formatShortString(boxBlockIntersectionEvaluator.getCollisionNormal())
               );
         }

         result.addTrigger(boxBlockIntersectionEvaluator, hitboxIndex);
      }
   }

   public void findIntersections(
      @Nonnull World world, @Nonnull Box collider, @Nonnull Vector3d pos, @Nonnull CollisionResult result, boolean triggerBlocks, boolean intersections
   ) {
      if (!this.isDisabled()) {
         result.reset();
         result.getConfig().setWorld(world);
         result.getConfig().extraData1 = triggerBlocks;
         result.getConfig().extraData2 = intersections;
         result.getBoxBlockIntersection().setBox(collider, pos).expandBox(0.01);
         result.getBoxBlockIntersection().box.forEachBlock(pos, 1.0E-5, result, (x, y, z, aResult) -> {
            CollisionConfig coll = aResult.getConfig();
            if (!coll.canCollide(x, y, z)) {
               return true;
            } else {
               int boundingBoxX = x + coll.getBoundingBoxOffsetX();
               int boundingBoxY = y + coll.getBoundingBoxOffsetY();
               int boundingBoxZ = z + coll.getBoundingBoxOffsetZ();
               if (!aResult.getBoxBlockIntersection().isBoxIntersecting(coll.getBoundingBox(), boundingBoxX, boundingBoxY, boundingBoxZ)) {
                  return true;
               } else {
                  boolean _triggerBlocks = (Boolean)coll.extraData1;
                  boolean _intersections = (Boolean)coll.extraData2;
                  int numDetails = coll.getDetailCount();
                  if (numDetails <= 1) {
                     if (_triggerBlocks && coll.blockCanTrigger) {
                        aResult.addTrigger(aResult.getBoxBlockIntersection(), 0);
                     }

                     if (_intersections) {
                        aResult.addCollision(aResult.getBoxBlockIntersection(), 0);
                     }
                  } else {
                     for (int i = 0; i < numDetails; i++) {
                        if (aResult.getBoxBlockIntersection().isBoxIntersecting(coll.getBoundingBox(i), boundingBoxX, boundingBoxY, boundingBoxZ)) {
                           if (_triggerBlocks && coll.blockCanTrigger) {
                              aResult.addTrigger(aResult.getBoxBlockIntersection(), i);
                           }

                           if (_intersections) {
                              aResult.addCollision(aResult.getBoxBlockIntersection(), i);
                           }
                        }
                     }
                  }

                  return true;
               }
            }
         });
         result.getConfig().clear();
      }
   }

   public int validatePosition(@Nonnull World world, @Nonnull Box collider, @Nonnull Vector3d pos, @Nonnull CollisionResult result) {
      return this.isDisabled()
         ? 0
         : this.validatePosition(world, collider, pos, null, (_this, collisionCode, collision, collisionConfig) -> true, false, result);
   }

   public <T> int validatePosition(
      @Nonnull World world,
      @Nonnull Box collider,
      @Nonnull Vector3d pos,
      int invalidBlockMaterials,
      @Nullable T t,
      @Nonnull CollisionFilter<BoxBlockIntersectionEvaluator, T> predicate,
      @Nonnull CollisionResult result
   ) {
      if (this.isDisabled()) {
         return 0;
      } else {
         int savedCollisionState = result.getCollisionByMaterial();
         result.setCollisionByMaterial(invalidBlockMaterials);
         int code = this.validatePosition(world, collider, pos, t, predicate, (invalidBlockMaterials & 16) == 0, result);
         result.setCollisionByMaterial(savedCollisionState);
         return code;
      }
   }

   private <T> int validatePosition(
      @Nonnull World world,
      @Nonnull Box collider,
      @Nonnull Vector3d pos,
      @Nullable T t,
      @Nullable CollisionFilter<BoxBlockIntersectionEvaluator, T> predicate,
      boolean disableDamageBlocks,
      @Nonnull CollisionResult result
   ) {
      CollisionModuleConfig config = this.config.get();
      result.getConfig().setWorld(world);
      result.getConfig().dumpInvalidBlocks = config.isDumpInvalidBlocks();
      result.getConfig().extraData1 = t;
      result.getConfig().extraData2 = predicate;
      result.getBoxBlockIntersection().setBox(collider, pos);
      boolean saveCheckTriggerState = result.isCheckingTriggerBlocks();
      boolean saveCheckDamageBlock = result.isCheckingDamageBlocks();
      result.disableTriggerBlocks();
      if (disableDamageBlocks) {
         result.disableDamageBlocks();
      }

      result.validate = 0;
      collider.forEachBlock(pos, 1.0E-5, result, (x, y, z, aResult) -> {
         CollisionConfig coll = aResult.getConfig();
         if (!coll.canCollide(x, y, z)) {
            return true;
         } else {
            BoxBlockIntersectionEvaluator boxBlockIntersection = aResult.getBoxBlockIntersection();
            int boundingBoxX = x + coll.getBoundingBoxOffsetX();
            int boundingBoxY = y + coll.getBoundingBoxOffsetY();
            int boundingBoxZ = z + coll.getBoundingBoxOffsetZ();
            int code = boxBlockIntersection.intersectBoxComputeOnGround(coll.getBoundingBox(), boundingBoxX, boundingBoxY, boundingBoxZ);
            if (coll.blockId == Integer.MIN_VALUE) {
               if (CollisionMath.isOverlapping(code)) {
                  aResult.validate = -1;
                  return false;
               } else {
                  return true;
               }
            } else if (CollisionMath.isDisjoint(code)) {
               return true;
            } else {
               Box _collider = boxBlockIntersection.box;
               Vector3d _pos = boxBlockIntersection.collisionPoint;
               Object _t = coll.extraData1;
               CollisionFilter<BoxBlockIntersectionEvaluator, Object> _predicate = (CollisionFilter<BoxBlockIntersectionEvaluator, Object>)coll.extraData2;
               int numDetails = coll.getDetailCount();
               if (numDetails <= 1) {
                  if (!_predicate.test(_t, code, boxBlockIntersection, coll)) {
                     return true;
                  }

                  if (CollisionMath.isOverlapping(code)) {
                     if (coll.dumpInvalidBlocks) {
                        logOverlap(_pos, _collider, coll, coll.getBoundingBox(), x, y, z, 0, code);
                     }

                     aResult.validate = -1;
                     return false;
                  }

                  if (boxBlockIntersection.isOnGround()) {
                     aResult.validate |= 1;
                  }

                  if (boxBlockIntersection.touchesCeil()) {
                     aResult.validate |= 2;
                  }
               } else {
                  for (int i = 0; i < numDetails; i++) {
                     code = boxBlockIntersection.intersectBoxComputeOnGround(coll.getBoundingBox(i), boundingBoxX, boundingBoxY, boundingBoxZ);
                     if (!CollisionMath.isDisjoint(code) && _predicate.test(_t, code, boxBlockIntersection, coll)) {
                        if (CollisionMath.isOverlapping(code)) {
                           if (coll.dumpInvalidBlocks) {
                              logOverlap(_pos, _collider, coll, coll.getBoundingBox(i), x, y, z, i, code);
                           }

                           aResult.validate = -1;
                           return false;
                        }

                        if (boxBlockIntersection.isOnGround()) {
                           aResult.validate |= 1;
                        }

                        if (boxBlockIntersection.touchesCeil()) {
                           aResult.validate |= 2;
                        }
                     }
                  }
               }

               return true;
            }
         }
      });
      if (saveCheckTriggerState) {
         result.enableTriggerBlocks();
      }

      if (saveCheckDamageBlock) {
         result.enableDamageBlocks();
      }

      result.getConfig().clear();
      return result.validate;
   }

   private static void addImmediateCollision(@Nonnull Vector3d pos, @Nonnull CollisionResult result, @Nonnull CollisionConfig config, int i) {
      BlockCollisionData data = result.newCollision();
      data.setStart(pos, 0.0);
      data.setEnd(1.0, result.getBoxBlockIntersection().getCollisionNormal());
      data.setBlockData(config);
      data.setDetailBoxIndex(i);
      data.setTouchingOverlapping(false, true);
   }

   public static boolean isBelowMovementThreshold(@Nonnull Vector3d v) {
      return v.squaredLength() < 1.0000000000000002E-10;
   }

   private static void logOverlap(
      @Nonnull Vector3d pos, @Nonnull Box collider, @Nonnull CollisionConfig config, @Nonnull Box hitBox, int x, int y, int z, int index, int intersectType
   ) {
      get()
         .getLogger()
         .at(Level.WARNING)
         .log(
            "Overlapping blocks - code=%s%s%s index=%s pos=%s loc=%s/%s/%s id=%s mat=%s name=%s box=%s hitbox=%s|%s",
            (intersectType & 8) != 0 ? "X" : "",
            (intersectType & 16) != 0 ? "Y" : "",
            (intersectType & 32) != 0 ? "Z" : "",
            index,
            Vector3d.formatShortString(pos),
            x + config.getBoundingBoxOffsetX(),
            y + config.getBoundingBoxOffsetY(),
            z + config.getBoundingBoxOffsetZ(),
            config.blockId,
            config.blockMaterial != null ? config.blockMaterial.name() : "none",
            new Object[]{
               config.blockType != null ? config.blockType.getId() : "none",
               collider,
               Vector3d.formatShortString(hitBox.min),
               Vector3d.formatShortString(hitBox.max)
            }
         );
   }
}
