package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.CombatSupport;
import java.util.List;
import javax.annotation.Nonnull;

public class NPCAttackCommand extends AbstractCommandCollection {
   public NPCAttackCommand() {
      super("attack", "server.commands.npc.attack.desc");
      this.addSubCommand(new NPCAttackCommand.SetAttackCommand());
      this.addSubCommand(new NPCAttackCommand.ClearAttackCommand());
   }

   public static class ClearAttackCommand extends NPCWorldCommandBase {
      public ClearAttackCommand() {
         super("clear", "server.commands.npc.attack.clear.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull NPCEntity npc, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref
      ) {
         npc.getRole().getCombatSupport().clearAttackOverrides();
      }
   }

   public static class SetAttackCommand extends NPCWorldCommandBase {
      @Nonnull
      private final OptionalArg<List<Interaction>> attackArg = this.withListOptionalArg(
         "attack", "server.commands.npc.attack.sequence", ArgTypes.INTERACTION_ASSET
      );

      public SetAttackCommand() {
         super("", "server.commands.npc.attack.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull NPCEntity npc, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref
      ) {
         if (this.attackArg.provided(context)) {
            List<Interaction> sequences = this.attackArg.get(context);
            CombatSupport combatSupport = npc.getRole().getCombatSupport();
            combatSupport.clearAttackOverrides();

            for (Interaction sequence : sequences) {
               combatSupport.addAttackOverride(sequence.getId());
            }
         }
      }
   }
}
