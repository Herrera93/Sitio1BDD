/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistencia;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import modelo.dto.DataTable;

/**
 *
 * @author jdosornio
 */
public interface Persistencia extends Remote {
    
    /**
     * Inserta secuencialmente todos los datos de un DataTable en su tabla
     * correspondiente segun el nombre de la tabla
     * 
     * @param tabla
     * @param tablas los nombres de las tablas a las que se desean insertar
     * datos, éstas tablas deben estar en el orden de inserción necesario para
     * que todas las tablas se puedan insertar sin que haya problemas de
     * integridad referencial.
     * @param datos un arreglo de objetos DataTable que contiene todos los datos
     * a insertar por cada tabla en el mismo orden que los nombres de las tablas
     * 
     * @return true en caso de que la inserción se haya realizado de forma
     * correcta, false en caso contrario.
     * 
     * @throws RemoteException en caso de que ocurra un error remoto al invocar
     * este método
     */
    public boolean insert(String tabla, DataTable  datos)
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
     * correcta, false en caso contrario.
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
     * @return true en caso de que la eliminación se haya realizado de forma
     * correcta, false en caso contrario.
     * 
     * @throws RemoteException en caso de que ocurra un error remoto al invocar
     * este método
     */
    public boolean delete(String tabla, Map<String, Object> attrWhere)
            throws RemoteException;
    
    /**
     * Obtiene todos los registros de una tabla dada que cumplan con las cláusulas
     * where de igualdad contenidas en el mapa suministrado
     * 
     * @param tabla el nombre de la tabla donde se desean consultar los registros
     * @param columnas los nombres de las columnas de la tabla que se desean
     * obtener
     * @param aliases los nombres de los aliases de las columnas en el mismo orden,
     * null en caso de que no se desee alias por esa columna
     * @param attrWhere un mapa que contiene las cláusulas where para consultar un
     * registro de la tabla, donde la llave es el nombre de la columna y el
     * valor el valor que esa columna debe tomar para que se obtenga ese
     * registro de la tabla. En caso de que este parámetro sea null se regresarán
     * todos los registros de la tabla
     * @param orderColumn obligatoria
     * 
     * @return un objeto DataTable con los registros que resultaron de la consulta,
     * un DataTable vacío en caso de que la consulta no haya retornado nada o
     * null en caso de que haya ocurrido una excepción al realizar la consulta
     * 
     * @throws RemoteException en caso de que ocurra un error remoto al invocar
     * este método
     */
    public DataTable get(String tabla, String[] columnas, String[] aliases,
            Map<String, Object> attrWhere, String orderColumn)
            throws RemoteException;
    
    public DataTable getImplementacionesByEmpleado(String numeroEmpleado)
            throws RemoteException;
    
}