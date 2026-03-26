package com.hypixel.hytale.server.core.ui.browser;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nullable;

public class AssetPackSaveBrowserEventData {
   public static final String KEY_PACK = "Pack";
   public static final String KEY_SEARCH = "@PackSearch";
   public static final String KEY_CREATE_NAME = "@CreateName";
   public static final String KEY_CREATE_GROUP = "@CreateGroup";
   public static final String KEY_CREATE_DESCRIPTION = "@CreateDescription";
   public static final String KEY_CREATE_VERSION = "@CreateVersion";
   public static final String KEY_CREATE_WEBSITE = "@CreateWebsite";
   public static final String KEY_CREATE_AUTHOR_NAME = "@CreateAuthorName";
   public static final String KEY_VALIDATE_CREATE = "ValidateCreate";
   public static final String KEY_CREATE_TARGET_DIR = "@CreateTargetDir";
   public static final String KEY_DIRECTORY_FILTER = "@DirectoryFilter";
   public static final BuilderCodec<AssetPackSaveBrowserEventData> CODEC = BuilderCodec.builder(
         AssetPackSaveBrowserEventData.class, AssetPackSaveBrowserEventData::new
      )
      .append(new KeyedCodec<>("Pack", Codec.STRING), (e, s) -> e.pack = s, e -> e.pack)
      .add()
      .append(new KeyedCodec<>("@PackSearch", Codec.STRING), (e, s) -> e.search = s, e -> e.search)
      .add()
      .append(new KeyedCodec<>("@CreateName", Codec.STRING), (e, s) -> e.createName = s, e -> e.createName)
      .add()
      .append(new KeyedCodec<>("@CreateGroup", Codec.STRING), (e, s) -> e.createGroup = s, e -> e.createGroup)
      .add()
      .append(new KeyedCodec<>("@CreateDescription", Codec.STRING), (e, s) -> e.createDescription = s, e -> e.createDescription)
      .add()
      .append(new KeyedCodec<>("@CreateVersion", Codec.STRING), (e, s) -> e.createVersion = s, e -> e.createVersion)
      .add()
      .append(new KeyedCodec<>("@CreateWebsite", Codec.STRING), (e, s) -> e.createWebsite = s, e -> e.createWebsite)
      .add()
      .append(new KeyedCodec<>("@CreateAuthorName", Codec.STRING), (e, s) -> e.createAuthorName = s, e -> e.createAuthorName)
      .add()
      .append(new KeyedCodec<>("ValidateCreate", Codec.STRING), (e, s) -> e.validateCreate = s, e -> e.validateCreate)
      .add()
      .append(new KeyedCodec<>("@CreateTargetDir", Codec.STRING), (e, s) -> e.createTargetDir = s, e -> e.createTargetDir)
      .add()
      .append(new KeyedCodec<>("@DirectoryFilter", Codec.STRING), (e, s) -> e.directoryFilter = s, e -> e.directoryFilter)
      .add()
      .build();
   @Nullable
   public String pack;
   @Nullable
   public String search;
   @Nullable
   public String createName;
   @Nullable
   public String createGroup;
   @Nullable
   public String createDescription;
   @Nullable
   public String createVersion;
   @Nullable
   public String createWebsite;
   @Nullable
   public String createAuthorName;
   @Nullable
   public String validateCreate;
   @Nullable
   public String createTargetDir;
   @Nullable
   public String directoryFilter;

   @Nullable
   public String getPack() {
      return this.pack;
   }

   @Nullable
   public String getSearch() {
      return this.search;
   }

   @Nullable
   public String getCreateName() {
      return this.createName;
   }

   @Nullable
   public String getCreateGroup() {
      return this.createGroup;
   }

   @Nullable
   public String getCreateDescription() {
      return this.createDescription;
   }

   @Nullable
   public String getCreateVersion() {
      return this.createVersion;
   }

   @Nullable
   public String getCreateWebsite() {
      return this.createWebsite;
   }

   @Nullable
   public String getCreateAuthorName() {
      return this.createAuthorName;
   }

   @Nullable
   public String getValidateCreate() {
      return this.validateCreate;
   }

   @Nullable
   public String getCreateTargetDir() {
      return this.createTargetDir;
   }

   @Nullable
   public String getDirectoryFilter() {
      return this.directoryFilter;
   }
}
