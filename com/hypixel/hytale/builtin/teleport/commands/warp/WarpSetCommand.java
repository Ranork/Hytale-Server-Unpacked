package com.hypixel.hytale.builtin.teleport.commands.warp;

import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.builtin.teleport.commands.event.ReplaceWarpEvent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.util.Set;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class WarpSetCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_TELEPORT_WARP_NOT_LOADED = Message.translation("server.commands.teleport.warp.notLoaded");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_TELEPORT_WARP_RESERVED_KEYWORD = Message.translation("server.commands.teleport.warp.reservedKeyword");
   @Nonnull
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.warp.set.name.desc", ArgTypes.STRING);
   @Nonnull
   private final FlagArg forceArg = this.withFlagArg("force", "server.commands.warp.set.force.desc");
   private final Set<String> keywords;

   public WarpSetCommand(@Nonnull Set<String> keywords) {
      super("set", "server.commands.warp.set.desc");
      this.keywords = keywords;
      this.requirePermission(HytalePermissions.fromCommand("warp.set"));
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      TeleportPlugin plugin = TeleportPlugin.get();
      if (!plugin.isWarpsLoaded()) {
         context.sendMessage(MESSAGE_COMMANDS_TELEPORT_WARP_NOT_LOADED);
      } else {
         String warpName = this.nameArg.get(context);
         String warpId = warpName.toLowerCase();
         Warp oldWarp = plugin.getWarps().get(warpId);
         if (context.provided(this.forceArg) || oldWarp == null || !cancel(context, oldWarp)) {
            if (this.keywords.contains(warpId)) {
               context.sendMessage(MESSAGE_COMMANDS_TELEPORT_WARP_RESERVED_KEYWORD);
            } else {
               TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

               assert transformComponent != null;

               HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

               assert headRotationComponent != null;

               Vector3d position = transformComponent.getPosition();
               Rotation3f headRotation = headRotationComponent.getRotation();
               Transform transform = new Transform(new Vector3d(position), new Rotation3f(headRotation));
               Warp newWarp = new Warp(transform, warpName, world, playerRef.getUsername(), Instant.now());
               TeleportPlugin.get().addWarp(newWarp, true);
               context.sendMessage(Message.translation("server.commands.teleport.warp.setWarp").param("name", newWarp.getId()));
            }
         }
      }
   }

   private static boolean cancel(@Nonnull CommandContext context, @Nonnull Warp warp) {
      IEventDispatcher<ReplaceWarpEvent, ReplaceWarpEvent> dispatch = HytaleServer.get().getEventBus().dispatchFor(ReplaceWarpEvent.class);
      if (!dispatch.hasListener()) {
         return false;
      } else {
         ReplaceWarpEvent result = dispatch.dispatch(new ReplaceWarpEvent(warp));
         if (!result.isCancelled()) {
            return false;
         } else {
            if (result.getCancelReason() != null) {
               context.sendMessage(result.getCancelReason());
            } else {
               context.sendMessage(WarpCommand.MESSAGE_COMMANDS_MODIFICATION_CANCELLED.param("name", warp.getId()));
            }

            return true;
         }
      }
   }
}
