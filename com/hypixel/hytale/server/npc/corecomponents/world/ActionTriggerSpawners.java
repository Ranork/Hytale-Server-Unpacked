package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionTriggerSpawners;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionTriggerSpawners extends ActionBase {
   protected static final ComponentType<EntityStore, SpawnMarkerEntity> SPAWN_MARKER_ENTITY_COMPONENT_TYPE = SpawnMarkerEntity.getComponentType();
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected final String spawner;
   protected final double range;
   protected final double rangeSquared;
   protected final int count;
   @Nullable
   protected final List<Ref<EntityStore>> triggerList;
   protected Ref<EntityStore> parentRef;

   public ActionTriggerSpawners(@Nonnull BuilderActionTriggerSpawners builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.spawner = builder.getSpawner(support);
      this.range = builder.getRange(support);
      this.rangeSquared = this.range * this.range;
      this.count = builder.getCount(support);
      this.triggerList = this.count > 0 ? new ObjectArrayList(this.count) : null;
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      role.getPositionCache().requireSpawnMarkerDistance(this.range);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      this.parentRef = ref;
      List<Ref<EntityStore>> spawners = role.getPositionCache().getSpawnMarkerList();
      if (this.count <= 0) {
         for (int i = 0; i < spawners.size(); i++) {
            Ref<EntityStore> spawnMarkerRef = this.filterMarker(spawners.get(i), store);
            if (spawnMarkerRef != null) {
               SpawnMarkerEntity spawnMarkerEntityComponent = store.getComponent(spawnMarkerRef, SPAWN_MARKER_ENTITY_COMPONENT_TYPE);

               assert spawnMarkerEntityComponent != null;

               spawnMarkerEntityComponent.trigger(spawnMarkerRef, store);
            }
         }

         return true;
      } else {
         RandomExtra.reservoirSample(spawners, (reference, _this, _store) -> _this.filterMarker(reference, _store), this.count, this.triggerList, this, store);

         for (int ix = 0; ix < this.triggerList.size(); ix++) {
            Ref<EntityStore> spawnMarkerRef = this.triggerList.get(ix);
            SpawnMarkerEntity spawnMarkerEntityComponent = store.getComponent(spawnMarkerRef, SPAWN_MARKER_ENTITY_COMPONENT_TYPE);

            assert spawnMarkerEntityComponent != null;

            spawnMarkerEntityComponent.trigger(spawnMarkerRef, store);
         }

         this.triggerList.clear();
         return true;
      }
   }

   @Nullable
   protected Ref<EntityStore> filterMarker(@Nonnull Ref<EntityStore> targetRef, @Nonnull Store<EntityStore> store) {
      if (!targetRef.isValid()) {
         return null;
      } else {
         TransformComponent parentTransformComponent = store.getComponent(this.parentRef, TRANSFORM_COMPONENT_TYPE);

         assert parentTransformComponent != null;

         Vector3d parentPosition = parentTransformComponent.getPosition();
         TransformComponent targetTransformComponent = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

         assert targetTransformComponent != null;

         Vector3d targetPosition = targetTransformComponent.getPosition();
         SpawnMarkerEntity targetMarkerEntityComponent = store.getComponent(targetRef, SPAWN_MARKER_ENTITY_COMPONENT_TYPE);
         return targetMarkerEntityComponent == null
               || !targetMarkerEntityComponent.isManualTrigger()
               || !(parentPosition.distanceSquaredTo(targetPosition) <= this.rangeSquared)
               || this.spawner != null && !this.spawner.equals(targetMarkerEntityComponent.getSpawnMarkerId())
            ? null
            : targetRef;
      }
   }
}
