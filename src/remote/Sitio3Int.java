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
public interface Sitio3Int extends Sitio {
        /**
     * Obtiene los id's de las implementaciones de evento en las que participen
     * los empleados que pertenezcan a un plantel determinado.
     * 
     * @param idPlantel el id del plantel del que se desea obtener las
     * implementaciones de evento
     * 
     * @return un objeto DataTable con todos los id's de las implementaciones de
     * evento en las que participen empleados del plantel con el id suministrado
     * o un DataTable vacío en caso de que no exista el plantel en este nodo o
     * que no existan implementaciones de evento para ese plantel.
     * 
     * @throws RemoteException en caso de que ocurra un error de tipo remoto al
     * invocar este método
     */
    public DataTable getImplementacionesByPlantel(int idPlantel) throws RemoteException;
}