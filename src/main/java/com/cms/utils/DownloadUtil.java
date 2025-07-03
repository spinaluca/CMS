package com.cms.utils;

import com.cms.common.PopupAvviso;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class DownloadUtil {

    public static void salvaInDownload(File sorgente, String nomeBase) {
        try {
            String home = System.getProperty("user.home");
            Path downloadDir = Paths.get(home, "Desktop", "downloadsCMS");

            if (!Files.exists(downloadDir)) {
                Files.createDirectories(downloadDir);
            }

            // Gestione estensione
            String extension = "";
            int dotIndex = sorgente.getName().lastIndexOf(".");
            if (dotIndex != -1) {
                extension = sorgente.getName().substring(dotIndex);
            }

            // Percorso iniziale
            Path dest = downloadDir.resolve(nomeBase + extension);
            int counter = 2;

            // Se esiste, aggiungi (2), (3), ...
            while (Files.exists(dest)) {
                dest = downloadDir.resolve(nomeBase + " (" + counter + ")" + extension);
                counter++;
            }

            Files.copy(sorgente.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            new PopupAvviso("File scaricato come: " + dest.getFileName()).show();
        } catch (IOException e) {
            e.printStackTrace();
            new PopupAvviso("Errore durante il download: " + e.getMessage()).show();
        }
    }
}