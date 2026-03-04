package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InterruptInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<InterruptInteraction> CODEC = BuilderCodec.builder(
         InterruptInteraction.class, InterruptInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Interrupts interactions on the target entity.")
      .<InteractionTarget>appendInherited(
         new KeyedCodec<>("Entity", InteractionTarget.CODEC), (o, i) -> o.entityTarget = i, o -> o.entityTarget, (o, p) -> o.entityTarget = p.entityTarget
      )
      .documentation("The entity to target for this interaction.")
      .addValidator(Validators.nonNull())
      .add()
      .<Set<InteractionType>>appendInherited(
         new KeyedCodec<>("InterruptTypes", InteractionModule.INTERACTION_TYPE_SET_CODEC),
         (o, i) -> o.interruptTypes = i,
         o -> o.interruptTypes,
         (o, p) -> o.interruptTypes = p.interruptTypes
      )
      .documentation("A set of interaction types that this interrupt will cancel")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("RequiredTag", Codec.STRING), (o, i) -> o.requiredTag = i, o -> o.requiredTag, (o, p) -> o.requiredTag = p.requiredTag
      )
      .documentation("The tag that the root interaction of an active interaction chain must have to be interrupted.\nIf not set then no tag is required.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("ExcludedTag", Codec.STRING), (o, i) -> o.excludedTag = i, o -> o.excludedTag, (o, p) -> o.excludedTag = p.excludedTag
      )
      .documentation("The tag that if the root interaction of an active interaction chain has then it will not be interrupted.")
      .add()
      .afterDecode(o -> {
         if (o.requiredTag != null) {
            o.requiredTagIndex = AssetRegistry.getOrCreateTagIndex(o.requiredTag);
         }

         if (o.excludedTag != null) {
            o.excludedTagIndex = AssetRegistry.getOrCreateTagIndex(o.excludedTag);
         }
      })
      .build();
   private InteractionTarget entityTarget = InteractionTarget.USER;
   @Nullable
   private Set<InteractionType> interruptTypes;
   @Nullable
   private String requiredTag;
   private int requiredTagIndex = Integer.MIN_VALUE;
   @Nullable
   private String excludedTag;
   private int excludedTagIndex = Integer.MIN_VALUE;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      Ref<EntityStore> targetRef = this.entityTarget.getEntity(context, ref);
      if (targetRef != null) {
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
         InteractionManager interactionManagerComponent = commandBuffer.getComponent(targetRef, InteractionModule.get().getInteractionManagerComponent());
         if (interactionManagerComponent != null) {
            ObjectIterator var8 = interactionManagerComponent.getChains().values().iterator();

            while (var8.hasNext()) {
               InteractionChain interactionChain = (InteractionChain)var8.next();
               if (this.interruptTypes == null || this.interruptTypes.contains(interactionChain.getType())) {
                  IntSet tags = interactionChain.getInitialRootInteraction().getData().getExpandedTagIndexes();
                  if ((this.requiredTag == null || tags.contains(this.requiredTagIndex)) && (this.excludedTag == null || !tags.contains(this.excludedTagIndex))
                     )
                   {
                     interactionManagerComponent.cancelChains(interactionChain);
                  }
               }
            }
         }
      }
   }
}
