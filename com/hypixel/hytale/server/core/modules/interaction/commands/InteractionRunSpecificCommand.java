package com.hypixel.hytale.server.core.modules.interaction.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EnumArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class InteractionRunSpecificCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final EnumArgumentType<InteractionType> INTERACTION_TYPE_ARG_TYPE = new EnumArgumentType<>(
      "server.commands.parsing.argtype.interactiontype.name", InteractionType.class
   );
   @Nonnull
   private final RequiredArg<InteractionType> interactionTypeArg = this.withRequiredArg(
      "interactionType", "server.commands.interaction.run.interactionType.desc", INTERACTION_TYPE_ARG_TYPE
   );
   @Nonnull
   private final RequiredArg<RootInteraction> rootInteractionArg = this.withRequiredArg(
      "interaction", "server.commands.interaction.runSpecific.rootinteraction.desc", ArgTypes.ROOT_INTERACTION_ASSET
   );

   public InteractionRunSpecificCommand() {
      super("specific", "server.commands.interaction.runSpecific.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      InteractionManager interactionManager = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());

      assert interactionManager != null;

      InteractionType interactionType = this.interactionTypeArg.get(context);
      RootInteraction rootInteraction = this.rootInteractionArg.get(context);
      InteractionContext interactionContext = InteractionContext.forInteraction(interactionManager, ref, interactionType, store);
      InteractionChain chain = interactionManager.initChain(interactionType, interactionContext, rootInteraction, false);
      interactionManager.queueExecuteChain(chain);
      context.sendMessage(
         Message.translation("server.commands.interaction.runSpecific.started").param("type", interactionType.name()).param("root", rootInteraction.getId())
      );
   }
}
