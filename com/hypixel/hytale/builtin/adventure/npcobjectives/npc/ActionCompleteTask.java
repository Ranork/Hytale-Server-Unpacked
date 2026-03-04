package com.hypixel.hytale.builtin.adventure.npcobjectives.npc;

import com.hypixel.hytale.builtin.adventure.npcobjectives.NPCObjectivesPlugin;
import com.hypixel.hytale.builtin.adventure.npcobjectives.npc.builders.BuilderActionCompleteTask;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionPlayAnimation;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.List;
import javax.annotation.Nonnull;

public class ActionCompleteTask extends ActionPlayAnimation {
   protected final boolean playAnimation;

   public ActionCompleteTask(@Nonnull BuilderActionCompleteTask builder, @Nonnull BuilderSupport support) {
      super(builder, support);
      this.playAnimation = builder.isPlayAnimation(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> targetRef = role.getStateSupport().getInteractionIterationTarget();
      boolean targetExists = targetRef != null && targetRef.isValid() && !store.getArchetype(targetRef).contains(DeathComponent.getComponentType());
      return super.canExecute(ref, role, sensorInfo, dt, store) && targetExists;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      UUIDComponent parentUuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
      if (parentUuidComponent == null) {
         return false;
      } else {
         Ref<EntityStore> targetPlayerReference = role.getStateSupport().getInteractionIterationTarget();
         if (targetPlayerReference == null) {
            return false;
         } else {
            PlayerRef targetPlayerRefComponent = store.getComponent(targetPlayerReference, PlayerRef.getComponentType());
            if (targetPlayerRefComponent == null) {
               return false;
            } else {
               List<String> activeTasks = role.getEntitySupport().getTargetPlayerActiveTasks();
               String animation = null;
               if (activeTasks != null) {
                  for (int i = 0; i < activeTasks.size(); i++) {
                     animation = NPCObjectivesPlugin.updateTaskCompletion(
                        store, targetPlayerReference, targetPlayerRefComponent, parentUuidComponent.getUuid(), activeTasks.get(i)
                     );
                  }
               }

               if (this.playAnimation && animation != null) {
                  this.setAnimationId(animation);
                  super.execute(ref, role, sensorInfo, dt, store);
               }

               return true;
            }
         }
      }
   }
}
