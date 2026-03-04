package com.hypixel.hytale.builtin.buildertools.prefabeditor.enums;

public enum PrefabRowSplitMode {
   BY_ALL_SUBFOLDERS("server.commands.editprefab.ui.rowSplit.byAllSubfolders"),
   BY_SPECIFIED_FOLDER("server.commands.editprefab.ui.rowSplit.bySpecifiedFolder"),
   NONE("server.commands.editprefab.ui.rowSplit.none");

   private final String localizationString;

   private PrefabRowSplitMode(String localizationString) {
      this.localizationString = localizationString;
   }

   public String getLocalizationString() {
      return this.localizationString;
   }
}
