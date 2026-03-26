package com.hypixel.hytale.builtin.buildertools.prefablist;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsUserData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.browser.AssetPackSaveBrowser;
import com.hypixel.hytale.server.core.ui.browser.AssetPackSaveBrowserConfig;
import com.hypixel.hytale.server.core.ui.browser.AssetPackSaveBrowserEventData;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabSavePage extends InteractiveCustomUIPage<PrefabSavePage.PageData> {
   @Nonnull
   private static final Message MESSAGE_SERVER_BUILDER_TOOLS_PREFAB_SAVE_NAME_REQUIRED = Message.translation("server.builderTools.prefabSave.nameRequired");
   @Nonnull
   private static final Message MESSAGE_PACK_REQUIRED = Message.translation("server.customUI.assetPackBrowser.packRequired");
   private final AssetPackSaveBrowser packBrowser = new AssetPackSaveBrowser(AssetPackSaveBrowserConfig.defaults());
   private boolean initialized = false;

   public PrefabSavePage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PrefabSavePage.PageData.CODEC);
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      if (!this.initialized) {
         this.initialized = true;
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         if (playerComponent != null) {
            String lastPack = BuilderToolsUserData.get(playerComponent).getLastSavePack();
            this.packBrowser.setSelectedPackKey(lastPack);
            if (lastPack != null && !this.packBrowser.hasSelectedPack()) {
               this.playerRef.sendMessage(Message.translation("server.customUI.assetPackBrowser.packNoLongerAvailable"));
            }
         }
      }

      commandBuilder.append("Pages/PrefabSavePage.ui");
      commandBuilder.set("#PackBrowserPage.Visible", false);
      commandBuilder.set("#CreatePackPage.Visible", false);
      if (this.packBrowser.hasSelectedPack()) {
         commandBuilder.set("#MainPage #SelectedPackLabel.Text", this.packBrowser.getSelectedPackDisplayName());
      }

      commandBuilder.set("#MainPage #Entities #CheckBox.Value", true);
      commandBuilder.set("#MainPage #Empty #CheckBox.Value", false);
      commandBuilder.set("#MainPage #Overwrite #CheckBox.Value", false);
      commandBuilder.set("#MainPage #FromClipboard #CheckBox.Value", false);
      commandBuilder.set("#MainPage #UsePlayerAnchor #CheckBox.Value", false);
      commandBuilder.set("#MainPage #ClearSupport #CheckBox.Value", false);
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#MainPage #SaveButton",
         new EventData()
            .append("Action", PrefabSavePage.Action.Save.name())
            .append("@Name", "#MainPage #NameInput.Value")
            .append("@Entities", "#MainPage #Entities #CheckBox.Value")
            .append("@Empty", "#MainPage #Empty #CheckBox.Value")
            .append("@Overwrite", "#MainPage #Overwrite #CheckBox.Value")
            .append("@FromClipboard", "#MainPage #FromClipboard #CheckBox.Value")
            .append("@UsePlayerAnchor", "#MainPage #UsePlayerAnchor #CheckBox.Value")
            .append("@ClearSupport", "#MainPage #ClearSupport #CheckBox.Value")
      );
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating, "#MainPage #CancelButton", new EventData().append("Action", PrefabSavePage.Action.Cancel.name())
      );
      this.packBrowser.buildEventBindings(eventBuilder, "#MainPage #BrowsePackButton");
      this.packBrowser.buildUI(commandBuilder, eventBuilder);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PrefabSavePage.PageData data) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      AssetPackSaveBrowser.ActionResult packResult = this.packBrowser
         .handleAction(data.action != null ? data.action.name() : null, data.packBrowserData, "#MainPage #SelectedPackLabel");
      if (packResult != null) {
         if (packResult.errorKey() != null) {
            this.playerRef.sendMessage(Message.translation(packResult.errorKey()));
         }

         if (packResult.packConfirmed() && this.packBrowser.hasSelectedPack()) {
            BuilderToolsUserData.get(playerComponent).setLastSavePack(this.packBrowser.getSelectedPack().getName());
         }

         this.sendUpdate(packResult.commandBuilder(), packResult.eventBuilder(), false);
      } else {
         switch (data.action) {
            case Save:
               if (data.name == null || data.name.isBlank()) {
                  this.playerRef.sendMessage(MESSAGE_SERVER_BUILDER_TOOLS_PREFAB_SAVE_NAME_REQUIRED);
                  this.sendUpdate(null, null, false);
                  return;
               }

               AssetPack targetPack = this.packBrowser.getSelectedPack();
               if (targetPack == null) {
                  this.playerRef.sendMessage(MESSAGE_PACK_REQUIRED);
                  this.sendUpdate(null, null, false);
                  return;
               }

               BuilderToolsUserData.get(playerComponent).setLastSavePack(targetPack.getName());
               playerComponent.getPageManager().setPage(ref, store, Page.None);
               Vector3i playerAnchor = this.getPlayerAnchor(ref, store, data.usePlayerAnchor && !data.fromClipboard);
               BuilderToolsPlugin.addToQueue(
                  playerComponent,
                  this.playerRef,
                  (r, s, componentAccessor) -> {
                     if (data.fromClipboard) {
                        s.save(r, data.name, true, data.overwrite, data.clearSupport, targetPack, componentAccessor);
                     } else {
                        s.saveFromSelection(
                           r, data.name, true, data.overwrite, data.entities, data.empty, playerAnchor, data.clearSupport, targetPack, componentAccessor
                        );
                     }
                  }
               );
               break;
            case Cancel:
               playerComponent.getPageManager().setPage(ref, store, Page.None);
         }
      }
   }

   @Nullable
   private Vector3i getPlayerAnchor(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, boolean usePlayerAnchor) {
      if (!usePlayerAnchor) {
         return null;
      } else {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent == null) {
            return null;
         } else {
            Vector3d position = transformComponent.getPosition();
            return new Vector3i(MathUtil.floor(position.getX()), MathUtil.floor(position.getY()), MathUtil.floor(position.getZ()));
         }
      }
   }

   public static enum Action {
      Save,
      Cancel,
      OpenPackBrowser,
      ConfirmPackBrowser,
      CancelPackBrowser,
      OpenCreatePack,
      CreatePack,
      CancelCreatePack,
      PackSearch,
      PackSelect;
   }

   protected static class PageData {
      public static final String NAME = "@Name";
      public static final String ENTITIES = "@Entities";
      public static final String EMPTY = "@Empty";
      public static final String OVERWRITE = "@Overwrite";
      public static final String FROM_CLIPBOARD = "@FromClipboard";
      public static final String USE_PLAYER_ANCHOR = "@UsePlayerAnchor";
      public static final String CLEAR_SUPPORT = "@ClearSupport";
      public static final BuilderCodec<PrefabSavePage.PageData> CODEC = BuilderCodec.builder(PrefabSavePage.PageData.class, PrefabSavePage.PageData::new)
         .append(
            new KeyedCodec<>("Action", new EnumCodec<>(PrefabSavePage.Action.class, EnumCodec.EnumStyle.LEGACY)),
            (o, action) -> o.action = action,
            o -> o.action
         )
         .add()
         .append(new KeyedCodec<>("@Name", Codec.STRING), (o, name) -> o.name = name, o -> o.name)
         .add()
         .append(new KeyedCodec<>("@Entities", Codec.BOOLEAN), (o, entities) -> o.entities = entities, o -> o.entities)
         .add()
         .append(new KeyedCodec<>("@Empty", Codec.BOOLEAN), (o, empty) -> o.empty = empty, o -> o.empty)
         .add()
         .append(new KeyedCodec<>("@Overwrite", Codec.BOOLEAN), (o, overwrite) -> o.overwrite = overwrite, o -> o.overwrite)
         .add()
         .append(new KeyedCodec<>("@FromClipboard", Codec.BOOLEAN), (o, fromClipboard) -> o.fromClipboard = fromClipboard, o -> o.fromClipboard)
         .add()
         .append(new KeyedCodec<>("@UsePlayerAnchor", Codec.BOOLEAN), (o, usePlayerAnchor) -> o.usePlayerAnchor = usePlayerAnchor, o -> o.usePlayerAnchor)
         .add()
         .append(new KeyedCodec<>("@ClearSupport", Codec.BOOLEAN), (o, clearSupport) -> o.clearSupport = clearSupport, o -> o.clearSupport)
         .add()
         .append(new KeyedCodec<>("Pack", Codec.STRING), (o, s) -> o.packBrowserData.pack = s, o -> o.packBrowserData.pack)
         .add()
         .append(new KeyedCodec<>("@PackSearch", Codec.STRING), (o, s) -> o.packBrowserData.search = s, o -> o.packBrowserData.search)
         .add()
         .append(new KeyedCodec<>("@CreateName", Codec.STRING), (o, s) -> o.packBrowserData.createName = s, o -> o.packBrowserData.createName)
         .add()
         .append(new KeyedCodec<>("@CreateGroup", Codec.STRING), (o, s) -> o.packBrowserData.createGroup = s, o -> o.packBrowserData.createGroup)
         .add()
         .append(
            new KeyedCodec<>("@CreateDescription", Codec.STRING), (o, s) -> o.packBrowserData.createDescription = s, o -> o.packBrowserData.createDescription
         )
         .add()
         .append(new KeyedCodec<>("@CreateVersion", Codec.STRING), (o, s) -> o.packBrowserData.createVersion = s, o -> o.packBrowserData.createVersion)
         .add()
         .append(new KeyedCodec<>("@CreateWebsite", Codec.STRING), (o, s) -> o.packBrowserData.createWebsite = s, o -> o.packBrowserData.createWebsite)
         .add()
         .append(new KeyedCodec<>("@CreateAuthorName", Codec.STRING), (o, s) -> o.packBrowserData.createAuthorName = s, o -> o.packBrowserData.createAuthorName)
         .add()
         .append(new KeyedCodec<>("ValidateCreate", Codec.STRING), (o, s) -> o.packBrowserData.validateCreate = s, o -> o.packBrowserData.validateCreate)
         .add()
         .build();
      public PrefabSavePage.Action action;
      public String name;
      public boolean entities = true;
      public boolean empty = false;
      public boolean overwrite = false;
      public boolean fromClipboard = false;
      public boolean usePlayerAnchor = false;
      public boolean clearSupport = false;
      public final AssetPackSaveBrowserEventData packBrowserData = new AssetPackSaveBrowserEventData();

      public PageData() {
      }
   }
}
