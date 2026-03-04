package com.hypixel.hytale.builtin.buildertools.prefabeditor.enums;

public enum WorldGenType {
   FLAT("server.commands.editprefab.ui.worldGenType.flat"),
   VOID("server.commands.editprefab.ui.worldGenType.void");

   private final String localizationString;

   private WorldGenType(String localizationString) {
      this.localizationString = localizationString;
   }

   public String getLocalizationString() {
      return this.localizationString;
   }
}
