package com.cms.utils;

import javafx.scene.Scene;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestore temi per il CMS (spostato da com.cms.common)
 */
public class ThemeManager {
    private static ThemeManager instance;
    private Theme currentTheme = Theme.LIGHT;
    private final Map<Scene, String> sceneStylesheets = new HashMap<>();

    public enum Theme {
        LIGHT("light-theme"),
        DARK("dark-theme"),
        HIGH_CONTRAST("high-contrast");
        private final String cssClass;
        Theme(String cssClass) { this.cssClass = cssClass; }
        public String getCssClass() { return cssClass; }
    }

    private ThemeManager() {}
    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        applyThemeToAllScenes();
    }
    public Theme getCurrentTheme() { return currentTheme; }
    public void registerScene(Scene scene) {
        if (scene.getStylesheets().isEmpty()) {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        }
        sceneStylesheets.put(scene, scene.getStylesheets().get(0));
        applyThemeToScene(scene);
    }
    private void applyThemeToScene(Scene scene) {
        if (scene.getRoot() != null) {
            scene.getRoot().getStyleClass().removeAll("light-theme", "dark-theme", "high-contrast");
            scene.getRoot().getStyleClass().add(currentTheme.getCssClass());
        }
    }
    private void applyThemeToAllScenes() { sceneStylesheets.keySet().forEach(this::applyThemeToScene); }
    public void unregisterScene(Scene scene) { sceneStylesheets.remove(scene); }
} 