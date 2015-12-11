/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package local;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import modelo.dao.BaseDAO;
import modelo.dto.DataTable;
import persistencia.Persistencia;
import transaction.TransactionManager;

/**
 *
 * @author jdosornio
 */
public class PersistenciaImpl extends UnicastRemoteObject implements Persistencia {

    public PersistenciaImpl() throws RemoteException {

    }

    @Override
    public boolean insert(String tabla, DataTable datos) throws RemoteException {
        boolean ok;

        if (tabla.equalsIgnoreCase("empleado")) {
            datos.rewind();
            ok = TransactionManager.insertEmpleado(datos);
            System.out.println("Inserción de empleado: " + tabla + ", resultado: "
                    + ok);

        } else if (tabla.equalsIgnoreCase(("plantel"))) {
            datos.rewind();
            ok = TransactionManager.insertPlantel(datos);
            System.out.println("Inserción de plantel: " + tabla + ", resultado: "
                    + ok);

        } else if (tabla.equalsIgnoreCase("implementacion_evento_empleado")) {
            ok = false;
        } else {
            System.out.println("insert replicado");
            ok = TransactionManager.insertReplicado(true, tabla, datos);
            System.out.println("Inserción replicado: " + tabla + ", resultado: "
                    + ok);
        }

        return ok;
    }

    @Override
    public boolean update(String tabla, DataTable datos, Map<String, ?> attrWhere) throws RemoteException {
        boolean ok = false;

        if (tabla.equalsIgnoreCase("empleado")) {
            datos.rewind();
            ok = TransactionManager.updateEmpleado(datos, attrWhere);
            System.out.println("Modificación de empleado, resultado: "
                    + ok);

        } else if (tabla.equalsIgnoreCase(("plantel"))) {
            datos.rewind();
            //ok = TransactionManager.insertPlantel(false, tabla, datos);
            System.out.println("Inserción de plantel, resultado: "
                    + ok);

        } else if (tabla.equalsIgnoreCase("implementacion_evento_empleado")) {
            ok = false;
        } else {
            System.out.println("update replicado");
            ok = TransactionManager.updateReplicado(tabla, datos, attrWhere);

            System.out.println("Modificación replicada: " + tabla + ", resultado: "
                    + ok);
    }

        return ok;
    }

    @Override
    public boolean delete(String tabla, Map<String, ?> attrWhere) throws RemoteException {
        boolean ok = false;
        
        if (tabla.equalsIgnoreCase("empleado")) {
            ok = TransactionManager.deleteEmpleado(attrWhere);
        } else if (tabla.equalsIgnoreCase(("plantel"))) {
            //ok = TransactionManager.insertPlantel(false, tabla, datos);
        } else if (tabla.equalsIgnoreCase("implementacion_evento_empleado")) {
            //ok = false;
        } else {
            ok = TransactionManager.deleteReplicado(tabla, attrWhere);
    }

        return ok;
    }

    @Override
    public DataTable get(String tabla, String[] columnas, String[] aliases,
            Map<String, ?> attrWhere) throws RemoteException {

        DataTable dt = null;

        if (!tabla.equalsIgnoreCase("empleado")
                && !tabla.equalsIgnoreCase("plantel")
                && !tabla.equalsIgnoreCase("implementacion_evento_empleado")) {
            //Todas son consultas locales....
            dt = new BaseDAO().get(tabla, columnas, aliases, attrWhere);
        } else if (tabla.equalsIgnoreCase("empleado")) {

            if (attrWhere == null || (!attrWhere.containsKey("direccion_id")
                    && !attrWhere.containsKey("departamento_id")
                    && !attrWhere.containsKey("numero"))) {
                //Consulta general o filtrada
                dt = TransactionManager.consultarEmpleados(attrWhere);

            } else if (attrWhere.containsKey("direccion_id")
                    || attrWhere.containsKey("departamento_id")) {
                //Consultas filtradas en el sitio 2
                //dt = TransactionManager.getEmpleadosByD(columnas, attrWhere);
            } else if (attrWhere.containsKey("numero")) {
                //Consulta especifica
                dt = TransactionManager.getEmpleado(columnas, attrWhere);
            }
        } else if (tabla.equalsIgnoreCase("plantel")) {
            if (attrWhere == null || !attrWhere.containsKey("id")) {
                //Consulta filtrada o general
                dt = TransactionManager.consultarPlanteles(attrWhere);
            } else {
                //Consulta especifica
                dt = TransactionManager.getPlantel(attrWhere);
            }
        } else if (tabla.equalsIgnoreCase("implementacion_evento_empleado")) {
            dt = TransactionManager.consultarImplementaciones(attrWhere);
        }

        return dt;
    }

    /**
     * Consulta para obtener las implementaciones donde haya participado un empleado.
     * @param numeroEmpleado
     * @return
     * @throws RemoteException 
     */
    @Override
    public DataTable getImplementacionesByEmpleado(String numeroEmpleado) throws RemoteException {
        HashMap<String, Object> condicion = new HashMap();
        condicion.put("empleado_numero", numeroEmpleado);
        String[] columna = {"implementacion_evento_id"};
        return get("implementacion_evento_empleado", columna, null, condicion);
    }
}
