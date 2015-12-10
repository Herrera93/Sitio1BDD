/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package remote;

import java.rmi.RemoteException;
import modelo.dto.DataTable;

/**
 *
 * @author jdosornio
 */
public interface Sitio4Int extends Sitio {
    
    /**
     * Obtiene el primer_nombre, segundo_nombre, apellido_paterno,
     * apellido_materno y id_plantel de un empleado dado un número de empleado.
     * 
     * @param numeroEmp el número del empleado que se desea obtener
     * 
     * @return un objeto DataTable con una fila que representa al empleado en
     * caso de encontrarse o un DataTable vacío en caso contrario.
     * 
     * @throws RemoteException en caso de que ocurra un error de tipo remoto al
     * invocar este método
     */
    public DataTable getEmpleado(String numeroEmp) throws RemoteException;
    
    /**
     * Obtiene el primer_nombre, segundo_nombre, apellido_paterno,
     * apellido_materno y puesto_id de todos los empleados que participen en una
     * implementación de evento determinada.
     * 
     * @param idImplementacion el id de la implementación del evento de donde se
     * desean obtener los datos de los empleados que participan en ella
     * 
     * @return un objeto DataTable con todos los datos de los empleados que
     * participen en la implementación del evento con el id suministrado o un
     * DataTable vacío en caso de que no exista la implementación del evento en
     * este nodo o que no existan empleados que participen en esta implementación
     * de evento
     * 
     * @throws RemoteException en caso de que ocurra un error de tipo remoto al
     * invocar este método
     */
    public DataTable getEmpleadosByImplementacion(int idImplementacion) throws RemoteException;
}