package com.hypixel.hytale.builtin.portals.components.voidevent.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFX;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VoidEventConfig {
   @Nonnull
   public static final BuilderCodec<VoidEventConfig> CODEC = BuilderCodec.builder(VoidEventConfig.class, VoidEventConfig::new)
      .append(new KeyedCodec<>("DurationSeconds", Codec.INTEGER), (config, o) -> config.durationSeconds = o, config -> config.durationSeconds)
      .documentation(
         "How long the void event lasts in seconds. The void event starts at the end of the instance. If your fragment is 10 minutes and this is 180 seconds, it will start 7 minutes in."
      )
      .add()
      .<InvasionPortalConfig>append(
         new KeyedCodec<>("InvasionPortals", InvasionPortalConfig.CODEC), (config, o) -> config.portalConfig = o, config -> config.portalConfig
      )
      .documentation("Configuration regarding the enemy portals that spawn around the players during the event")
      .add()
      .<VoidEventStage[]>append(
         new KeyedCodec<>("Stages", new ArrayCodec<>(VoidEventStage.CODEC, VoidEventStage[]::new)), (config, o) -> config.stages = o, config -> config.stages
      )
      .documentation(
         "Certain event characteristics happen over stages that can be defined here. Stages are spread in time. Only one stage is \"active\" at a time."
      )
      .add()
      .<String>append(new KeyedCodec<>("MusicAmbienceFX", Codec.STRING), (config, o) -> config.musicAmbienceFX = o, config -> config.musicAmbienceFX)
      .documentation("The ID of an AmbienceFX which will be used for the music during the event")
      .addValidator(AmbienceFX.VALIDATOR_CACHE.getValidator())
      .add()
      .afterDecode(VoidEventConfig::processConfig)
      .build();
   private int durationSeconds = 180;
   private InvasionPortalConfig portalConfig;
   private VoidEventStage[] stages;
   private List<VoidEventStage> stagesSortedByStartTime;
   private String musicAmbienceFX;

   public int getDurationSeconds() {
      return this.durationSeconds;
   }

   public int getShouldStartAfterSeconds(int portalTimeLimitSeconds) {
      return Math.max(10, portalTimeLimitSeconds - this.durationSeconds);
   }

   public InvasionPortalConfig getInvasionPortalConfig() {
      return this.portalConfig;
   }

   public VoidEventStage[] getStages() {
      return this.stages;
   }

   public List<VoidEventStage> getStagesSortedByStartTime() {
      return this.stagesSortedByStartTime;
   }

   @Nullable
   public String getMusicAmbienceFX() {
      return this.musicAmbienceFX;
   }

   private void processConfig() {
      this.stagesSortedByStartTime = new ObjectArrayList();
      if (this.stages != null) {
         Collections.addAll(this.stagesSortedByStartTime, this.stages);
         this.stagesSortedByStartTime.sort(Comparator.comparingInt(VoidEventStage::getSecondsInto));
      }
   }
}
