package com.hypixel.hytale.builtin.teleport;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class WarpListPage extends InteractiveCustomUIPage<WarpListPage.WarpListPageEventData> {
   @Nonnull
   private static final String PAGE_UI_FILE = "Pages/WarpEntryButton.ui";
   private final Consumer<String> callback;
   private final Map<String, Warp> warps;
   @Nonnull
   private String searchQuery = "";

   public WarpListPage(@Nonnull PlayerRef playerRef, Map<String, Warp> warps, Consumer<String> callback) {
      super(playerRef, CustomPageLifetime.CanDismiss, WarpListPage.WarpListPageEventData.CODEC);
      this.warps = warps;
      this.callback = callback;
   }

   private void buildWarpList(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      commandBuilder.clear("#WarpList");
      ObjectArrayList<String> warps = new ObjectArrayList(this.warps.keySet());
      if (warps.isEmpty()) {
         commandBuilder.appendInline("#WarpList", "Label { Text: %server.customUI.warpListPage.noWarps; Style: (Alignment: Center); }");
      } else {
         if (!this.searchQuery.isEmpty()) {
            warps.removeIf(w -> !w.toLowerCase().contains(this.searchQuery));
         }

         Collections.sort(warps);
         int i = 0;

         for (int bound = warps.size(); i < bound; i++) {
            String selector = "#WarpList[" + i + "]";
            String warp = (String)warps.get(i);
            commandBuilder.append("#WarpList", "Pages/WarpEntryButton.ui");
            commandBuilder.set(selector + " #Name.Text", warp);
            commandBuilder.set(selector + " #World.Text", this.warps.get(warp).getWorld());
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Warp", warp), false);
         }
      }
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/WarpListPage.ui");
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"));
      this.buildWarpList(commandBuilder, eventBuilder);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WarpListPage.WarpListPageEventData eventData) {
      if (eventData.getWarp() != null) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getPageManager().setPage(ref, store, Page.None);
         this.callback.accept(eventData.getWarp());
      } else if (eventData.getSearchQuery() != null) {
         this.searchQuery = eventData.getSearchQuery().trim().toLowerCase();
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.buildWarpList(commandBuilder, eventBuilder);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      }
   }

   public static class WarpListPageEventData {
      static final String KEY_WARP = "Warp";
      static final String KEY_SEARCH_QUERY = "@SearchQuery";
      @Nonnull
      public static final BuilderCodec<WarpListPage.WarpListPageEventData> CODEC = BuilderCodec.builder(
            WarpListPage.WarpListPageEventData.class, WarpListPage.WarpListPageEventData::new
         )
         .append(new KeyedCodec<>("Warp", Codec.STRING), (entry, s) -> entry.warp = s, entry -> entry.warp)
         .add()
         .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, entry -> entry.searchQuery)
         .add()
         .build();
      private String warp;
      private String searchQuery;

      public String getWarp() {
         return this.warp;
      }

      public String getSearchQuery() {
         return this.searchQuery;
      }
   }
}
