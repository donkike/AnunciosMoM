import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Hashtable;


public class GestorCanalesImpl extends UnicastRemoteObject implements IGestorCanales {
	
	public static final int CREAR = 1;
	public static final int ELIMINAR = 2;
	public static final int LISTAR = 3;
	public static final int SALIR = 0;
	
	private Hashtable<String, Canal> canales;
	private int currentId;
	
	public GestorCanalesImpl() throws RemoteException {
		canales = new Hashtable<String, Canal>();
		currentId = 0;
	}
	
	public void run() {
		verMenu();
	}
	
	public void verMenu() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int op = -1;
		while (true) {
			System.out.println("Seleccione la opci�n:");
			System.out.println("1. Crear canal.");
			System.out.println("2. Eliminar canal.");
			System.out.println("3. Listar canales.");
			System.out.println("0. Salir.");
			try {
				op = Integer.parseInt(br.readLine());
				String canal = null;
				switch(op) {
					case(CREAR):
						System.out.println("Ingrese el nombre del canal a crear:");
						canal = br.readLine();
						crearCanal(canal);
						break;
					case(ELIMINAR):
						imprimirCanales();
						System.out.println("Ingrese el nombre del canal a eliminar:");
						canal = br.readLine();
						eliminarCanal(canal);
						break;
					case(LISTAR):
						imprimirCanales();
						break;
					case(SALIR):
						System.exit(0);
				}
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	public void imprimirCanales() {
		System.out.println("Listado de canales:");
		for (String canal : listarCanales()) {
			System.out.println(canal);
		}
		System.out.println();
	}

	@Override
	public String[] listarCanales() {
		String[] listado = new String[canales.size()];
		Enumeration<String> nombresCanales = canales.keys();
		for (int i = 0; nombresCanales.hasMoreElements(); i++) {
			listado[i] = nombresCanales.nextElement();
		}
		return listado;
	}

	@Override
	public void crearCanal(String canal) throws RemoteException {
		canales.put(canal, new Canal(canal));	
		System.out.println("Canal " + canal + " creado.");
	}

	@Override
	public void eliminarCanal(String canal) throws RemoteException {
		canales.get(canal).eliminarCanal();
		canales.remove(canal);
		System.out.println("Canal " + canal + " eliminado.");
	}

	@Override
	public void registrar(IAdFuente fuente, int id, String canal) throws RemoteException, Exception {
		Canal canalPedido = canales.get(canal);
		if (canalPedido != null) {
			canalPedido.registrar(id, fuente);
		} else {
			throw new Exception("El canal no existe.");
		}	
	}

	@Override
	public void suscribir(IAdCliente cliente, int id, String canal) throws RemoteException, Exception {
		Canal canalPedido = canales.get(canal);
		if (canalPedido != null) {
			canalPedido.suscribir(id, cliente);
		} else {
			throw new Exception("El canal no existe.");
		}		
	}

	@Override
	public void eliminarRegistro(int id, String canal)
			throws RemoteException, Exception {
		canales.get(canal).eliminarFuente(id);
	}

	@Override
	public void eliminarSuscripcion(int id, String canal)
			throws RemoteException, Exception {
		canales.get(canal).eliminarCliente(id);
	}

	@Override
	public int getId() throws RemoteException {
		return currentId++;
	}
	
	public static void main(String[] args) {
		IGestorCanales gestor = null;
		try {
			gestor = new GestorCanalesImpl();
			Naming.bind("anuncios", gestor);
			gestor.run();
		} catch(Exception e) {
			System.out.println("Error al iniciar el gestor: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
