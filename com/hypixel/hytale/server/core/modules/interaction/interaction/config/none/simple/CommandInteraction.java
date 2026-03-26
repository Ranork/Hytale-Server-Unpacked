package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.simple;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class CommandInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<CommandInteraction> CODEC = BuilderCodec.builder(
         CommandInteraction.class, CommandInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Interaction that executes a server command as the owning player.")
      .appendInherited(
         new KeyedCodec<>("Command", Codec.STRING),
         (interaction, s) -> interaction.command = s,
         interaction -> interaction.command,
         (interaction, parent) -> interaction.command = parent.command
      )
      .add()
      .build();
   private String command;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getOwningEntity();
      Player player = ref.getStore().getComponent(ref, Player.getComponentType());
      if (player != null) {
         CommandManager.get().handleCommand(player, this.command);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "CommandInteraction{command=" + this.command + "} " + super.toString();
   }
}
