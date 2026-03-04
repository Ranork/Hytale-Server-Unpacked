package com.hypixel.hytale.server.core.modules.interaction.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
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

public class InteractionRunCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final EnumArgumentType<InteractionType> INTERACTION_TYPE_ARG_TYPE = new EnumArgumentType<>(
      "server.commands.parsing.argtype.interactiontype.name", InteractionType.class
   );
   @Nonnull
   private final RequiredArg<InteractionType> interactionTypeArg = this.withRequiredArg(
      "interactionType", "server.commands.interaction.run.interactionType.desc", INTERACTION_TYPE_ARG_TYPE
   );

   public InteractionRunCommand() {
      super("run", "server.commands.interaction.run.desc");
      this.addSubCommand(new InteractionRunSpecificCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      InteractionType interactionType = this.interactionTypeArg.get(context);
      InteractionManager interactionManagerComponent = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());

      assert interactionManagerComponent != null;

      InteractionContext interactionContext = InteractionContext.forInteraction(interactionManagerComponent, ref, interactionType, store);
      String root = interactionContext.getRootInteractionId(interactionType);
      if (root == null) {
         context.sendMessage(Message.translation("server.commands.interaction.run.rootNotFound").param("type", interactionType.name()));
      } else {
         RootInteraction interactionAsset = RootInteraction.getAssetMap().getAsset(root);
         if (interactionAsset == null) {
            context.sendMessage(
               Message.translation("server.commands.interaction.run.interactionAssetNotFound").param("root", root).param("type", interactionType.name())
            );
         } else {
            InteractionChain chain = interactionManagerComponent.initChain(interactionType, interactionContext, interactionAsset, false);
            interactionManagerComponent.queueExecuteChain(chain);
            context.sendMessage(Message.translation("server.commands.interaction.run.started").param("type", interactionType.name()));
         }
      }
   }
}
