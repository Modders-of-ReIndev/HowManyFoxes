package com.fox2code.howmanyfoxes.hmi.config;

import com.fox2code.foxloader.client.KeyBindingAPI;
import net.minecraft.client.util.KeyBinding;
import org.lwjgl.input.Keyboard;

public final class HMFKeyBinds {
    public static final KeyBinding KEY_GET_RECIPES = new KeyBinding("key.hmf.get-recipes", Keyboard.KEY_R);
    public static final KeyBinding KEY_GET_USES = new KeyBinding("key.hmf.get-uses", Keyboard.KEY_U);
    public static final KeyBinding KEY_PREV_RECIPE = new KeyBinding("key.hmf.previous-Recipe", Keyboard.KEY_BACK);
    public static final KeyBinding KEY_ALL_RECIPES = new KeyBinding("key.hmf.show-all-recipes", Keyboard.KEY_P);
    public static final KeyBinding KEY_CLEAR_SEARCHBOX = new KeyBinding("key.hmf.clear-search", Keyboard.KEY_DELETE);
    public static final KeyBinding KEY_FOCUS_SEARCHBOX = new KeyBinding("key.hmf.focus-search", Keyboard.KEY_RETURN);
    public static final KeyBinding KEY_TOGGLE_OVERLAY = new KeyBinding("key.hmf.toggle-hmf", Keyboard.KEY_O);

    public static final KeyBinding[] HMF_KEYBINDS = new KeyBinding[]{
            HMFKeyBinds.KEY_GET_RECIPES, HMFKeyBinds.KEY_GET_USES, HMFKeyBinds.KEY_PREV_RECIPE, HMFKeyBinds.KEY_ALL_RECIPES,
            HMFKeyBinds.KEY_CLEAR_SEARCHBOX, HMFKeyBinds.KEY_FOCUS_SEARCHBOX, HMFKeyBinds.KEY_TOGGLE_OVERLAY
    };

    //register keybinds here
    public static void register() {
        for (KeyBinding keyBinding : HMFKeyBinds.HMF_KEYBINDS) {
            KeyBindingAPI.registerKeyBinding(keyBinding);
        }
    }
}
