package fr.teklauncher.tek_updater.functions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.teklauncher.tek_updater.Main;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Launcher {

    // Fonction qui lance le launcher dans son dossier "launcher/bin"
    public static void startLauncher(Stage stage) throws IOException, InterruptedException {

        // Lance le launcher .bat
        ProcessBuilder processBuilder = new ProcessBuilder("./launcher/bin/launch.bat");
        processBuilder.start();


        System.exit(0); // Quitte l'application de mise à jour
        Platform.exit();
    }


    private int latestVersion;
    private final String extractToPath = "launcher/";
    // Fonction qui met à jour le launcher
    public void updateLauncher(Stage stage, Label progressLabel, ProgressBar progressBar) {

        // On vérifie si le dossier existe
        File launcher_dir = new File("launcher/");

        if (!launcher_dir.exists()) { // Si le launcher n'existe pas

            // On créer le dossier launcher
            String folderName = "launcher";

            Path exePath = Paths.get(System.getProperty("java.home"), "bin", "java");
            Path folderPath = Paths.get(exePath.getParent().toString(), folderName);

            try {
                Files.createDirectories(folderPath);

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur s'est produite : " + e.getMessage());
                alert.showAndWait();
            }

        }else{ // Si le launcher existe, on le supprime, puis on le recréer pour faire la mise à jour proprement

            try {
                FileUtils.deleteDirectory(launcher_dir); // Supprime le dossier et tout son contenu

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur s'est produite : " + e.getMessage());
                alert.showAndWait();
            }

            // On recréer le dossier launcher
            String folderName = "launcher";

            Path exePath = Paths.get(System.getProperty("java.home"), "bin", "java");
            Path folderPath = Paths.get(exePath.getParent().toString(), folderName);

            try {
                Files.createDirectories(folderPath);

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur s'est produite : " + e.getMessage());
                alert.showAndWait();
            }
        }

        // On récupère la dernière version sur le serveur
        // En utilisant l'API du serveur Web de TekCity
        String url = "https://tekcity.fr/launcher/updates/";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            // Créer une instance Gson
            Gson gson = new Gson();
            // Li l'url
            JsonObject jsonAPIObject = gson.fromJson(reader, JsonObject.class);

            this.latestVersion = jsonAPIObject.get("version").getAsInt();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur s'est produite : " + e.getMessage());
            alert.showAndWait();
        }

        final String downloadUrl = "https://tekcity.fr/launcher/updates/" + latestVersion + ".zip";

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {

                    URL url = new URL(downloadUrl);
                    InputStream inputStream = url.openStream();
                    ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
                    ZipEntry zipEntry;

                    long fileSize = 0;
                    long totalBytesRead = 0;
                    long contentLength = url.openConnection().getContentLengthLong();

                    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                        if (!zipEntry.isDirectory()) {
                            fileSize = zipEntry.getSize();
                            Path filePath = Paths.get(extractToPath, zipEntry.getName());
                            Files.createDirectories(filePath.getParent());
                            Files.deleteIfExists(filePath);
                            FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile());
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;
                                updateProgress(totalBytesRead, fileSize);

                                // Affichage de la mise à jour
                                if (totalBytesRead <= contentLength) {
                                    double progress = (double) totalBytesRead / contentLength * 100;
                                    String progressText = String.format("%.2f%%", progress);
                                    Platform.runLater(() -> {
                                        progressLabel.setText(progressText);
                                    });
                                }
                            }
                            fileOutputStream.close();
                        }
                    }
                    zipInputStream.close();

                } catch (IOException e) {

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Une erreur s'est produite : " + e.getMessage());
                    alert.showAndWait();

                } finally {

                    this.cancel();

                    startLauncher(stage);

                    // Fermeture de l'application
                    stage.hide();
                    stage.close();
                }

                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        Thread thread = new Thread(task);
        thread.start();
    }
}
