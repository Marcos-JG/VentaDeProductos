package org.tiendakinal.controller;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import org.tiendakinal.model.Factura;
import org.tiendakinal.model.Producto;
import org.tiendakinal.system.Main;

/**
 * FXML Controller class
 *
 * @author tecno facil outlet
 */
public class CompraInicioController implements Initializable {

    @FXML
    private Button btnRegresar, btnEliminar, btnFinalizar;

    @FXML
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, Integer> colId;
    @FXML
    private TableColumn<Producto, String> colCodigo;
    @FXML
    private TableColumn<Producto, String> colProducto;
    @FXML
    private TableColumn<Producto, String> colMarca;
    @FXML
    private TableColumn<Producto, Double> colPrecioUnitario;
    @FXML
    private TableColumn<Factura, Integer> colCantidad;
    @FXML
    private TableColumn<Factura, Double> colSubtotal;

    @FXML
    private TextField txtCodigo, txtProducto, txtMarca, txtPrecio, txtCantidad;
    private ObservableList<Producto> listaProductos;
    private Main principal;
    private int idCompraActual;
    private int cantidadActual = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        deshabilitarControles();
        txtCodigo.setDisable(false);
        txtCantidad.setDisable(false);
        setFormatoColumnaModelo();
        cargarDatos();
        tablaProductos.setOnMouseClicked(event -> {
            getCompraTextFiel();
        });
    }

    private void crearNuevaCompra() {
        try {
            if (idCompraActual == 0) {
                Connection conexion = Conexion.getInstancia().getConexion();
                Statement stmt = conexion.createStatement();

                stmt.executeUpdate("INSERT INTO Compras(estadoCompra, estadoPago) VALUES ('Pendiente', 'Pendiente')",
                        Statement.RETURN_GENERATED_KEYS);

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    idCompraActual = generatedKeys.getInt(1);
                    System.out.println("Nueva compra creada con ID: " + idCompraActual);
                } else {
                    mostrarAlerta("No se pudo obtener el ID de la nueva compra");
                }

                generatedKeys.close();
                stmt.close();
            }
        } catch (SQLException e) {
            mostrarAlerta("Error al crear nueva compra: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setFormatoColumnaModelo() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marcaProducto"));
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
    }

    public void cargarDatos() {
        listaProductos = FXCollections.observableArrayList(listarProductosEnFactura());
        tablaProductos.setItems(listaProductos);
        if (!listaProductos.isEmpty()) {
            tablaProductos.getSelectionModel().selectFirst();
        }
    }

    public void getCompraTextFiel() {
    Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
    if (productoSeleccionado != null) {
        txtCodigo.setText(productoSeleccionado.getCodigoBarras());
        txtProducto.setText(productoSeleccionado.getNombreProducto());
        txtMarca.setText(productoSeleccionado.getMarcaProducto());
        txtPrecio.setText(String.valueOf(productoSeleccionado.getPrecioProducto()));
        txtCantidad.setText(String.valueOf(productoSeleccionado.getCantidad()));
    }
}
    

    private void buscarProductoPorCodigo(String codigo) {
        try {
            Connection conexion = Conexion.getInstancia().getConexion();
            String sql = "SELECT idProducto, nombreProducto, marcaProducto, precioProducto FROM Productos WHERE codigoBarras = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String sqlVerificar = "SELECT cantidad FROM DetalleCompras WHERE idCompra = ? AND idProducto = ?";
                PreparedStatement stmtVerificar = conexion.prepareStatement(sqlVerificar);
                stmtVerificar.setInt(1, idCompraActual);
                stmtVerificar.setInt(2, rs.getInt("idProducto"));
                ResultSet rsVerificar = stmtVerificar.executeQuery();

                if (rsVerificar.next()) {
                    int cantidadActual = rsVerificar.getInt("cantidad");
                    txtCantidad.setText(String.valueOf(cantidadActual + 1));
                } else {
                    txtCantidad.setText("1");
                }

                txtProducto.setText(rs.getString("nombreProducto"));
                txtPrecio.setText(String.valueOf(rs.getDouble("precioProducto")));
                txtMarca.setText(rs.getString("marcaProducto"));

                agregarProductoConCantidad();
            }
        } catch (SQLException e) {
            mostrarAlerta("Error al buscar producto: " + e.getMessage());
        }
    }

    public ArrayList<Producto> listarProductosEnFactura() {
        ArrayList<Producto> productos = new ArrayList<>();
        try {
            Connection conexion = Conexion.getInstancia().getConexion();
            String sql = "SELECT p.idProducto, p.codigoBarras, p.nombreProducto, p.marcaProducto, p.precioProducto, "
                    + "dc.cantidad, dc.subtotal "
                    + "FROM DetalleCompras dc "
                    + "JOIN Productos p ON dc.idProducto = p.idProducto "
                    + "WHERE dc.idCompra = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, idCompraActual);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Producto producto = new Producto(
                        rs.getInt("idProducto"),
                        rs.getString("nombreProducto"),
                        rs.getString("marcaProducto"),
                        rs.getString("codigoBarras"),
                        rs.getDouble("precioProducto")
                );
                producto.setCantidad(rs.getInt("cantidad"));
                producto.setSubtotal(rs.getDouble("subtotal"));
                productos.add(producto);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error al cargar productos de la factura: " + e.getMessage());
        }
        return productos;
    }

    @FXML
    public void agregarProductoConCantidad() {
        try {
            String codigo = txtCodigo.getText().trim();
            String cantidadText = txtCantidad.getText().trim();

            if (codigo.isEmpty()) {
                mostrarAlerta("Debe ingresar un código de barras.");
                return;
            }
            if (cantidadText.isEmpty()) {
                mostrarAlerta("Debe ingresar una cantidad.");
                return;
            }

            int cantidad;
            try {
                cantidad = Integer.parseInt(cantidadText);
                if (cantidad <= 0) {
                    mostrarAlerta("La cantidad debe ser mayor que cero.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("La cantidad debe ser un número válido.");
                return;
            }

            Connection conexion = Conexion.getInstancia().getConexion();
            String sqlProducto = "SELECT idProducto, nombreProducto, marcaProducto, precioProducto FROM Productos WHERE codigoBarras = ?";
            PreparedStatement stmtProducto = conexion.prepareStatement(sqlProducto);
            stmtProducto.setString(1, codigo);
            ResultSet rsProducto = stmtProducto.executeQuery();

            if (rsProducto.next()) {
                int idProducto = rsProducto.getInt("idProducto");
                crearNuevaCompra();

                String sqlVerificar = "SELECT cantidad FROM DetalleCompras WHERE idCompra = ? AND idProducto = ?";
                PreparedStatement stmtVerificar = conexion.prepareStatement(sqlVerificar);
                stmtVerificar.setInt(1, idCompraActual);
                stmtVerificar.setInt(2, idProducto);
                ResultSet rsVerificar = stmtVerificar.executeQuery();

                if (rsVerificar.next()) {
                    int cantidadActual = rsVerificar.getInt("cantidad");
                    int nuevaCantidad = cantidadActual + cantidad;

                    String sqlActualizar = "UPDATE DetalleCompras SET cantidad = ?, subtotal = (SELECT precioProducto FROM Productos WHERE idProducto = ?) * ? WHERE idCompra = ? AND idProducto = ?";
                    PreparedStatement stmtActualizar = conexion.prepareStatement(sqlActualizar);
                    stmtActualizar.setInt(1, nuevaCantidad);
                    stmtActualizar.setInt(2, idProducto);
                    stmtActualizar.setInt(3, nuevaCantidad);
                    stmtActualizar.setInt(4, idCompraActual);
                    stmtActualizar.setInt(5, idProducto);
                    stmtActualizar.executeUpdate();
                } else {
                    CallableStatement procedimiento = conexion.prepareCall("{call sp_agregarDetalleCompra(?, ?, ?)}");
                    procedimiento.setInt(1, idCompraActual);
                    procedimiento.setInt(2, idProducto);
                    procedimiento.setInt(3, cantidad);
                    procedimiento.execute();
                }

                cargarDatos();
                limpiarTexto();

            } else {
                mostrarAlerta("No se encontró ningún producto con ese código.");
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al agregar producto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void eliminarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            try {
                Connection conexion = Conexion.getInstancia().getConexion();
                String sql = "DELETE FROM DetalleCompras WHERE idCompra = ? AND idProducto = ?";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setInt(1, idCompraActual);
                stmt.setInt(2, productoSeleccionado.getIdProducto());
                stmt.executeUpdate();

                cargarDatos();
            } catch (SQLException e) {
                mostrarAlerta("Error al eliminar producto: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Seleccione un producto de la tabla para eliminar");
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void limpiarTexto() {
        txtCodigo.clear();
        txtProducto.clear();
        txtMarca.clear();
        txtPrecio.clear();
        txtCantidad.clear();
        txtCodigo.requestFocus();
    }

    public void btnRegresarActionEvent(ActionEvent evento) {
        principal.getLoginView();
    }

    @FXML
    private void cambiarNuevoFactura() {
        agregarProductoConCantidad();
    }

    @FXML
    private void cambiarCancelarEliminar() {
        eliminarProducto();
    }

    @FXML
    private void FinalizarPedido() {
        double subtotal = 0;
        for (Producto producto : listaProductos) {
            subtotal += producto.getSubtotal();
        }

        principal.getCompraFinView(idCompraActual, subtotal);
    }
    
    private void deshabilitarControles() {
        txtProducto.setDisable(true);
        txtPrecio.setDisable(true);
        txtMarca.setDisable(true);
    }

    public void setPrincipal(Main principal) {
        this.principal = principal;
    }
}
