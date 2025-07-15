package org.tiendakinal.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.tiendakinal.database.Conexion;
import org.tiendakinal.model.Usuario;
import org.tiendakinal.system.Main;

/**
 * FXML Controller class
 *
 * @author tecno facil outlet
 */
public class InicioController implements Initializable {

    private Main principal;
    private ArrayList<Usuario> usuarios = new ArrayList<>();
        
    public void setPrincipal(Main principal) {
        this.principal = principal;
    }
    
    @FXML private Button btnIngresar;
    @FXML private Button btnCerrar;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtContraseña;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarUsuarios();
    }    

    private void cargarUsuarios() {        
    usuarios.clear();
    try {
        Connection conexion = Conexion.getInstancia().getConexion();
        if (conexion == null) {
            mostrarAlerta("Error", "No se pudo conectar a la base de datos");
            return;
        }
        PreparedStatement stmt = conexion.prepareStatement("CALL sp_ListarUsuario()");
        ResultSet rs = stmt.executeQuery();

        while(rs.next()) {
            Usuario usuario = new Usuario(
                rs.getInt("ID"),
                rs.getString("USUARIO"),
                rs.getString("CORREO"),
                rs.getString("CONTRASEÑA"),
                rs.getString("TIPO")
            );
            usuarios.add(usuario);
        }
    } catch (Exception e) {
        System.out.println("Error al cargar usuarios: " + e.getMessage());
        e.printStackTrace();
        mostrarAlerta("Error", "No se pudieron cargar los usuarios de la base de datos");
    }
}
    
    @FXML
    private void clickManejadorEventos(ActionEvent evento) {
        if (evento.getSource() == btnIngresar) {
            iniciarSesion();
        }
        if (evento.getSource() == btnCerrar) {
            System.exit(0);
        }
    }
    
    private void iniciarSesion() {
        String usuario = txtCorreo.getText();
        String contrasena = txtContraseña.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta("Error", "Debe completar ambos campos");
            return;
        }
        for (Usuario u : usuarios) {
            if (u.getCorreoUsuario().equals(usuario) && u.getContraseñaUsuario().equals(contrasena)) {
                    if (u.getTipo().equals("Empleado")) {
                        principal.getMenuView();
                        return;
                }
            }
        }
        mostrarAlerta("Error", "Correo o contraseña incorrecta");
    }
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
}
