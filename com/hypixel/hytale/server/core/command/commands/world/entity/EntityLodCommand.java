package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.entity.tracker.LegacyEntityTrackerSystems;
import javax.annotation.Nonnull;

public class EntityLodCommand extends CommandBase {
   @Nonnull
   private final RequiredArg<Double> ratioArg = this.withRequiredArg("ratio", "server.commands.entity.lod.ratio.desc", ArgTypes.DOUBLE);

   public EntityLodCommand() {
      super("lod", "server.commands.entity.lod.desc");
      this.addSubCommand(new EntityLodCommand.Default());
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      LegacyEntityTrackerSystems.LegacyLODCull.ENTITY_LOD_RATIO = this.ratioArg.get(context);
      context.sendMessage(Message.translation("server.commands.entity.lod.ratioSet").param("ratio", LegacyEntityTrackerSystems.LegacyLODCull.ENTITY_LOD_RATIO));
   }

   static class Default extends CommandBase {
      Default() {
         super("default", "server.commands.entity.lod.default.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         LegacyEntityTrackerSystems.LegacyLODCull.ENTITY_LOD_RATIO = 3.5E-5;
         context.sendMessage(
            Message.translation("server.commands.entity.lod.ratioSet").param("ratio", LegacyEntityTrackerSystems.LegacyLODCull.ENTITY_LOD_RATIO)
         );
      }
   }
}
