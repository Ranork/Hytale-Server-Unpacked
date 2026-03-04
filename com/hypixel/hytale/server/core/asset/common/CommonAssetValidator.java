package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.common.util.PatternUtil;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonAssetValidator implements Validator<String> {
   public static final CommonAssetValidator TEXTURE_ITEM = new CommonAssetValidator("png", "Blocks", "BlockTextures", "Items", "NPC", "Resources", "VFX");
   public static final CommonAssetValidator TEXTURE_CHARACTER = new CommonAssetValidator("png", "Characters", "NPC", "Items", "VFX");
   public static final CommonAssetValidator TEXTURE_CHARACTER_ATTACHMENT = new CommonAssetValidator(
      "png", "Characters", "NPC", "Items", "Cosmetics", "Items", "NPC", "Resources"
   );
   public static final CommonAssetValidator TEXTURE_TRAIL = new CommonAssetValidator("png", "Trails");
   public static final CommonAssetValidator TEXTURE_SKY = new CommonAssetValidator("png", "Sky");
   public static final CommonAssetValidator TEXTURE_PARTICLES = new CommonAssetValidator("png", "Particles");
   public static final CommonAssetValidator TEXTURE_ITEM_QUALITY = new CommonAssetValidator("png", true, "UI/ItemQualities");
   public static final CommonAssetValidator ICON_RESOURCE = new CommonAssetValidator("png", "Icons/ResourceTypes");
   public static final CommonAssetValidator ICON_ITEM = new CommonAssetValidator("png", "Icons/ItemsGenerated", "Icons/Items");
   public static final CommonAssetValidator ICON_ITEM_CATEGORIES = new CommonAssetValidator("png", "Icons/ItemCategories");
   public static final CommonAssetValidator ICON_CRAFTING = new CommonAssetValidator("png", "Icons/CraftingCategories");
   public static final CommonAssetValidator ICON_ENTITY_STAT = new CommonAssetValidator("png", "Icons/EntityStats");
   public static final CommonAssetValidator ICON_MODEL = new CommonAssetValidator("png", "Icons/ModelsGenerated", "Icons/Models");
   public static final CommonAssetValidator UI_RETICLE_PART = new CommonAssetValidator("png", "UI/Reticles");
   public static final ArrayValidator<String> UI_RETICLE_PARTS_ARRAY = new ArrayValidator<>(UI_RETICLE_PART);
   public static final CommonAssetValidator UI_SCREEN_EFFECT = new CommonAssetValidator("png", "ScreenEffects");
   public static final CommonAssetValidator UI_CRAFTING_DIAGRAM = new CommonAssetValidator("svg", "CraftingDiagrams");
   public static final CommonAssetValidator MODEL_ITEM = new CommonAssetValidator("blockymodel", "Blocks", "Items", "Resources", "NPC", "VFX", "Consumable");
   public static final CommonAssetValidator MODEL_CHARACTER = new CommonAssetValidator("blockymodel", "Characters", "NPC", "Items", "VFX");
   public static final CommonAssetValidator MODEL_CHARACTER_ATTACHMENT = new CommonAssetValidator(
      "blockymodel", "Characters", "NPC", "Items", "Cosmetics", "Items", "NPC", "Resources"
   );
   public static final CommonAssetValidator PREFAB_LIST = new CommonAssetValidator("json", "PrefabList");
   public static final CommonAssetValidator BLOCK_LIST = new CommonAssetValidator("json", "BlockTypeList");
   public static final CommonAssetValidator ANIMATION_ITEM_CHARACTER = new CommonAssetValidator("blockyanim", "Characters", "NPC");
   public static final CommonAssetValidator ANIMATION_ITEM_BLOCK = new CommonAssetValidator(
      "blockyanim", "Blocks", "Items", "Resources", "NPC", "VFX", "Consumable"
   );
   public static final CommonAssetValidator ANIMATION_CHARACTER = new CommonAssetValidator("blockyanim", "Characters", "NPC", "Equipment", "VFX", "Items");
   public static final CommonAssetValidator MUSIC = new CommonAssetValidator("ogg", "Music");
   public static final CommonAssetValidator SOUNDS = new CommonAssetValidator("ogg", "Sounds");
   @Nullable
   private final String[] requiredRoots;
   @Nullable
   private final String requiredExtension;
   private final boolean isUIAsset;

   public CommonAssetValidator(String requiredExtension, boolean isUIAsset, @Nullable String... requiredRoots) {
      if (requiredRoots != null) {
         for (int i = 0; i < requiredRoots.length; i++) {
            String req = requiredRoots[i];
            if (!req.endsWith("/")) {
               requiredRoots[i] = req + "/";
            }
         }
      }

      this.requiredRoots = requiredRoots;
      this.requiredExtension = requiredExtension;
      this.isUIAsset = isUIAsset;
   }

   public CommonAssetValidator(String requiredExtension, String... requiredRoots) {
      this(requiredExtension, false, requiredRoots);
   }

   public CommonAssetValidator() {
      this.requiredRoots = null;
      this.requiredExtension = null;
      this.isUIAsset = true;
   }

   public void accept(@Nullable String asset, @Nonnull ValidationResults results) {
      if (asset != null) {
         if (this.requiredRoots != null) {
            boolean valid = false;

            for (String root : this.requiredRoots) {
               if (asset.startsWith(root)) {
                  valid = true;
                  break;
               }
            }

            if (!valid) {
               results.fail("Common Asset '" + asset + "' must be within the root: " + Arrays.toString((Object[])this.requiredRoots));
            }
         }

         if (this.requiredExtension != null && !asset.endsWith(this.requiredExtension)) {
            results.fail("Common Asset '" + asset + "' must have the extension " + this.requiredExtension);
         }

         asset = PatternUtil.replaceBackslashWithForwardSlash(asset);
         if (!CommonAssetRegistry.hasCommonAsset(asset)) {
            if (this.isUIAsset && asset.endsWith(".png")) {
               String scaled2XVersionFilename = asset.substring(0, asset.lastIndexOf(".png")) + "@2x.png";
               if (!CommonAssetRegistry.hasCommonAsset(scaled2XVersionFilename)) {
                  results.fail("Common Asset '" + asset + "' doesn't exist!");
               }
            } else {
               results.fail("Common Asset '" + asset + "' doesn't exist!");
            }
         }
      }
   }

   @Override
   public void updateSchema(SchemaContext context, @Nonnull Schema target) {
      ((StringSchema)target).setHytaleCommonAsset(new StringSchema.CommonAsset(this.requiredExtension, this.isUIAsset, this.requiredRoots));
   }
}
