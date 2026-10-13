package dev.aether.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class AetherClient implements ClientModInitializer {
    private static KeyBinding openKey;
    private static boolean introPlayedThisSession = false;
    private static boolean wasInWorld = false;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.aether.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean inWorld = client.world != null && client.player != null;
            if (inWorld && !wasInWorld) {
                introPlayedThisSession = false;
            }
            wasInWorld = inWorld;

            while (openKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new AetherScreen(!introPlayedThisSession));
                    introPlayedThisSession = true;
                }
            }
        });
    }
}
