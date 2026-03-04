package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.model.config.DetailBox;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityRefCollisionProvider {
   protected static final int ALLOC_SIZE = 4;
   protected static final double EXTRA_DISTANCE = 8.0;
   protected EntityContactData[] contacts;
   protected EntityContactData[] sortBuffer;
   protected int count;
   protected final Vector2d minMax = new Vector2d();
   protected final Vector3d collisionPosition = new Vector3d();
   protected final Box tempBox = new Box();
   protected double nearestCollisionStart;
   @Nullable
   protected Vector3d position;
   @Nullable
   protected Vector3d direction;
   @Nullable
   protected Box boundingBox;
   @Nullable
   protected BiPredicate<Ref<EntityStore>, CommandBuffer<EntityStore>> entityFilter;
   @Nullable
   protected Ref<EntityStore> ignoreSelf;
   @Nullable
   protected Ref<EntityStore> ignoreOther;
   @Nonnull
   protected List<Ref<EntityStore>> tmpResults = new ObjectArrayList();
   @Nonnull
   protected Vector3d tmpVector = new Vector3d();
   @Nullable
   protected String hitDetail;

   public EntityRefCollisionProvider() {
      this.contacts = new EntityContactData[4];
      this.sortBuffer = new EntityContactData[4];

      for (int i = 0; i < this.contacts.length; i++) {
         this.contacts[i] = new EntityContactData();
      }
   }

   public int getCount() {
      return this.count;
   }

   @Nonnull
   public EntityContactData getContact(int i) {
      return this.contacts[i];
   }

   public void clear() {
      for (int i = 0; i < this.count; i++) {
         this.contacts[i].clear();
      }

      this.count = 0;
   }

   public double computeNearest(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Box entityBoundingBox,
      @Nonnull Vector3d pos,
      @Nonnull Vector3d dir,
      @Nullable Ref<EntityStore> ignoreSelf,
      @Nullable Ref<EntityStore> ignore
   ) {
      return this.computeNearest(
         commandBuffer, pos, dir, entityBoundingBox, dir.length() + 8.0, EntityRefCollisionProvider::defaultEntityFilter, ignoreSelf, ignore
      );
   }

   public double computeNearest(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Vector3d pos,
      @Nonnull Vector3d dir,
      @Nonnull Box boundingBox,
      double radius,
      @Nonnull BiPredicate<Ref<EntityStore>, CommandBuffer<EntityStore>> entityFilter,
      @Nullable Ref<EntityStore> ignoreSelf,
      @Nullable Ref<EntityStore> ignoreOther
   ) {
      this.ignoreSelf = ignoreSelf;
      this.ignoreOther = ignoreOther;
      this.nearestCollisionStart = Double.MAX_VALUE;
      this.entityFilter = entityFilter;
      this.iterateEntitiesInSphere(commandBuffer, pos, dir, boundingBox, radius, EntityRefCollisionProvider::acceptNearestIgnore);
      if (this.count == 0) {
         this.nearestCollisionStart = -Double.MAX_VALUE;
      }

      this.clearRefs();
      this.ignoreSelf = null;
      this.ignoreOther = null;
      return this.nearestCollisionStart;
   }

   protected void iterateEntitiesInSphere(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Vector3d pos,
      @Nonnull Vector3d dir,
      @Nonnull Box boundingBox,
      double radius,
      @Nonnull TriConsumer<EntityRefCollisionProvider, Ref<EntityStore>, CommandBuffer<EntityStore>> consumer
   ) {
      this.position = pos;
      this.direction = dir;
      this.boundingBox = boundingBox;
      SpatialResource<Ref<EntityStore>, EntityStore> tangibleEntitySpatialResourceType = commandBuffer.getResource(
         CollisionModule.get().getTangibleEntitySpatialResourceType()
      );
      this.tmpResults.clear();
      tangibleEntitySpatialResourceType.getSpatialStructure().collect(pos, radius, this.tmpResults);

      for (Ref<EntityStore> result : this.tmpResults) {
         consumer.accept(this, result, commandBuffer);
      }
   }

   protected void setContact(@Nonnull Ref<EntityStore> ref, @Nonnull String detailName) {
      this.collisionPosition.assign(this.position).addScaled(this.direction, this.minMax.x);
      this.contacts[0].assign(this.collisionPosition, this.minMax.x, this.minMax.y, ref, detailName);
      this.count = 1;
   }

   protected boolean isColliding(@Nonnull Ref<EntityStore> ref, @Nonnull Vector2d minMax, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      BoundingBox boundingBoxComponent = commandBuffer.getComponent(ref, BoundingBox.getComponentType());
      if (boundingBoxComponent == null) {
         return false;
      } else {
         TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent == null) {
            return false;
         } else {
            Box entityBoundingBox = boundingBoxComponent.getBoundingBox();
            if (boundingBoxComponent.getDetailBoxes() != null && !boundingBoxComponent.getDetailBoxes().isEmpty()) {
               for (Entry<String, DetailBox[]> e : boundingBoxComponent.getDetailBoxes().entrySet()) {
                  for (DetailBox v : e.getValue()) {
                     this.tmpVector.assign(v.getOffset());
                     this.tmpVector.rotateY(transformComponent.getRotation().getYaw());
                     this.tmpVector.add(transformComponent.getPosition());
                     if (CollisionMath.intersectSweptAABBs(this.position, this.direction, this.boundingBox, this.tmpVector, v.getBox(), minMax, this.tempBox)
                        && minMax.x <= 1.0) {
                        this.hitDetail = e.getKey();
                        return true;
                     }
                  }
               }

               this.hitDetail = null;
               return false;
            } else {
               this.hitDetail = null;
               return CollisionMath.intersectSweptAABBs(
                     this.position, this.direction, this.boundingBox, transformComponent.getPosition(), entityBoundingBox, minMax, this.tempBox
                  )
                  && minMax.x <= 1.0;
            }
         }
      }
   }

   protected void clearRefs() {
      this.position = null;
      this.direction = null;
      this.boundingBox = null;
      this.entityFilter = null;
   }

   public static boolean defaultEntityFilter(@Nonnull Ref<EntityStore> entity, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      if (!entity.isValid()) {
         return false;
      } else {
         Archetype<EntityStore> archetype = commandBuffer.getArchetype(entity);
         if (archetype.contains(Projectile.getComponentType())) {
            return false;
         } else if (archetype.contains(DeathComponent.getComponentType())) {
            return false;
         } else {
            Entity legacy = EntityUtils.getEntity(entity, commandBuffer);
            return legacy == null || legacy.isCollidable();
         }
      }
   }

   protected void acceptNearestIgnore(@Nonnull Ref<EntityStore> entity, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      if (this.entityFilter.test(entity, commandBuffer)
         && !entity.equals(this.ignoreSelf)
         && !entity.equals(this.ignoreOther)
         && this.isColliding(entity, this.minMax, commandBuffer)
         && this.minMax.x < this.nearestCollisionStart) {
         this.nearestCollisionStart = this.minMax.x;
         this.setContact(entity, this.hitDetail);
      }
   }
}
