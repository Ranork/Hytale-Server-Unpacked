package com.hypixel.hytale.server.core.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Options;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import joptsimple.OptionSet;

public class BackupConfig {
   public static final int DEFAULT_FREQUENCY_MINUTES = 30;
   public static final int DEFAULT_MAX_COUNT = 5;
   public static final int DEFAULT_ARCHIVE_MAX_COUNT = 5;
   @Nonnull
   public static final Codec<BackupConfig> CODEC = BuilderCodec.builder(BackupConfig.class, BackupConfig::new)
      .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN), (o, b) -> o.enabled = b, o -> o.enabled)
      .documentation("Determines whether automatic backups are enabled. Can be overridden by the --backup CLI option.")
      .add()
      .<Integer>append(new KeyedCodec<>("FrequencyMinutes", Codec.INTEGER), (o, i) -> o.frequencyMinutes = i, o -> o.frequencyMinutes)
      .addValidator(Validators.greaterThan(0))
      .documentation("The backup frequency in minutes. Must be at least 1. Can be overridden by the --backup-frequency CLI option.")
      .add()
      .<String>append(new KeyedCodec<>("Directory", Codec.STRING), (o, s) -> o.directory = s, o -> o.directory)
      .addValidator(Validators.nonEmptyString())
      .documentation("The backup directory path. Can be overridden by the --backup-directory CLI option.")
      .add()
      .<Integer>append(new KeyedCodec<>("MaxCount", Codec.INTEGER), (o, i) -> o.maxCount = i, o -> o.maxCount)
      .addValidator(Validators.greaterThan(0))
      .documentation("The maximum number of recent backups to retain. Must be at least 1. Can be overridden by the --backup-max-count CLI option.")
      .add()
      .<Integer>append(new KeyedCodec<>("ArchiveMaxCount", Codec.INTEGER), (o, i) -> o.archiveMaxCount = i, o -> o.archiveMaxCount)
      .addValidator(Validators.greaterThan(0))
      .documentation("The maximum number of archived backups to retain. Must be at least 1. Can be overridden by the --backup-archive-max-count CLI option.")
      .add()
      .build();
   @Nullable
   private Boolean enabled;
   @Nullable
   private Integer frequencyMinutes;
   @Nullable
   private String directory;
   @Nullable
   private Integer maxCount;
   @Nullable
   private Integer archiveMaxCount;
   @Nullable
   transient HytaleServerConfig hytaleServerConfig;

   public BackupConfig() {
   }

   public BackupConfig(@Nonnull HytaleServerConfig hytaleServerConfig) {
      this.hytaleServerConfig = hytaleServerConfig;
   }

   public void setHytaleServerConfig(@Nonnull HytaleServerConfig hytaleServerConfig) {
      this.hytaleServerConfig = hytaleServerConfig;
   }

   public boolean isEnabled() {
      if (Options.getOptionSet().has(Options.BACKUP)) {
         return true;
      } else {
         return this.enabled != null ? this.enabled : false;
      }
   }

   @Nullable
   public Boolean getEnabledConfig() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (this.hytaleServerConfig != null) {
         this.hytaleServerConfig.markChanged();
      }
   }

   public int getFrequencyMinutes() {
      OptionSet optionSet = Options.getOptionSet();
      if (optionSet.has(Options.BACKUP_FREQUENCY_MINUTES)) {
         return Math.max((Integer)optionSet.valueOf(Options.BACKUP_FREQUENCY_MINUTES), 1);
      } else {
         return this.frequencyMinutes != null ? Math.max(this.frequencyMinutes, 1) : 30;
      }
   }

   @Nullable
   public Integer getFrequencyMinutesConfig() {
      return this.frequencyMinutes;
   }

   public void setFrequencyMinutes(int frequencyMinutes) {
      this.frequencyMinutes = frequencyMinutes;
      if (this.hytaleServerConfig != null) {
         this.hytaleServerConfig.markChanged();
      }
   }

   @Nullable
   public Path getDirectory() {
      OptionSet optionSet = Options.getOptionSet();
      if (optionSet.has(Options.BACKUP_DIRECTORY)) {
         return (Path)optionSet.valueOf(Options.BACKUP_DIRECTORY);
      } else {
         return this.directory != null ? Path.of(this.directory) : null;
      }
   }

   @Nullable
   public String getDirectoryConfig() {
      return this.directory;
   }

   public void setDirectory(@Nullable String directory) {
      this.directory = directory;
      if (this.hytaleServerConfig != null) {
         this.hytaleServerConfig.markChanged();
      }
   }

   public int getMaxCount() {
      OptionSet optionSet = Options.getOptionSet();
      if (optionSet.has(Options.BACKUP_MAX_COUNT)) {
         return (Integer)optionSet.valueOf(Options.BACKUP_MAX_COUNT);
      } else {
         return this.maxCount != null ? this.maxCount : 5;
      }
   }

   @Nullable
   public Integer getMaxCountConfig() {
      return this.maxCount;
   }

   public void setMaxCount(int maxCount) {
      this.maxCount = maxCount;
      if (this.hytaleServerConfig != null) {
         this.hytaleServerConfig.markChanged();
      }
   }

   public int getArchiveMaxCount() {
      OptionSet optionSet = Options.getOptionSet();
      if (optionSet.has(Options.BACKUP_ARCHIVE_MAX_COUNT)) {
         return (Integer)optionSet.valueOf(Options.BACKUP_ARCHIVE_MAX_COUNT);
      } else {
         return this.archiveMaxCount != null ? this.archiveMaxCount : 5;
      }
   }

   @Nullable
   public Integer getArchiveMaxCountConfig() {
      return this.archiveMaxCount;
   }

   public void setArchiveMaxCount(int archiveMaxCount) {
      this.archiveMaxCount = archiveMaxCount;
      if (this.hytaleServerConfig != null) {
         this.hytaleServerConfig.markChanged();
      }
   }

   public boolean isConfigured() {
      return this.isEnabled() && this.getDirectory() != null;
   }
}
