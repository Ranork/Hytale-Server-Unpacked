package com.hypixel.hytale.server.core.asset.type.gameplay.sleep;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import javax.annotation.Nullable;

public class SleepSoundsConfig {
   public static final BuilderCodec<SleepSoundsConfig> CODEC = BuilderCodec.builder(SleepSoundsConfig.class, SleepSoundsConfig::new)
      .append(new KeyedCodec<>("Success", Codec.STRING), (config, o) -> config.success = o, config -> config.success)
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>append(new KeyedCodec<>("Fail", Codec.STRING), (config, o) -> config.fail = o, config -> config.fail)
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>append(new KeyedCodec<>("Notification", Codec.STRING), (config, o) -> config.notification = o, config -> config.notification)
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>append(new KeyedCodec<>("NotificationLoop", Codec.STRING), (config, o) -> config.notificationLoop = o, config -> config.notificationLoop)
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .add()
      .append(
         new KeyedCodec<>("NotificationCooldownSeconds", Codec.INTEGER),
         (config, o) -> config.notificationCooldownSeconds = o,
         config -> config.notificationCooldownSeconds
      )
      .add()
      .append(
         new KeyedCodec<>("NotificationLoopEnabled", Codec.BOOLEAN),
         (config, o) -> config.notificationLoopEnabled = o,
         config -> config.notificationLoopEnabled
      )
      .add()
      .build();
   private String success = "SFX_Sleep_Success";
   private String fail = "SFX_Sleep_Fail";
   private String notification = "SFX_Sleep_Notification";
   private String notificationLoop = "SFX_Sleep_Notification_Loop";
   private int notificationCooldownSeconds = 30;
   private boolean notificationLoopEnabled = true;

   @Nullable
   public String getSuccess() {
      return this.success;
   }

   public int getSuccessIndex() {
      return SoundEvent.getAssetMap().getIndex(this.success);
   }

   @Nullable
   public String getFail() {
      return this.fail;
   }

   public int getFailIndex() {
      return SoundEvent.getAssetMap().getIndex(this.fail);
   }

   @Nullable
   public String getNotification() {
      return this.notification;
   }

   public int getNotificationIndex() {
      return SoundEvent.getAssetMap().getIndex(this.notification);
   }

   @Nullable
   public String getNotificationLoop() {
      return this.notificationLoop;
   }

   public int getNotificationLoopIndex() {
      return SoundEvent.getAssetMap().getIndex(this.notificationLoop);
   }

   public long getNotificationCooldownSeconds() {
      return this.notificationCooldownSeconds;
   }

   public long getNotificationLoopCooldownMs() {
      return this.notificationCooldownSeconds * 1000;
   }

   public boolean isNotificationLoopEnabled() {
      return this.notificationLoopEnabled;
   }
}
