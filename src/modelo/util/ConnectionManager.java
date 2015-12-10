/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author jdosornio
 */
public class ConnectionManager {

    private static final ThreadLocal<Connection> conexion = new ThreadLocal<>();
    private static final String user = "postgres";
    private static final String pass = "1116291";
    private static final String url = "jdbc:postgresql://localhost:5432/capacisoft";
    private static final String driver = "org.postgresql.Driver";

    public static Connection conectar() {
        Connection con = conexion.get();

        try {

            if (con == null || con.isClosed()) {
           

                Class.forName(driver);

                con = DriverManager.getConnection(url, user, pass);
                con.setAutoCommit(false);
            }
            
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
            con = null;
        }

        conexion.set(con);

        return con;
    }

    public static void cerrar(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void cerrar(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static boolean commit() {
        Connection con = conexion.get();
        boolean ok = true;
        
        try {
            if (con != null && !con.isClosed()) {
                con.commit();
                System.out.println("commited");
            }
        } catch (SQLException ex) {
            ok = false;
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ok;
    }

    public static boolean rollback() {
        Connection con = conexion.get();
        boolean ok = true;
        
        try {
            if (con != null && !con.isClosed()) {
                con.rollback();
                System.out.println("rollbacked");
            }
        } catch (SQLException ex) {
            ok = false;
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ok;
    }

    public static void cerrar() {
        Connection con = conexion.get();

        try {
            if (con != null /*&& !con.isClosed()*/) {
                con.close();
                conexion.set(null);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void cerrarTodo(PreparedStatement ps, ResultSet rs) {
        cerrar(ps);
        cerrar(rs);
        cerrar();

    }
}