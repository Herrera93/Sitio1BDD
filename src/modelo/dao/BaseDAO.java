/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.dao;

import modelo.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.dto.DataTable;

/**
 *
 * @author jdosornio
 */
public class BaseDAO {

    public DataTable add(String tableName, DataTable data, boolean savePKs) {
        PreparedStatement ps = null;
        Connection conexion;
        String insertQuery = "INSERT INTO " + tableName + "( ";
        String valuesSection = "VALUES ( ";

        if (data == null || data.getRowCount() == 0) {
            return null;
        }

        try {
            //Crear query
            for (int i = 0; i < data.getColumnCount() - 1; i++) {
                if (savePKs && i == 0) {
                    //Si se quieren guardar las llaves primarias entonces
                    //ignorar la llave primaria que es la columna 1.
                    continue;
                }

                insertQuery += data.getColumnName(i) + ", ";
                valuesSection += "?, ";
            }
            insertQuery += data.getColumnName(data.getColumnCount() - 1) + " ) ";
            valuesSection += "? )";

            //Query completo...
            insertQuery += valuesSection;

            System.out.println(insertQuery);

            conexion = ConnectionManager.conectar();

            //Si se desea guardar las llaves generadas o no
            if (savePKs) {
                //Devolver la columna de llave primaria (primera)
                ps = conexion.prepareStatement(insertQuery,
                        new String[]{data.getColumnName(0)});
            } else {
                ps = conexion.prepareStatement(insertQuery);
            }

            //Cargar datos...
            //Reiniciar por si las dudas...
            data.rewind();

            while (data.next()) {
                for (int i = 0; i < data.getColumnCount(); i++) {
                    //Posicion del preparedStatement
                    int psPos = i + 1;
                    //Si se desea guardar las llaves generadas ignorar la pk
                    if (savePKs) {
                        if (i == 0) {
                            continue;
                        } else {
                            psPos--;
                        }
                    }

                    ps.setObject(psPos, data.getObject(data.getColumnName(i)));
                }
                //Ejecutar insert uno a uno para obtener la llave primaria
                ps.executeUpdate();

                if (savePKs) {
                    //Obtener llave primaria generada
                    try (ResultSet rsPK = ps.getGeneratedKeys()) {

                        if (rsPK.next()) {
                            //Guardar llave primaria generada en el DataTable
                            data.setObject(data.getColumnName(0), rsPK.getObject(1));
                        }
                    }
                }
            }

            //Si se guardaron llaves primarias regresar al inicio
            if (savePKs) {
                data.rewind();
            }

            System.out.println("Id 1: " + data.getValueAt(0, 0));

            //ConnectionManager.commit();
        } catch (SQLException ex) {
            ConnectionManager.rollback();
            ConnectionManager.cerrar();
            data = null;
            Logger.getLogger(BaseDAO.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            ConnectionManager.cerrar(ps);
            //ConnectionManager.cerrarTodo(ps, null);
        }

        return data;
    }

    public boolean update(String tableName, DataTable data, Map<String, ?> attrWhere) {
        PreparedStatement ps = null;
        Connection conexion;
        boolean ok = true;
        String updateQuery = "UPDATE " + tableName + " SET ";

        if (data == null || data.getRowCount() == 0) {
            return false;
        }

        try {
            //Crear query
            for (int i = 0; i < data.getColumnCount() - 1; i++) {
                updateQuery += data.getColumnName(i) + " = ?, ";
            }
            updateQuery += data.getColumnName(data.getColumnCount() - 1) + " = ?";

            //WHERE clauses...
            if (attrWhere != null && !attrWhere.isEmpty()) {
                updateQuery += " WHERE ";
                List<String> attrs = new ArrayList<>(attrWhere.keySet());

                for (int i = 0; i < attrs.size() - 1; i++) {
                    String key = attrs.get(i).toLowerCase();

                    updateQuery += key + " ";

                    if (!key.contains("=") && !key.contains("<")
                            && !key.contains(">") && !key.endsWith("like")) {
                        updateQuery += "= ";
                    }

                    updateQuery += "? AND ";
                }
                //Last one
                String key = attrs.get(attrs.size() - 1).toLowerCase();

                updateQuery += key + " ";

                if (!key.contains("=") && !key.contains("<")
                        && !key.contains(">") && !key.endsWith("like")) {
                    updateQuery += "= ";
                }

                updateQuery += "?";
            }

            System.out.println(updateQuery);

            conexion = ConnectionManager.conectar();

            ps = conexion.prepareStatement(updateQuery);

            //Cargar datos...
            //Reiniciar por si las dudas
            data.rewind();

            data.next();

            //SET
            for (int i = 0; i < data.getColumnCount(); i++) {
                ps.setObject(i + 1, data.getObject(data.getColumnName(i)));
            }

            //WHERE
            if (attrWhere != null && !attrWhere.isEmpty()) {
                int paramNumber = data.getColumnCount();

                for (String key : attrWhere.keySet()) {
                    ps.setObject(paramNumber + 1, attrWhere.get(key));
                    paramNumber++;
                }
            }

            ps.executeUpdate();

            //ConnectionManager.commit();
        } catch (SQLException ex) {
            ConnectionManager.rollback();
            ConnectionManager.cerrar();
            ok = false;
            Logger.getLogger(BaseDAO.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            ConnectionManager.cerrar(ps);
            //ConnectionManager.cerrarTodo(ps, null);
        }

        return ok;
    }

    public boolean delete(String tableName, Map<String, ?> attrWhere) {
        PreparedStatement ps = null;
        Connection conexion;
        boolean ok = true;
        String deleteQuery = "DELETE FROM " + tableName;

        try {
            //Crear query
            //WHERE clauses...
            if (attrWhere != null && !attrWhere.isEmpty()) {
                deleteQuery += " WHERE ";
                List<String> attrs = new ArrayList<>(attrWhere.keySet());

                for (int i = 0; i < attrs.size() - 1; i++) {
                    String key = attrs.get(i).toLowerCase();

                    deleteQuery += key + " ";

                    if (!key.contains("=") && !key.contains("<")
                            && !key.contains(">") && !key.endsWith("like")) {
                        deleteQuery += "= ";
                    }

                    deleteQuery += "? AND ";
                }
                //Last one
                String key = attrs.get(attrs.size() - 1).toLowerCase();

                deleteQuery += key + " ";

                if (!key.contains("=") && !key.contains("<")
                        && !key.contains(">") && !key.endsWith("like")) {
                    deleteQuery += "= ";
                }

                deleteQuery += "?";
            }

            System.out.println(deleteQuery);

            conexion = ConnectionManager.conectar();

            ps = conexion.prepareStatement(deleteQuery);
            //Cargar datos...
            //WHERE
            if (attrWhere != null && !attrWhere.isEmpty()) {
                int paramNumber = 0;

                for (String key : attrWhere.keySet()) {
                    ps.setObject(paramNumber + 1, attrWhere.get(key));
                    paramNumber++;
                }
            }

            ps.executeUpdate();

            //ConnectionManager.commit();
        } catch (SQLException ex) {
            ConnectionManager.rollback();
            ConnectionManager.cerrar();
            ok = false;
            Logger.getLogger(BaseDAO.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            ConnectionManager.cerrar(ps);
            //ConnectionManager.cerrarTodo(ps, null);
        }
        return ok;
    }

    public DataTable get(String tableName, String[] projectColumns,
            String[] projectAliases, Map<String, ?> attrWhere, String orderColumn) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conexion;
        DataTable dt = new DataTable();
        String selectQuery = "SELECT ";

        try {
            //Crear query
            //Project columns...
            if (projectColumns != null && projectColumns.length > 0
                    && projectAliases != null && projectAliases.length == projectColumns.length) {

                for (int i = 0; i < projectColumns.length - 1; i++) {
                    selectQuery += projectColumns[i];

                    if (projectAliases[i] != null && !projectAliases[i].isEmpty()) {
                        selectQuery += " AS " + projectAliases[i];
                    }

                    selectQuery += ", ";
                }
                selectQuery += projectColumns[projectColumns.length - 1];

                if (projectAliases[projectAliases.length - 1] != null
                        && !projectAliases[projectAliases.length - 1].isEmpty()) {
                    selectQuery += " AS " + projectAliases[projectAliases.length - 1];
                }

            } else {
                selectQuery += "*";
            }

            selectQuery += " FROM " + tableName;

            //WHERE clauses...
            if (attrWhere != null && !attrWhere.isEmpty()) {
                selectQuery += " WHERE ";
                List<String> attrs = new ArrayList<>(attrWhere.keySet());

                for (int i = 0; i < attrs.size() - 1; i++) {
                    String key = attrs.get(i);
                    String[] divide = key.split(" ");
                    String column = divide[0];
                    String operator;

                    if (divide.length > 1) {
                        operator = divide[1].toUpperCase();
                    } else {
                        operator = "";
                    }

                    selectQuery += column + " ";

                    if (operator.isEmpty()) {
                        selectQuery += "= ";
                    } else {
                        selectQuery += operator + " ";
                    }

                    //Llamar a otro metodo si contiene IN
                    if (operator.equals("IN")) {
                        Object values = attrWhere.get(key);
                        if (values == null || !(values instanceof DataTable)
                                || ((DataTable) values).isEmpty()) {
                            return null;
                        }

                        selectQuery += getIn((DataTable) values);
                    } else {
                        selectQuery += "?";
                    }

                    selectQuery += " AND ";
                }
                //Last one
                String key = attrs.get(attrs.size() - 1);
                String[] divide = key.split(" ");
                String column = divide[0];
                String operator;

                if (divide.length > 1) {
                    operator = divide[1].toUpperCase();
                } else {
                    operator = "";
                }

                selectQuery += column + " ";

                if (operator.isEmpty()) {
                    selectQuery += "= ";
                } else {
                    selectQuery += operator + " ";
                }

                //Llamar a otro metodo si contiene IN
                if (operator.equals("IN")) {
                    Object values = attrWhere.get(key);
                    if (values == null || !(values instanceof DataTable)
                            || ((DataTable) values).isEmpty()) {
                        return null;
                    }
                    selectQuery += getIn((DataTable)values);
                } else {
                    selectQuery += "?";
                }
            }

            selectQuery += " ORDER BY " + orderColumn;

            System.out.println(selectQuery);

            conexion = ConnectionManager.conectar();

            ps = conexion.prepareStatement(selectQuery);
            //Cargar datos...
            //WHERE
            if (attrWhere != null && !attrWhere.isEmpty()) {
                int paramNumber = 0;

                for (String key : attrWhere.keySet()) {
                    if (!key.endsWith("IN")) {
                        ps.setObject(paramNumber + 1, attrWhere.get(key));
                    }
                    paramNumber++;
                }
            }

            rs = ps.executeQuery();

            dt.populate(rs);

            //ConnectionManager.commit();
        } catch (SQLException ex) {
            //ConnectionManager.rollback();
            //ConnectionManager.cerrar();
            dt = null;
            Logger.getLogger(BaseDAO.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            ConnectionManager.cerrarTodo(ps, rs);
            //ConnectionManager.cerrarTodo(ps, null);
        }
        return dt;
    }

    private String getIn(DataTable values) {
        String query = "( ";

        //Poner toda la informacion
        for (int i = 0; i < values.getRowCount() - 1; i++) {
            if (values.getColumnClass(0) == String.class) {
                query += "'" + values.getValueAt(i, 0) + "', ";
            } else {
                query += values.getValueAt(i, 0) + ", ";
            }
        }

        int last = values.getRowCount() - 1;
        if (values.getColumnClass(0) == String.class) {
            query += "'" + values.getValueAt(last, 0) + "'";
        } else {
            query += values.getValueAt(last, 0);
        }

        query += " )";

        return query;
    }
}