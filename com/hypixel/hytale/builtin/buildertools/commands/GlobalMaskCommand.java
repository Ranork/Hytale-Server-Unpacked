package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class GlobalMaskCommand extends AbstractPlayerCommand {
   public GlobalMaskCommand() {
      super("gmask", "server.commands.globalmask.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addUsageVariant(new GlobalMaskCommand.GlobalMaskSetCommand());
      this.addSubCommand(new GlobalMaskCommand.GlobalMaskClearCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
         BlockMask currentMask = s.getGlobalMask();
         if (currentMask == null) {
            context.sendMessage(Message.translation("server.builderTools.globalmask.current.none"));
         } else {
            context.sendMessage(Message.translation("server.builderTools.globalmask.current").param("mask", currentMask.informativeToString()));
         }
      });
   }

   private static class GlobalMaskClearCommand extends AbstractPlayerCommand {
      public GlobalMaskClearCommand() {
         super("clear", "server.commands.globalmask.clear.desc");
         this.setPermissionGroup(GameMode.Creative);
         this.addAliases("disable", "c");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.setGlobalMask(null, componentAccessor));
      }
   }

   private static class GlobalMaskSetCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<BlockMask> maskArg = this.withRequiredArg("mask", "server.commands.globalmask.mask.desc", ArgTypes.BLOCK_MASK);

      public GlobalMaskSetCommand() {
         super("server.commands.globalmask.desc");
         this.setPermissionGroup(GameMode.Creative);
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         BlockMask mask = this.maskArg.get(context);

         try {
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.setGlobalMask(mask, componentAccessor));
         } catch (IllegalArgumentException var9) {
            context.sendMessage(Message.translation("server.builderTools.globalmask.setFailed").param("reason", var9.getMessage()));
         }
      }
   }
}
