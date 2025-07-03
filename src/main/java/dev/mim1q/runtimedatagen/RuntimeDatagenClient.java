package dev.mim1q.runtimedatagen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class RuntimeDatagenClient implements ClientModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger("runtimedatagen");

  @Override
  public void onInitializeClient() {
    if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
      registerCursedDatagenReloading();
    }
  }

  private void registerCursedDatagenReloading() {
    var keyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
      "Run Datagen",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_O,
      "key.categories.misc"
    ));

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (keyBind.wasPressed()) {
        try {
          // Access the private static runInternal method of FabricDataGenHelper - an access widener didn't seem to work
          // noinspection UnstableApiUsage
          var fabricDataGenHelperRunInternal = FabricDataGenHelper.class.getDeclaredMethod("runInternal");
          fabricDataGenHelperRunInternal.setAccessible(true);
          assert MinecraftClient.getInstance().player != null;
          new Thread(() -> {
            try {
              // Run the Fabric data generation process
              fabricDataGenHelperRunInternal.invoke(null);
              MinecraftClient.getInstance().player.sendMessage(Text.of("Datagen started. Reloading resources - please wait"), true);
              var datagenPath = Path.of(System.getProperty("fabric-api.datagen.output-dir"));
              var projectPath = datagenPath.getParent().getParent().getParent();

              // Run the gradle processResources task to apply changes to the resources
              var process = new ProcessBuilder(
                projectPath.resolve("gradlew.bat").toAbsolutePath().toString(),
                "processResources"
              )
                .directory(projectPath.toFile())
                .start();
              process.waitFor();

              // Wait for a little bit before reloading
              Thread.sleep(200);

              // Reload resources within the Minecraft world
              MinecraftClient.getInstance().reloadResourcesConcurrently().thenAccept(_x -> {
                MinecraftClient.getInstance().player.sendMessage(Text.of("Datagen finished successfully"), true);
              });
            } catch (Exception e) {
              MinecraftClient.getInstance().player.sendMessage(Text.of("Datagen failed"), true);
              throw new RuntimeException(e);
            }
          }).start();
        } catch (Exception e) {
          LOGGER.error("Failed to run datagen", e);
        }
      }
    });
  }
}
