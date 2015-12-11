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
    private static final String EMPLEADO_PLANTEL_ID = "plantel_id";
    private static final String EMPLEADO_CORREO = "correo";
    private static final String EMPLEADO_ADSCRIPCION_ID = "adscripcion_id";
    private static final String[] FRAG_LLAVES = {EMPLEADO_ID,
        EMPLEADO_CORREO, EMPLEADO_ADSCRIPCION_ID, "departamento_id", EMPLEADO_PLANTEL_ID, "direccion_id"};

    private static final String[] FRAG_DATOS = {EMPLEADO_ID, "primer_nombre", "segundo_nombre",
        "apellido_paterno", "apellido_materno", "puesto_id"};
    private static final String PLANTEL_ID = "id";
    private static final String EMPLEADO = "empleado";
    private static final String PLANTEL = "plantel";
    private static final String IMPLEMENTACION_EVENTO_EMPLEADO = "implementacion_evento_empleado";
    private static final String IMPLEMENTACION_EVENTO_ID = "implementacion_evento_id";
    private static final String EMPLEADO_NUMERO = "empleado_numero";

    private static final String PLANTEL_ZONA_ID = "zona_id";
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
        System.out.println("Zona emp: " + zonaEmp);
        if (zonaEmp != null && zonaEmp == -1) {

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
                //Zona 1 (Local)
                Map<String, Object> condicion = new HashMap<>();
                condicion.put(PLANTEL_ID, datos.getInt("plantel_id"));

                DataTable plantel = QueryManager.uniGet(Interfaces.LOCALHOST,
                        PLANTEL, null, null, condicion, PLANTEL_ID);

                //se verifica en su nodo si se encuentra el plantel al que se insertara
                // cambiar por sus nodos el nombre de la variable de sitio y la interface
                if (plantel != null && !plantel.isEmpty()) {
                    //este es su nodo ya no lo inserten de nuevo
                    result = QueryManager.localInsert(false, EMPLEADO, fragmentos[NOMBRES])
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

                    plantel = QueryManager.uniGet(Interfaces.SITIO_3, PLANTEL,
                            null, null, condicion, PLANTEL_ID);

                    if (plantel != null && !plantel.isEmpty()) {
                        //aqui se encuentra

                        result = QueryManager.uniInsert(false, Interfaces.SITIO_3, EMPLEADO,
                                fragmentos[NOMBRES]) != null ? BIEN : MAL;

                        result *= QueryManager.uniInsert(false, Interfaces.SITIO_4, EMPLEADO,
                                fragmentos[LLAVES]) != null ? BIEN : MAL;

                        System.out.println("Sitio 4: " + result);

                        sitios.add(Interfaces.SITIO_3);
                        sitios.add(Interfaces.SITIO_4);

                    } else {
//                        aqui se veririca la zona 3

                        plantel = QueryManager.uniGet(Interfaces.SITIO_7, PLANTEL,
                                null, null, condicion, PLANTEL_ID);

                        if (plantel != null && !plantel.isEmpty()) {

                            result = QueryManager.uniInsert(false, Interfaces.SITIO_5, EMPLEADO,
                                    fragmentos[LLAVES]) != null ? BIEN : MAL;

                            result *= QueryManager.uniInsert(false, Interfaces.SITIO_6, EMPLEADO,
                                    fragmentos[LLAVES]) != null ? BIEN : MAL;
                            System.out.println("Sitio 6: " + result);

                            result *= QueryManager.uniInsert(false, Interfaces.SITIO_7, EMPLEADO,
                                    fragmentos[NOMBRES]) != null ? BIEN : MAL;

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
        } else {
            ok = false;
            System.out.println("Empleado id existe");
        }
        System.out.println("Insert empleado: " + ok);
        System.out.println("---------End Insert Empleado transaction----------");
        return ok;
    }

    //Modificar para su sitio
    public static boolean insertPlantel(DataTable datos) {
        boolean ok = true;

        System.out.println("---------Start Plantel transaction---------- ");

        short result = MAL;
        datos.rewind();
        datos.next();
        List<Interfaces> sitios = new ArrayList<>();

        Integer siguienteID = obtenerSiguienteID(PLANTEL, PLANTEL_ID, Interfaces.LOCALHOST,
                Interfaces.SITIO_4, Interfaces.SITIO_7);

        if (siguienteID > 0) {
            datos.rewind();
            datos.next();
            datos.setObject(PLANTEL_ID, siguienteID);

            if (null != datos.getInt("zona_id")) {
                switch (datos.getInt("zona_id")) {
                    case 1:
                        System.out.println("Zona 1");
                        result = QueryManager.localInsert(false, PLANTEL, datos)
                                != null ? BIEN : MAL;
                        result *= QueryManager.uniInsert(false, Interfaces.SITIO_2, PLANTEL, datos)
                                != null ? BIEN : MAL;

                        sitios.add(Interfaces.LOCALHOST);
                        sitios.add(Interfaces.SITIO_2);
                        break;
                    case 2:
                        System.out.println("Zona 2");
                        result = QueryManager.uniInsert(false, Interfaces.SITIO_3, PLANTEL, datos)
                                != null ? BIEN : MAL;
                        result *= QueryManager.uniInsert(false, Interfaces.SITIO_4, PLANTEL, datos)
                                != null ? BIEN : MAL;

                        sitios.add(Interfaces.SITIO_3);
                        sitios.add(Interfaces.SITIO_4);
                        break;
                    case 3:
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
                        break;
                }
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

    /**
     * Retorna verdadero si existe el id del empleado, falso de otra forma.
     *
     * @param datos
     * @return
     */
    public static boolean existeEmpleado(DataTable datos) {
        boolean ok;
        Map<String, Object> condicion = new HashMap<>();
        datos.rewind();
        datos.next();
        condicion.put(EMPLEADO_ID, datos.getString(EMPLEADO_ID));

        try {
            //Localhost (Zona 1)
            ok = QueryManager.uniGet(Interfaces.LOCALHOST, EMPLEADO, null, null,
                    condicion, EMPLEADO_ID).next();
            if (!ok) {
                //Zona 2
                ok = QueryManager.uniGet(Interfaces.SITIO_4, EMPLEADO, null, null,
                        condicion, EMPLEADO_ID).next();
                if (!ok) {
                    //Zona 3
                    ok = QueryManager.uniGet(Interfaces.SITIO_7, EMPLEADO, null, null,
                            condicion, EMPLEADO_ID).next();
                }
            }

        } catch (NullPointerException e) {
            System.out.println("NullPointer uniGet verificarExistenciaEmpleado");
            ok = true;
        }
        return ok;
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
    public static boolean updateEmpleado(DataTable datos,
            Map<String, ?> attrWhere) {
        boolean ok = true;

        System.out.println("---------Start Update Empleado transaction---------- ");
        datos.rewind();
        datos.next();

        Integer zonaEmp = zonaEmpleado(datos.getString(EMPLEADO_ID));
        System.out.println("Zona emp: " + zonaEmp);
        short result = MAL;
        DataTable[] fragmentos;
        List<Interfaces> sitios = new ArrayList<>();
        fragmentos = datos.fragmentarVertical(FRAG_DATOS, FRAG_LLAVES);
        datos.rewind();
        datos.next();
        DataTable viejoEmpleado = getEmpleado(null, attrWhere);
        Integer zonaViejoEmp = zonaEmpleado(viejoEmpleado.getString(EMPLEADO_ID));
        
        if (datos.getInt(EMPLEADO_ADSCRIPCION_ID) != 2) {
            //Insert en sitio 1 y 2
            
            if(viejoEmpleado.getInt(EMPLEADO_ADSCRIPCION_ID) != 2){
                //Update local
                result = QueryManager.localUpdate(EMPLEADO, fragmentos[NOMBRES], attrWhere)
                        != null ? BIEN : MAL;
                result *= QueryManager.uniUpdate(Interfaces.SITIO_2, EMPLEADO, fragmentos[LLAVES], attrWhere)
                        != null ? BIEN : MAL;
            }else{
                result = deleteEmpleado(attrWhere) ? BIEN : MAL;
                result *= QueryManager.localUpdate(EMPLEADO, fragmentos[NOMBRES], attrWhere)
                        != null ? BIEN : MAL;
                result *= QueryManager.uniUpdate(Interfaces.SITIO_2, EMPLEADO, fragmentos[LLAVES], attrWhere)
                        != null ? BIEN : MAL;
            }

            sitios.add(Interfaces.LOCALHOST);
            sitios.add(Interfaces.SITIO_2);
        } else {
            //Zona 1 (Local)
            Map<String, Object> condicion = new HashMap<>();
            condicion.put(PLANTEL_ID, datos.getInt(EMPLEADO_PLANTEL_ID));

            DataTable plantel = QueryManager.uniGet(Interfaces.LOCALHOST,
                    PLANTEL, null, null, condicion, PLANTEL_ID);

            //se verifica en su nodo si se encuentra el plantel al que se insertara
            // cambiar por sus nodos el nombre de la variable de sitio y la interface
            if (plantel != null && !plantel.isEmpty()) {
                if(zonaViejoEmp != 1){
                    deleteEmpleado(attrWhere);
                }
                
                if(zonaEmp == 1 && zonaViejoEmp == 1){
                    result = QueryManager.localUpdate(EMPLEADO, fragmentos[NOMBRES], attrWhere)
                            != null ? BIEN : MAL;
                    result *= QueryManager.uniUpdate(Interfaces.SITIO_2, EMPLEADO, fragmentos[LLAVES], attrWhere)
                            != null ? BIEN : MAL;
                }else {
                    result = insertEmpleado(datos) ? BIEN : MAL;
                }

                sitios.add(Interfaces.LOCALHOST);
                sitios.add(Interfaces.SITIO_2);

            } else {
//                    revisar en los demas nodos
//                     tienen que verificar en los demas nodos en un solo sitio si se encuentra el plantel
//                     aqui se verifica la zona 2
//                    busca en la zona 2 si se encuentra el platel
                
                plantel = QueryManager.uniGet(Interfaces.SITIO_3,
                    PLANTEL, null, null, condicion, PLANTEL_ID);
                
                if (plantel != null && !plantel.isEmpty()) {
                    if(zonaViejoEmp != 2){
                        deleteEmpleado(attrWhere);
                    }

                    if(zonaEmp == 2 && zonaViejoEmp == 2){
                        result = QueryManager.uniUpdate(Interfaces.SITIO_3, EMPLEADO, fragmentos[LLAVES], attrWhere)
                                != null ? BIEN : MAL;
                        result *= QueryManager.uniUpdate(Interfaces.SITIO_4, EMPLEADO, fragmentos[NOMBRES], attrWhere)
                                != null ? BIEN : MAL;
                    }else {
                        result = insertEmpleado(datos) ? BIEN : MAL;
                    }

                    sitios.add(Interfaces.SITIO_3);
                    sitios.add(Interfaces.SITIO_4);
                }else{
                    plantel = QueryManager.uniGet(Interfaces.SITIO_7,
                    PLANTEL, null, null, condicion, PLANTEL_ID);
                    
                    if (plantel != null && !plantel.isEmpty()) {
                        if(zonaViejoEmp != 3){
                            deleteEmpleado(attrWhere);
                        }

                        if(zonaEmp == 3 && zonaViejoEmp == 3){
                            result = QueryManager.uniUpdate(Interfaces.SITIO_5,
                                    EMPLEADO, fragmentos[LLAVES], attrWhere)
                                    != null ? BIEN : MAL;
                            result *= QueryManager.uniUpdate(Interfaces.SITIO_6, 
                                    EMPLEADO, fragmentos[LLAVES], attrWhere)
                                    != null ? BIEN : MAL;
                            result *= QueryManager.uniUpdate(Interfaces.SITIO_7, 
                                    EMPLEADO, fragmentos[NOMBRES], attrWhere)
                                    != null ? BIEN : MAL;
                        }else {
                            result = insertEmpleado(datos) ? BIEN : MAL;
                        }

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

        System.out.println("Update empleado: " + ok);
        System.out.println("---------End Update Empleado transaction----------");
        return ok;
    }

    public static boolean updatePlantel(DataTable datos, Map condiciones) {
        Boolean ok = false;

        System.out.println("---------Start Plantel transaction---------- ");

        short result = MAL;
        datos.rewind();
        datos.next();
        List<Interfaces> inter = new ArrayList<>();

        if (null != datos.getInt("zona_id")) {
            switch (datos.getInt("zona_id")) {
                case 1:
                    System.out.println("Zona 1");
                    ok = QueryManager.uniUpdate(Interfaces.SITIO_2, PLANTEL, datos,
                            condiciones);
                    result = ok != null ? BIEN : MAL;

                    result *= QueryManager.uniUpdate(Interfaces.LOCALHOST, PLANTEL, datos,
                            condiciones) != null ? BIEN : MAL;

                    inter.add(Interfaces.LOCALHOST);
                    inter.add(Interfaces.SITIO_2);
                    break;
                case 2:
                    System.out.println("Zona 2");

                    ok = QueryManager.uniUpdate(Interfaces.SITIO_3, PLANTEL, datos,
                            condiciones);
                    result = ok != null ? BIEN : MAL;
                    result *= QueryManager.uniUpdate(Interfaces.SITIO_4, PLANTEL, datos,
                            condiciones) != null ? BIEN : MAL;

                    inter.add(Interfaces.SITIO_3);
                    inter.add(Interfaces.SITIO_4);
                    break;
                case 3:
                    System.out.println("Zona 3");
                    ok = QueryManager.uniUpdate(Interfaces.SITIO_7, PLANTEL, datos, condiciones);

                    result = ok != null ? BIEN : MAL;

                    result *= QueryManager.uniUpdate(Interfaces.SITIO_5, PLANTEL, datos,
                            condiciones) != null ? BIEN : MAL;

                    result *= QueryManager.uniUpdate(Interfaces.SITIO_6, PLANTEL, datos,
                            condiciones) != null ? BIEN : MAL;

                    inter.add(Interfaces.SITIO_5);
                    inter.add(Interfaces.SITIO_6);
                    inter.add(Interfaces.SITIO_7);
                    break;
            }
        }

        if (result == MAL) {
            ok = false;
            rollback(inter);
        } else {
            commit(inter);
        }

        System.out.println("---------End Plantel transaction----------");
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

        String numeroEmpleado = attrWhere.get(EMPLEADO_ID).toString();

        Integer zonaEmpleado = zonaEmpleado(numeroEmpleado);

        if (zonaEmpleado == null || zonaEmpleado == -1) {
            return false;
        }

        List<Interfaces> interfaces = new ArrayList<>();

        switch (zonaEmpleado) {
            case 1:
                interfaces.add(Interfaces.LOCALHOST);
                interfaces.add(Interfaces.SITIO_2);

                ok = QueryManager.multiDelete(EMPLEADO, attrWhere,
                        interfaces.toArray(new Interfaces[interfaces.size()]));
                break;
            case 2:
                interfaces.add(Interfaces.SITIO_3);
                interfaces.add(Interfaces.SITIO_4);

                ok = QueryManager.multiDelete(EMPLEADO, attrWhere,
                        interfaces.toArray(new Interfaces[interfaces.size()]));
                break;
            case 3:
                interfaces.add(Interfaces.SITIO_5);
                interfaces.add(Interfaces.SITIO_6);
                interfaces.add(Interfaces.SITIO_7);

                ok = QueryManager.multiDelete(EMPLEADO, attrWhere,
                        interfaces.toArray(new Interfaces[interfaces.size()]));
                break;
        }

        if (ok) {
            commit(interfaces);
        } else {
            ok = false;
            rollback(interfaces);
        }

        System.out.println("--------- Delete Empleado: " + ok);
        System.out.println("---------End Delete Empleado transaction---------- ");

        return ok;
    }

    //Modificar para su sitio
    public static boolean deletePlantel(Map<String, ?> attrWhere) {
        System.out.println("---------Start Delete Plantel transaction---------- ");
        boolean ok = true;

        DataTable plantel = getPlantel(attrWhere);

        if (plantel != null && !plantel.isEmpty()) {
            //Si existe el plantel...
            plantel.next();
            int idZona = plantel.getInt(PLANTEL_ZONA_ID);
            List<Interfaces> interfaces = new ArrayList<>();

            switch (idZona) {
                case 1:
                    interfaces.add(Interfaces.LOCALHOST);
                    interfaces.add(Interfaces.SITIO_2);

                    ok = QueryManager.multiDelete(PLANTEL, attrWhere,
                            interfaces.toArray(new Interfaces[interfaces.size()]));
                    break;
                case 2:
                    interfaces.add(Interfaces.SITIO_3);
                    interfaces.add(Interfaces.SITIO_4);

                    ok = QueryManager.multiDelete(PLANTEL, attrWhere,
                            interfaces.toArray(new Interfaces[interfaces.size()]));
                    break;
                case 3:
                    interfaces.add(Interfaces.SITIO_5);
                    interfaces.add(Interfaces.SITIO_6);
                    interfaces.add(Interfaces.SITIO_7);

                    ok = QueryManager.multiDelete(PLANTEL, attrWhere,
                            interfaces.toArray(new Interfaces[interfaces.size()]));
                    break;
            }

            if (ok) {
                commit(interfaces);
            } else {
                rollback(interfaces);
            }
        } else {
            //Error, deberia encontrarse el plantel
            ok = false;
        }

        System.out.println("--------- Delete Plantel: " + ok);
        System.out.println("---------End Delete Plantel transaction---------- ");

        return ok;
    }

    public static DataTable consultarEmpleados(Map attrWhere) {
        DataTable empleados;

        System.out.println("---------Start GetEmpleados transaction---------- ");

        //Ver que contiene las condiciones para saber a donde dirigirse
        if (attrWhere.containsKey(EMPLEADO_PLANTEL_ID)
                || attrWhere.containsKey(EMPLEADO_CORREO)
                || attrWhere.containsKey(EMPLEADO_ADSCRIPCION_ID)) {
            //Hacer primero el select con las llaves

            //Zona 1
            //Obtener datos de filtro
            //.......
            DataTable fragLlaves = QueryManager.uniGet(Interfaces.SITIO_2, EMPLEADO,
                    null, null, attrWhere, EMPLEADO_ID);

            //Si hay error regresar error
            if (fragLlaves == null) {
                return null;
            }

            //Obtener el fragmento correspondiente del otro sitio que corresponda
            //con los registros obtenidos
            HashMap<String, DataTable> condicionIN = new HashMap<>();

            //Si no se regreso nada solo unir a otra dataTable vacia
            DataTable fragDatos;
            if (!fragLlaves.isEmpty()) {
                condicionIN.put(EMPLEADO_ID + " IN",
                        fragLlaves.obtenerColumnas(new String[]{EMPLEADO_ID}));

                fragDatos = QueryManager.uniGet(Interfaces.LOCALHOST, EMPLEADO,
                        null, null, condicionIN, EMPLEADO_ID);
            } else {
                //Si la primera tabla esta vacia no tiene caso buscar en el otro
                //sitio
                fragDatos = new DataTable(FRAG_DATOS, 0, 0);
            }

            //Combinar ambos fragmentos
            DataTable empleadosZona1 = DataTable.combinarFragV(fragDatos, fragLlaves,
                    EMPLEADO_ID);

            //Zona 2
            //Obtener datos de filtro
            fragLlaves = QueryManager.uniGet(Interfaces.SITIO_3, EMPLEADO,
                    null, null, attrWhere, EMPLEADO_ID);

            //Si hay error regresar error
            if (fragLlaves == null) {
                return null;
            }

            //Obtener el fragmento correspondiente del otro sitio que corresponda
            //con los registros obtenidos
            condicionIN.clear();

            //Si no se regreso nada solo unir a otra dataTable vacia
            if (!fragLlaves.isEmpty()) {
                condicionIN.put(EMPLEADO_ID + " IN",
                        fragLlaves.obtenerColumnas(new String[]{EMPLEADO_ID}));

                fragDatos = QueryManager.uniGet(Interfaces.SITIO_4, EMPLEADO,
                        null, null, condicionIN, EMPLEADO_ID);
            } else {
                //Si la primera tabla esta vacia no tiene caso buscar en el otro
                //sitio
                fragDatos = new DataTable(FRAG_DATOS, 0, 0);
            }

            //Combinar ambos fragmentos
            DataTable empleadosZona2 = DataTable.combinarFragV(fragDatos, fragLlaves,
                    EMPLEADO_ID);

            //Zona 3
            //Obtener datos de filtro
            fragLlaves = QueryManager.uniGet(Interfaces.SITIO_5, EMPLEADO,
                    null, null, attrWhere, EMPLEADO_ID);

            //Si hay error regresar error
            if (fragLlaves == null) {
                return null;
            }

            //Obtener el fragmento correspondiente del otro sitio que corresponda
            //con los registros obtenidos
            condicionIN.clear();

            //Si no se regreso nada solo unir a otra dataTable vacia
            if (!fragLlaves.isEmpty()) {
                condicionIN.put(EMPLEADO_ID + " IN",
                        fragLlaves.obtenerColumnas(new String[]{EMPLEADO_ID}));

                fragDatos = QueryManager.uniGet(Interfaces.SITIO_7, EMPLEADO,
                        null, null, condicionIN, EMPLEADO_ID);
            } else {
                //Si la primera tabla esta vacia no tiene caso buscar en el otro
                //sitio
                fragDatos = new DataTable(FRAG_DATOS, 0, 0);
            }

            //Combinar ambos fragmentos
            DataTable empleadosZona3 = DataTable.combinarFragV(fragDatos, fragLlaves,
                    EMPLEADO_ID);

            //Combinar los resultados de las 3 zonas
            empleados = DataTable.combinarFragH(empleadosZona1, empleadosZona2,
                    empleadosZona3);
        } else {
            //Hacer primero el select con los datos

            //Zona 1
            //Obtener datos de filtro
            //.......
            DataTable fragDatos = QueryManager.uniGet(Interfaces.LOCALHOST, EMPLEADO,
                    null, null, attrWhere, EMPLEADO_ID);

            //Si hay error regresar error
            if (fragDatos == null) {
                return null;
            }

            //Obtener el fragmento correspondiente del otro sitio que corresponda
            //con los registros obtenidos
            HashMap<String, DataTable> condicionIN = new HashMap<>();

            //Si no se regreso nada solo unir a otra dataTable vacia
            DataTable fragLlaves;
            if (!fragDatos.isEmpty()) {
                condicionIN.put(EMPLEADO_ID + " IN",
                        fragDatos.obtenerColumnas(new String[]{EMPLEADO_ID}));

                fragLlaves = QueryManager.uniGet(Interfaces.SITIO_2, EMPLEADO,
                        null, null, condicionIN, EMPLEADO_ID);
            } else {
                //Si la primera tabla esta vacia no tiene caso buscar en el otro
                //sitio
                fragLlaves = new DataTable(FRAG_LLAVES, 0, 0);
            }

            //Combinar ambos fragmentos
            DataTable empleadosZona1 = DataTable.combinarFragV(fragDatos, fragLlaves,
                    EMPLEADO_ID);

            //Zona 2
            //Obtener datos de filtro
            //.......
            fragDatos = QueryManager.uniGet(Interfaces.SITIO_4, EMPLEADO,
                    null, null, attrWhere, EMPLEADO_ID);

            //Si hay error regresar error
            if (fragDatos == null) {
                return null;
            }

            //Obtener el fragmento correspondiente del otro sitio que corresponda
            //con los registros obtenidos
            condicionIN.clear();

            //Si no se regreso nada solo unir a otra dataTable vacia
            if (!fragDatos.isEmpty()) {
                condicionIN.put(EMPLEADO_ID + " IN",
                        fragDatos.obtenerColumnas(new String[]{EMPLEADO_ID}));

                fragLlaves = QueryManager.uniGet(Interfaces.SITIO_3, EMPLEADO,
                        null, null, condicionIN, EMPLEADO_ID);
            } else {
                //Si la primera tabla esta vacia no tiene caso buscar en el otro
                //sitio
                fragLlaves = new DataTable(FRAG_LLAVES, 0, 0);
            }

            //Combinar ambos fragmentos
            DataTable empleadosZona2 = DataTable.combinarFragV(fragDatos, fragLlaves,
                    EMPLEADO_ID);

            //Zona 3
            //Obtener datos de filtro
            //.......
            fragDatos = QueryManager.uniGet(Interfaces.SITIO_7, EMPLEADO,
                    null, null, attrWhere, EMPLEADO_ID);

            //Si hay error regresar error
            if (fragDatos == null) {
                return null;
            }

            //Obtener el fragmento correspondiente del otro sitio que corresponda
            //con los registros obtenidos
            condicionIN.clear();

            //Si no se regreso nada solo unir a otra dataTable vacia
            if (!fragDatos.isEmpty()) {
                condicionIN.put(EMPLEADO_ID + " IN",
                        fragDatos.obtenerColumnas(new String[]{EMPLEADO_ID}));

                fragLlaves = QueryManager.uniGet(Interfaces.SITIO_5, EMPLEADO,
                        null, null, condicionIN, EMPLEADO_ID);
            } else {
                //Si la primera tabla esta vacia no tiene caso buscar en el otro
                //sitio
                fragLlaves = new DataTable(FRAG_LLAVES, 0, 0);
            }

            //Combinar ambos fragmentos
            DataTable empleadosZona3 = DataTable.combinarFragV(fragDatos, fragLlaves,
                    EMPLEADO_ID);

            //Combinar los resultados de las 3 zonas
            empleados = DataTable.combinarFragH(empleadosZona1, empleadosZona2,
                    empleadosZona3);

        }

        System.out.println("---------End GetEmpleados transaction---------- ");

        return empleados;
    }

    public static DataTable consultarEmpleadosByD(Map attrWhere) {
        DataTable empleados;

        System.out.println("---------Start GetEmpleados transaction---------- ");
        //Hacer primero el select con las llaves

        //Departamento o Direccion
        //Obtener datos de filtro
        //.......
        DataTable fragLlaves = QueryManager.uniGet(Interfaces.SITIO_2, EMPLEADO,
                null, null, attrWhere, EMPLEADO_ID);

        //Si hay error regresar error
        if (fragLlaves == null) {
            return null;
        }

        //Obtener el fragmento correspondiente del otro sitio que corresponda
        //con los registros obtenidos
        HashMap<String, DataTable> condicionIN = new HashMap<>();

        //Si no se regreso nada solo unir a otra dataTable vacia
        DataTable fragDatos;
        if (!fragLlaves.isEmpty()) {
            condicionIN.put(EMPLEADO_ID + " IN",
                    fragLlaves.obtenerColumnas(new String[]{EMPLEADO_ID}));

            fragDatos = QueryManager.uniGet(Interfaces.LOCALHOST, EMPLEADO,
                    null, null, condicionIN, EMPLEADO_ID);
        } else {
            //Si la primera tabla esta vacia no tiene caso buscar en el otro
            //sitio
            fragDatos = new DataTable(FRAG_DATOS, 0, 0);
        }

        //Combinar ambos fragmentos
        empleados = DataTable.combinarFragV(fragDatos, fragLlaves,
                EMPLEADO_ID);

        System.out.println("---------End GetEmpleados transaction---------- ");

        return empleados;
    }

    public static DataTable consultarEmpleados() {
        System.out.println("---------Start GetEmpleados transaction---------- ");

        String[] columnas = {
            "numero",
            "primer_nombre",
            "segundo_nombre",
            "apellido_paterno",
            "apellido_materno"};

        //Zona 1
        DataTable fragDatosZona1 = QueryManager.uniGet(Interfaces.LOCALHOST,
                EMPLEADO, columnas, null, null, EMPLEADO_ID);

        DataTable fragDatosZona2 = QueryManager.uniGet(Interfaces.SITIO_4,
                EMPLEADO, columnas, null, null, EMPLEADO_ID);

        DataTable fragDatosZona3 = QueryManager.uniGet(Interfaces.SITIO_7,
                EMPLEADO, columnas, null, null, EMPLEADO_ID);

        System.out.println("---------End GetEmpleados transaction---------- ");

        return DataTable.combinarFragH(fragDatosZona1, fragDatosZona2, fragDatosZona3);
    }

    public static DataTable consultarPlanteles(Map attrWhere) {

        System.out.println("---------Start GetPlanteles transaction---------- ");

        //Zona 1
        DataTable fragDatosZona1 = QueryManager.uniGet(
                Interfaces.LOCALHOST, PLANTEL, null, null, attrWhere, PLANTEL_ID);
        //Zona 2
        DataTable fragDatosZona2 = QueryManager.uniGet(
                Interfaces.SITIO_3, PLANTEL, null, null, attrWhere, PLANTEL_ID);
        //Zona 3
        DataTable fragDatosZona3 = QueryManager.uniGet(
                Interfaces.SITIO_7, PLANTEL, null, null, attrWhere, PLANTEL_ID);

        System.out.println("---------End GetEmpleados transaction---------- ");

        return DataTable.combinarFragH(fragDatosZona1, fragDatosZona2,
                fragDatosZona3);
    }

    public static DataTable consultarImplementacionesByEmpleado(String numero) {
        DataTable implementaciones = null;

        System.out.println("---------Start GetImplementaciones transaction---------- ");

        Integer zona = zonaEmpleado(numero);

        if (zona != null && zona != -1) {
            //El empleado existe, ahora a buscar sus implementaciones
            HashMap<String, String> condicion = new HashMap<>();
            condicion.put(EMPLEADO_NUMERO, numero);

            switch (zona) {
                case 1:
                    implementaciones = QueryManager.uniGet(Interfaces.LOCALHOST,
                            IMPLEMENTACION_EVENTO_EMPLEADO,
                            new String[]{IMPLEMENTACION_EVENTO_ID},
                            new String[]{null}, condicion,
                            IMPLEMENTACION_EVENTO_ID);
                    break;

                case 2:
                    implementaciones = QueryManager.uniGet(Interfaces.SITIO_4,
                            IMPLEMENTACION_EVENTO_EMPLEADO,
                            new String[]{IMPLEMENTACION_EVENTO_ID},
                            new String[]{null}, condicion,
                            IMPLEMENTACION_EVENTO_ID);
                    break;

                case 3:
                    implementaciones = QueryManager.uniGet(Interfaces.SITIO_7,
                            IMPLEMENTACION_EVENTO_EMPLEADO,
                            new String[]{IMPLEMENTACION_EVENTO_ID},
                            new String[]{null}, condicion,
                            IMPLEMENTACION_EVENTO_ID);
                    break;
            }
        } else {
            implementaciones = null;
        }

        System.out.println("---------End GetImplementaciones transaction---------- ");

        return implementaciones;
    }

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
                EMPLEADO, columnas, null, condicion, EMPLEADO_ID);
        if (columnas == null || segundoFragmento) {
            DataTable llaves = QueryManager.uniGet(Interfaces.LOCALHOST, EMPLEADO,
                    fragLlaves, null, condicion, EMPLEADO_ID);
            empleado = DataTable.combinarFragV(empleado, llaves, EMPLEADO_ID);
        }

        if (empleado == null || empleado.getRowCount() == 0) {
            //En caso de no encontrarse se busca en el nodo 4
            empleado = QueryManager.uniGet(Interfaces.SITIO_4, EMPLEADO, null,
                    null, condicion, EMPLEADO_ID);
            if (columnas == null || segundoFragmento) {
                DataTable llaves = QueryManager.uniGet(Interfaces.SITIO_2, EMPLEADO,
                        fragLlaves, null, condicion, EMPLEADO_ID);
                empleado = DataTable.combinarFragV(empleado, llaves, EMPLEADO_ID);
            }

            if (empleado == null || empleado.getRowCount() == 0) {
                //Por ultimo se busca en el sitio 7 en caso de no encontrarse
                empleado = QueryManager.uniGet(Interfaces.SITIO_7, EMPLEADO, null,
                        null, condicion, EMPLEADO_ID);
                if (columnas == null || listaLlaves.retainAll(listaColumnas)) {
                    DataTable llaves = QueryManager.uniGet(Interfaces.SITIO_6, EMPLEADO,
                            fragLlaves, null, condicion, EMPLEADO_ID);
                    empleado = DataTable.combinarFragV(empleado, llaves, EMPLEADO_ID);
                }
            }
        }

        System.out.println("---------End GetEmpleado transaction----------");
        return empleado;
    }

    public static DataTable getPlantel(Map<String, ?> condicion) {
        System.out.println("---------Start GetPlantel transaction---------- ");

        //Se busca en el nodo local (Zona 1)
        DataTable plantel = QueryManager.uniGet(Interfaces.LOCALHOST,
                PLANTEL, null, null, condicion, PLANTEL_ID);

        if (plantel == null || plantel.isEmpty()) {
            //Si no se encontró en la Zona 1 buscar en la Zona 2
            plantel = QueryManager.uniGet(Interfaces.SITIO_4, PLANTEL, null, null,
                    condicion, PLANTEL_ID);

            if (plantel == null || plantel.isEmpty()) {
                //Si no esta en la Zona 2 buscar en la Zona 3
                plantel = QueryManager.uniGet(Interfaces.SITIO_7, PLANTEL,
                        null, null, condicion, PLANTEL_ID);

                //Si no se encontró aquí regresar el plantel vacío de todos modos
            }
        }

        System.out.println("---------End GetPlantel transaction----------");
        return plantel;
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

    public static void commit(List<Interfaces> interfaces) {

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

    public static void rollback(List<Interfaces> interfaces) {
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
            ok = QueryManager.uniGet(Interfaces.LOCALHOST, EMPLEADO, null, null, condicion, EMPLEADO_ID)
                    .next();
            if (!ok) {
                ok = QueryManager.uniGet(Interfaces.SITIO_4, EMPLEADO, null, null, condicion, EMPLEADO_ID)
                        .next();
                if (!ok) {
                    ok = QueryManager.uniGet(Interfaces.SITIO_7, EMPLEADO, null, null, condicion, EMPLEADO_ID)
                            .next();
                    if (ok) {
                        zona = 3;
                    }
                } else {
                    zona = 2;
                }
            } else {
                zona = 1;
            }

        } catch (NullPointerException e) {
            System.out.println("NullPointer uniGet zonaEmpleado");
            zona = null;
        }

        return zona;
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

}
