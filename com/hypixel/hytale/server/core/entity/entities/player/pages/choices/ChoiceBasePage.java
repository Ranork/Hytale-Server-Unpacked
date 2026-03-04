package com.hypixel.hytale.server.core.entity.entities.player.pages.choices;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public abstract class ChoiceBasePage extends InteractiveCustomUIPage<ChoiceBasePage.ChoicePageEventData> {
   private final ChoiceElement[] elements;
   private final String pageLayout;

   public ChoiceBasePage(@Nonnull PlayerRef playerRef, ChoiceElement[] elements, String pageLayout) {
      super(playerRef, CustomPageLifetime.CanDismiss, ChoiceBasePage.ChoicePageEventData.CODEC);
      this.elements = elements;
      this.pageLayout = pageLayout;
   }

   protected ChoiceElement[] getElements() {
      return this.elements;
   }

   protected String getPageLayout() {
      return this.pageLayout;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append(this.pageLayout);
      commandBuilder.clear("#ElementList");
      if (this.elements != null && this.elements.length != 0) {
         for (int i = 0; i < this.elements.length; i++) {
            String selector = "#ElementList[" + i + "]";
            ChoiceElement element = this.elements[i];
            element.addButton(commandBuilder, eventBuilder, selector, this.playerRef);
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Index", Integer.toString(i)), false);
         }
      }
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ChoiceBasePage.ChoicePageEventData data) {
      ChoiceElement element = this.elements[data.getIndex()];
      if (element.canFulfillRequirements(store, ref, this.playerRef)) {
         ChoiceInteraction[] interactions = element.getInteractions();

         for (ChoiceInteraction interaction : interactions) {
            interaction.run(store, ref, this.playerRef);
         }
      }
   }

   public static class ChoicePageEventData {
      static final String ELEMENT_INDEX = "Index";
      public static final BuilderCodec<ChoiceBasePage.ChoicePageEventData> CODEC = BuilderCodec.builder(
            ChoiceBasePage.ChoicePageEventData.class, ChoiceBasePage.ChoicePageEventData::new
         )
         .append(new KeyedCodec<>("Index", Codec.STRING), (choicePageEventData, s) -> {
            choicePageEventData.indexStr = s;
            choicePageEventData.index = Integer.parseInt(s);
         }, choicePageEventData -> choicePageEventData.indexStr)
         .add()
         .build();
      private String indexStr;
      private int index;

      public int getIndex() {
         return this.index;
      }
   }
}
