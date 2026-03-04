package com.hypixel.hytale.builtin.asseteditor.event;

import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.event.IAsyncEvent;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class AssetEditorRequestDataSetEvent implements IAsyncEvent<String> {
   private final EditorClient editorClient;
   private final String dataSet;
   private String[] results;

   public AssetEditorRequestDataSetEvent(EditorClient editorClient, String dataSet, String[] results) {
      this.editorClient = editorClient;
      this.dataSet = dataSet;
      this.results = results;
   }

   public String getDataSet() {
      return this.dataSet;
   }

   public EditorClient getEditorClient() {
      return this.editorClient;
   }

   public String[] getResults() {
      return this.results;
   }

   public void setResults(String[] results) {
      this.results = results;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AssetEditorRequestDataSetEvent{editorClient="
         + this.editorClient
         + ", dataSet='"
         + this.dataSet
         + "', results="
         + Arrays.toString((Object[])this.results)
         + "}";
   }
}
