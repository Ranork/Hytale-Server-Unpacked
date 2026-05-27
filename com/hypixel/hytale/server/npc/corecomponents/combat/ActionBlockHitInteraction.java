package com.hypixel.hytale.server.npc.corecomponents.combat;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderActionBlockHitInteraction;
import com.hypixel.hytale.server.npc.movement.controllers.BlockHit;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.BlockCollisionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionBlockHitInteraction extends ActionBase {
   private static final InteractionType INTERACTION_TYPE = InteractionType.Collision;
   @Nullable
   protected final String interactionId;

   public ActionBlockHitInteraction(@Nonnull BuilderActionBlockHitInteraction builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.interactionId = builder.getInteraction(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store)
         && this.interactionId != null
         && sensorInfo instanceof BlockCollisionProvider provider
         && provider.getHitCount() > 0;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      if (!(sensorInfo instanceof BlockCollisionProvider provider)) {
         return true;
      } else if (this.interactionId == null) {
         return true;
      } else {
         InteractionManager interactionManagerComponent = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
         if (interactionManagerComponent == null) {
            return true;
         } else {
            RootInteraction rootInteraction = RootInteraction.getRootInteractionOrUnknown(this.interactionId);
            World world = store.getExternalData().getWorld();
            int hitCount = provider.getHitCount();

            for (int i = 0; i < hitCount; i++) {
               BlockHit hit = provider.getHit(i);
               BlockPosition pos = new BlockPosition(hit.blockX, hit.blockY, hit.blockZ);
               InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, INTERACTION_TYPE, store);
               context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK_RAW, pos);
               context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, world.getBaseBlock(pos));
               InteractionChain chain = interactionManagerComponent.initChain(INTERACTION_TYPE, context, rootInteraction, -1, pos, false);
               interactionManagerComponent.queueExecuteChain(chain);
            }

            return true;
         }
      }
   }
}
