import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;


public class AdCliente extends UnicastRemoteObject implements IAdCliente {
	
	public static final int CANALES_REG = 1;
	public static final int CANALES = 2;
	public static final int REGISTRAR = 3;
	public static final int ELIMINAR = 4;
	public static final int SALIR = 0;

	private IGestorCanales gestor;
	private Hashtable<String, MessageConsumer> canales;
	private Connection connection;
	private Session session;
	private int id;
	
	protected AdCliente(IGestorCanales gestor) throws RemoteException {
		super();
		canales = new Hashtable<String, MessageConsumer>();
		this.gestor = gestor;
	}
	
	public void run(String host) {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
			"tcp://" + host + ":61616");
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			connection.start();
		} catch (Exception e) {
			System.out.print("Error al correr el AdCliente: " + e.getMessage());
		}
	}
	
	public void verMenu() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int op = -1;
		while (true) {
			System.out.println("Escoge una opci�n:");
			System.out.println("Seleccione la opci�n:");
			System.out.println("1. Ver canales registrados.");
			System.out.println("2. Ver todos los canales.");
			System.out.println("3. Registrarse a canal.");
			System.out.println("4. Eliminar canal.");
			System.out.println("0. Salir.");
			try {
				String canal = null;
				op = Integer.parseInt(br.readLine());
				switch(op) {
				case(CANALES_REG):
					imprimirCanalesRegistrados();
					break;
				case(CANALES):
					imprimirCanales();
					break;
				case(REGISTRAR):
					imprimirCanales();
					System.out.println("Escoge el canal:");
					canal = br.readLine();
					suscribir(canal);
					break;
				case(ELIMINAR):
					imprimirCanalesRegistrados();
					System.out.println("Ingresa el canal a eliminar de tus suscripciones:");
					canal = br.readLine();
					try{
						eliminarCanal(canal);
					}catch(Exception e){
						System.out.println("No se pudo eliminar la suscripcion");
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
		
	
	public void suscribir(String canal) {
		try {
			gestor.suscribir(this, this.id, canal);
			Destination destination = session.createTopic(canal);
			MessageConsumer consumer = session.createConsumer(destination);			
			MessageListener listener = new MessageListener() {
				public void onMessage(Message msg) {
					if (msg instanceof TextMessage) {
						TextMessage textMessage = (TextMessage) msg;
						String text = null;
						try {
							text = textMessage.getText();
						} catch (JMSException e) {
							System.out.println("Error al recibir mensaje: " + e.getMessage());
						}
						System.out.println("Recibido: " + text);
					} else {
						System.out.println("Recibido: " + msg);
					}
				}
			};
			consumer.setMessageListener(listener);
			canales.put(canal, consumer);
		} catch (Exception e) {
			System.out.println("No se puedo registrar al canal " + canal + ": " + e.getMessage());
		}
	}

	@Override
	public void eliminarCanal(String canal) {
		try {
			gestor.eliminarSuscripcion(this.id, canal);
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
				AdCliente cliente = new AdCliente(gestor);
				cliente.run(host);
				cliente.verMenu();
			} catch (Exception e) {
				System.out.println("No se pudo iniciar el cliente: " + e.getMessage());
				e.printStackTrace();
			}			
		}
	}

}
