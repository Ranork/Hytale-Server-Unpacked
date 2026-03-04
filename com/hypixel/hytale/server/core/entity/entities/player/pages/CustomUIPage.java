package com.hypixel.hytale.server.core.entity.entities.player.pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CustomUIPage {
   @Nonnull
   protected final PlayerRef playerRef;
   @Nonnull
   protected CustomPageLifetime lifetime;

   public CustomUIPage(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
      this.playerRef = playerRef;
      this.lifetime = lifetime;
   }

   public void setLifetime(@Nonnull CustomPageLifetime lifetime) {
      this.lifetime = lifetime;
   }

   @Nonnull
   public CustomPageLifetime getLifetime() {
      return this.lifetime;
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, String rawData) {
      throw new UnsupportedOperationException("CustomUIPage doesn't support events! " + this + ": " + rawData);
   }

   public abstract void build(@Nonnull Ref<EntityStore> var1, @Nonnull UICommandBuilder var2, @Nonnull UIEventBuilder var3, @Nonnull Store<EntityStore> var4);

   protected void rebuild() {
      Ref<EntityStore> ref = this.playerRef.getReference();
      if (ref != null) {
         Store<EntityStore> store = ref.getStore();
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.build(ref, commandBuilder, eventBuilder, ref.getStore());
         playerComponent.getPageManager()
            .updateCustomPage(new CustomPage(this.getClass().getName(), false, true, this.lifetime, commandBuilder.getCommands(), eventBuilder.getEvents()));
      }
   }

   protected void sendUpdate() {
      this.sendUpdate(null, false);
   }

   protected void sendUpdate(@Nullable UICommandBuilder commandBuilder) {
      this.sendUpdate(commandBuilder, false);
   }

   protected void sendUpdate(@Nullable UICommandBuilder commandBuilder, boolean clear) {
      Ref<EntityStore> ref = this.playerRef.getReference();
      if (ref != null) {
         Store<EntityStore> store = ref.getStore();
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         playerComponent.getPageManager()
            .updateCustomPage(
               new CustomPage(
                  this.getClass().getName(),
                  false,
                  clear,
                  this.lifetime,
                  commandBuilder != null ? commandBuilder.getCommands() : UICommandBuilder.EMPTY_COMMAND_ARRAY,
                  UIEventBuilder.EMPTY_EVENT_BINDING_ARRAY
               )
            );
      }
   }

   protected void close() {
      Ref<EntityStore> ref = this.playerRef.getReference();
      if (ref != null) {
         Store<EntityStore> store = ref.getStore();
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         playerComponent.getPageManager().setPage(ref, store, Page.None);
      }
   }

   public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
   }
}
