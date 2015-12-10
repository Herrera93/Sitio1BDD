/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import modelo.dto.DataTable;
import remote.util.InterfaceManager.Interfaces;

/**
 *
 * @author jdosornio
 */
public interface Sitio extends Remote {
    
    /**
     * Inserta secuencialmente todos los datos de un DataTable en su tabla
     * correspondiente segun el nombre de la tabla
     * 
     * @param savePKs guardar las llaves primarias
     * @param tablas los nombres de las tablas a las que se desean insertar
     * datos, éstas tablas deben estar en el orden de inserción necesario para
     * que todas las tablas se puedan insertar sin que haya problemas de
     * integridad referencial.
     * @param datos un arreglo de objetos DataTable que contiene todos los datos
     * a insertar por cada tabla en el mismo orden que los nombres de las tablas
     * 
     * @return 1 en caso de que la inserción se haya realizado de forma correcta
     * o un 0 en caso de que haya ocurrido un error y se haya hecho rollback en
     * este nodo.
     * 
     * @throws RemoteException en caso de que ocurra un error remoto al invocar
     * este método
     */
    public DataTable insert(boolean savePKs, String tabla, DataTable  datos)
            throws RemoteException;
    
    /**
     * Actualiza los datos de un DataTable en la tabla con el nombre suministrado
     * en aquellos registros que cumplan con las cláusulas de igualdad en el mapa
     * enviado.
     * 
     * @param tabla el nombre de la tabla donde se actualizarán los datos
     * @param datos un objeto DataTable que contiene todos los datos que se van
     * a actualizar en la tabla
     * @param attrWhere un mapa que contiene las cláusulas where para modificar
     * la tabla, donde la llave es el nombre de la columna y el valor el valor
     * que esa columna debe tomar para que se actualicen los valores del DataTable
     * en ese registro
     * 
     * @return true en caso de que la actualización se haya realizado de forma
     * correcta o un false en caso de que haya ocurrido un error y se haya hecho
     * rollback en este nodo.
     * 
     * @throws RemoteException en caso de que ocurra un error remoto al invocar
     * este método
     */
    public boolean update(String tabla, DataTable datos, Map<String, ?> attrWhere)
            throws RemoteException;
    
    /**
     * Elimina todos los registros de una tabla dada que cumplan con las cláusulas
     * where de igualdad contenidas en el mapa suministrado
     * 
     * @param tabla el nombre de la tabla donde se desean eliminar los registros
     * @param attrWhere un mapa que contiene las cláusulas where para eliminar un
     * registro de la tabla, donde la llave es el nombre de la columna y el
     * valor el valor que esa columna debe tomar para que se elimine ese
     * registro de la tabla
     * 
     * @return 1 en caso de que la eliminación se haya realizado de forma
     * correcta o un 0 en caso de que haya ocurrido un error y se haya hecho
     * rollback en este nodo.
     * 
     * @throws RemoteException en caso de que ocurra un error remoto al invocar
     * este método
     */
    public boolean delete(String tabla, Map<String, ?> attrWhere) throws RemoteException;
    
    /**
     * Obtener el resultado de la consulta en la tabla elegida.
     * 
     * @param tabla nombre de la tabla donde se hará la consulta
     * @param columnas los nombres de las columnas de la tabla que se desean
     * obtener
     * @param aliases los nombres de los aliases que se desean por cada columna
     * ordenados igual que lo nombres de columna, null si no se desea alias.
     * @param attrWhere hashmap que contiene las condiciones where, con la llave
     * como columna y el valor como el valor que tomará la columna en el query.
     * Si ese hashmap es null, entonces se devolverán todos los registros
     * 
     * @return el objeto DataTable con los registros que se obtuvieron, un DataTable
     * vacío en caso de que no se hayan encontrado registros o null en caso de que
     * haya ocurrido un error con la base de datos.
     * 
     * @throws RemoteException en caso de que ocurra un error remoto.
     */
    public DataTable get(String tabla, String[] columnas, String[] aliases,
            Map<String, ?> attrWhere) throws RemoteException;
    
    /**
     * Este método sirve para indicar al nodo que se debe realizar un commit
     * de los cambios realizados en su base de datos
     * 
     * @return true en caso de que el commit se haya realizado de forma 
     * satisfactoria, false en caso contrario
     * 
     * @throws RemoteException en caso de que ocurra un error remoto al invocar
     * este método
     */
    public boolean commit() throws RemoteException;
    
    /**
     * Este método sirve para indiciar al nodo que se debe realizar un rollback
     * de los cambios realizados en su base de datos
     * 
     * @return true en caso de que el rollback se haya realizado en forma
     * satisfactoria, false en caso contrario
     * 
     * @throws RemoteException en caso de que ocurra un error remoto al invocar
     * este método
     */
    public boolean rollback() throws RemoteException;
    
    /**
     * Actualiza la lista de eventos que un proveedor puede impartir
     * 
     * @param idProveedor el id del proveedor al cuál se le desea sustituir su
     * lista de eventos que puede impartir
     * @param idsEvento un arreglo de ids de evento de todos los eventos de la
     * nueva lista de eventos que podrá impartir el proveedor con el id
     * suministrado
     * 
     * @return 1 en caso de que la actualización se haya realizado de forma
     * correcta o un 0 en caso de que haya ocurrido un error y se haya hecho
     * rollback en este nodo.
     * 
     * @throws RemoteException en caso de que ocurra un error remoto al invocar
     * este método
     */
    public short updateEventosByProveedor(int idProveedor, int[] idsEvento) throws RemoteException;
    
    /**
     * Actualiza la lista de empleados que participarán en una implementación de
     * evento dada.
     * 
     * @param idImplementacion el id de la implementación del evento a la cuál
     * se le desea sustituir su lista de empleados asistentes
     * @param idsEmpleado un arreglo de ids de empleado de todos los empleados
     * de la nueva lista de empleados asistirán a la implementación del evento
     * con el id suministrado
     * 
     * @return 1 en caso de que la actualización se haya realizado de forma
     * correcta o un 0 en caso de que haya ocurrido un error y se haya hecho
     * rollback en este nodo.
     * 
     * @throws RemoteException en caso de que ocurra un error remoto al invocar
     * este método
     */
    public short updateEmpleadosByImplementacion(int idImplementacion, int[] idsEmpleado)
            throws RemoteException;
    
    public void setConexionesSitos(Map<Interfaces, Object[]> conexiones)
            throws RemoteException;
    
}