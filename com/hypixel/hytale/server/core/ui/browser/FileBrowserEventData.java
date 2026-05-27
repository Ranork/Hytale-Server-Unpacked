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
   public static final String KEY_PREVIEW = "Preview";
   public static final String KEY_KEYCODE = "Keycode";
   public static final String KEY_TILT = "Tilt";
   public static final String KEY_SPIN_SPEED = "SpinSpeed";
   public static final String KEY_MAX_SIZE = "MaxSize";
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
      .addField(new KeyedCodec<>("Preview", Codec.STRING), (entry, s) -> entry.preview = s, entry -> entry.preview)
      .addField(new KeyedCodec<>("Keycode", Codec.INTEGER), (entry, i) -> entry.keycode = i, entry -> entry.keycode)
      .addField(new KeyedCodec<>("Tilt", Codec.INTEGER), (entry, i) -> entry.tilt = i, entry -> entry.tilt)
      .addField(new KeyedCodec<>("SpinSpeed", Codec.INTEGER), (entry, i) -> entry.spinSpeed = i, entry -> entry.spinSpeed)
      .addField(new KeyedCodec<>("MaxSize", Codec.INTEGER), (entry, i) -> entry.maxSize = i, entry -> entry.maxSize)
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
   private String preview;
   @Nullable
   private Integer keycode;
   @Nullable
   private Integer tilt;
   @Nullable
   private Integer spinSpeed;
   @Nullable
   private Integer maxSize;

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

   @Nullable
   public String getPreview() {
      return this.preview;
   }

   @Nullable
   public Integer getKeycode() {
      return this.keycode;
   }

   @Nullable
   public Integer getTilt() {
      return this.tilt;
   }

   @Nullable
   public Integer getSpinSpeed() {
      return this.spinSpeed;
   }

   @Nullable
   public Integer getMaxSize() {
      return this.maxSize;
   }

   public static FileBrowserEventData file(String file) {
      FileBrowserEventData data = new FileBrowserEventData();
      data.file = file;
      return data;
   }

   public static FileBrowserEventData preview(String path) {
      FileBrowserEventData data = new FileBrowserEventData();
      data.preview = path;
      return data;
   }
}
