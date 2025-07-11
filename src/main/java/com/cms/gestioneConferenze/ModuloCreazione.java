package com.cms.gestioneConferenze;

import com.cms.gestioneAccount.ControlAccount;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class ModuloCreazione {

    private final ControlConferenze ctrl;
    private final ControlAccount ctrlAccount;

    public ModuloCreazione(ControlConferenze ctrl, ControlAccount ctrlAccount) {
        this.ctrl = ctrl;
        this.ctrlAccount = ctrlAccount;
    }

    public void show() {
        Stage stage = new Stage();

        // Titoli
        Label titleLabel = new Label("Crea Conferenza");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");

        Label subtitleLabel = new Label("Inserisci i dati per creare la conferenza");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 0 0 24 0;");

        // Stile labels e input
        String labelStyle = "-fx-text-fill: #374151; -fx-font-weight: 600; -fx-font-size: 14px;";
        String inputStyle = "-fx-background-color: #ffffff; -fx-text-fill: #1e293b; " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 12 16 12 16; -fx-font-size: 14px;";

        // Campi
        TextField acronimoField = new TextField(); acronimoField.setPromptText("Acronimo"); acronimoField.setStyle(inputStyle);
        TextField titoloField = new TextField(); titoloField.setPromptText("Titolo completo"); titoloField.setStyle(inputStyle);
        TextArea descrizioneArea = new TextArea(); descrizioneArea.setPromptText("Descrizione dettagliata...");
        descrizioneArea.setWrapText(true); 
        descrizioneArea.setStyle(inputStyle);
        descrizioneArea.setPrefHeight(80);
        descrizioneArea.setPrefWidth(585);
        descrizioneArea.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-text-fill: #1e293b; " +
            "-fx-border-color: #cbd5e1; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 0; " +
            "-fx-font-size: 14px; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent;"
        );


        TextField luogoField = new TextField(); 
        luogoField.setPromptText("Città, Paese"); 
        luogoField.setStyle(inputStyle);
        
        // DatePicker con stile personalizzato
        DatePicker dpSub = new DatePicker(); 
        dpSub.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-text-fill: #1e293b;" +
            "-fx-border-color: #cbd5e1;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-background-insets: 0;" +
            "-fx-border-insets: 0;" +
            "-fx-padding: 0;" +
            "-fx-font-size: 14px;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
        dpSub.setPrefWidth(400);
        
        DatePicker dpRev = new DatePicker(); 
        dpRev.setStyle(dpSub.getStyle());
        dpRev.setPrefWidth(400);
        
        DatePicker dpGrad = new DatePicker(); 
        dpGrad.setStyle(dpSub.getStyle());
        dpGrad.setPrefWidth(400);
        
        DatePicker dpCR = new DatePicker(); 
        dpCR.setStyle(dpSub.getStyle());
        dpCR.setPrefWidth(400);
        
        DatePicker dpFB = new DatePicker(); 
        dpFB.setStyle(dpSub.getStyle());
        dpFB.setPrefWidth(400);
        
        DatePicker dpFinal = new DatePicker(); 
        dpFinal.setStyle(dpSub.getStyle());
        dpFinal.setPrefWidth(400);

        // Applica stile personalizzato ai componenti interni dei DatePicker
        dpSub.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            dpSub.lookupAll(".arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            dpSub.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });
        
        dpRev.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            dpRev.lookupAll(".arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            dpRev.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });
        
        dpGrad.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            dpGrad.lookupAll(".arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            dpGrad.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });
        
        dpCR.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            dpCR.lookupAll(".arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            dpCR.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });
        
        dpFB.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            dpFB.lookupAll(".arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            dpFB.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });
        
        dpFinal.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            dpFinal.lookupAll(".arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            dpFinal.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });

        Spinner<Integer> minRev = new Spinner<>(1, 10, 1); 
        minRev.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-text-fill: #1e293b;" +
            "-fx-border-color: #cbd5e1;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-background-insets: 0;" +
            "-fx-border-insets: 0;" +
            "-fx-padding: 0;" +
            "-fx-font-size: 14px;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
        minRev.setPrefWidth(400);
        
        Spinner<Integer> valMin = new Spinner<>(-10, 10, 0); 
        valMin.setStyle(minRev.getStyle());
        valMin.setPrefWidth(400);
        
        Spinner<Integer> valMax = new Spinner<>(-10, 10, 3); 
        valMax.setStyle(minRev.getStyle());
        valMax.setPrefWidth(400);
        
        Spinner<Integer> nWin = new Spinner<>(1, 100, 3); 
        nWin.setStyle(minRev.getStyle());
        nWin.setPrefWidth(400);

        // Applica stile personalizzato ai componenti interni degli Spinner
        minRev.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            minRev.lookupAll(".increment-arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            minRev.lookupAll(".decrement-arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            minRev.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });
        
        valMin.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            valMin.lookupAll(".increment-arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            valMin.lookupAll(".decrement-arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            valMin.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });
        
        valMax.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            valMax.lookupAll(".increment-arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            valMax.lookupAll(".decrement-arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            valMax.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });
        
        nWin.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            nWin.lookupAll(".increment-arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            nWin.lookupAll(".decrement-arrow-button").forEach(node -> {
                node.setStyle(
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-color: #f1f5f9;" +
                    "-fx-border-color: transparent;"
                );
            });
            nWin.lookupAll(".text-field").forEach(node -> {
                node.setStyle(
                    "-fx-background-color: #ffffff;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
                );
            });
        });

        ChoiceBox<String> distrib = new ChoiceBox<>();
        distrib.getItems().addAll("MANUALE", "AUTOMATICA", "BROADCAST");
        distrib.setValue("MANUALE");
        distrib.setStyle(inputStyle);
        distrib.setPrefWidth(400);

        // Errore
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px;");

        // HBox righe
        HBox row1 = new HBox(16,
            new VBox(8, new Label("Acronimo:"){{
                setStyle(labelStyle);
            }}, acronimoField),
            new VBox(8, new Label("Titolo:"){{
                setStyle(labelStyle);
            }}, titoloField),
            new VBox(8, new Label("Luogo:"){{
                setStyle(labelStyle);
            }}, luogoField)
        );

        HBox row2 = new HBox(16,
            new VBox(8, new Label("Descrizione:"){{
                setStyle(labelStyle);
            }}, descrizioneArea)
        );

        HBox row3 = new HBox(16,
            new VBox(8, new Label("Scadenza Sottomissioni:"){{
                setStyle(labelStyle);
            }}, dpSub),
            new VBox(8, new Label("Scadenza Revisioni:"){{
                setStyle(labelStyle);
            }}, dpRev),
            new VBox(8, new Label("Pubblicazione Graduatoria:"){{
                setStyle(labelStyle);
            }}, dpGrad)
        );

        HBox row4 = new HBox(16,
            new VBox(8, new Label("Scadenza Camera-ready:"){{
                setStyle(labelStyle);
            }}, dpCR),
            new VBox(8, new Label("Scadenza Feedback Editore:"){{
                setStyle(labelStyle);
            }}, dpFB),
            new VBox(8, new Label("Scadenza Versioni Finali:"){{
                setStyle(labelStyle);
            }}, dpFinal)
        );

        HBox row5 = new HBox(16,
            new VBox(8, new Label("Min. Revisori:"){{
                setStyle(labelStyle);
            }}, minRev),
            new VBox(8, new Label("Num. Vincitori:"){{
                setStyle(labelStyle);
            }}, nWin),
            new VBox(8, new Label("Distribuzione:"){{
                setStyle(labelStyle);
            }}, distrib)
        );

        HBox row6 = new HBox(16,
            new VBox(8, new Label("Valutazione Min:"){{
                setStyle(labelStyle);
            }}, valMin),
            new VBox(8, new Label("Valutazione Max:"){{
                setStyle(labelStyle);
            }}, valMax)
        );

        // Bottoni
        Button createButton = new Button("Crea Conferenza");
        createButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.3),4,0,0,2);");

        Button backButton = new Button("Annulla");
        backButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; " +
                "-fx-border-color: transparent; -fx-padding: 12 24 12 24; " +
                "-fx-background-radius: 8; -fx-font-weight: 600; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(107,114,128,0.3),4,0,0,2);");

        HBox buttonContainer = new HBox(12, createButton, backButton);
        buttonContainer.setAlignment(Pos.CENTER);

        // VBox principale
        VBox formContainer = new VBox(24,
            titleLabel, subtitleLabel,
            row1, row2, row3, row4, row5, row6,
            errorLabel, buttonContainer
        );
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; " +
                "-fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; " +
                "-fx-padding: 32; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1),10,0,0,2); " +
                "-fx-max-width: 800;");

        VBox layout = new VBox(formContainer);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f8fafc; -fx-padding: 10;");


        // Validazione e gestione eventi
        Runnable validate = () -> {
            boolean emptyFields = acronimoField.getText().isEmpty() || titoloField.getText().isEmpty() || 
                    descrizioneArea.getText().isEmpty() || luogoField.getText().isEmpty() || 
                dpSub.getValue() == null || dpRev.getValue() == null || dpGrad.getValue() == null ||
                dpCR.getValue() == null || dpFB.getValue() == null || dpFinal.getValue() == null;

            LocalDate today = LocalDate.now();
            boolean datesOk = !emptyFields &&
                dpSub.getValue().isAfter(today) &&
                dpRev.getValue().isAfter(today) &&
                dpGrad.getValue().isAfter(today) &&
                dpCR.getValue().isAfter(today) &&
                dpFB.getValue().isAfter(today) &&
                dpFinal.getValue().isAfter(today);

            boolean inOrder = !emptyFields &&
                dpSub.getValue().isBefore(dpRev.getValue()) &&
                dpRev.getValue().isBefore(dpGrad.getValue()) &&
                dpGrad.getValue().isBefore(dpCR.getValue()) &&
                dpCR.getValue().isBefore(dpFB.getValue()) &&
                dpFB.getValue().isBefore(dpFinal.getValue());

            boolean numericValid = minRev.getValue() >= 1 && nWin.getValue() >= 1;

            // Controllo date duplicate
            boolean duplicateDates = false;
            if (!emptyFields) {
                Set<LocalDate> dateSet = new HashSet<>();
                dateSet.add(dpSub.getValue());
                dateSet.add(dpRev.getValue());
                dateSet.add(dpGrad.getValue());
                dateSet.add(dpCR.getValue());
                dateSet.add(dpFB.getValue());
                dateSet.add(dpFinal.getValue());
                duplicateDates = dateSet.size() < 6;
            }

            if (emptyFields) {
                errorLabel.setText("Tutti i campi devono essere compilati.");
                createButton.setDisable(true);
            } else if (duplicateDates) {
                errorLabel.setText("Due o più date non possono essere uguali.");
                createButton.setDisable(true);
            } else if (!datesOk) {
                errorLabel.setText("Le date devono partire da domani in poi.");
                createButton.setDisable(true);
            } else if (!inOrder) {
                errorLabel.setText("Le scadenze devono essere in ordine cronologico.");
                createButton.setDisable(true);
            } else if (!numericValid) {
                errorLabel.setText("Minimo revisori e vincitori deve essere almeno 1.");
                createButton.setDisable(true);
            } else {
                errorLabel.setText("");
                createButton.setDisable(false);
            }
        };

        acronimoField.textProperty().addListener((obs, oldV, newV) -> validate.run());
        titoloField.textProperty().addListener((obs, oldV, newV) -> validate.run());
        descrizioneArea.textProperty().addListener((obs, oldV, newV) -> validate.run());
        luogoField.textProperty().addListener((obs, oldV, newV) -> validate.run());
        dpSub.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        dpRev.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        dpGrad.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        dpCR.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        dpFB.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        dpFinal.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        minRev.valueProperty().addListener((obs, oldV, newV) -> validate.run());
        nWin.valueProperty().addListener((obs, oldV, newV) -> validate.run());

        // Logica bottoni
        createButton.setOnAction(e -> {
            if (acronimoField.getText().isEmpty() || titoloField.getText().isEmpty() ||
                descrizioneArea.getText().isEmpty() || luogoField.getText().isEmpty() ||
                dpSub.getValue() == null || dpRev.getValue() == null || dpGrad.getValue() == null ||
                dpCR.getValue() == null || dpFB.getValue() == null || dpFinal.getValue() == null) {
                errorLabel.setText("Tutti i campi sono obbligatori.");
                return;
            }

        Map<String, String> data = new HashMap<>();
        data.put("acronimo", acronimoField.getText());
        data.put("titolo", titoloField.getText());
        data.put("descrizione", descrizioneArea.getText());
        data.put("luogo", luogoField.getText());
            data.put("scadenzaSottomissione", dpSub.getValue().toString());
            data.put("scadenzaRevisioni", dpRev.getValue().toString());
            data.put("dataGraduatoria", dpGrad.getValue().toString());
            data.put("scadenzaCameraReady", dpCR.getValue().toString());
            data.put("scadenzaFeedbackEditore", dpFB.getValue().toString());
            data.put("scadenzaVersioneFinale", dpFinal.getValue().toString());
            data.put("numeroMinimoRevisori", minRev.getValue().toString());
            data.put("valutazioneMinima", valMin.getValue().toString());
            data.put("valutazioneMassima", valMax.getValue().toString());
            data.put("numeroVincitori", nWin.getValue().toString());
            data.put("modalitaDistribuzione", distrib.getValue());

            ctrl.creaConferenza(data, ctrlAccount.getUtenteCorrente());
            stage.close();
            ctrlAccount.apriHomepageChair();
        });

        backButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(layout, 690, 920);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CMS - Crea Conferenza");
        stage.show();
    }
}