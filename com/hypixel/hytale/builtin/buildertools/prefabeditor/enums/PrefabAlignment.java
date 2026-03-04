package com.hypixel.hytale.builtin.buildertools.prefabeditor.enums;

public enum PrefabAlignment {
   ANCHOR("server.commands.editprefab.ui.alignment.anchor"),
   ZERO("server.commands.editprefab.ui.alignment.zero");

   private final String localizationString;

   private PrefabAlignment(String localizationString) {
      this.localizationString = localizationString;
   }

   public String getLocalizationString() {
      return this.localizationString;
   }
}
