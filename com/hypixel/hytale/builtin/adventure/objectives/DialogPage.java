package com.hypixel.hytale.builtin.adventure.objectives;

import com.hypixel.hytale.builtin.adventure.objectives.config.task.UseEntityObjectiveTaskAsset;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DialogPage extends InteractiveCustomUIPage<DialogPage.DialogPageEventData> {
   @Nonnull
   public static final String LAYOUT = "Pages/DialogPage.ui";
   private final UseEntityObjectiveTaskAsset.DialogOptions dialogOptions;

   public DialogPage(@Nonnull PlayerRef playerRef, UseEntityObjectiveTaskAsset.DialogOptions dialogOptions) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, DialogPage.DialogPageEventData.CODEC);
      this.dialogOptions = dialogOptions;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/DialogPage.ui");
      commandBuilder.set("#EntityName.Text", Message.translation(this.dialogOptions.getEntityNameKey()));
      commandBuilder.set("#Dialog.Text", Message.translation(this.dialogOptions.getDialogKey()));
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton");
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull DialogPage.DialogPageEventData data) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         playerComponent.getPageManager().setPage(ref, store, Page.None);
      }
   }

   public static class DialogPageEventData {
      @Nonnull
      public static final BuilderCodec<DialogPage.DialogPageEventData> CODEC = BuilderCodec.builder(
            DialogPage.DialogPageEventData.class, DialogPage.DialogPageEventData::new
         )
         .build();
   }
}
