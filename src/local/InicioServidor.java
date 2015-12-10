package local;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase se encarga de crear un registro nuevo para el servicio de
 * persistencia de esta aplicación y escuchar en un ciclo infinito a
 * invocaciones remotas de distintos clientes
 *
 * @author Jesus Donaldo Osornio Hernández
 * @version 1 18 Mayo 2015
 */
public class InicioServidor {
    /**
     * Crea el registro, agrega el objeto que implementará la interface remota y
     * proveerá los servicios
     *
     * @param <T>
     * @param puerto El puerto por el cual es servidor escuchara.
     * @param serviceLabel
     * @param serviceImp
     */
    public static <T extends Remote> void iniciarServidor(int puerto,
            String serviceLabel, Class<T> serviceImp) {
        try {
            //Crea el nuevo registro en el puerto 9000
            Registry registro = LocateRegistry.createRegistry(puerto);
            //Agrega la etiqueta para identificar el servicio y crea un nuevo
            //Objeto para proveer los métodos remotos
            registro.rebind(serviceLabel, serviceImp.newInstance());
            
            System.out.println("Servicio registrado e iniciado: " + serviceLabel + 
                    " en el puerto: " + puerto + " con la implementación: " + 
                    serviceImp.getName());
            
        } catch (RemoteException | InstantiationException | IllegalAccessException ex) {
            
            Logger.getLogger(InicioServidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
