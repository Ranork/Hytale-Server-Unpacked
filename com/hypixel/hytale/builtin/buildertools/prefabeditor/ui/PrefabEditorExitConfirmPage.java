package com.hypixel.hytale.builtin.buildertools.prefabeditor.ui;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
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
import java.util.List;
import javax.annotation.Nonnull;

public class PrefabEditorExitConfirmPage extends InteractiveCustomUIPage<PrefabEditorExitConfirmPage.PageData> {
   @Nonnull
   private final PrefabEditSession prefabEditSession;
   @Nonnull
   private final World world;
   @Nonnull
   private final List<PrefabEditingMetadata> dirtyPrefabs;

   public PrefabEditorExitConfirmPage(
      @Nonnull PlayerRef playerRef, @Nonnull PrefabEditSession prefabEditSession, @Nonnull World world, @Nonnull List<PrefabEditingMetadata> dirtyPrefabs
   ) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PrefabEditorExitConfirmPage.PageData.CODEC);
      this.prefabEditSession = prefabEditSession;
      this.world = world;
      this.dirtyPrefabs = dirtyPrefabs;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/PrefabEditorExitConfirm.ui");
      commandBuilder.set("#WarningTitle.TextSpans", Message.translation("server.commands.editprefab.exit.unsavedWarning.title"));
      commandBuilder.set(
         "#WarningMessage.TextSpans", Message.translation("server.commands.editprefab.exit.unsavedWarning.message").param("count", this.dirtyPrefabs.size())
      );
      int index = 0;

      for (PrefabEditingMetadata prefab : this.dirtyPrefabs) {
         String fullPath = prefab.getPrefabPath().toString().replace('\\', '/');
         String fileName = prefab.getPrefabPath().getFileName().toString();
         String displayName = fileName.endsWith(".prefab.json") ? fileName.substring(0, fileName.length() - ".prefab.json".length()) : fileName;
         commandBuilder.append("#PrefabList", "Pages/BasicTextButton.ui");
         commandBuilder.set("#PrefabList[" + index + "].Text", "• " + displayName);
         commandBuilder.set("#PrefabList[" + index + "].TooltipText", fullPath);
         index++;
      }

      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating, "#ConfirmExitButton", new EventData().append("Action", PrefabEditorExitConfirmPage.Action.ConfirmExit.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating, "#CancelButton", new EventData().append("Action", PrefabEditorExitConfirmPage.Action.Cancel.name())
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating, "#SaveAndExitButton", new EventData().append("Action", PrefabEditorExitConfirmPage.Action.SaveAndExit.name())
      );
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PrefabEditorExitConfirmPage.PageData data) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      switch (data.action) {
         case ConfirmExit:
            playerComponent.getPageManager().setPage(ref, store, Page.None);
            PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
            prefabEditSessionManager.exitEditSession(ref, this.world, this.playerRef, store);
            break;
         case Cancel:
            playerComponent.getPageManager().setPage(ref, store, Page.None);
            break;
         case SaveAndExit:
            playerComponent.getPageManager().openCustomPage(ref, store, new PrefabEditorSaveSettingsPage(this.playerRef, this.prefabEditSession));
      }
   }

   public static enum Action {
      ConfirmExit,
      Cancel,
      SaveAndExit;
   }

   protected static class PageData {
      public static final BuilderCodec<PrefabEditorExitConfirmPage.PageData> CODEC = BuilderCodec.builder(
            PrefabEditorExitConfirmPage.PageData.class, PrefabEditorExitConfirmPage.PageData::new
         )
         .append(
            new KeyedCodec<>("Action", new EnumCodec<>(PrefabEditorExitConfirmPage.Action.class, EnumCodec.EnumStyle.LEGACY)),
            (o, action) -> o.action = action,
            o -> o.action
         )
         .add()
         .build();
      public PrefabEditorExitConfirmPage.Action action;

      public PageData() {
      }
   }
}
