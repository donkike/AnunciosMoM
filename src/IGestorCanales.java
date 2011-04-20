import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IGestorCanales extends Remote {
	
	public void run() throws RemoteException;
	public String[] listarCanales() throws RemoteException;
	public void crearCanal(String canal) throws RemoteException;
	public void eliminarCanal(String canal) throws RemoteException;
	public int getId() throws RemoteException;
	public void registrar(IAdFuente fuente, int id, String canal) throws RemoteException, Exception;
	public void eliminarRegistro(int id, String canal) throws RemoteException, Exception;
	public void suscribir(IAdCliente cliente, int id, String canal) throws RemoteException, Exception;
	public void eliminarSuscripcion(int id, String canal) throws RemoteException, Exception;
	
}
