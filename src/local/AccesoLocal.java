/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package local;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import modelo.dao.BaseDAO;
import modelo.util.ConnectionManager;
import modelo.dao.EmpleadoDAO;
import modelo.dto.DataTable;
import remote.Sitio1Int;
import remote.util.InterfaceManager;

/**
 *
 * @author jdosornio
 */
public class AccesoLocal extends UnicastRemoteObject implements Sitio1Int {

    
    public AccesoLocal() throws RemoteException {

    }

    @Override
    public DataTable getEmpleado(String numeroEmp) throws RemoteException {
        return new EmpleadoDAO().get(numeroEmp);
    }

    @Override
    public DataTable getEmpleadosByImplementacion(int idImplementacion) throws RemoteException {
        return new EmpleadoDAO().getByImplementacion(idImplementacion);
    }

    @Override
    public DataTable insert(boolean savePKs, String tabla, DataTable datos)
            throws RemoteException {
        DataTable ok;
        BaseDAO dao = new BaseDAO();

        ok = dao.add(tabla, datos, savePKs);

        System.out.println("Inserción de " + tabla + " , resultado: "
                + ok);

        return ok;
    }

    @Override
    public boolean update(String tabla, DataTable datos, Map<String, ?> attrWhere)
            throws RemoteException {
        
        boolean ok = new BaseDAO().update(tabla, datos, attrWhere);
        
        System.out.println("Se actualizó la tabla: " + tabla + " resultado: " + ok);
        
        return ok;
    }

    @Override
    public boolean delete(String tabla, Map<String, ?> attrWhere) throws RemoteException {
        
        boolean ok = new BaseDAO().delete(tabla, attrWhere);
        
        System.out.println("Se eliminó de la tabla: " + tabla + " resultado: " + ok);
        
        return ok;
    }

    @Override
    public DataTable get(String tabla, String[] columnas, String[] aliases, Map<String, ?> attrWhere,
            String columnName) throws RemoteException {
        return new BaseDAO().get(tabla, columnas, aliases, attrWhere, columnName);
    }

    @Override
    public boolean commit() throws RemoteException {
        System.out.println("Commit!");
        boolean ok = ConnectionManager.commit();
        ConnectionManager.cerrar();

        return ok;
    }

    @Override
    public boolean rollback() throws RemoteException {
        System.out.println("Rollback!");
        boolean ok = ConnectionManager.rollback();
        ConnectionManager.cerrar();

        return ok;
    }
    
    @Override
    public short updateEventosByProveedor(int idProveedor, int[] idsEvento) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public short updateEmpleadosByImplementacion(int idImplementacion, int[] idsEmpleado) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setConexionesSitos(Map<InterfaceManager.Interfaces, Object[]> conexiones)
            throws RemoteException {

        InterfaceManager.setIntefacesConexion(conexiones);
        InterfaceManager.conexionRemota = true;
        
        System.out.println("Se obtuvieron " + InterfaceManager
                .getInterfacesConexion().size() + " interfaces");
    }
}
