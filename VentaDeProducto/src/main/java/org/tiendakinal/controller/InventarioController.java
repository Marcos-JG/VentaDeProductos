package org.tiendakinal.controller;

import java.io.InputStream;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.tiendakinal.database.Conexion;
import org.tiendakinal.model.Producto;
import org.tiendakinal.report.Report;
import org.tiendakinal.system.Main;

/**
 * FXML Controller class
 *
 * @author tecno facil outlet
 */
public class InventarioController implements Initializable {

    @FXML
    private Button btnRegresar, btnAgregar, btnEditar, btnEliminar, btnReporte;

    @FXML
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, Integer> colId, colStock;
    @FXML
    private TableColumn<Producto, String> colNombre, colCodigoBarras, colMarca;
    @FXML
    private TableColumn<Producto, Double> colPrecio;

    @FXML
    private TextField txtId, txtNombre, txtMarca, txtPrecio, txtStock, txtCodigoBarras;

    private ObservableList<Producto> listaProductos;
    private Main principal;
    private Producto modeloProducto;

    private enum Acciones {
        Agregar, Editar, Eliminar, Ninguna
    };
    private Acciones accion = Acciones.Ninguna;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setFormatoColumnaModelo();
        cargarDatos();
        tablaProductos.setOnMouseClicked(event -> {
            getProductoTextFiel();
            deshabilitarControles();
        });
        deshabilitarControles();
    }

    public void setFormatoColumnaModelo() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marcaProducto"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioProducto"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockProducto"));
        colCodigoBarras.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
    }

    public void cargarDatos() {
        listaProductos = FXCollections.observableArrayList(listarProductos());
        tablaProductos.setItems(listaProductos);
        if (!listaProductos.isEmpty()) {
            tablaProductos.getSelectionModel().selectFirst();
            getProductoTextFiel();
        }
    }

    public void getProductoTextFiel() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            txtId.setText(String.valueOf(productoSeleccionado.getIdProducto()));
            txtNombre.setText(productoSeleccionado.getNombreProducto());
            txtMarca.setText(productoSeleccionado.getMarcaProducto());
            txtPrecio.setText(String.valueOf(productoSeleccionado.getPrecioProducto()));
            txtStock.setText(String.valueOf(productoSeleccionado.getStockProducto()));
            txtCodigoBarras.setText(productoSeleccionado.getCodigoBarras());
        }
    }

    public ArrayList<Producto> listarProductos() {
        ArrayList<Producto> productos = new ArrayList<>();
        try {
            Connection conexion = Conexion.getInstancia().getConexion();
            String sql = "SELECT idProducto, nombreProducto,  marcaProducto, precioProducto, stockProducto, codigoBarras FROM Productos";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                productos.add(new Producto(
                        rs.getInt("idProducto"),
                        rs.getString("nombreProducto"),
                        rs.getString("marcaProducto"),
                        rs.getDouble("precioProducto"),
                        rs.getInt("stockProducto"),
                        rs.getString("codigoBarras")
                ));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error al cargar productos: " + e.getMessage());
        }
        return productos;
    }

    private Producto getModeloProducto() {
        int idProducto = txtId.getText().isEmpty() ? 0 : Integer.parseInt(txtId.getText());
        String nombreProducto = txtNombre.getText();
        String marcaProducto = txtMarca.getText();
        double precioProducto = txtPrecio.getText().isEmpty() ? 0 : Double.parseDouble(txtPrecio.getText());
        int stockProducto = txtStock.getText().isEmpty() ? 0 : Integer.parseInt(txtStock.getText());
        String codigoBarras = txtCodigoBarras.getText();

        return new Producto(idProducto, nombreProducto, marcaProducto, precioProducto, stockProducto, codigoBarras);
    }

    public void agregarProducto() {
        if (validarCampos()) {
            modeloProducto = getModeloProducto();
            try {
                CallableStatement stmt = Conexion.getInstancia().getConexion()
                        .prepareCall("{call sp_agregarProducto(?, ?, ?, ?, ?)}");
                stmt.setString(1, modeloProducto.getNombreProducto());
                stmt.setString(2, modeloProducto.getMarcaProducto());
                stmt.setDouble(3, modeloProducto.getPrecioProducto());
                stmt.setInt(4, modeloProducto.getStockProducto());
                stmt.setString(5, modeloProducto.getCodigoBarras());
                stmt.execute();
                cargarDatos();
                cambiarOriginal();
            } catch (SQLException e) {
                mostrarAlerta("Error al agregar producto: " + e.getMessage());
            }
        }
    }

    public void editarProducto() {
        if (validarCampos()) {
            modeloProducto = getModeloProducto();
            try {
                CallableStatement stmt = Conexion.getInstancia().getConexion()
                        .prepareCall("{call sp_ActualizarProducto(?, ?, ?, ?, ?, ?)}");
                stmt.setInt(1, modeloProducto.getIdProducto());
                stmt.setString(2, modeloProducto.getNombreProducto());
                stmt.setString(3, modeloProducto.getMarcaProducto());
                stmt.setDouble(4, modeloProducto.getPrecioProducto());
                stmt.setInt(5, modeloProducto.getStockProducto());
                stmt.setString(6, modeloProducto.getCodigoBarras());
                stmt.execute();
                cargarDatos();
                cambiarOriginal();
            } catch (SQLException e) {
                mostrarAlerta("Error al editar producto: " + e.getMessage());
            }
        }
    }

    public void eliminarProducto() {
        modeloProducto = getModeloProducto();
        try {
            CallableStatement stmt = Conexion.getInstancia().getConexion()
                    .prepareCall("{call sp_EliminarProducto(?)}");
            stmt.setInt(1, modeloProducto.getIdProducto());
            stmt.execute();
            cargarDatos();
        } catch (SQLException e) {
            mostrarAlerta("Error al eliminar producto: " + e.getMessage());
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty()
                || txtMarca.getText().isEmpty()
                || txtPrecio.getText().isEmpty()
                || txtStock.getText().isEmpty()
                || txtCodigoBarras.getText().isEmpty()) {

            mostrarAlerta("Campo/s vacios");
            return false;
        }

        try {
            Double.parseDouble(txtPrecio.getText());
            Integer.parseInt(txtStock.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Precio y Stock deben ser valores numéricos");
            return false;
        }

        return true;
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void limpiarTexto() {
        txtId.clear();
        txtNombre.clear();
        txtMarca.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtCodigoBarras.clear();
    }

    public void btnRegresarActionEvent(ActionEvent evento) {
        principal.getMenuView();
    }

    @FXML
    private void cambiarNuevoProducto() {
        switch (accion) {
            case Ninguna:
                cambiarGuardarEditar();
                accion = Acciones.Agregar;
                limpiarTexto();
                habilitarControles(true);
                break;
            case Agregar:
                agregarProducto();
                break;
            case Editar:
                editarProducto();
                break;
        }
    }

    @FXML
    private void cambiarEdicionProducto() {
        cambiarGuardarEditar();
        accion = Acciones.Editar;
        habilitarControles(true);
    }

    @FXML
    private void cambiarCancelarEliminar() {
        if (accion == Acciones.Ninguna) {
            eliminarProducto();
        } else {
            cambiarOriginal();
        }
    }

    private void cambiarGuardarEditar() {
        btnAgregar.setText("Guardar");
        btnEliminar.setText("Cancelar");
        btnEditar.setDisable(true);
    }

    private void cambiarOriginal() {
        btnAgregar.setText("Agregar");
        btnEliminar.setText("Eliminar");
        btnEditar.setDisable(false);
        accion = Acciones.Ninguna;
        deshabilitarControles();
    }

    private void deshabilitarControles() {
        txtId.setDisable(true);
        txtNombre.setDisable(true);
        txtPrecio.setDisable(true);
        txtStock.setDisable(true);
        txtCodigoBarras.setDisable(true);
        tablaProductos.setDisable(false);
    }

    private void habilitarControles(boolean habilitar) {
        txtId.setDisable(true); // ID siempre deshabilitado
        txtNombre.setDisable(!habilitar);
        txtPrecio.setDisable(!habilitar);
        txtStock.setDisable(!habilitar);
        txtCodigoBarras.setDisable(!habilitar);
        tablaProductos.setDisable(habilitar);
    }

    public void setPrincipal(Main principal) {
        this.principal = principal;
    }

    //------------------------------------------------------------------------------------------------------------------INICIA EL REPORTE
    //Map parametros
    private Map<String, Object> parametros;

    //cargador inputStream
    private InputStream cargarReporte(String urlReporte) {
        InputStream reporte = null;
        try {
            reporte = Main.class.getResourceAsStream(urlReporte);
            reporte.getClass().getResource(urlReporte);
        } catch (Exception e) {
            System.out.println("Error al cargar" + urlReporte + e.getMessage());
            e.printStackTrace();
        }
        return reporte;
    }

    //inicializador de impresion  de reporte
    @FXML
    private void imprimirReporte() {
        Connection conexion = Conexion.getInstancia().getConexion();
        parametros = new HashMap<>();
        parametros.put("img", "src/main/resources/image/");
        Report.generarReporte(conexion, parametros, cargarReporte("/org/tiendaKinal/report/Inventario.jasper"));
        Report.mostrarReporte();
    }
    //------------------------------------------------------------------------------------------------------------------FINALIZA EL REPORTE

}
