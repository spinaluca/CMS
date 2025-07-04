package com.cms.utils;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * Componenti moderni personalizzati per il CMS (spostato da com.cms.common)
 */
public class ModernComponents {

    public static Button createModernButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", styleClass);
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        return button;
    }

    public static VBox createAnimatedCard(String title, String content) {
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(24));
        card.getStyleClass().add("card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("headline-medium");
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("body-medium");
        contentLabel.setWrapText(true);
        card.getChildren().addAll(titleLabel, contentLabel);

        card.setOpacity(0);
        card.setTranslateY(20);
        FadeTransition fade = new FadeTransition(Duration.millis(500), card);
        fade.setToValue(1.0);
        TranslateTransition translate = new TranslateTransition(Duration.millis(500), card);
        translate.setToY(0);
        new ParallelTransition(fade, translate).play();
        return card;
    }

    public static ProgressIndicator createModernLoader() {
        ProgressIndicator loader = new ProgressIndicator();
        loader.getStyleClass().add("loading-spinner");
        loader.setPrefSize(40, 40);
        RotateTransition rotate = new RotateTransition(Duration.millis(1000), loader);
        rotate.setByAngle(360);
        rotate.setCycleCount(Timeline.INDEFINITE);
        rotate.play();
        return loader;
    }

    public static VBox createToastNotification(String message, NotificationType type) {
        VBox toast = new VBox(8);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(16, 20, 16, 20));
        toast.setMaxWidth(400);
        String iconText = "";
        String styleClass = "notification";
        switch (type) {
            case SUCCESS:
                iconText = "✓";
                styleClass += " notification-success";
                break;
            case WARNING:
                iconText = "⚠";
                styleClass += " notification-warning";
                break;
            case ERROR:
                iconText = "✕";
                styleClass += " notification-error";
                break;
            case INFO:
                iconText = "ℹ";
                styleClass += " notification-info";
                break;
        }
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(iconText);
        icon.getStyleClass().add("icon");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("message");
        messageLabel.setWrapText(true);
        content.getChildren().addAll(icon, messageLabel);
        toast.getChildren().add(content);
        toast.getStyleClass().add(styleClass);

        toast.setTranslateX(400);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toast);
        slideIn.setToX(0);
        slideIn.play();
        Timeline autoHide = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> {
                if (toast.getParent() instanceof Pane) {
                    ((Pane) toast.getParent()).getChildren().remove(toast);
                }
            });
            fadeOut.play();
        }));
        autoHide.play();
        return toast;
    }

    public static VBox createFloatingLabelInput(String labelText, boolean isPassword) {
        VBox container = new VBox();
        container.getStyleClass().add("input-group");
        Label label = new Label(labelText);
        label.getStyleClass().addAll("floating-label", "caption");
        TextField input = isPassword ? new PasswordField() : new TextField();
        input.getStyleClass().add("form-input");
        input.setPromptText(labelText);
        input.focusedProperty().addListener((obs, oldVal, newVal) -> {
            TranslateTransition labelTransition = new TranslateTransition(Duration.millis(200), label);
            ScaleTransition labelScale = new ScaleTransition(Duration.millis(200), label);
            if (newVal || !input.getText().isEmpty()) {
                labelTransition.setToY(-10);
                labelScale.setToX(0.85);
                labelScale.setToY(0.85);
                label.setStyle("-fx-text-fill: #2563eb;");
            } else {
                labelTransition.setToY(0);
                labelScale.setToX(1.0);
                labelScale.setToY(1.0);
                label.setStyle("-fx-text-fill: #64748b;");
            }
            new ParallelTransition(labelTransition, labelScale).play();
        });
        container.getChildren().addAll(label, input);
        return container;
    }

    public static VBox createStatCard(String label, String value, String change, boolean isPositive) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        Label labelText = new Label(label);
        labelText.getStyleClass().add("stat-label");
        if (change != null && !change.isEmpty()) {
            Label changeLabel = new Label(change);
            changeLabel.getStyleClass().addAll("stat-change", isPositive ? "stat-change-positive" : "stat-change-negative");
            card.getChildren().addAll(valueLabel, labelText, changeLabel);
        } else {
            card.getChildren().addAll(valueLabel, labelText);
        }
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        return card;
    }

    public static HBox createDividerWithText(String text) {
        HBox divider = new HBox(16);
        divider.setAlignment(Pos.CENTER);
        divider.setPadding(new Insets(16, 0, 16, 0));
        Separator leftLine = new Separator();
        leftLine.getStyleClass().add("divider-line");
        HBox.setHgrow(leftLine, Priority.ALWAYS);
        Label dividerText = new Label(text);
        dividerText.getStyleClass().addAll("overline", "divider-text");
        Separator rightLine = new Separator();
        rightLine.getStyleClass().add("divider-line");
        HBox.setHgrow(rightLine, Priority.ALWAYS);
        divider.getChildren().addAll(leftLine, dividerText, rightLine);
        return divider;
    }

    public static Label createBadge(String text, BadgeStyle style) {
        Label badge = new Label(text);
        badge.getStyleClass().add("badge");
        switch (style) {
            case PRIMARY:
                badge.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
                break;
            case SUCCESS:
                badge.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
                break;
            case WARNING:
                badge.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white;");
                break;
            case DANGER:
                badge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                break;
            case SECONDARY:
                badge.setStyle("-fx-background-color: #64748b; -fx-text-fill: white;");
                break;
        }
        badge.setPadding(new Insets(4, 8, 4, 8));
        badge.setStyle(badge.getStyle() + "-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 600;");
        return badge;
    }

    public enum NotificationType { SUCCESS, WARNING, ERROR, INFO }
    public enum BadgeStyle { PRIMARY, SUCCESS, WARNING, DANGER, SECONDARY }
} 