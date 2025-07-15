package org.tiendakinal.system;

import java.io.InputStream;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.tiendakinal.controller.CompraFinController;
import org.tiendakinal.controller.CompraInicioController;
import org.tiendakinal.controller.InicioController;
import org.tiendakinal.controller.InventarioController;
import org.tiendakinal.controller.MenuController;

/**
 * clean javafx:run
 *
 * @author informatica
 */
public class Main extends Application {

    private Stage escenarioPrincipal;
    private Scene siguienteEscena;
    private static String URL = "/view/";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage escenarioPrincipal) throws Exception {
        this.escenarioPrincipal = escenarioPrincipal;
        escenarioPrincipal.setTitle("TIENDA KINAL");

//        Image icono = new Image(getClass().getResource("/org/rogervalladares/image/logoImagen.png").toExternalForm());
//        escenarioPrincipal.getIcons().add(icono);
        getLoginView();
        escenarioPrincipal.show();
    }

    public Initializable cambiarEscena(String fxml, double ancho, double alto) throws Exception {
        Initializable interfazDeCambio = null;
        FXMLLoader cargadorFXML = new FXMLLoader();
        InputStream archivoFXML = Main.class.getResourceAsStream(URL + fxml);

        cargadorFXML.setBuilderFactory(new JavaFXBuilderFactory());
        cargadorFXML.setLocation(Main.class.getResource(URL + fxml));

        siguienteEscena = new Scene(cargadorFXML.load(archivoFXML), ancho, alto);
        escenarioPrincipal.setScene(siguienteEscena);
        escenarioPrincipal.sizeToScene();

        interfazDeCambio = cargadorFXML.getController();
        return interfazDeCambio;
    }

    public void getLoginView() {
        try {
            InicioController control
                    = (InicioController) cambiarEscena("InicioView.fxml", 600, 600);
            control.setPrincipal(this);
        } catch (Exception ex) {
            System.out.println("Error al ir al login/Inicio: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void getMenuView() {
        try {
            MenuController control
                    = (MenuController) cambiarEscena("MenuView.fxml", 650, 650);
            control.setPrincipal(this);
        } catch (Exception ex) {
            System.out.println("Error al ir a Menu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void getInventarioView() {
        try {
            InventarioController control
                    = (InventarioController) cambiarEscena("InventarioView.fxml", 913, 691);
            control.setPrincipal(this);
        } catch (Exception ex) {
            System.out.println("Error al ir a Menu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void getCompraInicioView() {
        try {
            CompraInicioController control
                    = (CompraInicioController) cambiarEscena("CompraInicioView.fxml", 1394, 606);
            control.setPrincipal(this);
        } catch (Exception ex) {
            System.out.println("Error al ir a Menu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void getCompraFinView() {
        try {
            CompraFinController control
                    = (CompraFinController) cambiarEscena("CompraFinView.fxml", 1242, 672);
            control.setPrincipal(this);
        } catch (Exception ex) {
            System.out.println("Error al ir a Menu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    

    public void getCompraFinView(int idCompra, double subtotal) {
        try {
            FXMLLoader cargadorFXML = new FXMLLoader();
            InputStream archivoFXML = Main.class.getResourceAsStream(URL + "CompraFinView.fxml");

            cargadorFXML.setBuilderFactory(new JavaFXBuilderFactory());
            cargadorFXML.setLocation(Main.class.getResource(URL + "CompraFinView.fxml"));

            siguienteEscena = new Scene(cargadorFXML.load(archivoFXML), 1242, 672);
            escenarioPrincipal.setScene(siguienteEscena);
            escenarioPrincipal.sizeToScene();

            CompraFinController control = cargadorFXML.getController();
            control.setPrincipal(this);
            control.initData(idCompra, subtotal); // Pasar los datos a Factura2Controller
        } catch (Exception ex) {
            System.out.println("Error al ir a factura2: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
