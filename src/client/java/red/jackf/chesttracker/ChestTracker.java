package red.jackf.chesttracker;

import blue.endless.jankson.annotation.Nullable;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.api.gui.ScreenBlacklist;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.config.ChestTrackerConfig;
import red.jackf.chesttracker.gui.DeveloperOverlay;
import red.jackf.chesttracker.gui.GuiApiDefaults;
import red.jackf.chesttracker.gui.screen.ChestTrackerScreen;
import red.jackf.chesttracker.gui.util.ImagePixelReader;
import red.jackf.chesttracker.provider.InteractionTrackerImpl;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.memory.MemoryIntegrity;
import red.jackf.chesttracker.provider.DefaultProvider;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.chesttracker.rendering.NameRenderer;
import red.jackf.chesttracker.storage.ConnectionSettings;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.whereisit.client.api.events.ShouldIgnoreKey;

import java.time.Instant;
import java.util.Optional;

public class ChestTracker implements ClientModInitializer {
    public static final String ID = "chesttracker";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }

    public static final Logger LOGGER = LogManager.getLogger();

    public static Logger getLogger(String suffix) {
        return LogManager.getLogger(ChestTracker.class.getCanonicalName() + "/" + suffix);
    }

    public static final KeyMapping OPEN_GUI = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.chesttracker.open_gui", InputConstants.Type.KEYSYM, InputConstants.KEY_GRAVE, "chesttracker.title")
    );

    private void openInGame(Minecraft client, @Nullable Screen parent) {
        client.setScreen(new ChestTrackerScreen(parent));
    }

    @Override
    public void onInitializeClient() {
        ChestTrackerConfig.init();
        LOGGER.debug("Loading ChestTracker");

        // load and unload memory storage
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(() -> {
            var coord = Coordinate.getCurrent();
            if (coord.isPresent()) {
                ProviderHandler.load(coord.get());
                MemoryBank.loadDefault(coord.get());
            } else {
                ProviderHandler.unload();
                MemoryBank.unload();
            }
        }));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MemoryBank.unload();
            ProviderHandler.unload();
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            // opening Chest Tracker GUI with no screen open
            if (client.screen == null && client.getOverlay() == null)
                while (OPEN_GUI.consumeClick()) {
                    openInGame(client, null);
                }
        });

        ClientTickEvents.START_WORLD_TICK.register(ignored -> {
            if (MemoryBank.INSTANCE != null) MemoryBank.INSTANCE.getMetadata().incrementLoadedTime();
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (Minecraft.getInstance().level == null) return;
            if (screen instanceof AbstractContainerScreen<?>) {
                // opening Chest Tracker GUI with a screen open
                ScreenKeyboardEvents.afterKeyPress(screen).register((parent, key, scancode, modifiers) -> {
                    // don't search in search bars, etc
                    if (ShouldIgnoreKey.EVENT.invoker().shouldIgnoreKey()) {
                        return;
                    }

                    if (OPEN_GUI.matches(key, scancode)) {
                        openInGame(client, parent);
                    }
                });

                // counting items after screen close
                if (!ScreenBlacklist.isBlacklisted(screen.getClass()))
                    ScreenEvents.remove(screen).register(screen1 -> {
                        if (ProviderHandler.INSTANCE == null) return;
                        var bank = MemoryBank.INSTANCE;
                        if (bank == null) return;
                        if (Minecraft.getInstance().level == null) return;

                        Optional<MemoryBuilder.Entry> entry = ProviderHandler.INSTANCE.createMemory((AbstractContainerScreen<?>) screen1);

                        if (entry.isPresent()) {
                            Memory memory = entry.get().memory().build(
                                    bank.getMetadata().getLoadedTime(),
                                    Minecraft.getInstance().level.getGameTime(),
                                    Instant.now());
                            if (bank.getMetadata().getFilteringSettings().onlyRememberNamed && memory.name() == null) return;
                            bank.addMemory(entry.get().key(), entry.get().position(), memory);
                            InteractionTrackerImpl.INSTANCE.clear();
                        }
                    });
                else
                    LOGGER.debug("Blacklisted screen class, ignoring");
            }
        });

        NameRenderer.setup();
        InteractionTrackerImpl.setup();
        MemoryIntegrity.setup();
        ImagePixelReader.setup();
        Storage.setup();
        DeveloperOverlay.setup();
        GuiApiDefaults.setup();
        DefaultProvider.setup();

        ConnectionSettings.load();
    }
}
