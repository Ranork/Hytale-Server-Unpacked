package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.RoleUtils;
import javax.annotation.Nonnull;

public class NPCGiveCommand extends NPCWorldCommandBase {
   @Nonnull
   private final RequiredArg<Item> itemArg = this.withRequiredArg("item", "server.commands.npc.give.item.desc", ArgTypes.ITEM_ASSET);

   public NPCGiveCommand() {
      super("give", "server.commands.npc.give.desc");
      this.addSubCommand(new NPCGiveCommand.GiveNothingCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull NPCEntity npc, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref
   ) {
      Item item = this.itemArg.get(context);
      String itemName = item.getId();
      if (item.getArmor() != null) {
         RoleUtils.setArmor(npc, itemName);
      } else {
         RoleUtils.setItemInHand(npc, itemName);
      }
   }

   public static class GiveNothingCommand extends NPCWorldCommandBase {
      public GiveNothingCommand() {
         super("nothing", "server.commands.npc.give.nothing.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull NPCEntity npc, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref
      ) {
         RoleUtils.setItemInHand(npc, null);
      }
   }
}
