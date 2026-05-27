package com.hypixel.hytale.server.npc.corecomponents.combat;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderActionEntityHitInteraction;
import com.hypixel.hytale.server.npc.movement.controllers.EntityHit;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.EntityCollisionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionEntityHitInteraction extends ActionBase {
   private static final InteractionType INTERACTION_TYPE = InteractionType.Collision;
   @Nullable
   protected final String interactionId;

   public ActionEntityHitInteraction(@Nonnull BuilderActionEntityHitInteraction builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.interactionId = builder.getInteraction(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store)
         && this.interactionId != null
         && sensorInfo instanceof EntityCollisionProvider provider
         && provider.getHitCount() > 0;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      if (sensorInfo instanceof EntityCollisionProvider provider) {
         if (this.interactionId == null) {
            return true;
         } else {
            InteractionManager interactionManagerComponent = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
            if (interactionManagerComponent == null) {
               return true;
            } else {
               RootInteraction rootInteraction = RootInteraction.getRootInteractionOrUnknown(this.interactionId);
               int hitCount = provider.getHitCount();

               for (int i = 0; i < hitCount; i++) {
                  EntityHit hit = provider.getHit(i);
                  Ref<EntityStore> targetRef = hit.entity;
                  if (targetRef != null && targetRef.isValid()) {
                     NetworkId networkIdComponent = store.getComponent(targetRef, NetworkId.getComponentType());
                     int networkId = networkIdComponent != null ? networkIdComponent.getId() : -1;
                     InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, INTERACTION_TYPE, store);
                     context.getMetaStore().putMetaObject(Interaction.TARGET_ENTITY, targetRef);
                     InteractionChain chain = interactionManagerComponent.initChain(INTERACTION_TYPE, context, rootInteraction, networkId, null, false);
                     interactionManagerComponent.queueExecuteChain(chain);
                  }
               }

               return true;
            }
         }
      } else {
         return true;
      }
   }
}
