package Hauptfenster;

/**
 * @author Quirin
 */
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author Quirin Ertl
 *
 * Wenn das zurückgegebene Boolean false ist, muss das Programm geschlossen werden
 */
public class LoginDialog extends Dialog<Boolean> {

    private final String passwortAusDatenbank = "f89dc3f0f567f5c9e3d4b57a13b46851b98a4f097906244e27c3b4c7402ab9170cf06a2d80e43978624e2f45f44e328a3da2d87300b5ed576420f2160d32f046";

    public LoginDialog() {
        super();
        this.setTitle("Login");
        this.setHeaderText("Bitte das Passwort eingeben!");
        this.setGraphic(new ImageView(this.getClass().getResource("/Icons/Passwort.png").toString()));
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/Icons/Passwort.png").toString()));

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        this.getDialogPane().setContent(grid);

        PasswordField password = new PasswordField();
        password.setPromptText("Passwort");
        grid.add(new Label("Passwort:"), 0, 0);
        grid.add(password, 1, 0);

        Platform.runLater(() -> password.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return this.passwortAusDatenbank.equals(this.eingabeHashen(password.textProperty().getValueSafe()));
            }
            if(dialogButton == ButtonType.CANCEL) {
                System.exit(0);
            }
            return false;
        });
    }

    /**
     * Erzeugt Hashwert des Eingabe-Strings mit SHA-512-Algorithmus
     *
     * @param eingabe
     * @return ein 128 Zeichen langer Hashwert der Eingabe
     */
    private String eingabeHashen(String eingabe) {
        MessageDigest md;
        String out = "";
        try {
            md = MessageDigest.getInstance("SHA-512");
            md.update(eingabe.getBytes());
            byte[] mb = md.digest();

            for (byte temp : mb) {
                String s = Integer.toHexString(new Byte(temp));
                while (s.length() < 2) {
                    s = "0" + s;
                }
                s = s.substring(s.length() - 2);
                out += s;
            }
        } catch (NoSuchAlgorithmException ex) {
            Hauptfenster.fehlermeldung(ex);
        }
        return out;
    }
}
