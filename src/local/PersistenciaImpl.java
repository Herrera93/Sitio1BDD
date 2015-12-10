/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package local;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
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
            ok = TransactionManager.insertPlantel(false, tabla, datos);
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
            //ok = TransactionManager.insertEmpleado(datos);
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
            //ok = TransactionManager.insertEmpleado(true, tabla, datos);
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
        }  else if(tabla.equalsIgnoreCase("empleado")){
            if(attrWhere == null){
                dt = TransactionManager.consultarEmpleados();
            }else if(attrWhere.containsKey("numero")){                
                dt = TransactionManager.getEmpleado(columnas, attrWhere);
            }
        }else if(tabla.equalsIgnoreCase("plantel")){
            if(attrWhere == null) {
                dt = TransactionManager.consultarPlanteles();
            }
        }

        return dt;
    }
}
