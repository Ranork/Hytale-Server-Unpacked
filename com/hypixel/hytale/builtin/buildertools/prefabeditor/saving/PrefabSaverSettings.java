package com.hypixel.hytale.builtin.buildertools.prefabeditor.saving;

public class PrefabSaverSettings {
   private boolean relativize;
   private boolean overwriteExisting;
   private boolean empty;
   private boolean blocks;
   private boolean entities;
   private boolean keepAnchors;
   private boolean clearSupportValues;

   public boolean isRelativize() {
      return this.relativize;
   }

   public void setRelativize(boolean relativize) {
      this.relativize = relativize;
   }

   public boolean isOverwriteExisting() {
      return this.overwriteExisting;
   }

   public void setOverwriteExisting(boolean overwriteExisting) {
      this.overwriteExisting = overwriteExisting;
   }

   public boolean isEmpty() {
      return this.empty;
   }

   public void setEmpty(boolean empty) {
      this.empty = empty;
   }

   public boolean isBlocks() {
      return this.blocks;
   }

   public void setBlocks(boolean blocks) {
      this.blocks = blocks;
   }

   public boolean isEntities() {
      return this.entities;
   }

   public void setEntities(boolean entities) {
      this.entities = entities;
   }

   public boolean isKeepAnchors() {
      return this.keepAnchors;
   }

   public void setKeepAnchors(boolean keepAnchors) {
      this.keepAnchors = keepAnchors;
   }

   public boolean isClearSupportValues() {
      return this.clearSupportValues;
   }

   public void setClearSupportValues(boolean clearSupportValues) {
      this.clearSupportValues = clearSupportValues;
   }
}
