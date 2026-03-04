package com.hypixel.hytale.server.core.ui.browser;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nullable;

public class FileBrowserEventData {
   public static final String KEY_FILE = "File";
   public static final String KEY_ROOT = "@Root";
   public static final String KEY_SEARCH_QUERY = "@SearchQuery";
   public static final String KEY_SEARCH_RESULT = "SearchResult";
   public static final String KEY_BROWSE = "Browse";
   public static final BuilderCodec<FileBrowserEventData> CODEC = BuilderCodec.builder(FileBrowserEventData.class, FileBrowserEventData::new)
      .addField(new KeyedCodec<>("File", Codec.STRING), (entry, s) -> entry.file = s, entry -> entry.file)
      .addField(new KeyedCodec<>("@Root", Codec.STRING), (entry, s) -> entry.root = s, entry -> entry.root)
      .addField(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, entry -> entry.searchQuery)
      .addField(new KeyedCodec<>("SearchResult", Codec.STRING), (entry, s) -> entry.searchResult = s, entry -> entry.searchResult)
      .addField(
         new KeyedCodec<>("Browse", Codec.STRING),
         (entry, s) -> entry.browse = "true".equalsIgnoreCase(s),
         entry -> entry.browse != null && entry.browse ? "true" : null
      )
      .build();
   @Nullable
   private String file;
   @Nullable
   private String root;
   @Nullable
   private String searchQuery;
   @Nullable
   private String searchResult;
   @Nullable
   private Boolean browse;

   @Nullable
   public String getFile() {
      return this.file;
   }

   @Nullable
   public String getRoot() {
      return this.root;
   }

   @Nullable
   public String getSearchQuery() {
      return this.searchQuery;
   }

   @Nullable
   public String getSearchResult() {
      return this.searchResult;
   }

   public boolean isBrowseRequested() {
      return this.browse != null && this.browse;
   }

   public static FileBrowserEventData file(String file) {
      FileBrowserEventData data = new FileBrowserEventData();
      data.file = file;
      return data;
   }
}
