package com.hypixel.hytale.builtin.asseteditor.event;

import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.event.IAsyncEvent;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class AssetEditorFetchAutoCompleteDataEvent implements IAsyncEvent<String> {
   private final EditorClient editorClient;
   private final String dataSet;
   private final String query;
   private String[] results;

   public AssetEditorFetchAutoCompleteDataEvent(EditorClient editorClient, String dataSet, String query) {
      this.editorClient = editorClient;
      this.dataSet = dataSet;
      this.query = query;
   }

   public String getQuery() {
      return this.query;
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
      return "AssetEditorFetchAutoCompleteDataEvent{editorClient="
         + this.editorClient
         + ", dataSet='"
         + this.dataSet
         + "', query='"
         + this.query
         + "', results="
         + Arrays.toString((Object[])this.results)
         + "}";
   }
}
