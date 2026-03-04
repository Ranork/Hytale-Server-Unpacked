package com.hypixel.hytale.common.plugin;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthorInfo {
   @Nonnull
   public static final Codec<AuthorInfo> CODEC = BuilderCodec.builder(AuthorInfo.class, AuthorInfo::new)
      .append(new KeyedCodec<>("Name", Codec.STRING), (authorInfo, s) -> authorInfo.name = s, authorInfo -> authorInfo.name)
      .add()
      .append(new KeyedCodec<>("Email", Codec.STRING), (authorInfo, s) -> authorInfo.email = s, authorInfo -> authorInfo.email)
      .add()
      .append(new KeyedCodec<>("Url", Codec.STRING), (authorInfo, s) -> authorInfo.url = s, authorInfo -> authorInfo.url)
      .add()
      .build();
   @Nullable
   private String name;
   @Nullable
   private String email;
   @Nullable
   private String url;

   @Nullable
   public String getName() {
      return this.name;
   }

   @Nullable
   public String getEmail() {
      return this.email;
   }

   @Nullable
   public String getUrl() {
      return this.url;
   }

   public void setName(@Nullable String name) {
      this.name = name;
   }

   public void setEmail(@Nullable String email) {
      this.email = email;
   }

   public void setUrl(@Nullable String url) {
      this.url = url;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AuthorInfo{name='" + this.name + "', email='" + this.email + "', url='" + this.url + "'}";
   }
}
