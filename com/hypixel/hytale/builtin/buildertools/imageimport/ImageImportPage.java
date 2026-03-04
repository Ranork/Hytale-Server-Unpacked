package com.hypixel.hytale.builtin.buildertools.imageimport;

import com.hypixel.hytale.builtin.buildertools.BlockColorIndex;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.utils.PasteToolUtil;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.browser.FileBrowserConfig;
import com.hypixel.hytale.server.core.ui.browser.FileBrowserEventData;
import com.hypixel.hytale.server.core.ui.browser.ServerFileBrowser;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

public class ImageImportPage extends InteractiveCustomUIPage<ImageImportPage.PageData> {
   private static final int DEFAULT_MAX_SIZE = 128;
   private static final int MIN_SIZE = 1;
   private static final int MAX_SIZE = 512;
   private static final String ASSET_PACK_SUB_PATH = "Server/Imports/Images";
   @Nonnull
   private String imagePath = "";
   private int maxDimension = 128;
   @Nonnull
   private String orientationStr = "wall_xy";
   @Nonnull
   private ImageImportPage.Orientation orientation = ImageImportPage.Orientation.VERTICAL_XY;
   @Nonnull
   private String originStr = "bottom_center";
   @Nonnull
   private ImageImportPage.Origin origin = ImageImportPage.Origin.BOTTOM_CENTER;
   @Nullable
   private String statusMessage = null;
   private boolean isError = false;
   private boolean isProcessing = false;
   private boolean showBrowser = false;
   @Nonnull
   private final ServerFileBrowser browser;

