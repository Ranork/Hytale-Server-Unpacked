package com.hypixel.hytale.builtin.adventure.npcobjectives.npc;

import com.hypixel.hytale.builtin.adventure.npcobjectives.NPCObjectivesPlugin;
import com.hypixel.hytale.builtin.adventure.npcobjectives.npc.builders.BuilderSensorHasTask;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.EntitySupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorHasTask extends SensorBase {
   @Nullable
   protected final String[] tasksById;

   public SensorHasTask(@Nonnull BuilderSensorHasTask builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.tasksById = builder.getTasksById(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else if (this.tasksById != null && this.tasksById.length != 0) {
         Ref<EntityStore> targetRef = role.getStateSupport().getInteractionIterationTarget();
         if (targetRef != null && targetRef.isValid()) {
            Archetype<EntityStore> targetArchetype = store.getArchetype(targetRef);
            if (targetArchetype.contains(DeathComponent.getComponentType())) {
               return false;
            } else {
               UUIDComponent targetUuidComponent = store.getComponent(targetRef, UUIDComponent.getComponentType());
               if (targetUuidComponent == null) {
                  return false;
               } else {
                  UUID targetUuid = targetUuidComponent.getUuid();
                  UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
                  if (uuidComponent == null) {
                     return false;
                  } else {
                     UUID uuid = uuidComponent.getUuid();
                     EntitySupport entitySupport = role.getEntitySupport();
                     boolean match = false;

                     for (String taskById : this.tasksById) {
                        if (NPCObjectivesPlugin.hasTask(targetUuid, uuid, taskById)) {
                           match = true;
                           entitySupport.addTargetPlayerActiveTask(taskById);
                        }
                     }

                     return match;
                  }
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }
}
