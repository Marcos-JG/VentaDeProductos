    package org.tiendakinal.controller;

    import java.io.InputStream;
    import java.net.URL;
    import java.sql.CallableStatement;
    import java.sql.Connection;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.sql.Statement;
    import java.sql.Types;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.ResourceBundle;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.fxml.FXML;
    import javafx.fxml.Initializable;
    import javafx.scene.control.Alert;
    import javafx.scene.control.Button;
    import javafx.scene.control.ComboBox;
    import javafx.scene.control.ListCell;
    import javafx.scene.control.RadioButton;
    import javafx.scene.control.TextField;
    import javafx.scene.control.ToggleGroup;
    import org.tiendakinal.database.Conexion;
    import org.tiendakinal.system.Main;
    import org.tiendakinal.model.Usuario;
    import org.tiendakinal.report.Report;

    /**
     * FXML Controller class
     *
     * @author tecno facil outlet
     */
    public class CompraFinController implements Initializable {

        @FXML
        private Button btnPagar, btnEnviar, btnCancelar;
        @FXML
        private TextField txtNit, txtNombre, txtSubtotal, txtTotal;
        @FXML
        private ComboBox<Usuario> cbEmpleado;
        @FXML
        private RadioButton rbEfectivo, rbTarjeta;
        @FXML
        private ToggleGroup tgMetodoPago;

        private Main principal;
        private int idCompraActual;
        private double subtotal;

        @Override
        public void initialize(URL url, ResourceBundle rb) {
            deshabilitarCampos();
            cargarEmpleados();
        }

        public void initData(int idCompra, double subtotal) {
            this.idCompraActual = idCompra;
            this.subtotal = subtotal;
            mostrarDatos();
        }

        private void deshabilitarCampos() {
            txtSubtotal.setDisable(!true);
            txtTotal.setDisable(!true);
        }

        private void cargarEmpleados() {
            try {
                Connection conexion = Conexion.getInstancia().getConexion();
                String sql = "SELECT idUsuario, nombreUsuario FROM Usuarios WHERE tipo = 'Empleado'";
                Statement stmt = conexion.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                ObservableList<Usuario> empleados = FXCollections.observableArrayList();
                while (rs.next()) {
                    Usuario empleado = new Usuario();
                    empleado.setIdUsuario(rs.getInt("idUsuario"));
                    empleado.setNombreUsuario(rs.getString("nombreUsuario"));
                    empleados.add(empleado);
                }

                cbEmpleado.setItems(empleados);
                cbEmpleado.setCellFactory(param -> new ListCell<Usuario>() {
                    @Override
                    protected void updateItem(Usuario item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getIdUsuario() + " | " + item.getNombreUsuario());
                        }
                    }
                });

                cbEmpleado.setButtonCell(new ListCell<Usuario>() {
                    @Override
                    protected void updateItem(Usuario item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getIdUsuario() + " | " + item.getNombreUsuario());
                        }
                    }
                });

                if (!empleados.isEmpty()) {
                    cbEmpleado.getSelectionModel().selectFirst();
                }
            } catch (SQLException e) {
                mostrarAlerta("Error al cargar empleados: " + e.getMessage());
            }
        }

        private void mostrarDatos() {
            txtSubtotal.setText(String.format("%.2f", subtotal));
            double total = subtotal * 1.12;
            txtTotal.setText(String.format("%.2f", total));
        }

        @FXML
        private void pagar() {
            if (validarCampos()) {
                try {
                    Connection conexion = Conexion.getInstancia().getConexion();

                    String metodoPago = rbEfectivo.isSelected() ? "Efectivo" : "Tarjeta";
                    double total = Double.parseDouble(txtTotal.getText());

                    // Primero crear el pago
                    CallableStatement procedimientoPago = conexion.prepareCall("{call sp_AgregarPagos(?, ?, ?, ?)}");
                    procedimientoPago.setString(1, metodoPago);
                    procedimientoPago.setDouble(2, total);
                    procedimientoPago.setInt(3, idCompraActual);
                    procedimientoPago.registerOutParameter(4, Types.INTEGER);
                    procedimientoPago.execute();
                    int idPago = procedimientoPago.getInt(4);

                    // Crear la factura
                    Usuario empleado = cbEmpleado.getSelectionModel().getSelectedItem();
                    CallableStatement procedimientoFactura = conexion.prepareCall("{call sp_AgregarFactura(?, ?, ?, ?)}");
                    procedimientoFactura.setString(1, metodoPago);
                    procedimientoFactura.setInt(2, empleado.getIdUsuario());
                    procedimientoFactura.setInt(3, idCompraActual);
                    procedimientoFactura.setInt(4, idPago);
                    procedimientoFactura.execute();

                    // Obtener el ID de la factura recién creada
                    Statement stmtFactura = conexion.createStatement();
                    ResultSet rsFactura = stmtFactura.executeQuery("SELECT MAX(idFactura) FROM Facturas");
                    int idFactura = rsFactura.next() ? rsFactura.getInt(1) : 0;

                    // Manejar el cliente
                    String nombreCliente = txtNombre.getText().trim();
                    String nitCliente = txtNit.getText().trim();

                    if (!nombreCliente.isEmpty() || !nitCliente.isEmpty()) {
                        // Si hay datos, usar sp_AgregarCliente con los valores proporcionados
                        CallableStatement procedimientoCliente = conexion.prepareCall("{call sp_AgregarCliente(?, ?, ?, ?)}");
                        procedimientoCliente.setString(1, nombreCliente.isEmpty() ? "Consumidor final" : nombreCliente);
                        procedimientoCliente.setString(2, nitCliente.isEmpty() ? "Consumidor final" : nitCliente);
                        procedimientoCliente.setInt(3, idCompraActual);
                        procedimientoCliente.setInt(4, idFactura);
                        procedimientoCliente.execute();
                    } else {
                        // Si no hay datos, usar sp_AgregarCliente2 con valores por defecto
                        CallableStatement procedimientoCliente = conexion.prepareCall("{call sp_AgregarCliente2(?, ?)}");
                        procedimientoCliente.setInt(1, idCompraActual);
                        procedimientoCliente.setInt(2, idFactura);
                        procedimientoCliente.execute();
                    }

                    // Actualizar estado de la compra
                    Statement stmtUpdate = conexion.createStatement();
                    stmtUpdate.executeUpdate("UPDATE Compras SET estadoCompra = 'Completada', estadoPago = 'Pagado' WHERE idCompra = " + idCompraActual);

                    // Generar reporte
                    imprimirReporte(idCompraActual);
                    mostrarMensaje("", "Pago registrado y factura generada exitosamente");
                    principal.getCompraInicioView();

                } catch (SQLException e) {
                    mostrarAlerta("Error al registrar pago: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }


        //Map parametros
        private Map<String, Object> parametros;

        //cargador de imputStream
        private InputStream cargarReporte(String urlReporte) {
            InputStream reporte = null;
            try {
                reporte = Main.class.getResourceAsStream(urlReporte);
                if (reporte == null) {
                    throw new RuntimeException("No se encontró el archivo del reporte: " + urlReporte);
                }
            } catch (Exception e) {
                System.out.println("Error al cargar Reporte: " + urlReporte + " → " + e.getMessage());
                e.printStackTrace();
            }
            return reporte;
        }


            private void imprimirReporte(int idCompra) {
                try {
                    Connection conexion = Conexion.getInstancia().getConexion();
                    parametros = new HashMap<>();
                    parametros.put("idCompra", idCompra);
//                    parametros.put("URL", getClass().getResource("org/tiendaKinal/report/").toString());
                    parametros.put("URL","org/tiendaKinal/report/" );
                    System.out.println("ID de compra enviado al reporte: " + idCompra);
                    InputStream reporte = cargarReporte("/org/tiendakinal/report/VentaProductos.jasper");
                    Report.generarReporte(conexion, parametros, reporte);
                    Report.mostrarReporte();
                } catch (Exception e) {
                    mostrarAlerta("Error al generar reporte: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        @FXML
        private void cancelarPedido() {
            try {
                Connection conexion = Conexion.getInstancia().getConexion();
                Statement stmt = conexion.createStatement();
                stmt.executeUpdate("UPDATE Compras SET estadoCompra = 'Cancelada' WHERE idCompra = " + idCompraActual);

                principal.getCompraInicioView();
            } catch (SQLException e) {
                mostrarAlerta("Error al cancelar pedido: " + e.getMessage());
            }
        }

        private boolean validarCampos() {
            if (cbEmpleado.getSelectionModel().isEmpty()) {
                mostrarAlerta("Seleccione un empleado");
                return false;
            }

            if (!rbEfectivo.isSelected() && !rbTarjeta.isSelected()) {
                mostrarAlerta("Seleccione un método de pago");
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

        private void mostrarMensaje(String titulo, String mensaje) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }

        public void setPrincipal(Main principal) {
            this.principal = principal;
        }

    }
