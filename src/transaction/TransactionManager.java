/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.dto.DataTable;
import modelo.util.ConnectionManager;
import remote.Sitio;
import remote.util.InterfaceManager;
import remote.util.InterfaceManager.Interfaces;
import remote.util.QueryManager;

/**
 *
 * @author jdosornio
 */
public class TransactionManager {

    private static final String EMPLEADO_ID = "numero";
    private static final String[] FRAG_LLAVES = {EMPLEADO_ID, "correo", "adscripcion_id",
        "departamento_id", "plantel_id", "direccion_id"};
    private static final String[] FRAG_DATOS = {EMPLEADO_ID, "primer_nombre", "segundo_nombre",
        "apellido_paterno", "apellido_materno", "puesto_id"};
    private static final String PLANTEL_ID = "id";
    private static final String EMPLEADO = "empleado";
    private static final String PLANTEL = "plantel";
    private static final String IMPLEMENTACION = "implementacion_evento_empleado";
    private static final String IMPLEMENTACION_ID = "implementacion_evento_id";
    private static final short BIEN = 1;
    private static final short LLAVES = BIEN;
    private static final short MAL = 0;
    private static final short NOMBRES = MAL;

    public static boolean insertReplicado(boolean savePKs, String tabla,
            DataTable datos) {
        boolean ok = true;

        System.out.println("---------Start Global transaction----------");
        try {
            short result = QueryManager.broadInsert(savePKs, tabla, datos);

            if (result == MAL) {
                ok = false;
                rollback();
            } else {
                commit();
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
            ok = false;
        }

        System.out.println("---------End Global transaction----------");
        return ok;
    }

    //Modificar para su sitio
    public static boolean insertEmpleado(DataTable datos) {
        boolean ok = true;

        System.out.println("---------Start Insert Empleado transaction---------- ");
        datos.rewind();
        datos.next();

        Integer zonaEmp = zonaEmpleado(datos.getString(EMPLEADO_ID));
        System.out.println("Zona emp: "+zonaEmp);
//        if (zonaEmp != null && zonaEmp == -1) {

            short result = MAL;
            DataTable[] fragmentos;
            List<Interfaces> sitios = new ArrayList<>();
            fragmentos = datos.fragmentarVertical(FRAG_DATOS, FRAG_LLAVES);
            datos.rewind();
            datos.next();
            if (datos.getInt("adscripcion_id") != 2) {
                //Insert en sitio 1 y 2

                result = QueryManager.uniInsert(false, Interfaces.LOCALHOST, EMPLEADO,
                        fragmentos[NOMBRES]) != null ? BIEN : MAL;
                System.out.println("Sitio 1: " + result);
                result *= QueryManager.uniInsert(false, Interfaces.SITIO_2, EMPLEADO,
                        fragmentos[LLAVES]) != null ? BIEN : MAL;
                System.out.println("Sitio 2: " + result);

            sitios.add(Interfaces.LOCALHOST);
            sitios.add(Interfaces.SITIO_2);
            } else {

            Map<String, Object> condicion = new HashMap<>();
                condicion.put(PLANTEL_ID, datos.getInt("plantel_id"));

            DataTable plantel = QueryManager.uniGet(Interfaces.SITIO_3,
                    PLANTEL, null, null, condicion);

                //se verifica en su nodo si se encuentra el plantel al que se insertara
                // cambiar por sus nodos el nombre de la variable de sitio y la interface
            if (plantel != null && plantel.getRowCount() != 0) {
                //este es su nodo ya no lo inserten de nuevo
                    result = QueryManager.localInsert(false, EMPLEADO, fragmentos[LLAVES])
                            != null ? BIEN : MAL;

                    System.out.println("Sitio Local: " + result);

                    result *= QueryManager.uniInsert(false, Interfaces.SITIO_2,
                            EMPLEADO, fragmentos[NOMBRES]) != null ? BIEN : MAL;
                    System.out.println("Sitio 2: " + result);

                    sitios.add(Interfaces.LOCALHOST);
                    sitios.add(Interfaces.SITIO_2);

                } else {
//                    revisar en los demas nodos
//                     tienen que verificar en los demas nodos en un solo sitio si se encuentra el plantel
//                     aqui se verifica la zona 1
//                    busca en la zona 1 si se encuentra el platel

                plantel = QueryManager.uniGet(Interfaces.SITIO_2, PLANTEL,
                        null, null, condicion);

                if (plantel != null && plantel.getRowCount() != 0) {
                        //aqui se encuentra

                        result = QueryManager.uniInsert(false, Interfaces.SITIO_3, EMPLEADO,
                                fragmentos[NOMBRES]) != null ? BIEN : MAL;
                        System.out.println("Sitio 3: " + result);

                    result *= QueryManager.uniInsert(false, Interfaces.SITIO_4, EMPLEADO,
                                fragmentos[LLAVES]) != null ? BIEN : MAL;

                        System.out.println("Sitio 4: " + result);

                        sitios.add(Interfaces.SITIO_3);
                        sitios.add(Interfaces.SITIO_4);

                    } else {
//                        aqui se veririca la zona 3

                    plantel = QueryManager.uniGet(Interfaces.SITIO_7, PLANTEL,
                            null, null, condicion);

                    if (plantel != null && plantel.getRowCount() != 0) {

                            result = QueryManager.uniInsert(false, Interfaces.SITIO_5, EMPLEADO,
                                    fragmentos[LLAVES]) != null ? BIEN : MAL;
                            System.out.println("Sitio 5: " + result);

                            result *= QueryManager.uniInsert(false, Interfaces.SITIO_6, EMPLEADO,
                                    fragmentos[LLAVES]) != null ? BIEN : MAL;
                            System.out.println("Sitio 6: " + result);

                            result *= QueryManager.uniInsert(false, Interfaces.SITIO_7, EMPLEADO,
                                    fragmentos[NOMBRES]) != null ? BIEN : MAL;
                            System.out.println("Sitio 7: " + result);

                        sitios.add(Interfaces.SITIO_5);
                        sitios.add(Interfaces.SITIO_6);
                        sitios.add(Interfaces.SITIO_7);

                        }
                    }
                }
            }
        if (result == BIEN) {
            commit(sitios);
            } else {
            ok = false;
            rollback(sitios);
            }
//        } else {
//            ok = false;
//            System.out.println("Empleado id existe");
//        }
        System.out.println("Insert empleado: " + ok);
        System.out.println("---------End Insert Empleado transaction----------");
        return ok;
    }

    /**
     * Retorna el número de la zona a la que pertenece, -1 si no existe el
     * empleado, null si hay problemas al obtener la información.
     *
     * @param numero
     * @return
     */
    //Modificar para su sitio
    public static Integer zonaEmpleado(String numero) {
        boolean ok;
        Integer zona = -1;
        Map<String, Object> condicion = new HashMap<>();
        condicion.put(EMPLEADO_ID, numero);

        try {
            ok = QueryManager.uniGet(Interfaces.LOCALHOST, EMPLEADO, null, null, condicion)
                    .next();
            if (!ok) {
                ok = QueryManager.uniGet(Interfaces.SITIO_7, EMPLEADO, null, null, condicion)
                        .next();
                if (!ok) {
                    ok = QueryManager.uniGet(Interfaces.SITIO_1, EMPLEADO, null, null, condicion)
                            .next();
                    if (ok) {
                        zona = 1;
                    }
                } else {
                    zona = 3;
                }
            } else {
                zona = 2;
            }

        } catch (NullPointerException e) {
            System.out.println("NullPointer uniGet zonaEmpleado");
            zona = null;
        }
        return zona;
    }

    //Modificar para su sitio
    public static boolean insertPlantel(DataTable datos) {
        boolean ok = true;

        System.out.println("---------Start Plantel transaction---------- ");

        short result = MAL;
        datos.rewind();
        datos.next();
        List<Interfaces> sitios = new ArrayList<>();

        Integer siguienteID = obtenerSiguienteID(PLANTEL, PLANTEL_ID, Interfaces.SITIO_1,
                Interfaces.LOCALHOST, Interfaces.SITIO_7);

        if (siguienteID > 0) {
            datos.rewind();
            datos.next();
            datos.setObject(PLANTEL_ID, siguienteID);

            if (datos.getInt("zona_id") == 1) {

                    System.out.println("Zona 1");
                result = QueryManager.localInsert(false, PLANTEL, datos)
                        != null ? BIEN : MAL;
                result *= QueryManager.uniInsert(false, Interfaces.SITIO_2, PLANTEL, datos)
                            != null ? BIEN : MAL;

                sitios.add(Interfaces.LOCALHOST);
                sitios.add(Interfaces.SITIO_2);

            } else if (datos.getInt("zona_id") == 2) {

                System.out.println("Zona 2");
                result = QueryManager.uniInsert(true, Interfaces.SITIO_3,PLANTEL, datos)
                        != null ? BIEN : MAL;
                result *= QueryManager.uniInsert(false, Interfaces.SITIO_4, PLANTEL, datos)
                            != null ? BIEN : MAL;

                sitios.add(Interfaces.SITIO_3);
                sitios.add(Interfaces.SITIO_4);

            } else if (datos.getInt("zona_id") == 3) {

                    System.out.println("Zona 3");
                result = QueryManager.uniInsert(false, Interfaces.SITIO_5, PLANTEL, datos)
                            != null ? BIEN : MAL;
                result *= QueryManager.uniInsert(false, Interfaces.SITIO_6, PLANTEL, datos)
                            != null ? BIEN : MAL;
                result *= QueryManager.uniInsert(false, Interfaces.SITIO_7, PLANTEL, datos)
                        != null ? BIEN : MAL;

                sitios.add(Interfaces.SITIO_5);
                sitios.add(Interfaces.SITIO_6);
                sitios.add(Interfaces.SITIO_7);
            }

            if (result == MAL) {
                ok = false;
                rollback(sitios);
            } else {
                commit(sitios);
            }
        }
        System.out.println("insert plantel: " + ok);
        System.out.println("---------End Plantel transaction----------");
        return ok;
    }

    private static int obtenerSiguienteID(String tabla, String columnaID,
            Interfaces... interfacesSitios) {

        int mayor = -1;
        int idSitio;
        for (Interfaces interfaceSitio : interfacesSitios) {
            idSitio = QueryManager.getMaxId(interfaceSitio, tabla, columnaID);
            if (idSitio > mayor) {
                mayor = idSitio;
            }
        }

        return ++mayor;
    }

    public static boolean updateReplicado(String tabla, DataTable datos,
            Map<String, ?> attrWhere) {

        boolean ok = true;

        System.out.println("---------Start Global transaction----------");
        try {
            short result = QueryManager.broadUpdate(tabla, datos, attrWhere);

            if (result == 0) {
                ok = false;
                rollback();
            } else {
                commit();
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
            ok = false;
        }

        System.out.println("---------End Global transaction----------");
        return ok;
    }

    //Modificar para su sitio
    public static boolean updateEmpleado(DataTable datos, Map<String, ?> attrWhere) {
        System.out.println("---------Start Update Empleado transaction---------- ");
        boolean ok = true;

        System.out.println(datos.toString());
        System.out.println(datos.getRowCount());
        datos.next();
        String idEmpleado = datos.getString(EMPLEADO_ID);
        Integer zona = zonaEmpleado(idEmpleado);
        Map<String, Object> condicion = new HashMap<>();
        condicion.put(EMPLEADO_ID, idEmpleado);

        if (zona != null) {
            switch (zona) {
                case 1:
                    if (multiDelete(EMPLEADO, condicion,
                            Interfaces.LOCALHOST, Interfaces.SITIO_2)) {
                        insertEmpleado(datos);
                    } else {
                        System.out.println("No se pudo eliminar empleado, no se completo modificación");
                    }
                    break;
                case 2:
                    if (multiDelete(EMPLEADO, condicion,
                            Interfaces.SITIO_3, Interfaces.SITIO_4)) {
                        insertEmpleado(datos);
                    } else {
                        System.out.println("No se pudo eliminar empleado, no se completo modificación");
                    }

                    break;
                case 3:
                    if (multiDelete(EMPLEADO, condicion,
                            Interfaces.SITIO_5, Interfaces.SITIO_6, Interfaces.SITIO_7)) {
                        insertEmpleado(datos);
                    } else {
                        System.out.println("No se pudo eliminar empleado, no se completo modificación");
                    }

                    break;
                default:

            }
        }

        System.out.println("--------- Update Empleado: " + ok);
        System.out.println("---------End Update Empleado transaction---------- ");
        return ok;
    }

    public static boolean deleteReplicado(String tabla, Map<String, ?> attrWhere) {
        boolean ok = true;

        System.out.println("---------Start Global transaction----------");
        try {
            short result = QueryManager.broadDelete(tabla, attrWhere);

            if (result == 0) {
                ok = false;
                rollback();
            } else {
                commit();
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
            ok = false;
        }

        System.out.println("---------End Global transaction----------");
        return ok;
    }

    //Modificar para su sitio
    public static boolean deleteEmpleado(Map<String, ?> attrWhere) {
        System.out.println("---------Start Delete Empleado transaction---------- ");
        boolean ok = true;

        List<Interfaces> sitios = new ArrayList<>();

//        int zona = zonaEmpleado(String.valueOf(attrWhere.get(COLUMNA_ID_EMPLEADO)));
        DataTable empleado = QueryManager.uniGet(Interfaces.LOCALHOST,
                EMPLEADO, null, null, attrWhere);

        if (empleado != null && empleado.getRowCount() != 0) {
            sitios.add(Interfaces.LOCALHOST);
            sitios.add(Interfaces.SITIO_2);
            ok = multiDelete(EMPLEADO, attrWhere,
                    sitios.toArray(new Interfaces[sitios.size()]));
        } else {
            //Zona 1
            empleado = QueryManager.uniGet(Interfaces.SITIO_2, EMPLEADO,
                    null, null, attrWhere);

            if (empleado != null && empleado.getRowCount() != 0) {
                sitios.add(Interfaces.SITIO_3);
                sitios.add(Interfaces.SITIO_4);
                ok = multiDelete(EMPLEADO, attrWhere,
                        sitios.toArray(new Interfaces[sitios.size()]));
            } else {
                //Zona 3
                empleado = QueryManager.uniGet(Interfaces.SITIO_7, EMPLEADO,
                        null, null, attrWhere);

                if (empleado != null && empleado.getRowCount() != 0) {
                    sitios.add(Interfaces.SITIO_5);
                    sitios.add(Interfaces.SITIO_6);
                    sitios.add(Interfaces.SITIO_7);
                    ok = multiDelete(EMPLEADO, attrWhere,
                            sitios.toArray(new Interfaces[sitios.size()]));
                }
            }

            if (ok) {
                commit(sitios);
            } else {
                ok = false;
                rollback(sitios);
            }
        }
        System.out.println("--------- Delete Empleado: " + ok);
        System.out.println("---------End Delete Empleado transaction---------- ");
        return ok;
    }

    private static boolean multiDelete(String tabla, Map<String, ?> attrWhere,
            Interfaces... interfaces) {
        short result = BIEN;

        for (Interfaces interfaceSitio : interfaces) {
            if (interfaceSitio == Interfaces.LOCALHOST) {
                result *= QueryManager.localDelete(tabla, attrWhere)
                        == true ? BIEN : MAL;
            } else {
                result *= QueryManager.uniDelete(interfaceSitio, tabla,
                        attrWhere) == true ? BIEN : MAL;
            }
            System.out.println("Delete " + interfaceSitio + ": " + result);
        }

        if(result == BIEN){
            commit(Arrays.asList(interfaces));
            System.out.println("Commit Delete Empledo");
        } else {
            rollback(Arrays.asList(interfaces));
    }

        return result == BIEN;
    }

    //Modificar para su sitio
    public static DataTable consultarEmpleados(Map attrWhere) {
        String[] columnas = {
            "numero",
            "primer_nombre",
            "segundo_nombre",
            "apellido_paterno",
            "apellido_materno",};

//        DataTable fragDatosSitio1 = QueryManager.uniGet(Interfaces.SITIO_1, "empleado", columnas, null, null);
//        DataTable fragDatosSitio4 = QueryManager.uniGet(Interfaces.SITIO_4, "empleado", columnas, null, null);
//        DataTable fragDatosSitio7 = QueryManager.uniGet(Interfaces.SITIO_7, "empleado", columnas, null, null);
//
//        return DataTable.combinarFragH(fragDatosSitio1, fragDatosSitio4, fragDatosSitio7);
        DataTable fragDatosSitio4 = QueryManager.uniGet(Interfaces.LOCALHOST, "empleado", columnas, null, null);
        
        return fragDatosSitio4;

    }
    
    //Modificar para su sitio
    public static DataTable getEmpleado(String[] columnas, Map<String, ?> condicion) {
        System.out.println("---------Start GetEmpleado transaction---------- ");

        String[] fragLlaves = {"numero", "correo", "adscripcion_id",
            "departamento_id", "plantel_id", "direccion_id"};
        List<String> listaLlaves = Arrays.asList(fragLlaves);
        List<String> listaColumnas = null;
        boolean segundoFragmento = false;
        if (columnas != null) {
            listaColumnas = Arrays.asList(columnas);
            segundoFragmento = listaLlaves.retainAll(listaColumnas);
            fragLlaves = (String[]) listaLlaves.toArray();
        } else {
            fragLlaves = null;
        }
        
        //Se busca en el nodo local al Empleado[NOMBRES]
        DataTable empleado = QueryManager.uniGet(Interfaces.LOCALHOST,
                EMPLEADO, columnas, null, condicion);
        if (columnas == null || segundoFragmento) {
            DataTable llaves = QueryManager.uniGet(Interfaces.LOCALHOST, EMPLEADO,
                fragLlaves, null, condicion);
            empleado = DataTable.combinarFragV(empleado, llaves, EMPLEADO_ID);
        }
        
        if (empleado == null || empleado.getRowCount() == 0) {
            //En caso de no encontrarse se busca en el nodo 4
            empleado = QueryManager.uniGet(Interfaces.SITIO_4, EMPLEADO, null,
                    null, condicion);
            if (columnas == null || segundoFragmento) {
                DataTable llaves = QueryManager.uniGet(Interfaces.SITIO_2, EMPLEADO,
                    fragLlaves, null, condicion);
                empleado = DataTable.combinarFragV(empleado, llaves, EMPLEADO_ID);
            }
            
            if (empleado == null || empleado.getRowCount() == 0) {
                //Por ultimo se busca en el sitio 7 en caso de no encontrarse
                empleado = QueryManager.uniGet(Interfaces.SITIO_7, EMPLEADO, null,
                        null, condicion);
                if (columnas == null || listaLlaves.retainAll(listaColumnas)) {
                    DataTable llaves = QueryManager.uniGet(Interfaces.SITIO_6, EMPLEADO,
                        fragLlaves, null, condicion);
                    empleado = DataTable.combinarFragV(empleado, llaves, EMPLEADO_ID);
                }
            }
        }

        System.out.println("---------End GetEmpleado transaction----------");
        return empleado;
    }
    
    //Modificar para su sitio
    public static DataTable consultarPlanteles(Map attrWhere) {
        
//        //Zona 1
//        DataTable fragDatosZona1 = QueryManager.uniGet(
//                Interfaces.SITIO_1, PLANTEL, null, null, attrWhere);
//        //Zona 2
//        DataTable fragDatosZona2 = QueryManager.uniGet(
//                Interfaces.LOCALHOST, PLANTEL, null, null, attrWhere);
//        //Zona 3
//        DataTable fragDatosZona3 = QueryManager.uniGet(
//                Interfaces.SITIO_7, PLANTEL, null, null, attrWhere);
//
//        return DataTable.combinarFragH(fragDatosZona1, fragDatosZona2,
//                fragDatosZona3);
        //Zona 2
        DataTable fragDatosZona2 = QueryManager.uniGet(
                Interfaces.LOCALHOST, PLANTEL, null, null, attrWhere);
        return fragDatosZona2;
    }

    //Modificar para su sitio
    public static DataTable getPlantel(Map<String, ?> condicion) {
        System.out.println("---------Start GetPlantel transaction---------- ");

        //Se busca en el nodo local (Zona 1)
        DataTable plantel = QueryManager.uniGet(Interfaces.LOCALHOST,
                PLANTEL, null, null, condicion);

        if (plantel == null || plantel.isEmpty()) {
            //Si no se encontró en la Zona 1 buscar en la Zona 2
            plantel = QueryManager.uniGet(Interfaces.SITIO_4, PLANTEL, null, null,
                    condicion);

            if (plantel == null || plantel.isEmpty()) {
                //Si no esta en la Zona 2 buscar en la Zona 3
                plantel = QueryManager.uniGet(Interfaces.SITIO_7, PLANTEL,
                        null, null, condicion);

                //Si no se encontró aquí regresar el plantel vacío de todos modos
            }
        }

        System.out.println("---------End GetEmpleado transaction----------");
        return plantel;
    }
    
    public static DataTable consultarImplementaciones(Map<String, ?> condicion){
        int zona = zonaEmpleado((String) condicion.get("empleado_id"));
        DataTable implementaciones = null;
        String[] columna = {IMPLEMENTACION_ID};
        
        switch(zona){
            case 1:
                implementaciones = QueryManager.uniGet(Interfaces.LOCALHOST, 
                    IMPLEMENTACION, columna, null, condicion);
                break;
            case 2:
                implementaciones = QueryManager.uniGet(Interfaces.SITIO_3, 
                    IMPLEMENTACION, columna, null, condicion);
                break;
            case 3:
                implementaciones = QueryManager.uniGet(Interfaces.SITIO_7, 
                    IMPLEMENTACION, columna, null, condicion);
                break;
        }
        
        return implementaciones;
    }

    public static void commit() throws InterruptedException {
        List<Thread> hilosInsert = new ArrayList<>();

        //Commit local
        ConnectionManager.commit();
        ConnectionManager.cerrar();

        //Obtener todas las interfaces de sitio
        for (InterfaceManager.Interfaces interfaceSitio : InterfaceManager.getInterfacesRegistradas()) {

            if (interfaceSitio.equals(Interfaces.LOCALHOST)) {
                continue;
            }

            Runnable hacerCommit = new Runnable() {
                @Override
                public void run() {
                    try {
                        Sitio sitio = InterfaceManager.getInterface(
                                InterfaceManager.getInterfaceServicio(interfaceSitio));

                        if (sitio != null) {
                            boolean ok = sitio.commit();

                            System.out.println("Thread de commit a la interface: "
                                    + interfaceSitio + ", resultado = " + ok);
                        }
                    } catch (RemoteException | NotBoundException ex) {
                        Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            Thread hilo = new Thread(hacerCommit);
            hilo.start();
            hilosInsert.add(hilo);
        }

        for (Thread hilo : hilosInsert) {
            hilo.join();
        }

        System.out.println("fin de commit global");
    }

    public static void rollback() throws InterruptedException {
        List<Thread> hilosInsert = new ArrayList<>();

        //Rollback local
        ConnectionManager.rollback();
        ConnectionManager.cerrar();

        //Obtener todas las interfaces de sitio
        for (InterfaceManager.Interfaces interfaceSitio : InterfaceManager.getInterfacesRegistradas()) {

            if (interfaceSitio.equals(Interfaces.LOCALHOST)) {
                continue;
            }

            Runnable hacerRollback = new Runnable() {
                @Override
                public void run() {
                    try {
                        Sitio sitio = InterfaceManager.getInterface(
                                InterfaceManager.getInterfaceServicio(interfaceSitio));

                        if (sitio != null) {
                            boolean ok = sitio.rollback();

                            System.out.println("Thread de rollback a la interface: "
                                    + interfaceSitio + ", resultado = " + ok);
                        }
                    } catch (RemoteException | NotBoundException ex) {
                        Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            Thread hilo = new Thread(hacerRollback);
            hilo.start();
            hilosInsert.add(hilo);
        }

        for (Thread hilo : hilosInsert) {
            hilo.join();
        }

        System.out.println("fin de rollback global");
    }

    private static void commit(List<Interfaces> interfaces) {

        for (Interfaces interfaceSitio : interfaces) {
            if (interfaceSitio == Interfaces.LOCALHOST) {
                ConnectionManager.commit();
                ConnectionManager.cerrar();
            } else {
                try {
                    Sitio sitio = InterfaceManager.getInterface(
                            InterfaceManager.getInterfaceServicio(interfaceSitio));
                    if (sitio != null) {
                        boolean ok = sitio.commit();

                        System.out.println("Thread de commit a la interface: "
                                + interfaceSitio + ", resultado = " + ok);
}
                } catch (RemoteException | NotBoundException ex) {
                    Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static void rollback(List<Interfaces> interfaces) {
        for (Interfaces interfaceSitio : interfaces) {
            if (interfaceSitio == Interfaces.LOCALHOST) {
                ConnectionManager.rollback();
                ConnectionManager.cerrar();
            } else {
                try {
                    Sitio sitio = InterfaceManager.getInterface(
                            InterfaceManager.getInterfaceServicio(interfaceSitio));
                    if (sitio != null) {
                        boolean ok = sitio.rollback();

                        System.out.println("Thread de commit a la interface: "
                                + interfaceSitio + ", resultado = " + ok);
                    }
                } catch (RemoteException | NotBoundException ex) {
                    Logger.getLogger(TransactionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
