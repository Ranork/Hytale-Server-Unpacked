package com.hypixel.hytale.server.core.command.commands.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.pages.UIGalleryPage;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class UIGalleryCommand extends AbstractAsyncCommand {
   @Nonnull
   private static final Message MESSAGE_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");

   public UIGalleryCommand() {
      super("ui-gallery", "server.commands.uigallery.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      if (!context.isPlayer()) {
         return CompletableFuture.completedFuture(null);
      } else {
         Ref<EntityStore> playerRef = context.senderAsPlayerRef();
         if (playerRef != null && playerRef.isValid()) {
            Store<EntityStore> store = playerRef.getStore();
            World world = store.getExternalData().getWorld();
            return CompletableFuture.runAsync(() -> {
               Player playerComponent = store.getComponent(playerRef, Player.getComponentType());
               PlayerRef playerRefComponent = store.getComponent(playerRef, PlayerRef.getComponentType());
               if (playerComponent != null && playerRefComponent != null) {
                  playerComponent.getPageManager().openCustomPage(playerRef, store, new UIGalleryPage(playerRefComponent));
               }
            }, world);
         } else {
            context.sendMessage(MESSAGE_PLAYER_NOT_IN_WORLD);
            return CompletableFuture.completedFuture(null);
         }
      }
   }
}
