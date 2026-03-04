package com.hypixel.hytale.server.core.entity.entities.player.pages.audio;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.AudioUtil;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlaySoundPage extends InteractiveCustomUIPage<PlaySoundPage.PlaySoundPageEventData> {
   private static final String COMMON_TEXT_BUTTON_DOCUMENT = "Common/TextButton.ui";
   private static final Value<String> BUTTON_LABEL_STYLE = Value.ref("Common/TextButton.ui", "LabelStyle");
   private static final Value<String> BUTTON_LABEL_STYLE_SELECTED = Value.ref("Common/TextButton.ui", "SelectedLabelStyle");
   @Nonnull
   private String searchQuery = "";
   private List<String> soundEvents;
   @Nullable
   private String selectedSoundEvent;
   private float volumeDecibels = 0.0F;
   private float pitchSemitones = 0.0F;

   public PlaySoundPage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismiss, PlaySoundPage.PlaySoundPageEventData.CODEC);
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/PlaySoundPage.ui");
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#VolumeSlider", EventData.of("@Volume", "#VolumeSlider.Value"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#PitchSlider", EventData.of("@Pitch", "#PitchSlider.Value"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Play", new EventData().append("Type", "Play"), false);
      this.buildSoundEventList(ref, commandBuilder, eventBuilder, store);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PlaySoundPage.PlaySoundPageEventData data) {
      if (data.searchQuery != null) {
         this.searchQuery = data.searchQuery.trim().toLowerCase();
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.buildSoundEventList(ref, commandBuilder, eventBuilder, store);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else if (data.volume != null) {
         this.volumeDecibels = data.volume;
         UICommandBuilder commandBuilder = new UICommandBuilder();
         commandBuilder.set("#VolumeValue.Text", String.valueOf((int)this.volumeDecibels));
         this.sendUpdate(commandBuilder, null, false);
      } else if (data.pitch != null) {
         this.pitchSemitones = data.pitch;
         UICommandBuilder commandBuilder = new UICommandBuilder();
         commandBuilder.set("#PitchValue.Text", String.valueOf(this.pitchSemitones));
         this.sendUpdate(commandBuilder, null, false);
      } else {
         String var11 = data.type;
         switch (var11) {
            case "Select":
               if (data.soundEvent != null) {
                  UICommandBuilder commandBuilder = new UICommandBuilder();
                  this.selectSoundEvent(ref, data.soundEvent, commandBuilder, store);
                  this.sendUpdate(commandBuilder, null, false);
               }
               break;
            case "Play":
               if (this.selectedSoundEvent != null) {
                  int index = SoundEvent.getAssetMap().getIndex(this.selectedSoundEvent);
                  float linearVolume = AudioUtil.decibelsToLinearGain(this.volumeDecibels);
                  float linearPitch = AudioUtil.semitonesToLinearPitch(this.pitchSemitones);
                  SoundUtil.playSoundEvent2d(ref, index, SoundCategory.SFX, linearVolume, linearPitch, store);
               }
         }
      }
   }

   private void buildSoundEventList(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      commandBuilder.clear("#SoundList");
      int soundEventCount = SoundEvent.getAssetMap().getAssetMap().size();
      if (!this.searchQuery.isEmpty()) {
         Object2IntMap<String> map = new Object2IntOpenHashMap(soundEventCount);

         for (String value : SoundEvent.getAssetMap().getAssetMap().keySet()) {
            int fuzzyDistance = StringCompareUtil.getFuzzyDistance(value, this.searchQuery, Locale.ENGLISH);
            if (fuzzyDistance > 0) {
               map.put(value, fuzzyDistance);
            }
         }

         this.soundEvents = map.keySet().stream().sorted().sorted(Comparator.comparingInt(map::getInt).reversed()).limit(20L).collect(Collectors.toList());
      } else {
         this.soundEvents = SoundEvent.getAssetMap().getAssetMap().keySet().stream().sorted(String::compareTo).collect(Collectors.toList());
      }

      int i = 0;

      for (int bound = this.soundEvents.size(); i < bound; i++) {
         String id = this.soundEvents.get(i);
         String selector = "#SoundList[" + i + "]";
         commandBuilder.append("#SoundList", "Common/TextButton.ui");
         commandBuilder.set(selector + " #Button.Text", id);
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating, selector + " #Button", new EventData().append("Type", "Select").append("SoundEvent", id), false
         );
      }

      if (!this.soundEvents.isEmpty()) {
         if (!this.soundEvents.contains(this.selectedSoundEvent)) {
            this.selectSoundEvent(ref, this.soundEvents.getFirst(), commandBuilder, componentAccessor);
         } else if (this.selectedSoundEvent != null) {
            this.selectSoundEvent(ref, this.selectedSoundEvent, commandBuilder, componentAccessor);
         }
      }
   }

   private void selectSoundEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull String soundEvent,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.selectedSoundEvent != null && this.soundEvents.contains(this.selectedSoundEvent)) {
         commandBuilder.set("#SoundList[" + this.soundEvents.indexOf(this.selectedSoundEvent) + "] #Button.Style", BUTTON_LABEL_STYLE);
      }

      commandBuilder.set("#SoundList[" + this.soundEvents.indexOf(soundEvent) + "] #Button.Style", BUTTON_LABEL_STYLE_SELECTED);
      commandBuilder.set("#SoundName.Text", soundEvent);
      this.selectedSoundEvent = soundEvent;
   }

   public static class PlaySoundPageEventData {
      static final String KEY_SOUND_EVENT = "SoundEvent";
      static final String KEY_TYPE = "Type";
      static final String KEY_SEARCH_QUERY = "@SearchQuery";
      static final String KEY_VOLUME = "@Volume";
      static final String KEY_PITCH = "@Pitch";
      public static final BuilderCodec<PlaySoundPage.PlaySoundPageEventData> CODEC = BuilderCodec.builder(
            PlaySoundPage.PlaySoundPageEventData.class, PlaySoundPage.PlaySoundPageEventData::new
         )
         .append(new KeyedCodec<>("SoundEvent", Codec.STRING), (entry, s) -> entry.soundEvent = s, entry -> entry.soundEvent)
         .add()
         .append(new KeyedCodec<>("Type", Codec.STRING), (entry, s) -> entry.type = s, entry -> entry.type)
         .add()
         .append(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, entry -> entry.searchQuery)
         .add()
         .append(new KeyedCodec<>("@Volume", Codec.FLOAT), (entry, f) -> entry.volume = f, entry -> entry.volume)
         .add()
         .append(new KeyedCodec<>("@Pitch", Codec.FLOAT), (entry, f) -> entry.pitch = f, entry -> entry.pitch)
         .add()
         .build();
      private String soundEvent;
      private String type;
      private String searchQuery;
      private Float volume;
      private Float pitch;
   }
}
