package com.fox2code.howmanyfoxes.hmi;

import net.minecraft.client.util.KeyBinding;

public class HMFKeyBinds {
    public static final KeyBinding KEY_GET_RECIPES = new KeyBinding("key.hmf.get-recipes", 19);
    public static final KeyBinding KEY_GET_USES = new KeyBinding("key.hmf.get-uses", 22);
    public static final KeyBinding KEY_PREV_RECIPE = new KeyBinding("key.hmf.previous-Recipe", 14);
    public static final KeyBinding KEY_ALL_RECIPES = new KeyBinding("key.hmf.show-all-recipes", 0);
    public static final KeyBinding KEY_CLEAR_SEARCHBOX = new KeyBinding("key.hmf.clear-search", 211);
    public static final KeyBinding KEY_FOCUS_SEARCHBOX = new KeyBinding("key.hmf.focus-search", 28);
    public static final KeyBinding KEY_TOGGLE_OVERLAY = new KeyBinding("key.hmf.toggle-hmf", 24);

    public static final KeyBinding[] HMF_KEYBINDS = new KeyBinding[]{
            HMFKeyBinds.KEY_GET_RECIPES, HMFKeyBinds.KEY_GET_USES, HMFKeyBinds.KEY_PREV_RECIPE, HMFKeyBinds.KEY_ALL_RECIPES,
            HMFKeyBinds.KEY_CLEAR_SEARCHBOX, HMFKeyBinds.KEY_FOCUS_SEARCHBOX, HMFKeyBinds.KEY_TOGGLE_OVERLAY
    };
}