   public ImageImportPage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismiss, ImageImportPage.PageData.CODEC);
      FileBrowserConfig config = FileBrowserConfig.builder()
         .listElementId("#BrowserPage #FileList")
         .searchInputId("#BrowserPage #SearchInput")
         .currentPathId("#BrowserPage #CurrentPath")
         .allowedExtensions(".png", ".jpg", ".jpeg", ".gif", ".bmp")
         .enableRootSelector(false)
         .enableSearch(true)
         .enableDirectoryNav(true)
         .maxResults(50)
         .assetPackMode(true, "Server/Imports/Images")
         .build();
      this.browser = new ServerFileBrowser(config);
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/ImageImportPage.ui");
      commandBuilder.set("#ImagePath #Input.Value", this.imagePath);
      commandBuilder.set("#MaxSizeInput #Input.Value", this.maxDimension);
      List<DropdownEntryInfo> orientationEntries = new ArrayList<>();
      orientationEntries.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.imageImport.orientation.wall_xy"), "wall_xy"));
      orientationEntries.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.imageImport.orientation.wall_xz"), "wall_xz"));
      orientationEntries.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.imageImport.orientation.floor"), "floor"));
      commandBuilder.set("#OrientationInput #Input.Entries", orientationEntries);
      commandBuilder.set("#OrientationInput #Input.Value", this.orientationStr);
      List<DropdownEntryInfo> originEntries = new ArrayList<>();
      originEntries.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.origin.bottom_front_left"), "bottom_front_left"));
      originEntries.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.origin.bottom_center"), "bottom_center"));
      originEntries.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.origin.center"), "center"));
      originEntries.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.origin.top_center"), "top_center"));
      commandBuilder.set("#OriginInput #Input.Entries", originEntries);
      commandBuilder.set("#OriginInput #Input.Value", this.originStr);
      this.updateStatus(commandBuilder);
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ImagePath #Input", EventData.of("@ImagePath", "#ImagePath #Input.Value"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#MaxSizeInput #Input", EventData.of("@MaxSize", "#MaxSizeInput #Input.Value"), false);
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.ValueChanged, "#OrientationInput #Input", EventData.of("@Orientation", "#OrientationInput #Input.Value"), false
      );
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#OriginInput #Input", EventData.of("@Origin", "#OriginInput #Input.Value"), false);
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ImportButton", EventData.of("Import", "true"));
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ImagePath #BrowseButton", EventData.of("Browse", "true"));
      commandBuilder.set("#FormContainer.Visible", !this.showBrowser);
      commandBuilder.set("#BrowserPage.Visible", this.showBrowser);
      if (this.showBrowser) {
         this.buildBrowserPage(commandBuilder, eventBuilder);
      }
   }

   private void buildBrowserPage(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      this.browser.buildSearchInput(commandBuilder, eventBuilder);
      this.browser.buildCurrentPath(commandBuilder);
      this.browser.buildFileList(commandBuilder, eventBuilder);
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BrowserPage #SelectButton", EventData.of("BrowserSelect", "true"));
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BrowserPage #CancelButton", EventData.of("BrowserCancel", "true"));
   }

   private void updateStatus(@Nonnull UICommandBuilder commandBuilder) {
      if (this.statusMessage != null) {
         commandBuilder.set("#StatusText.Text", this.statusMessage);
         commandBuilder.set("#StatusText.Visible", true);
         commandBuilder.set("#StatusText.Style.TextColor", this.isError ? "#e74c3c" : "#cfd8e3");
      } else {
         commandBuilder.set("#StatusText.Visible", false);
      }
   }

   private void setError(@Nonnull String message) {
      this.statusMessage = message;
      this.isError = true;
      this.isProcessing = false;
      this.rebuild();
   }

   private void setStatus(@Nonnull String message) {
      this.statusMessage = message;
      this.isError = false;
      this.rebuild();
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ImageImportPage.PageData data) {
      if (data.browse != null && data.browse) {
         this.showBrowser = true;
         this.rebuild();
      } else if (data.browserCancel != null && data.browserCancel) {
         this.showBrowser = false;
         this.rebuild();
      } else if (data.browserSelect != null && data.browserSelect) {
         this.showBrowser = false;
         this.rebuild();
      } else if (!this.showBrowser || !this.handleBrowserEvent(data)) {
         boolean needsUpdate = false;
         if (data.imagePath != null) {
            this.imagePath = StringUtil.stripQuotes(data.imagePath.trim());
            this.statusMessage = null;
            needsUpdate = true;
         }

         if (data.maxSize != null) {
            this.maxDimension = Math.max(1, Math.min(512, data.maxSize));
            needsUpdate = true;
         }

         if (data.orientation != null) {
            this.orientationStr = data.orientation.trim().toLowerCase();
            String var5 = this.orientationStr;

            this.orientation = switch (var5) {
               case "wall_xz", "xz", "vertical_xz" -> ImageImportPage.Orientation.VERTICAL_XZ;
               case "floor", "horizontal", "horizontal_xz" -> ImageImportPage.Orientation.HORIZONTAL_XZ;
               default -> ImageImportPage.Orientation.VERTICAL_XY;
            };
            needsUpdate = true;
         }

         if (data.origin != null) {
            this.originStr = data.origin.trim().toLowerCase();
            String var7 = this.originStr;

            this.origin = switch (var7) {
               case "bottom_front_left" -> ImageImportPage.Origin.BOTTOM_FRONT_LEFT;
               case "center" -> ImageImportPage.Origin.CENTER;
               case "top_center" -> ImageImportPage.Origin.TOP_CENTER;
               default -> ImageImportPage.Origin.BOTTOM_CENTER;
            };
            needsUpdate = true;
         }

         if (data.doImport != null && data.doImport && !this.isProcessing) {
            this.performImport(ref, store);
         } else {
            if (needsUpdate) {
               this.sendUpdate();
            }
         }
      }
   }

   private boolean handleBrowserEvent(@Nonnull ImageImportPage.PageData data) {
      if (data.searchQuery != null) {
         this.browser.setSearchQuery(data.searchQuery.trim().toLowerCase());
         this.rebuildBrowser();
         return true;
      } else {
         if (data.file != null) {
            String fileName = data.file;
            if ("..".equals(fileName)) {
               this.browser.navigateUp();
               this.rebuildBrowser();
               return true;
            }

            if (this.browser.handleEvent(FileBrowserEventData.file(fileName))) {
               this.rebuildBrowser();
               return true;
            }

            String virtualPath = this.browser.getAssetPackCurrentPath().isEmpty() ? fileName : this.browser.getAssetPackCurrentPath() + "/" + fileName;
            Path resolvedPath = this.browser.resolveAssetPackPath(virtualPath);
            if (resolvedPath != null && Files.isRegularFile(resolvedPath)) {
               this.imagePath = resolvedPath.toString();
               this.showBrowser = false;
               this.rebuild();
               return true;
            }
         }

         if (data.searchResult != null) {
            Path resolvedPath = this.browser.resolveAssetPackPath(data.searchResult);
            if (resolvedPath != null && Files.isRegularFile(resolvedPath)) {
               this.imagePath = resolvedPath.toString();
               this.showBrowser = false;
               this.rebuild();
               return true;
            }
         }

         return false;
      }
   }

   private void rebuildBrowser() {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      UIEventBuilder eventBuilder = new UIEventBuilder();
      this.browser.buildFileList(commandBuilder, eventBuilder);
      this.browser.buildCurrentPath(commandBuilder);
      this.sendUpdate(commandBuilder, eventBuilder, false);
   }

   private void performImport(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      if (this.imagePath.isEmpty()) {
         this.setError("Please enter a path to an image file");
      } else {
         Path path = Paths.get(this.imagePath);
         if (!AssetModule.get().isWithinPackSubDir(path, "Server/Imports/Images")) {
            this.setError("File must be within an asset pack's imports directory");
         } else if (!Files.exists(path)) {
            this.setError("File not found: " + this.imagePath);
         } else {
            this.isProcessing = true;
            this.setStatus("Processing...");
            Player playerComponent = store.getComponent(ref, Player.getComponentType());
            PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerComponent != null && playerRefComponent != null) {
               String finalPath = this.imagePath;
               int finalMaxSize = this.maxDimension;
               ImageImportPage.Orientation finalOrientation = this.orientation;
               ImageImportPage.Origin finalOrigin = this.origin;
               BuilderToolsPlugin.addToQueue(
                  playerComponent,
                  playerRefComponent,
                  (r, builderState, componentAccessor) -> {
                     try {
                        BufferedImage image = null;

                        try {
                           image = ImageIO.read(Paths.get(finalPath).toFile());
                        } catch (Exception var38) {
                        }

                        if (image == null) {
                           this.setError("Unable to read image file (unsupported format or corrupted). Try PNG format.");
                           return;
                        }

                        int width = image.getWidth();
                        int height = image.getHeight();
                        float scale = 1.0F;
                        if (width > finalMaxSize || height > finalMaxSize) {
                           scale = (float)finalMaxSize / Math.max(width, height);
                           width = Math.round(width * scale);
                           height = Math.round(height * scale);
                        }

                        BlockColorIndex colorIndex = BuilderToolsPlugin.get().getBlockColorIndex();
                        if (colorIndex.isEmpty()) {
                           this.setError("Block color index not initialized");
                           return;
                        }

                        int sizeX;
                        int sizeY;
                        int sizeZ;
                        switch (finalOrientation) {
                           case VERTICAL_XY:
                              sizeX = width;
                              sizeY = height;
                              sizeZ = 1;
                              break;
                           case VERTICAL_XZ:
                              sizeX = width;
                              sizeY = 1;
                              sizeZ = height;
                              break;
                           case HORIZONTAL_XZ:
                              sizeX = width;
                              sizeY = 1;
                              sizeZ = height;
                              break;
                           default:
                              sizeX = width;
                              sizeY = height;
                              sizeZ = 1;
                        }

                        int offsetX = 0;
                        int offsetY = 0;
                        int offsetZ = 0;
                        switch (finalOrigin) {
                           case BOTTOM_FRONT_LEFT:
                           default:
                              break;
                           case BOTTOM_CENTER:
                              offsetX = -sizeX / 2;
                              offsetZ = -sizeZ / 2;
                              break;
                           case CENTER:
                              offsetX = -sizeX / 2;
                              offsetY = -sizeY / 2;
                              offsetZ = -sizeZ / 2;
                              break;
                           case TOP_CENTER:
                              offsetX = -sizeX / 2;
                              offsetY = -sizeY;
                              offsetZ = -sizeZ / 2;
                        }

                        BlockSelection selection = new BlockSelection(width * height, 0);
                        selection.setPosition(0, 0, 0);
                        int blockCount = 0;
                        float finalScale = scale;

                        for (int imgY = 0; imgY < height; imgY++) {
                           for (int imgX = 0; imgX < width; imgX++) {
                              int srcX = Math.min((int)(imgX / finalScale), image.getWidth() - 1);
                              int srcY = Math.min((int)(imgY / finalScale), image.getHeight() - 1);
                              int rgba = image.getRGB(srcX, srcY);
                              int alpha = rgba >> 24 & 0xFF;
                              if (alpha >= 128) {
                                 int red = rgba >> 16 & 0xFF;
                                 int green = rgba >> 8 & 0xFF;
                                 int blue = rgba & 0xFF;
                                 int blockId = colorIndex.findClosestBlock(red, green, blue);
                                 if (blockId > 0) {
                                    int blockX;
                                    int blockY;
                                    int blockZ;
                                    switch (finalOrientation) {
                                       case VERTICAL_XY:
                                          blockX = imgX;
                                          blockY = height - 1 - imgY;
                                          blockZ = 0;
                                          break;
                                       case VERTICAL_XZ:
                                          blockX = imgX;
                                          blockY = 0;
                                          blockZ = height - 1 - imgY;
                                          break;
                                       case HORIZONTAL_XZ:
                                          blockX = imgX;
                                          blockY = 0;
                                          blockZ = imgY;
                                          break;
                                       default:
                                          blockX = imgX;
                                          blockY = height - 1 - imgY;
                                          blockZ = 0;
                                    }

                                    selection.addBlockAtLocalPos(blockX + offsetX, blockY + offsetY, blockZ + offsetZ, blockId, 0, 0, 0);
                                    blockCount++;
                                 }
                              }
                           }
                        }

                        selection.setSelectionArea(
                           new Vector3i(offsetX, offsetY, offsetZ), new Vector3i(sizeX - 1 + offsetX, sizeY - 1 + offsetY, sizeZ - 1 + offsetZ)
                        );
                        builderState.setSelection(selection);
                        builderState.sendSelectionToClient();
                        this.statusMessage = String.format("Success! %d blocks copied to clipboard (%dx%dx%d)", blockCount, sizeX, sizeY, sizeZ);
                        this.isProcessing = false;
                        playerRefComponent.sendMessage(
                           Message.translation("server.builderTools.imageImport.success")
                              .param("count", blockCount)
                              .param("width", sizeX)
                              .param("height", sizeY)
                              .param("depth", sizeZ)
                        );
                        playerComponent.getPageManager().setPage(r, store, Page.None);
                        PasteToolUtil.switchToPasteTool(playerComponent, playerRefComponent);
                     } catch (Exception var39) {
                        ((HytaleLogger.Api)BuilderToolsPlugin.get().getLogger().at(Level.WARNING).withCause(var39)).log("Image import error");
                        this.setError("Error: " + var39.getMessage());
                     }
                  }
               );
            } else {
               this.setError("Player not found");
            }
         }
      }
   }

   public static enum Orientation {
      VERTICAL_XY,
      VERTICAL_XZ,
      HORIZONTAL_XZ;
   }

   public static enum Origin {
      BOTTOM_FRONT_LEFT,
      BOTTOM_CENTER,
      CENTER,
      TOP_CENTER;
   }

   public static class PageData {
      static final String KEY_IMAGE_PATH = "@ImagePath";
      static final String KEY_MAX_SIZE = "@MaxSize";
      static final String KEY_ORIENTATION = "@Orientation";
      static final String KEY_ORIGIN = "@Origin";
      static final String KEY_IMPORT = "Import";
      static final String KEY_BROWSE = "Browse";
      static final String KEY_BROWSER_SELECT = "BrowserSelect";
      static final String KEY_BROWSER_CANCEL = "BrowserCancel";
      public static final BuilderCodec<ImageImportPage.PageData> CODEC = BuilderCodec.builder(ImageImportPage.PageData.class, ImageImportPage.PageData::new)
         .addField(new KeyedCodec<>("@ImagePath", Codec.STRING), (e, s) -> e.imagePath = s, e -> e.imagePath)
         .addField(new KeyedCodec<>("@MaxSize", Codec.INTEGER), (e, i) -> e.maxSize = i, e -> e.maxSize)
         .addField(new KeyedCodec<>("@Orientation", Codec.STRING), (e, s) -> e.orientation = s, e -> e.orientation)
         .addField(new KeyedCodec<>("@Origin", Codec.STRING), (e, s) -> e.origin = s, e -> e.origin)
         .addField(
            new KeyedCodec<>("Import", Codec.STRING), (e, s) -> e.doImport = "true".equalsIgnoreCase(s), e -> e.doImport != null && e.doImport ? "true" : null
         )
         .addField(new KeyedCodec<>("Browse", Codec.STRING), (e, s) -> e.browse = "true".equalsIgnoreCase(s), e -> e.browse != null && e.browse ? "true" : null)
         .addField(
            new KeyedCodec<>("BrowserSelect", Codec.STRING),
            (e, s) -> e.browserSelect = "true".equalsIgnoreCase(s),
            e -> e.browserSelect != null && e.browserSelect ? "true" : null
         )
         .addField(
            new KeyedCodec<>("BrowserCancel", Codec.STRING),
            (e, s) -> e.browserCancel = "true".equalsIgnoreCase(s),
            e -> e.browserCancel != null && e.browserCancel ? "true" : null
         )
         .addField(new KeyedCodec<>("File", Codec.STRING), (e, s) -> e.file = s, e -> e.file)
         .addField(new KeyedCodec<>("@SearchQuery", Codec.STRING), (e, s) -> e.searchQuery = s, e -> e.searchQuery)
         .addField(new KeyedCodec<>("SearchResult", Codec.STRING), (e, s) -> e.searchResult = s, e -> e.searchResult)
         .build();
      @Nullable
      private String imagePath;
      @Nullable
      private Integer maxSize;
      @Nullable
      private String orientation;
      @Nullable
      private String origin;
      @Nullable
      private Boolean doImport;
      @Nullable
      private Boolean browse;
      @Nullable
      private Boolean browserSelect;
      @Nullable
      private Boolean browserCancel;
      @Nullable
      private String file;
      @Nullable
      private String searchQuery;
      @Nullable
      private String searchResult;
   }
}
