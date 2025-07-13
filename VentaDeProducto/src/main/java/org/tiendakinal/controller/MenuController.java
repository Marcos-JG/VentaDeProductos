package org.tiendakinal.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import org.tiendakinal.model.Usuario;
import org.tiendakinal.system.Main;

/**
 * FXML Controller class
 *
 * @author tecno facil outlet
 */
public class MenuController implements Initializable {

    private Main principal;
        
    public void setPrincipal(Main principal) {
        this.principal = principal;
    }
    
    @FXML private Button btnInventario;
    @FXML private Button btnInicioCompra;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    } 
    
    @FXML
    private void clickManejadorEventos(ActionEvent evento) {
        if (evento.getSource() == btnInventario) {
            principal.getInventarioView();
        }
        if (evento.getSource() == btnInicioCompra) {
             principal.getMenuView();
        }
    }
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
