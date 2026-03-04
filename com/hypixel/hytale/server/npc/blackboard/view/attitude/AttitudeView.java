package com.hypixel.hytale.server.npc.blackboard.view.attitude;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.PrioritisedProviderView;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import javax.annotation.Nonnull;

public class AttitudeView extends PrioritisedProviderView<IAttitudeProvider, AttitudeView> {
   private final World world;

   public AttitudeView(World world) {
      this.world = world;
      this.registerProvider(0, (ref, self, target, accessor) -> self.getWorldSupport().getOverriddenAttitude(target));
      this.registerProvider(200, (ref, self, target, accessor) -> NPCPlugin.get().getAttitudeMap().getAttitude(self, target, accessor));
      this.registerProvider(
         Integer.MAX_VALUE,
         (ref, self, target, accessor) -> {
            WorldSupport worldSupport = self.getWorldSupport();
            return accessor.getArchetype(target).contains(Player.getComponentType())
               ? worldSupport.getDefaultPlayerAttitude()
               : worldSupport.getDefaultNPCAttitude();
         }
      );
   }

   @Nonnull
   public Attitude getAttitude(
      @Nonnull Ref<EntityStore> ref, @Nonnull Role self, @Nonnull Ref<EntityStore> target, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Attitude result = null;
      int pos = 0;

      while (result == null) {
         if (pos >= this.providers.size()) {
            return Attitude.NEUTRAL;
         }

         result = this.providers.get(pos++).getProvider().getAttitude(ref, self, target, componentAccessor);
      }

      return result;
   }

   @Override
   public boolean isOutdated(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      return false;
   }

   public AttitudeView getUpdatedView(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World entityWorld = componentAccessor.getExternalData().getWorld();
      if (!entityWorld.equals(this.world)) {
         Blackboard blackboardResource = componentAccessor.getResource(Blackboard.getResourceType());
         return blackboardResource.getView(AttitudeView.class, ref, componentAccessor);
      } else {
         return this;
      }
   }

   @Override
   public void initialiseEntity(@Nonnull Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent) {
   }

   @Override
   public void cleanup() {
   }

   @Override
   public void onWorldRemoved() {
   }
}
