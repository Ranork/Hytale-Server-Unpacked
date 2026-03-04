package com.hypixel.hytale.server.core.entity.entities.player.pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageEvent;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.protocol.packets.interface_.SetPage;
import com.hypixel.hytale.protocol.packets.window.OpenWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.modules.anchoraction.AnchorActionModule;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PageManager {
   @Nullable
   private WindowManager windowManager;
   private PlayerRef playerRef;
   @Nullable
   private CustomUIPage customPage;
   @Nonnull
   private final AtomicInteger customPageRequiredAcknowledgments = new AtomicInteger();

   public void init(@Nonnull PlayerRef playerRef, @Nonnull WindowManager windowManager) {
      this.windowManager = windowManager;
      this.playerRef = playerRef;
   }

   public void clearCustomPageAcknowledgements() {
      this.customPageRequiredAcknowledgments.set(0);
   }

   @Nullable
   public CustomUIPage getCustomPage() {
      return this.customPage;
   }

   public void setPage(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Page page) {
      this.setPage(ref, store, page, false);
   }

   public void setPage(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Page page, boolean canCloseThroughInteraction) {
      if (this.customPage != null) {
         this.customPage.onDismiss(ref, store);
         this.customPage = null;
         this.customPageRequiredAcknowledgments.incrementAndGet();
      }

      this.playerRef.getPacketHandler().writeNoCache(new SetPage(page, canCloseThroughInteraction));
   }

   public void openCustomPage(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull CustomUIPage page) {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      UIEventBuilder eventBuilder = new UIEventBuilder();
      if (this.customPage != null) {
         this.customPage.onDismiss(ref, ref.getStore());
      }

      page.build(ref, commandBuilder, eventBuilder, store);
      this.updateCustomPage(new CustomPage(page.getClass().getName(), true, true, page.getLifetime(), commandBuilder.getCommands(), eventBuilder.getEvents()));
      this.customPage = page;
   }

   public boolean setPageWithWindows(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Page page, boolean canCloseThroughInteraction, @Nonnull Window... windows
   ) {
      if (this.windowManager == null) {
         return false;
      } else {
         List<OpenWindow> windowPackets = this.windowManager.openWindows(ref, store, windows);
         if (windowPackets == null) {
            return false;
         } else {
            this.setPage(ref, store, page, canCloseThroughInteraction);

            for (OpenWindow packet : windowPackets) {
               this.playerRef.getPacketHandler().write(packet);
            }

            return true;
         }
      }
   }

   public boolean openCustomPageWithWindows(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull CustomUIPage page, @Nonnull Window... windows
   ) {
      if (this.windowManager == null) {
         return false;
      } else {
         List<OpenWindow> windowPackets = this.windowManager.openWindows(ref, store, windows);
         if (windowPackets == null) {
            return false;
         } else {
            this.openCustomPage(ref, store, page);

            for (OpenWindow packet : windowPackets) {
               this.playerRef.getPacketHandler().write(packet);
            }

            return true;
         }
      }
   }

   public void updateCustomPage(@Nonnull CustomPage page) {
      this.customPageRequiredAcknowledgments.incrementAndGet();
      this.playerRef.getPacketHandler().write(page);
   }

   public void handleEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull CustomPageEvent event) {
      switch (event.type) {
         case Dismiss:
            if (this.customPage == null) {
               return;
            }

            this.customPage.onDismiss(ref, store);
            this.customPage = null;
            break;
         case Data:
            if (this.customPageRequiredAcknowledgments.get() != 0) {
               return;
            }

            if (this.customPage != null) {
               this.customPage.handleDataEvent(ref, store, event.data);
            } else {
               AnchorActionModule.get().tryHandle(this.playerRef, event.data);
            }
            break;
         case Acknowledge:
            if (this.customPageRequiredAcknowledgments.decrementAndGet() < 0) {
               this.customPageRequiredAcknowledgments.incrementAndGet();
               throw new IllegalArgumentException("Client sent unexpected acknowledgement");
            }
      }
   }
}
