package com.hypixel.hytale.builtin.path.entities;

import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.builtin.path.path.PatrolPath;
import com.hypixel.hytale.builtin.path.waypoint.IPrefabPathWaypoint;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PatrolPathMarkerEntity extends Entity implements IPrefabPathWaypoint {
   public static final BuilderCodec<PatrolPathMarkerEntity> CODEC = BuilderCodec.builder(
         PatrolPathMarkerEntity.class, PatrolPathMarkerEntity::new, Entity.CODEC
      )
      .append(
         new KeyedCodec<>("PathId", Codec.UUID_BINARY),
         (patrolPathMarkerEntity, uuid) -> patrolPathMarkerEntity.pathId = uuid,
         patrolPathMarkerEntity -> patrolPathMarkerEntity.pathId
      )
      .setVersionRange(5, 5)
      .add()
      .<String>append(
         new KeyedCodec<>("PathName", Codec.STRING),
         (patrolPathMarkerEntity, s) -> patrolPathMarkerEntity.pathName = s,
         patrolPathMarkerEntity -> patrolPathMarkerEntity.pathName
      )
      .setVersionRange(5, 5)
      .add()
      .<String>append(
         new KeyedCodec<>("Path", Codec.STRING),
         (patrolPathMarkerEntity, s) -> patrolPathMarkerEntity.pathName = s,
         patrolPathMarkerEntity -> patrolPathMarkerEntity.pathName
      )
      .setVersionRange(0, 4)
      .add()
      .addField(
         new KeyedCodec<>("PathLength", Codec.INTEGER),
         (patrolPathMarkerEntity, i) -> patrolPathMarkerEntity.tempPathLength = i.shortValue(),
         patrolPathMarkerEntity -> patrolPathMarkerEntity.parentPath != null
            ? patrolPathMarkerEntity.parentPath.length()
            : patrolPathMarkerEntity.tempPathLength
      )
      .addField(
         new KeyedCodec<>("Order", Codec.INTEGER),
         (patrolPathMarkerEntity, i) -> patrolPathMarkerEntity.order = i,
         patrolPathMarkerEntity -> patrolPathMarkerEntity.order
      )
      .addField(
         new KeyedCodec<>("PauseTime", Codec.DOUBLE),
         (patrolPathMarkerEntity, d) -> patrolPathMarkerEntity.pauseTime = d,
         patrolPathMarkerEntity -> patrolPathMarkerEntity.pauseTime
      )
      .addField(
         new KeyedCodec<>("ObsvAngle", Codec.DOUBLE),
         (patrolPathMarkerEntity, d) -> patrolPathMarkerEntity.observationAngle = d.floatValue(),
         patrolPathMarkerEntity -> (double)patrolPathMarkerEntity.observationAngle
      )
      .build();
   @Nullable
   private UUID pathId;
   private String pathName;
   private int order;
   private double pauseTime;
   private float observationAngle;
   private short tempPathLength;
   private IPrefabPath parentPath;

   @Nullable
   public static ComponentType<EntityStore, PatrolPathMarkerEntity> getComponentType() {
      return EntityModule.get().getComponentType(PatrolPathMarkerEntity.class);
   }

   public PatrolPathMarkerEntity() {
   }

   public PatrolPathMarkerEntity(World world) {
      super(world);
   }

   public void setParentPath(IPrefabPath parentPath) {
      this.parentPath = parentPath;
   }

   @Nullable
   public UUID getPathId() {
      return this.pathId;
   }

   public void setPathId(UUID pathId) {
      this.pathId = pathId;
   }

   public String getPathName() {
      return this.pathName;
   }

   public void setPathName(String pathName) {
      this.pathName = pathName;
   }

   @Nonnull
   public static String generateDisplayName(int worldgenId, PatrolPathMarkerEntity patrolPathMarkerEntity) {
      return String.format(
         "%s.%s (%s) #%s [Wait %ss] <Rotate %.2fdeg>",
         worldgenId,
         patrolPathMarkerEntity.pathId,
         patrolPathMarkerEntity.pathName,
         patrolPathMarkerEntity.order,
         patrolPathMarkerEntity.pauseTime,
         patrolPathMarkerEntity.observationAngle * (180.0F / (float)Math.PI)
      );
   }

   public short getTempPathLength() {
      return this.tempPathLength;
   }

   @Override
   public void initialise(
      @Nonnull UUID id,
      @Nonnull String pathName,
      int index,
      double pauseTime,
      float observationAngle,
      int worldGenId,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.pathId = id;
      this.pathName = pathName;
      WorldPathData worldPathData = componentAccessor.getResource(WorldPathData.getResourceType());
      this.parentPath = worldPathData.getOrConstructPrefabPath(worldGenId, this.pathId, pathName, PatrolPath::new);
      this.pauseTime = pauseTime;
      this.observationAngle = observationAngle;
      if (index < 0) {
         this.order = this.parentPath.registerNewWaypoint(this, worldGenId);
      } else {
         this.order = index;
         this.parentPath.registerNewWaypointAt(index, this, worldGenId);
      }

      this.tempPathLength = (short)this.parentPath.length();
   }

   @Override
   public IPath<IPrefabPathWaypoint> getParentPath() {
      return this.parentPath;
   }

   @Override
   public boolean isCollidable() {
      return false;
   }

   @Override
   public boolean isHiddenFromLivingEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Player playerComponent = componentAccessor.getComponent(targetRef, Player.getComponentType());
      return playerComponent == null || playerComponent.getGameMode() != GameMode.Creative;
   }

   @Override
   public int getOrder() {
      return this.order;
   }

   public void setOrder(int order) {
      this.order = order;
      this.markNeedsSave();
   }

   @Override
   public double getPauseTime() {
      return this.pauseTime;
   }

   public void setPauseTime(double pauseTime) {
      this.pauseTime = pauseTime;
      this.markNeedsSave();
   }

   @Override
   public float getObservationAngle() {
      return this.observationAngle;
   }

   @Override
   public void onReplaced() {
      this.pathId = null;
      this.remove();
   }

   public void setObservationAngle(float observationAngle) {
      this.observationAngle = observationAngle;
      this.markNeedsSave();
   }

   @Nonnull
   @Override
   public Vector3d getWaypointPosition(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Ref<EntityStore> ref = this.getReference();

      assert ref != null && ref.isValid() : "Entity reference is null or invalid";

      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      return transformComponent.getPosition();
   }

   @Nonnull
   @Override
   public Vector3f getWaypointRotation(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Ref<EntityStore> ref = this.getReference();

      assert ref != null && ref.isValid() : "Entity reference is null or invalid";

      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      return transformComponent.getRotation();
   }

   @Nonnull
   @Override
   public String toString() {
      return "PatrolPathMarkerEntity{pathId="
         + this.pathId
         + ", path='"
         + this.pathName
         + "', order="
         + this.order
         + ", pauseTime="
         + this.pauseTime
         + ", observationAngle="
         + this.observationAngle
         + ", tempPathLength="
         + this.tempPathLength
         + ", parentPath="
         + this.parentPath
         + "} "
         + super.toString();
   }
}
