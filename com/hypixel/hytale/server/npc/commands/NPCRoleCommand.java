package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import javax.annotation.Nonnull;

public class NPCRoleCommand extends NPCWorldCommandBase {
   @Nonnull
   private final RequiredArg<BuilderInfo> roleArg = this.withRequiredArg("role", "server.commands.npc.role.role.desc", NPCCommand.NPC_ROLE);

   public NPCRoleCommand() {
      super("role", "server.commands.npc.role.desc");
      this.addUsageVariant(new NPCRoleCommand.GetRoleCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull NPCEntity npc, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref
   ) {
      BuilderInfo roleInfo = this.roleArg.get(context);
      if (npc.getRole().isRoleChangeRequested()) {
         context.sendMessage(Message.translation("server.commands.npc.role.unableToSetRole"));
      } else {
         RoleChangeSystem.requestRoleChange(ref, npc.getRole(), roleInfo.getIndex(), true, store);
         context.sendMessage(Message.translation("server.commands.npc.role.roleSet").param("role", roleInfo.getKeyName()));
      }
   }

   public static class GetRoleCommand extends NPCWorldCommandBase {
      public GetRoleCommand() {
         super("server.commands.npc.role.get.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull NPCEntity npc, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref
      ) {
         context.sendMessage(Message.translation("server.commands.npc.role.npcHasRole").param("role", npc.getRoleName()));
      }
   }
}
