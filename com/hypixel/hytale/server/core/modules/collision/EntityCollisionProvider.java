package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityCollisionProvider {
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
   protected BiPredicate<Ref<EntityStore>, ComponentAccessor<EntityStore>> entityFilter;
   @Nullable
   protected Ref<EntityStore> ignoreSelf;
   @Nullable
   protected Ref<EntityStore> ignoreOther;

   public EntityCollisionProvider() {
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
   public EntityContactData getContact(int index) {
      return this.contacts[index];
   }

   public void clear() {
      for (int i = 0; i < this.count; i++) {
         this.contacts[i].clear();
      }

      this.count = 0;
   }

   public double computeNearest(
      @Nonnull Box entityBoundingBox,
      @Nonnull Vector3d pos,
      @Nonnull Vector3d dir,
      @Nullable Ref<EntityStore> ignoreSelf,
      @Nullable Ref<EntityStore> ignore,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.computeNearest(
         pos, dir, entityBoundingBox, dir.length() + 8.0, EntityCollisionProvider::defaultEntityFilter, ignoreSelf, ignore, componentAccessor
      );
   }

   public double computeNearest(
      @Nonnull Vector3d pos,
      @Nonnull Vector3d dir,
      @Nonnull Box boundingBox,
      double radius,
      @Nonnull BiPredicate<Ref<EntityStore>, ComponentAccessor<EntityStore>> entityFilter,
      @Nullable Ref<EntityStore> ignoreSelf,
      @Nullable Ref<EntityStore> ignoreOther,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.ignoreSelf = ignoreSelf;
      this.ignoreOther = ignoreOther;
      this.nearestCollisionStart = Double.MAX_VALUE;
      this.entityFilter = entityFilter;
      this.iterateEntitiesInSphere(
         pos,
         dir,
         boundingBox,
         radius,
         (ref, _this) -> acceptNearestIgnore(ref, _this, componentAccessor),
         (ref, _this1) -> acceptNearestIgnore(ref, _this1, componentAccessor),
         componentAccessor
      );
      if (this.count == 0) {
         this.nearestCollisionStart = -Double.MAX_VALUE;
      }

      this.clearRefs();
      this.ignoreSelf = null;
      this.ignoreOther = null;
      return this.nearestCollisionStart;
   }

   protected void iterateEntitiesInSphere(
      @Nonnull Vector3d pos,
      @Nonnull Vector3d dir,
      @Nonnull Box boundingBox,
      double radius,
      @Nonnull BiConsumer<Ref<EntityStore>, EntityCollisionProvider> consumer,
      @Nonnull BiConsumer<Ref<EntityStore>, EntityCollisionProvider> consumerPlayer,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.position = pos;
      this.direction = dir;
      this.boundingBox = boundingBox;
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = componentAccessor.getResource(EntityModule.get().getEntitySpatialResourceType());
      entitySpatialResource.getSpatialStructure().collect(pos, radius, results);
      ObjectListIterator playerSpatialResource = results.iterator();

      while (playerSpatialResource.hasNext()) {
         Ref<EntityStore> ref = (Ref<EntityStore>)playerSpatialResource.next();
         if (ref.isValid()) {
            consumer.accept(ref, this);
         }
      }

      results.clear();
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResourcex = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
      playerSpatialResourcex.getSpatialStructure().collect(pos, radius, results);
      ObjectListIterator var15 = results.iterator();

      while (var15.hasNext()) {
         Ref<EntityStore> ref = (Ref<EntityStore>)var15.next();
         if (ref.isValid()) {
            consumerPlayer.accept(ref, this);
         }
      }
   }

   protected void setContact(@Nonnull Entity entity) {
      this.collisionPosition.assign(this.position).addScaled(this.direction, this.minMax.x);
      this.contacts[0].assign(this.collisionPosition, this.minMax.x, this.minMax.y, entity.getReference(), null);
      this.count = 1;
   }

   protected boolean isColliding(@Nonnull Ref<EntityStore> ref, @Nonnull Vector2d minMax, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      BoundingBox boundingBoxComponent = componentAccessor.getComponent(ref, BoundingBox.getComponentType());
      if (boundingBoxComponent == null) {
         return false;
      } else {
         Box boundingBox = boundingBoxComponent.getBoundingBox();
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         return CollisionMath.intersectSweptAABBs(this.position, this.direction, this.boundingBox, position, boundingBox, minMax, this.tempBox)
            && minMax.x <= 1.0;
      }
   }

   protected void clearRefs() {
      this.position = null;
      this.direction = null;
      this.boundingBox = null;
      this.entityFilter = null;
   }

   public static boolean defaultEntityFilter(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Archetype<EntityStore> archetype = componentAccessor.getArchetype(ref);
      boolean isProjectile = archetype.contains(Projectile.getComponentType()) || archetype.contains(ProjectileComponent.getComponentType());
      if (isProjectile) {
         return false;
      } else {
         boolean isDead = archetype.contains(DeathComponent.getComponentType());
         return isDead ? false : ref.isValid();
      }
   }

   protected static void acceptNearestIgnore(
      @Nonnull Ref<EntityStore> ref, @Nonnull EntityCollisionProvider collisionProvider, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Entity entity = EntityUtils.getEntity(ref, componentAccessor);
      if (entity != null) {
         if (entity.isCollidable()
            && !ref.equals(collisionProvider.ignoreSelf)
            && !ref.equals(collisionProvider.ignoreOther)
            && (collisionProvider.entityFilter == null || collisionProvider.entityFilter.test(ref, componentAccessor))
            && collisionProvider.isColliding(ref, collisionProvider.minMax, componentAccessor)
            && collisionProvider.minMax.x < collisionProvider.nearestCollisionStart) {
            collisionProvider.nearestCollisionStart = collisionProvider.minMax.x;
            collisionProvider.setContact(entity);
         }
      }
   }
}
