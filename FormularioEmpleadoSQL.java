import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormularioEmpleadoSQL extends JFrame {
    private JTextField txtId, txtNombre, txtApellidoP, txtApellidoM, txtSalario;
    private JComboBox<String> comboPuesto;
    private JButton btnGuardar, btnEliminar, btnModificar;
    private JTable tablaEmpleados;
    private DefaultTableModel modelo;

    private static final String URL = "jdbc:mysql://localhost:3306/empresa";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "ACRETSODNFQVPSZX976372IXAKOGTULB1";

    public FormularioEmpleadoSQL() {
        setTitle("Formulario Empleado");
        setSize(750, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());



        //////////////////// Título //////////////////////
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(255, 180, 180));
        JLabel lblTitulo = new JLabel("Formulario de Empleados", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);




        //////////////////// Panel para el formulario //////////////////////
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(new Color(255, 200, 200));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        txtId = new JTextField();
        txtNombre = new JTextField();
        txtApellidoP = new JTextField();
        txtApellidoM = new JTextField();
        txtSalario = new JTextField();



        //////////////////// ComboBox para los Puestos //////////////////////
        comboPuesto = new JComboBox<>(new String[]{
            "Administrador", "Vendedor", "Almacenista", "Transportista", "Ingeniero en Sistemas"
        });

        addFila(panelFormulario, gbc, 0, "ID Empleado:", txtId);
        addFila(panelFormulario, gbc, 1, "Nombre:", txtNombre);
        addFila(panelFormulario, gbc, 2, "Apellido Paterno:", txtApellidoP);
        addFila(panelFormulario, gbc, 3, "Apellido Materno:", txtApellidoM);
        addFila(panelFormulario, gbc, 4, "Puesto:", comboPuesto);
        addFila(panelFormulario, gbc, 5, "Salario:", txtSalario);



        //////////////////// Botones del formulario //////////////////////
        btnGuardar = new JButton("Guardar");
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panelFormulario.add(btnGuardar, gbc);

        btnEliminar = new JButton("Eliminar");
        gbc.gridy = 7; gbc.gridwidth = 1;
        panelFormulario.add(btnEliminar, gbc);

        btnModificar = new JButton("Modificar");
        gbc.gridx = 1; gbc.gridy = 7;
        panelFormulario.add(btnModificar, gbc);



        //////////////////// Panel para la tabla de registros //////////////////////
        String[] columnas = {"ID", "Nombre", "Apellido Paterno", "Apellido Materno", "Puesto", "Salario"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaEmpleados = new JTable(modelo);
        JScrollPane scrollTabla = new JScrollPane(tablaEmpleados);
        scrollTabla.getViewport().setBackground(new Color(255, 220, 220));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelFormulario, scrollTabla);
        splitPane.setDividerLocation(350);
        add(splitPane, BorderLayout.CENTER);



        //- Acción Guardar -//
        btnGuardar.addActionListener(e -> guardarRegistro());

        //- Acción Eliminar
        btnEliminar.addActionListener(e -> eliminarRegistro());

        //- Acción Modificar
        btnModificar.addActionListener(e -> modificarRegistro());



        //- Cargar los datos al seleccionar una fila -//
        tablaEmpleados.getSelectionModel().addListSelectionListener(event -> {
            int fila = tablaEmpleados.getSelectedRow();
            if (fila != -1) {
                txtId.setText(modelo.getValueAt(fila, 0).toString());
                txtNombre.setText(modelo.getValueAt(fila, 1).toString());
                txtApellidoP.setText(modelo.getValueAt(fila, 2).toString());
                txtApellidoM.setText(modelo.getValueAt(fila, 3).toString());
                comboPuesto.setSelectedItem(modelo.getValueAt(fila, 4).toString());
                txtSalario.setText(modelo.getValueAt(fila, 5).toString());
            }
        });

        inicializarBD();
        cargarDatos();
        setVisible(true);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }

    private void inicializarBD() {
        String sql = "CREATE TABLE IF NOT EXISTS empleados ("
            + "id INT PRIMARY KEY, "
            + "nombre VARCHAR(100) NOT NULL, "
            + "apellido_p VARCHAR(100) NOT NULL, "
            + "apellido_m VARCHAR(100) DEFAULT '', "
            + "puesto VARCHAR(50) NOT NULL, "
            + "salario DECIMAL(10,2) NOT NULL)";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al inicializar BD: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        String sql = "SELECT * FROM empleados ORDER BY id";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido_p"),
                    rs.getString("apellido_m"),
                    rs.getString("puesto"),
                    rs.getDouble("salario")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }


    //////////////////// Funciones de cada boton //////////////////////
    

    private void guardarRegistro() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            double salario = Double.parseDouble(txtSalario.getText().trim());

            if (salario <= 0) {
                JOptionPane.showMessageDialog(this, "El salario debe ser mayor que 0", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtNombre.getText().trim().isEmpty() || 
                txtApellidoP.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y apellido paterno no pueden estar en blanco", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String nombre = txtNombre.getText().trim();
            String apellidoP = txtApellidoP.getText().trim();
            String apellidoM = txtApellidoM.getText().trim();
            String puesto = comboPuesto.getSelectedItem().toString();

            String sql = "INSERT INTO empleados (id, nombre, apellido_p, apellido_m, puesto, salario) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, nombre);
                ps.setString(3, apellidoP);
                ps.setString(4, apellidoM);
                ps.setString(5, puesto);
                ps.setDouble(6, salario);
                ps.executeUpdate();
            }

            modelo.addRow(new Object[]{id, nombre, apellidoP, apellidoM, puesto, salario});
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Registro guardado correctamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID debe ser entero y salario válido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }


    
    private void eliminarRegistro() {
        int fila = tablaEmpleados.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un registro para eliminar", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int id = (int) modelo.getValueAt(fila, 0);

        String sql = "DELETE FROM empleados WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            return;
        }

        modelo.removeRow(fila);
        JOptionPane.showMessageDialog(this, "Registro eliminado correctamente");
        limpiarCampos();
    }


  
    private void modificarRegistro() {
        int fila = tablaEmpleados.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un registro para modificar", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            double salario = Double.parseDouble(txtSalario.getText().trim());

            if (salario <= 0) {
                JOptionPane.showMessageDialog(this, "El salario debe ser mayor que 0", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtNombre.getText().trim().isEmpty() || 
                txtApellidoP.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y apellido paterno no pueden estar en blanco", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String nombre = txtNombre.getText().trim();
            String apellidoP = txtApellidoP.getText().trim();
            String apellidoM = txtApellidoM.getText().trim();
            String puesto = comboPuesto.getSelectedItem().toString();

            String sql = "UPDATE empleados SET nombre = ?, apellido_p = ?, apellido_m = ?, puesto = ?, salario = ? WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setString(2, apellidoP);
                ps.setString(3, apellidoM);
                ps.setString(4, puesto);
                ps.setDouble(5, salario);
                ps.setInt(6, id);
                ps.executeUpdate();
            }

            modelo.setValueAt(id, fila, 0);
            modelo.setValueAt(nombre, fila, 1);
            modelo.setValueAt(apellidoP, fila, 2);
            modelo.setValueAt(apellidoM, fila, 3);
            modelo.setValueAt(puesto, fila, 4);
            modelo.setValueAt(salario, fila, 5);

            JOptionPane.showMessageDialog(this, "Registro modificado correctamente");
            limpiarCampos();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID debe ser entero y salario válido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al modificar: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addFila(JPanel panel, GridBagConstraints gbc, int fila, String texto, JComponent campo) {
        JLabel label = new JLabel(texto);
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0.3;
        panel.add(label, gbc);

        gbc.gridx = 1; gbc.gridy = fila; gbc.weightx = 0.7;
        panel.add(campo, gbc);
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtApellidoP.setText("");
        txtApellidoM.setText("");
        txtSalario.setText("");
        comboPuesto.setSelectedIndex(0);
    }


    //\\

    
    public static void main(String[] args) {
        new FormularioEmpleadoSQL();
    }
}
