package com.hypixel.hytale.builtin.buildertools.prefabeditor.ui;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PrefabEditorLoadOptionsPage extends InteractiveCustomUIPage<PrefabEditorLoadOptionsPage.PageData> {
   @Nonnull
   private final World world;

   public PrefabEditorLoadOptionsPage(@Nonnull PlayerRef playerRef, @Nonnull World world) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PrefabEditorLoadOptionsPage.PageData.CODEC);
      this.world = world;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/PrefabEditorLoadOptions.ui");
      commandBuilder.set("#WarningTitle.TextSpans", Message.translation("server.commands.editprefab.prefabEditorLoadOptions.title"));
      commandBuilder.set("#WarningMessage.TextSpans", Message.translation("server.commands.editprefab.prefabEditorLoadOptions.message"));
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#LoadExistingSessionButton",
         new EventData().append("Action", PrefabEditorLoadOptionsPage.Action.LoadExisting.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating, "#CancelButton", new EventData().append("Action", PrefabEditorLoadOptionsPage.Action.Cancel.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating, "#CreateNewSessionButton", new EventData().append("Action", PrefabEditorLoadOptionsPage.Action.CreateNew.name())
      );
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PrefabEditorLoadOptionsPage.PageData data) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      switch (data.action) {
         case LoadExisting:
            playerComponent.getPageManager().setPage(ref, store, Page.None);
            prefabEditSessionManager.sendToEditWorld(ref, this.world, this.playerRef);
            break;
         case Cancel:
            playerComponent.getPageManager().setPage(ref, store, Page.None);
            break;
         case CreateNew:
            prefabEditSessionManager.exitEditSession(ref, this.world, this.playerRef, store)
               .thenRun(() -> playerComponent.getPageManager().openCustomPage(ref, store, new PrefabEditorLoadSettingsPage(this.playerRef)));
      }
   }

   public static enum Action {
      LoadExisting,
      Cancel,
      CreateNew;
   }

   protected static class PageData {
      public static final BuilderCodec<PrefabEditorLoadOptionsPage.PageData> CODEC = BuilderCodec.builder(
            PrefabEditorLoadOptionsPage.PageData.class, PrefabEditorLoadOptionsPage.PageData::new
         )
         .append(
            new KeyedCodec<>("Action", new EnumCodec<>(PrefabEditorLoadOptionsPage.Action.class, EnumCodec.EnumStyle.LEGACY)),
            (o, action) -> o.action = action,
            o -> o.action
         )
         .add()
         .build();
      public PrefabEditorLoadOptionsPage.Action action;

      public PageData() {
      }
   }
}
