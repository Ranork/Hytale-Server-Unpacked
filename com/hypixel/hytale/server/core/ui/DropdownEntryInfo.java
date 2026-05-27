package com.hypixel.hytale.server.core.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class DropdownEntryInfo {
   public static final BuilderCodec<DropdownEntryInfo> CODEC = BuilderCodec.builder(DropdownEntryInfo.class, DropdownEntryInfo::new)
      .addField(new KeyedCodec<>("Label", LocalizableString.CODEC), (entryInfo, label) -> entryInfo.label = label, entryInfo -> entryInfo.label)
      .addField(new KeyedCodec<>("Value", Codec.STRING), (entryInfo, value) -> entryInfo.value = value, entryInfo -> entryInfo.value)
      .addField(
         new KeyedCodec<>("Tooltip", LocalizableString.CODEC, false), (entryInfo, tooltip) -> entryInfo.tooltip = tooltip, entryInfo -> entryInfo.tooltip
      )
      .build();
   private LocalizableString label;
   private String value;
   private LocalizableString tooltip;

   public DropdownEntryInfo(LocalizableString label, String value) {
      this.label = label;
      this.value = value;
   }

   public DropdownEntryInfo(LocalizableString label, String value, LocalizableString tooltip) {
      this.label = label;
      this.value = value;
      this.tooltip = tooltip;
   }

   private DropdownEntryInfo() {
   }
}
