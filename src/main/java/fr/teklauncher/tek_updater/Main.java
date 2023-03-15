package fr.teklauncher.tek_updater;

import com.google.gson.JsonObject;
import fr.teklauncher.tek_updater.functions.Launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.*;

import javafx.stage.Stage;

import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import com.google.gson.Gson;

public class Main extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {

        // On charge la/les polices
        Font tektur = Font.loadFont(getClass().getResourceAsStream("/fonts/Tektur-Bold.ttf"), 30);

        // On charge le titre
        Label titre = new Label("Vérification et mise à jour du Launcher de TekCity");
        titre.setFont(tektur);
        titre.setTextFill(Color.WHITE);

        // ProgressBar pour la mise à jour
        ProgressBar progressBar = new ProgressBar(0);

        // Splash screen Pane
        StackPane root = new StackPane();
        root.setId("pane");

        // Initialisation des valeurs de progression
        Label progressLabel = new Label("0%");
        progressLabel.setFont(tektur);
        progressLabel.setTextFill(Color.WHITE);

        progressBar.prefWidthProperty().bind(stage.widthProperty());

        // On ajoute les affichages à la racine
        root.getChildren().addAll(titre, progressBar, progressLabel);

        StackPane.setAlignment(titre, Pos.TOP_CENTER);
        StackPane.setMargin(titre, new Insets(50, 0, 0, 0));
        StackPane.setMargin(progressLabel, new Insets(50,0,0,0));
        Scene scene = new Scene(root, 800, 200); // Scène

        // Fichier CSS
        scene.getStylesheets().addAll(Objects.requireNonNull(Launcher.class.getResource("/style.css")).toExternalForm());

        // affichage de la scène
        stage.setResizable(false); // Rend la scène impossible à redimensionner
        stage.setScene(scene);
        stage.show();

        // Vérification de l'existence du launcher
        File finalDirectory = new File("launcher/");

        if (!finalDirectory.exists()) { // Si le launcher n'existe pas
            // On télécharge l'archive de la dernière version du launcher
            Launcher launcher = new Launcher();
            launcher.updateLauncher(stage, progressLabel, progressBar);

        }else{
            // Le dossier existe, on vérifie que les fichiers sont à l'intérieur
            File[] fichiers = finalDirectory.listFiles();

            if(fichiers == null){ // Si aucun fichiers n'est trouvé dans le dossier
                // On télécharge l'archive de la dernière version du launcher
                Launcher launcher = new Launcher();
                launcher.updateLauncher(stage, progressLabel, progressBar);

            }else{ // Des fichiers existent on vérifie la version du fichier release
                for (File fichier : fichiers) {

                    // On vérifie la version du fichier de release de Jlink
                    if (fichier.getName().equals("release")) {

                        try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {

                            String line;
                            while ((line = br.readLine()) != null) {
                                if (line.startsWith("VERSION=")) {

                                    // On récupère donc la version actuel du release dans le dossier launcher
                                    int currentVersion = Integer.parseInt(line.substring("VERSION=".length()));

                                    // On récupère également la dernière version sur le serveur
                                    // En utilisant l'API du serveur Web de TekCity
                                    String url = "https://tekcity.fr/launcher/updates/";

                                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
                                        // Créer une instance Gson
                                        Gson gson = new Gson();
                                        // Li l'url
                                        JsonObject jsonAPIObject = gson.fromJson(reader, JsonObject.class);

                                        int latestVersion = jsonAPIObject.get("version").getAsInt();

                                        // On compare ensuite les 2 versions
                                        if(currentVersion < latestVersion){
                                            // Si la version n'est pas à jour
                                            // On télécharge l'archive de la dernière version du launcher
                                            Launcher launcher = new Launcher();
                                            launcher.updateLauncher(stage, progressLabel, progressBar);

                                        }else{
                                            // Sinon la version est à jour !
                                            // On lance l'application
                                            Launcher.startLauncher(stage);
                                        }

                                    } catch (IOException e) {
                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setTitle("Erreur");
                                        alert.setHeaderText(null);
                                        alert.setContentText("Une erreur s'est produite : " + e.getMessage());
                                        alert.showAndWait();
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                }
                            }
                        } catch (IOException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erreur");
                            alert.setHeaderText(null);
                            alert.setContentText("Une erreur s'est produite : " + e.getMessage());
                            alert.showAndWait();
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}