package com.sheryv.slimod.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.sheryv.slimod.SLIMod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ConfigProvider {
  public static void loadServerConfig() {
    CommentedConfig config = loadConfig(FMLPaths.CONFIGDIR.get().resolve(SLIMod.NAME + ".toml"));
    ConfigHandler.validate();
    ConfigHandler.SERVER.setConfig(config);
    if (ConfigHandler.isLoggingEnabled()) {
      SLIMod.LOGGER.info("Loaded config: {}, {}, {}", ConfigHandler.getLimits(), ConfigHandler.getProbability(), ConfigHandler.getPerMob());
    }
  }
  
  public static void reload() {
    loadServerConfig();
  }
  
  private static CommentedConfig loadConfig(Path path) {
    final CommentedFileConfig configData = CommentedFileConfig.builder(path)
        .sync()
        .autosave()
        .writingMode(WritingMode.REPLACE)
        .build();
    
    configData.load();
    return configData;
  }
  
}
