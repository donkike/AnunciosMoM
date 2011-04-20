
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;


public class Canal {	
	
	private String nombre;
	private Hashtable<Integer, IAdCliente> clientes;
	private Hashtable<Integer, IAdFuente> fuentes;
	
	public Canal(String nombre) {
		this.nombre = nombre;
		clientes = new Hashtable<Integer, IAdCliente>();
		fuentes = new Hashtable<Integer, IAdFuente>();
	}
	
	public void eliminarCanal() throws RemoteException {		
		for (IAdCliente cliente : clientes.values()) 
			cliente.eliminarCanal(nombre);
		for (IAdFuente fuente : fuentes.values())
			fuente.eliminarCanal(nombre);
	}
	
	public void suscribir(int id, IAdCliente cliente) {
		clientes.put(id, cliente);
	}
	
	public void registrar(int id, IAdFuente fuente) {
		fuentes.put(id, fuente);
	}
	
	public String getNombre() {
		return nombre;
	}
	
	public void eliminarFuente(int id){
		fuentes.remove(id);
	}

	public void eliminarCliente(int id){
		clientes.remove(id);
	}
}
