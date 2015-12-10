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
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.dto.DataTable;

/**
 *
 * @author jdosornio
 */
public class EmpleadoDAO {
    //Tabla entidad
    private static final String EMPLEADO = "empleado";
    
    //Atributos entidad
    private static final String NUMERO = "numero";
    
    //Otras tablas
    private static final String IMPLEMENTACION_EVENTO_EMPLEADO =
            "implementacion_evento_empleado";
    
    //Otros atributos
    private static final String IMPLEMENTACION_EVENTO_ID = 
            "implementacion_evento_id";
    private static final String EMPLEADO_NUMERO = "empleado_numero";
    
    
    //Queries
    private static final String GET_EMPLEADO = "SELECT * FROM " + EMPLEADO + 
            " WHERE " + NUMERO + " = ?";
    private static final String GET_EMPLEADOS_BY_IMPLEMENTACION = "SELECT * " +
            "FROM " + EMPLEADO + " WHERE " + NUMERO + " IN ( " +
            "SELECT " + EMPLEADO_NUMERO + " FROM " + IMPLEMENTACION_EVENTO_EMPLEADO + 
            " WHERE " + IMPLEMENTACION_EVENTO_ID + " = ? )";
    
    
    public DataTable get(String numeroEmp) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        DataTable result = null;
        Connection conexion;

        try {
            conexion = ConnectionManager.conectar();
            
            ps = conexion.prepareStatement(GET_EMPLEADO);
            ps.setString(1, numeroEmp);

            rs = ps.executeQuery();

            result = new DataTable();

            result.populate(rs);

            //ConnectionManager.commit();
        } catch (SQLException ex) {
            ConnectionManager.rollback();
            Logger.getLogger(EmpleadoDAO.class.getName()).log(Level.SEVERE, null, ex);
            
        } finally {
            ConnectionManager.cerrarTodo(ps, rs);
        }
        return result;
    }
    
    public DataTable getByImplementacion(int idImplementacion) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        DataTable result = null;
        Connection conexion;

        try {
            conexion = ConnectionManager.conectar();
            
            ps = conexion.prepareStatement(GET_EMPLEADOS_BY_IMPLEMENTACION);
            ps.setInt(1, idImplementacion);

            rs = ps.executeQuery();

            result = new DataTable();

            result.populate(rs);

            //ConnectionManager.commit();
        } catch (SQLException ex) {
            ConnectionManager.rollback();
            Logger.getLogger(EmpleadoDAO.class.getName()).log(Level.SEVERE, null, ex);
            
        } finally {
            ConnectionManager.cerrarTodo(ps, rs);
        }
        return result;
    }
    
//    public static void main(String[] args) {
//        DataTable res = new EmpleadoDAO().getByImplementacion(3);
//        
//        System.out.println(Arrays.toString(res.getTableNames()));
//        
//        while(res.next()) {
//            for (int i = 0; i < res.getColumnCount(); i++) {
//                System.out.println(res.getColumnName(i) + ": " + res.getObject(res.getColumnName(i)));
//            }
//        }
//    }
}