package com.hypixel.hytale.server.core.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class DropdownEntryInfo {
   public static final BuilderCodec<DropdownEntryInfo> CODEC = BuilderCodec.builder(DropdownEntryInfo.class, DropdownEntryInfo::new)
      .addField(new KeyedCodec<>("Label", LocalizableString.CODEC), (p, t) -> p.label = t, p -> p.label)
      .addField(new KeyedCodec<>("Value", Codec.STRING), (p, t) -> p.value = t, p -> p.value)
      .build();
   private LocalizableString label;
   private String value;

   public DropdownEntryInfo(LocalizableString label, String value) {
      this.label = label;
      this.value = value;
   }

   private DropdownEntryInfo() {
   }
}
