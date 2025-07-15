package com.cms.common;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;

public class SelezioneFile {
    // Mostra un file chooser per selezionare un file PDF
    public static Optional<File> scegliFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona file PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF files", "*.pdf")
        );
        File file = fileChooser.showOpenDialog(stage);
        return Optional.ofNullable(file);
    }
}