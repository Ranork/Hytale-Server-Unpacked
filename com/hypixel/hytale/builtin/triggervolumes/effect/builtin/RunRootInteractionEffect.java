package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RunRootInteractionEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<RunRootInteractionEffect> CODEC = BuilderCodec.builder(
         RunRootInteractionEffect.class, RunRootInteractionEffect::new, BASE_CODEC
      )
      .append(new KeyedCodec<>("RootInteraction", Codec.STRING), (e, v) -> e.rootInteractionId = v, e -> e.rootInteractionId)
      .add()
      .append(new KeyedCodec<>("InteractionType", new EnumCodec<>(InteractionType.class), false), (e, v) -> e.interactionType = v, e -> e.interactionType)
      .add()
      .build();
   @Nullable
   private String rootInteractionId;
   @Nonnull
   private InteractionType interactionType = InteractionType.Use;

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.rootInteractionId != null && !this.rootInteractionId.isBlank()) {
         if (this.interactionType != null) {
            Ref<EntityStore> entityRef = context.getEntityRef();
            Store<EntityStore> store = context.getStore();
            InteractionManager interactionManager = store.getComponent(entityRef, InteractionModule.get().getInteractionManagerComponent());
            if (interactionManager != null) {
               RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(this.rootInteractionId);
               if (rootInteraction != null) {
                  InteractionContext interactionContext = InteractionContext.forInteraction(interactionManager, entityRef, this.interactionType, store);
                  InteractionChain chain = interactionManager.initChain(this.interactionType, interactionContext, rootInteraction, false);
                  interactionManager.queueExecuteChain(chain);
               }
            }
         }
      }
   }
}
