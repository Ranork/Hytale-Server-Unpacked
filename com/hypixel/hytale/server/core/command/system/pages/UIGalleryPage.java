package com.hypixel.hytale.server.core.command.system.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import javax.annotation.Nonnull;

public class UIGalleryPage extends InteractiveCustomUIPage<UIGalleryPage.UIGalleryEventData> {
   private static final Value<String> CATEGORY_BUTTON_STYLE = Value.ref("Pages/UIGallery/CategoryButton.ui", "LabelStyle");
   private static final Value<String> CATEGORY_BUTTON_SELECTED_STYLE = Value.ref("Pages/UIGallery/CategoryButton.ui", "SelectedLabelStyle");
   private UIGalleryPage.Category selectedCategory = UIGalleryPage.Category.BUTTONS;
   private final IntSet expandedCodeBlocks = new IntOpenHashSet();

   public UIGalleryPage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismiss, UIGalleryPage.UIGalleryEventData.CODEC);
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/UIGallery/UIGalleryPage.ui");
      this.buildCategoryList(commandBuilder, eventBuilder);
      this.displayCategory(this.selectedCategory, commandBuilder, eventBuilder);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UIGalleryPage.UIGalleryEventData data) {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      UIEventBuilder eventBuilder = new UIEventBuilder();
      if (data.category != null) {
         UIGalleryPage.Category newCategory = UIGalleryPage.Category.fromId(data.category);
         if (newCategory != this.selectedCategory) {
            int oldIndex = this.selectedCategory.ordinal();
            commandBuilder.set("#CategoryList[" + oldIndex + "].Style", CATEGORY_BUTTON_STYLE);
            int newIndex = newCategory.ordinal();
            commandBuilder.set("#CategoryList[" + newIndex + "].Style", CATEGORY_BUTTON_SELECTED_STYLE);
            this.expandedCodeBlocks.clear();
            this.selectedCategory = newCategory;
            this.displayCategory(this.selectedCategory, commandBuilder, eventBuilder);
         }

         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else if (data.toggleCode != null) {
         try {
            int codeIndex = Integer.parseInt(data.toggleCode);
            this.toggleCodeBlock(codeIndex, commandBuilder);
            this.sendUpdate(commandBuilder, eventBuilder, false);
         } catch (NumberFormatException var9) {
         }
      }
   }

   private void buildCategoryList(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      commandBuilder.clear("#CategoryList");

      for (int i = 0; i < UIGalleryPage.Category.values().length; i++) {
         UIGalleryPage.Category category = UIGalleryPage.Category.values()[i];
         commandBuilder.append("#CategoryList", "Pages/UIGallery/CategoryButton.ui");
         commandBuilder.set("#CategoryList[" + i + "].TextSpans", Message.translation(category.getNameKey()));
         if (category == this.selectedCategory) {
            commandBuilder.set("#CategoryList[" + i + "].Style", CATEGORY_BUTTON_SELECTED_STYLE);
         }

         eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CategoryList[" + i + "]", EventData.of("Category", category.getId()));
      }
   }

   private void displayCategory(@Nonnull UIGalleryPage.Category category, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      commandBuilder.set("#CategoryTitle.TextSpans", Message.translation(category.getNameKey()));
      commandBuilder.set("#CategoryDescription.TextSpans", Message.translation(category.getDescriptionKey()));
      commandBuilder.clear("#CategoryContent");
      commandBuilder.append("#CategoryContent", category.getContentPath());

      for (int i = 0; i < category.getCodeBlockCount(); i++) {
         String selector = "#CategoryContent #Code" + i + " #ToggleCode";
         eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("ToggleCode", String.valueOf(i)));
      }
   }

   private void toggleCodeBlock(int index, @Nonnull UICommandBuilder commandBuilder) {
      String containerSelector = "#CategoryContent #Code" + index + " #CodeContainer";
      String buttonSelector = "#CategoryContent #Code" + index + " #ToggleCode";
      if (this.expandedCodeBlocks.contains(index)) {
         this.expandedCodeBlocks.remove(index);
         commandBuilder.set(containerSelector + ".Visible", false);
         commandBuilder.set(buttonSelector + ".TextSpans", Message.raw("Show Code"));
      } else {
         this.expandedCodeBlocks.add(index);
         commandBuilder.set(containerSelector + ".Visible", true);
         commandBuilder.set(buttonSelector + ".TextSpans", Message.raw("Hide Code"));
      }
   }

   private static enum Category {
      BUTTONS(
         "buttons",
         "server.customUI.uiGallery.category.buttons",
         "server.customUI.uiGallery.category.buttons.desc",
         "Pages/UIGallery/Categories/ButtonsContent.ui",
         7
      ),
      INPUTS(
         "inputs",
         "server.customUI.uiGallery.category.inputs",
         "server.customUI.uiGallery.category.inputs.desc",
         "Pages/UIGallery/Categories/InputsContent.ui",
         4
      ),
      SELECTION(
         "selection",
         "server.customUI.uiGallery.category.selection",
         "server.customUI.uiGallery.category.selection.desc",
         "Pages/UIGallery/Categories/SelectionContent.ui",
         4
      ),
      CONTAINERS(
         "containers",
         "server.customUI.uiGallery.category.containers",
         "server.customUI.uiGallery.category.containers.desc",
         "Pages/UIGallery/Categories/ContainersContent.ui",
         7
      ),
      TEXT("text", "server.customUI.uiGallery.category.text", "server.customUI.uiGallery.category.text.desc", "Pages/UIGallery/Categories/TextContent.ui", 3),
      SLIDERS(
         "sliders",
         "server.customUI.uiGallery.category.sliders",
         "server.customUI.uiGallery.category.sliders.desc",
         "Pages/UIGallery/Categories/SlidersContent.ui",
         3
      ),
      PROGRESS(
         "progress",
         "server.customUI.uiGallery.category.progress",
         "server.customUI.uiGallery.category.progress.desc",
         "Pages/UIGallery/Categories/ProgressContent.ui",
         3
      ),
      SCROLLBARS(
         "scrollbars",
         "server.customUI.uiGallery.category.scrollbars",
         "server.customUI.uiGallery.category.scrollbars.desc",
         "Pages/UIGallery/Categories/ScrollbarsContent.ui",
         3
      ),
      NAVIGATION(
         "navigation",
         "server.customUI.uiGallery.category.navigation",
         "server.customUI.uiGallery.category.navigation.desc",
         "Pages/UIGallery/Categories/NavigationContent.ui",
         2
      ),
      TOOLTIPS(
         "tooltips",
         "server.customUI.uiGallery.category.tooltips",
         "server.customUI.uiGallery.category.tooltips.desc",
         "Pages/UIGallery/Categories/TooltipsContent.ui",
         2
      );

      private final String id;
      private final String nameKey;
      private final String descriptionKey;
      private final String contentPath;
      private final int codeBlockCount;

      private Category(String id, String nameKey, String descriptionKey, String contentPath, int codeBlockCount) {
         this.id = id;
         this.nameKey = nameKey;
         this.descriptionKey = descriptionKey;
         this.contentPath = contentPath;
         this.codeBlockCount = codeBlockCount;
      }

      public String getId() {
         return this.id;
      }

      public String getNameKey() {
         return this.nameKey;
      }

      public String getDescriptionKey() {
         return this.descriptionKey;
      }

      public String getContentPath() {
         return this.contentPath;
      }

      public int getCodeBlockCount() {
         return this.codeBlockCount;
      }

      public static UIGalleryPage.Category fromId(String id) {
         for (UIGalleryPage.Category category : values()) {
            if (category.id.equals(id)) {
               return category;
            }
         }

         return BUTTONS;
      }
   }

   public static class UIGalleryEventData {
      static final String KEY_CATEGORY = "Category";
      static final String KEY_TOGGLE_CODE = "ToggleCode";
      public static final BuilderCodec<UIGalleryPage.UIGalleryEventData> CODEC = BuilderCodec.builder(
            UIGalleryPage.UIGalleryEventData.class, UIGalleryPage.UIGalleryEventData::new
         )
         .addField(new KeyedCodec<>("Category", Codec.STRING), (entry, s) -> entry.category = s, entry -> entry.category)
         .addField(new KeyedCodec<>("ToggleCode", Codec.STRING), (entry, s) -> entry.toggleCode = s, entry -> entry.toggleCode)
         .build();
      private String category;
      private String toggleCode;
   }
}
