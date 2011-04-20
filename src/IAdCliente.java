import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IAdCliente extends Remote {
	
	public void eliminarCanal(String canal) throws RemoteException;

}
