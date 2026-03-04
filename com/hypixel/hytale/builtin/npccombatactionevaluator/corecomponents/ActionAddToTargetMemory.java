package com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents;

import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders.BuilderActionAddToTargetMemory;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemory;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionAddToTargetMemory extends ActionBase {
   @Nonnull
   private static final ComponentType<EntityStore, TargetMemory> TARGET_MEMORY = TargetMemory.getComponentType();

   public ActionAddToTargetMemory(@Nonnull BuilderActionAddToTargetMemory builder) {
      super(builder);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && sensorInfo != null && sensorInfo.hasPosition();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      TargetMemory targetMemoryComponent = ref.getStore().getComponent(ref, TARGET_MEMORY);
      if (targetMemoryComponent == null) {
         return true;
      } else {
         IPositionProvider positionProvider = sensorInfo.getPositionProvider();
         if (positionProvider == null) {
            return true;
         } else {
            Ref<EntityStore> targetRef = positionProvider.getTarget();
            if (targetRef != null && targetRef.isValid()) {
               Int2FloatOpenHashMap hostiles = targetMemoryComponent.getKnownHostiles();
               if (hostiles.put(targetRef.getIndex(), targetMemoryComponent.getRememberFor()) <= 0.0F) {
                  targetMemoryComponent.getKnownHostilesList().add(targetRef);
               }

               return true;
            } else {
               return true;
            }
         }
      }
   }
}
