package com.hypixel.hytale.builtin.teleport.commands.warp;

import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.builtin.teleport.WarpListPage;
import com.hypixel.hytale.common.util.ListUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class WarpListCommand extends CommandBase {
   private static final int WARPS_PER_LIST_PAGE = 8;
   private static final Message MESSAGE_COMMANDS_TELEPORT_WARP_NOT_LOADED = Message.translation("server.commands.teleport.warp.notLoaded");
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   private static final Message MESSAGE_COMMANDS_TELEPORT_WARP_NO_WARPS = Message.translation("server.commands.teleport.warp.noWarps");
   private static final Message MESSAGE_COMMANDS_TELEPORT_WARP_PAGE_NUM_TOO_HIGH = Message.translation("server.commands.teleport.warp.pageNumTooHigh");
   @Nonnull
   private final OptionalArg<Integer> pageArg = this.withOptionalArg("page", "server.commands.warp.list.page.desc", ArgTypes.INTEGER);

   public WarpListCommand() {
      super("list", "server.commands.warp.list.desc");
      this.requirePermission(HytalePermissions.fromCommand("warp.list"));
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (!TeleportPlugin.get().isWarpsLoaded()) {
         context.sendMessage(MESSAGE_COMMANDS_TELEPORT_WARP_NOT_LOADED);
      } else {
         Map<String, Warp> warps = TeleportPlugin.get().getWarps();
         if (context.isPlayer() && !this.pageArg.provided(context)) {
            Ref<EntityStore> ref = context.senderAsPlayerRef();
            if (ref != null && ref.isValid()) {
               Store<EntityStore> store = ref.getStore();
               World playerWorld = store.getExternalData().getWorld();
               playerWorld.execute(() -> {
                  Player playerComponent = store.getComponent(ref, Player.getComponentType());

                  assert playerComponent != null;

                  PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  playerComponent.getPageManager().openCustomPage(ref, store, new WarpListPage(playerRefComponent, warps, warp -> {
                     try {
                        WarpCommand.tryGo(context, warp, ref, store);
                     } catch (Exception var5x) {
                        throw SneakyThrow.sneakyThrow(var5x);
                     }
                  }));
               });
            } else {
               context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            }
         } else if (warps.isEmpty()) {
            context.sendMessage(MESSAGE_COMMANDS_TELEPORT_WARP_NO_WARPS);
         } else {
            int pageNumber = this.pageArg.provided(context) ? this.pageArg.get(context) : 1;
            if (pageNumber < 1) {
               context.sendMessage(Message.translation("server.commands.teleport.warp.pageNumTooLow").param("page", pageNumber));
            } else {
               List<Warp> innerWarps = new ObjectArrayList(warps.values());
               innerWarps.sort((o1, o2) -> o2.getCreationDate().compareTo(o1.getCreationDate()));
               List<List<Warp>> paginated = ListUtil.partition(innerWarps, 8);
               if (paginated.size() < pageNumber) {
                  context.sendMessage(MESSAGE_COMMANDS_TELEPORT_WARP_PAGE_NUM_TOO_HIGH);
               } else {
                  context.sendMessage(
                     Message.translation("server.commands.teleport.warp.listHeader").param("page", pageNumber).param("pages", paginated.size())
                  );
                  int startIndex = (pageNumber - 1) * 8;
                  List<Warp> page = paginated.get(pageNumber - 1);
                  int i = 1;

                  for (Warp w : page) {
                     context.sendMessage(
                        Message.translation("server.commands.teleport.warp.listEntry")
                           .param("index", startIndex + i)
                           .param("name", w.getId())
                           .param("creator", w.getCreator())
                     );
                     i++;
                  }
               }
            }
         }
      }
   }
}
