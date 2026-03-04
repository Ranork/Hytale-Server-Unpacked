package com.hypixel.hytale.builtin.portals.systems.voidevent;

import com.hypixel.hytale.builtin.portals.components.voidevent.VoidEvent;
import com.hypixel.hytale.builtin.portals.components.voidevent.config.VoidEventConfig;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class StartVoidEventInFragmentSystem extends DelayedSystem<EntityStore> {
   public StartVoidEventInFragmentSystem() {
      super(1.0F);
   }

   @Override
   public void delayedTick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      PortalWorld portalWorldResource = store.getResource(PortalWorld.getResourceType());
      if (portalWorldResource.exists()) {
         if (portalWorldResource.getPortalType().isVoidInvasionEnabled()) {
            World world = store.getExternalData().getWorld();
            VoidEventConfig voidEventConfig = portalWorldResource.getVoidEventConfig();
            int timeLimitSeconds = portalWorldResource.getTimeLimitSeconds();
            int shouldStartAfter = voidEventConfig.getShouldStartAfterSeconds(timeLimitSeconds);
            int elapsedSeconds = (int)Math.ceil(portalWorldResource.getElapsedSeconds(world));
            Ref<EntityStore> voidEventRef = portalWorldResource.getVoidEventRef();
            boolean exists = voidEventRef != null && voidEventRef.isValid();
            boolean shouldExist = elapsedSeconds >= shouldStartAfter;
            if (exists && !shouldExist) {
               store.removeEntity(voidEventRef, RemoveReason.REMOVE);
            }

            if (shouldExist && !exists) {
               Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
               holder.addComponent(VoidEvent.getComponentType(), new VoidEvent());
               Ref<EntityStore> spawnedEventRef = store.addEntity(holder, AddReason.SPAWN);
               portalWorldResource.setVoidEventRef(spawnedEventRef);
            }
         }
      }
   }
}
