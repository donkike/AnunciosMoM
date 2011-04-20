import java.rmi.Remote;
import java.rmi.RemoteException;


public interface IAdFuente extends Remote {
	
	public void eliminarCanal(String nombre) throws RemoteException;

}
