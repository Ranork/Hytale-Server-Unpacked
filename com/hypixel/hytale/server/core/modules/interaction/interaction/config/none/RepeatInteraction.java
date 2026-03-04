package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.meta.DynamicMetaStore;
import com.hypixel.hytale.server.core.meta.MetaKey;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.StringTag;
import javax.annotation.Nonnull;

public class RepeatInteraction extends SimpleInteraction {
   public static final BuilderCodec<RepeatInteraction> CODEC = BuilderCodec.builder(RepeatInteraction.class, RepeatInteraction::new, SimpleInteraction.CODEC)
      .documentation(
         "Forks from the current interaction into one or more chains that run the specified interactions.\n\nWhen run this will create a new chain that will run the interactions specified in `ForkInteractions`. This will then wait until that chain completes. If the chain completes successfully it will then check the `Repeat` field to see if it needs to run again, if not then the interactions `Next` are run otherwise this repeats with the next fork. If the chain fails then any repeating is ignored and the interactions `Failed` are run instead."
      )
      .<String>appendInherited(
         new KeyedCodec<>("ForkInteractions", RootInteraction.CHILD_ASSET_CODEC),
         (i, s) -> i.forkInteractions = s,
         i -> i.forkInteractions,
         (i, parent) -> i.forkInteractions = parent.forkInteractions
      )
      .documentation("The interactions to run in the forks created by this interaction.")
      .addValidator(Validators.nonNull())
      .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
      .add()
      .<Integer>appendInherited(new KeyedCodec<>("Repeat", Codec.INTEGER), (i, s) -> i.repeat = s, i -> i.repeat, (i, parent) -> i.repeat = parent.repeat)
      .documentation("The number of times to repeat. -1 is considered as infinite, be careful when using this value.")
      .addValidator(Validators.or(Validators.greaterThanOrEqual(1), Validators.equal(-1)))
      .add()
      .build();
   private static final MetaKey<InteractionChain> FORKED_CHAIN = Interaction.META_REGISTRY.registerMetaObject(i -> null);
   private static final MetaKey<Integer> REMAINING_REPEATS = Interaction.META_REGISTRY.registerMetaObject(i -> null);
   private static final StringTag TAG_FORK = StringTag.of("Fork");
   private static final StringTag TAG_NEXT = StringTag.of("Next");
   private static final StringTag TAG_FAILED = StringTag.of("Failed");
   protected String forkInteractions;
   protected int repeat = 1;

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.None;
   }

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      DynamicMetaStore<Interaction> instanceStore = context.getInstanceStore();
      if (firstRun && this.repeat != -1) {
         instanceStore.putMetaObject(REMAINING_REPEATS, this.repeat);
      }

      InteractionChain chain = instanceStore.getMetaObject(FORKED_CHAIN);
      if (chain != null) {
         switch (chain.getServerState()) {
            case NotFinished:
               context.getState().state = InteractionState.NotFinished;
               return;
            case Finished:
               if (this.repeat != -1 && instanceStore.getMetaObject(REMAINING_REPEATS) <= 0) {
                  context.getState().state = InteractionState.Finished;
                  super.tick0(firstRun, time, type, context, cooldownHandler);
                  return;
               }

               context.getState().state = InteractionState.NotFinished;
               break;
            case Failed:
               context.getState().state = InteractionState.Failed;
               super.tick0(firstRun, time, type, context, cooldownHandler);
               return;
         }
      }

      chain = context.fork(context.duplicate(), RootInteraction.getRootInteractionOrUnknown(this.forkInteractions), true);
      instanceStore.putMetaObject(FORKED_CHAIN, chain);
      context.getState().state = InteractionState.NotFinished;
      if (this.repeat != -1) {
         instanceStore.putMetaObject(REMAINING_REPEATS, instanceStore.getMetaObject(REMAINING_REPEATS) - 1);
      }
   }

   @Override
   protected void simulateTick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      InteractionChain chain = context.getInstanceStore().getMetaObject(FORKED_CHAIN);
      DynamicMetaStore<Interaction> instanceStore = context.getInstanceStore();
      if (chain != null) {
         switch (chain.getServerState()) {
            case NotFinished:
               context.getState().state = InteractionState.NotFinished;
               break;
            case Finished:
               if (this.repeat != -1 && instanceStore.getMetaObject(REMAINING_REPEATS) <= 0) {
                  context.getState().state = InteractionState.Finished;
                  super.simulateTick0(firstRun, time, type, context, cooldownHandler);
               } else {
                  context.getState().state = InteractionState.NotFinished;
               }
               break;
            case Failed:
               context.getState().state = InteractionState.Failed;
               super.simulateTick0(firstRun, time, type, context, cooldownHandler);
         }
      } else {
         context.getState().state = InteractionState.NotFinished;
      }
   }

   @Override
   public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
      if (this.forkInteractions != null
         && InteractionManager.walkInteractions(
            collector, context, TAG_FORK, RootInteraction.getRootInteractionOrUnknown(this.forkInteractions).getInteractionIds()
         )) {
         return true;
      } else {
         return this.next != null && InteractionManager.walkInteraction(collector, context, TAG_NEXT, this.next)
            ? true
            : this.failed != null && InteractionManager.walkInteraction(collector, context, TAG_FAILED, this.failed);
      }
   }

   @Override
   public boolean needsRemoteSync() {
      return true;
   }

   @Nonnull
   @Override
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.RepeatInteraction();
   }

   @Override
   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.RepeatInteraction p = (com.hypixel.hytale.protocol.RepeatInteraction)packet;
      p.forkInteractions = RootInteraction.getRootInteractionIdOrUnknown(this.forkInteractions);
      p.repeat = this.repeat;
   }

   @Nonnull
   @Override
   public String toString() {
      return "RepeatInteraction{forkInteractions='" + this.forkInteractions + "', repeat=" + this.repeat + "} " + super.toString();
   }
}
