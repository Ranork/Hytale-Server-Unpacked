package com.hypixel.hytale.server.npc.metadata;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.npc.storage.AlarmStore;
import javax.annotation.Nullable;

public class CapturedNPCMetadata {
   public static final String KEY = "CapturedEntity";
   public static final BuilderCodec<CapturedNPCMetadata> CODEC = BuilderCodec.builder(CapturedNPCMetadata.class, CapturedNPCMetadata::new)
      .appendInherited(
         new KeyedCodec<>("IconPath", Codec.STRING), (meta, s) -> meta.iconPath = s, meta -> meta.iconPath, (meta, parent) -> meta.iconPath = parent.iconPath
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("NpcNameKey", Codec.STRING),
         (meta, s) -> meta.npcNameKey = s,
         meta -> meta.npcNameKey,
         (meta, parent) -> meta.npcNameKey = parent.npcNameKey
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("FullItemIcon", Codec.STRING),
         (meta, s) -> meta.fullItemIcon = s,
         meta -> meta.fullItemIcon,
         (meta, parent) -> meta.fullItemIcon = parent.fullItemIcon
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AlarmStore", AlarmStore.CODEC),
         (meta, store) -> meta.alarmStore = store,
         meta -> meta.alarmStore,
         (meta, parent) -> meta.alarmStore = parent.alarmStore
      )
      .add()
      .build();
   public static final KeyedCodec<CapturedNPCMetadata> KEYED_CODEC = new KeyedCodec<>("CapturedEntity", CODEC);
   private String iconPath;
   private String npcNameKey;
   private String fullItemIcon;
   @Nullable
   private AlarmStore alarmStore;

   public String getIconPath() {
      return this.iconPath;
   }

   public String getNpcNameKey() {
      return this.npcNameKey;
   }

   public String getFullItemIcon() {
      return this.fullItemIcon;
   }

   @Nullable
   public AlarmStore getAlarmStore() {
      return this.alarmStore;
   }

   public void setIconPath(String iconPath) {
      this.iconPath = iconPath;
   }

   public void setNpcNameKey(String npcNameKey) {
      this.npcNameKey = npcNameKey;
   }

   public void setFullItemIcon(String fullItemIcon) {
      this.fullItemIcon = fullItemIcon;
   }

   public void setAlarmStore(@Nullable AlarmStore alarmStore) {
      this.alarmStore = alarmStore;
   }
}
