import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class AdFuente extends UnicastRemoteObject implements IAdFuente {
	
	public static final int ENVIAR = 1;
	public static final int CANALES_REG = 2;
	public static final int CANALES = 3;
	public static final int REGISTRAR = 4;
	public static final int ELIMINAR = 5;
	public static final int SALIR = 0;

	private IGestorCanales gestor;
	private Hashtable<String, MessageProducer> canales;
	private Connection connection;
	private Session session;
	private int id;
	
	protected AdFuente(IGestorCanales gestor) throws RemoteException {
		super();
		canales = new Hashtable<String, MessageProducer>();
		this.gestor = gestor;
		this.id = gestor.getId();
	}
	
	public void verMenu() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int op = -1;
		while (true) {
			System.out.println("Seleccione la opci�n:");
			System.out.println("1. Enviar mensaje.");
			System.out.println("2. Ver canales registrados.");
			System.out.println("3. Ver todos los canales.");
			System.out.println("4. Registrarse a canal.");
			System.out.println("5. Eliminar canal.");
			System.out.println("0. Salir.");
			try {
				op = Integer.parseInt(br.readLine());
				String canal = null, mensaje = null;
				switch(op) {
					case(ENVIAR):
						imprimirCanalesRegistrados();
						System.out.println("Escriba el canal:");
						canal = br.readLine();
						System.out.println("Escriba el mensaje:");
						mensaje = br.readLine();
						enviar(mensaje, canal);
						break;
					case(CANALES_REG):
						imprimirCanalesRegistrados();
						break;
					case(CANALES):
						imprimirCanales();
						break;
					case(REGISTRAR):
						imprimirCanales();
						System.out.println("Escriba el canal a registrarse:");
						canal = br.readLine();
						registrar(canal);
						break;
					case(ELIMINAR):
						System.out.println("Ingrese el canal a eliminar de sus registros:");
						canal = br.readLine();
						try {
							eliminarCanal(canal);					
						}catch(Exception e){
							System.out.println("No se pudo eliminar el registro");
						}
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
		try {
			System.out.println("Todos los canales:");
			for (String canalListado : gestor.listarCanales()) {
				System.out.println(canalListado);
			}
		} catch (RemoteException e) {
			System.out.println("No se pudo listar los canales: " + e.getMessage());
		}
	}
	
	public void imprimirCanalesRegistrados() {
		System.out.println("Canales registrados:");
		Enumeration<String> nombresCanales = canales.keys();
		while (nombresCanales.hasMoreElements()) {
			String canal = nombresCanales.nextElement();
			System.out.println(canal);
		}
		System.out.println();
	}
		
	public void run(String host) {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
			"tcp://" + host + ":61616");
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (Exception e) {
			System.out.print("Error al correr el AdFuente: " + e.getMessage());
		}
	}
	
	public void registrar(String canal) {
		try {
			gestor.registrar(this, this.id, canal);
			Destination destination = session.createTopic(canal);
			MessageProducer producer = session.createProducer(destination);
			canales.put(canal, producer);
		} catch(Exception e) {
			System.out.println("No se pudo registrar al canal " + canal + ": " + e.getMessage());
		}
	}
	
	public void enviar(String mensaje, String canal) {
		try {
			canales.get(canal).send(session.createTextMessage(canal + " :: " + mensaje));
		} catch(Exception e) {
			System.out.println("No se pudo enviar el mensaje: " + e.getMessage());
		}
	}

	@Override
	public void eliminarCanal(String canal) {
		try {
			gestor.eliminarRegistro(this.id, canal);
			canales.get(canal).close();
			canales.remove(canal);
		} catch (Exception e) {
			System.out.print("No se pudo eliminar el canal: " + e.getMessage());
		}	
	}
	
	public static void main(String[] args) {
		IGestorCanales gestor = null;
		if (args.length >= 1) {
			String host = args[0];
			try {
				gestor = (IGestorCanales)Naming.lookup("rmi://" + host + "/anuncios");
				AdFuente fuente = new AdFuente(gestor);
				fuente.run(host);
				fuente.verMenu();
			} catch (Exception e) {
				System.out.println("Error al iniciar la fuente: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
