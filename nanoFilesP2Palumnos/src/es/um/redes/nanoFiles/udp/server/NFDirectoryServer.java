package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/*
	 *Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	
	// FILELIST
	
	//Mapea dirección del servidor a su lista de ficheros compartidos
	// InetSocketAddress (ip y puerto)
	private HashMap<InetSocketAddress, ArrayList<FileInfo>> serverToFileList = new HashMap<>();
	
	
	//getter 
	public HashMap<InetSocketAddress, ArrayList<FileInfo>> getServerToFileList() {
		return serverToFileList;
	}
	
	

	
	
	
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * (Boletín SocketsUDP) Inicializar el atributo socket: Crear un socket
		 * UDP ligado al puerto especificado por el argumento directoryPort en la
		 * máquina local,
		 */
		
		socket = new DatagramSocket(DIRECTORY_PORT);
		
		/*
		 * (Boletín SocketsUDP) Inicializar atributos que mantienen el estado del
		 * servidor de directorio: ficheros, etc.)
		 */



		if (NanoFiles.testModeUDP) {
			if (socket == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public DatagramPacket receiveDatagram() throws IOException {
		System.out.println("Esperando datagrama");
		DatagramPacket datagramReceivedFromClient = null;
		boolean datagramReceived = false;
		while (!datagramReceived) {
			/*
			 * (Boletín SocketsUDP) Crear un búfer para recibir datagramas y un
			 * datagrama asociado al búfer (datagramReceivedFromClient)
			 */

			
			/*
			 * (Boletín SocketsUDP) Recibimos a través del socket un datagrama
			 */
			
			byte[] buffer = new byte[DirMessage.PACKET_MAX_SIZE];
			datagramReceivedFromClient = new DatagramPacket(buffer, buffer.length);
			socket.receive(datagramReceivedFromClient);


				// Vemos si el mensaje debe ser ignorado (simulación de un canal no confiable)
				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println("Directory ignored datagram from " + datagramReceivedFromClient.getSocketAddress());
				} else {
					datagramReceived = true;
					System.out.println("Servidor: Datagrama recibido desde " + datagramReceivedFromClient.getSocketAddress());
					System.out.println("Contenido crudo del datagrama:\n" + new String(datagramReceivedFromClient.getData(), 0, datagramReceivedFromClient.getLength()));

					System.out.println("Directory received datagram from " + datagramReceivedFromClient.getSocketAddress()
									+ " of size " + datagramReceivedFromClient.getLength() + " bytes.");
				}
			}

//		}

		return datagramReceivedFromClient;
	}

	public void runTest() throws IOException {

		System.out.println("[testMode] Directory starting...");

		System.out.println("[testMode] Attempting to receive 'ping' message...");
		DatagramPacket rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);

		System.out.println("[testMode] Attempting to receive 'ping&PROTOCOL_ID' message...");
		rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);
	}

	private void sendResponseTestMode(DatagramPacket pkt) throws IOException {
		/*
		 * (Boletín SocketsUDP) Construir un String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración.
		 */
		
		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: " + messageFromClient);
		
		 String response;
	        if (messageFromClient.equals("ping")) {
	            response = "pingok";
	        } else if (messageFromClient.startsWith("ping&")) {
	            String protocolId = messageFromClient.substring(5);		//Extraer el PROTOCOL_ID omitiendo los 5 primeros caracteres "ping&"
	            if (protocolId.equals(NanoFiles.PROTOCOL_ID)) {
	                response = "welcome";	//valido
	            } else {
	                response = "denied";	// no valido
	            }
	        } else {
	            response = "invalid";
	        }
	        
	        //Convertir la respuesta en bytes y enviarla. Se imprime tambien
	        byte[] responseData = response.getBytes();
	        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, pkt.getSocketAddress());
	        socket.send(responsePacket);
	        System.out.println("Sent response: " + response);
		
		
		
		
		/*
		 * (Boletín SocketsUDP) Después, usar la cadena para comprobar que su
		 * valor es "ping"; en ese caso, enviar como respuesta un datagrama con la
		 * cadena "pingok". Si el mensaje recibido no es "ping", se informa del error y
		 * se envía "invalid" como respuesta.    HECHO
		 */

		/*
		 * (Boletín Estructura-NanoFiles) Ampliar el código para que, en el caso
		 * de que la cadena recibida no sea exactamente "ping", comprobar si comienza
		 * por "ping&" (es del tipo "ping&PROTOCOL_ID", donde PROTOCOL_ID será el
		 * identificador del protocolo diseñado por el grupo de prácticas (ver
		 * NanoFiles.PROTOCOL_ID). Se debe extraer el "protocol_id" de la cadena
		 * recibida y comprobar que su valor coincide con el de NanoFiles.PROTOCOL_ID,
		 * en cuyo caso se responderá con "welcome" (en otro caso, "denied").   HECHO
		 */




	}

	public void run() throws IOException {

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			DatagramPacket rcvDatagram = receiveDatagram();

			sendResponse(rcvDatagram);

		}
	}

	private void sendResponse(DatagramPacket pkt) throws IOException {
		
		
		/*
		 * (Boletín MensajesASCII) Construir String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración. Después, usar la cadena para construir un objeto
		 * DirMessage que contenga en sus atributos los valores del mensaje. A partir de
		 * este objeto, se podrá obtener los valores de los campos del mensaje mediante
		 * métodos "getter" para procesar el mensaje y consultar/modificar el estado del
		 * servidor.
		 */
		try {
		byte[] bDatos = pkt.getData();
		String sDatos = new String(bDatos, 0,pkt.getLength());
		DirMessage dmDatos = DirMessage.fromString(sDatos);
		

		

		/*
		 * Una vez construido un objeto DirMessage con el contenido del datagrama
		 * recibido, obtener el tipo de operación solicitada por el mensaje y actuar en
		 * consecuencia, enviando uno u otro tipo de mensaje en respuesta.
		 */
		String operation = dmDatos.getOperation();

		/*
		 * (Boletín MensajesASCII) Construir un objeto DirMessage (msgToSend) con
		 * la respuesta a enviar al cliente, en función del tipo de mensaje recibido,
		 * leyendo/modificando según sea necesario el "estado" guardado en el servidor
		 * de directorio (atributos files, etc.). Los atributos del objeto DirMessage
		 * contendrán los valores adecuados para los diferentes campos del mensaje a
		 * enviar como respuesta (operation, etc.)
		 */



		DirMessage dmRespuesta = null;

		/*
		 *  (Boletín MensajesASCII) Comprobamos si el protocolId del mensaje del
		 * cliente coincide con el nuestro.
		 */
		/*
		 * (Boletín MensajesASCII) Construimos un mensaje de respuesta que indique
		 * el éxito/fracaso del ping (compatible, incompatible), y lo devolvemos como
		 * resultado del método.
		 */									// ****** SWTICH*******
 		/*
		 * (Boletín MensajesASCII) Imprimimos por pantalla el resultado de
		 * procesar la petición recibida (éxito o fracaso) con los datos relevantes, a
		 * modo de depuración en el servidor
		 */

		
		
		switch (operation) {
		case DirMessageOps.OPERATION_PING: {
				
				if (dmDatos.getProtocolId().equals(NanoFiles.PROTOCOL_ID)) {
					dmRespuesta = new DirMessage(DirMessageOps.OPERATION_PINGOK);
				} else {
					dmRespuesta = new DirMessage(DirMessageOps.OPERATION_PINGERROR);
				}

			break;
		}
		
		case DirMessageOps.OPERATION_SERVE: {

			
			//Obtener IP desde donde se ha recibido el datagrama UDP
			InetAddress peerIP = pkt.getAddress();
			int peerPort = dmDatos.getPort();  //Puerto tcp que ha enviado el peer
			
			//Se crea direccion completa del peer (IP + puerto TCP)
			InetSocketAddress peerAddress = new InetSocketAddress(peerIP, peerPort);
			
			if (dmDatos.getFileList() == null) {
			    System.err.println("El mensaje SERVE no contiene lista de ficheros");
			    dmRespuesta = new DirMessage(DirMessageOps.OPEARTION_SERVEERROR); // si lo tienes, o SERVEOK si quieres forzar
			    break;
			}

			
			// Clon de la lista de ficheros del mansaje serve por seguridad
			ArrayList<FileInfo> files = new ArrayList<>(dmDatos.getFileList());
			
			//Guardamos al HashMap
			serverToFileList.put(peerAddress, files);
			
			
			//CHECKK
			System.out.println("Registrado servidor en " + peerAddress + " con "+ files.size() + " ficheros");
			System.out.println("Servidor: Procesando mensaje SERVE");
			System.out.println("IP peer: " + peerIP);
			System.out.println("Dirección completa peer: " + peerAddress);
			System.out.println("Número de ficheros recibidos: " + files.size());

			for (FileInfo f : files) {
				System.out.println(" - " + f.fileName + " (" + f.fileSize + " bytes) hash: " + f.fileHash);
			}
			//CHECKK
			
			
			
			//Envia confirmación


			dmRespuesta = new DirMessage(DirMessageOps.OPERATION_SERVEOK);
			
			System.out.println("SERVIDOR: Enviando respuesta al puerto " + pkt.getPort());
			System.out.println("SERVIDOR: Socket remoto del cliente = " + pkt.getAddress() + ":" + pkt.getPort());
			System.out.println("SERVIDOR: Dirección completa destino del datagrama de respuesta = " + pkt.getSocketAddress());

			break;
		}

		case DirMessageOps.OPERATION_FILELIST: {

			
			//Recolectar todos los ficheros publicados por todos los peers
			ArrayList<FileInfo> allFiles = new ArrayList<>();
			for (ArrayList<FileInfo> peerFiles : serverToFileList.values()) {
				allFiles.addAll(peerFiles);
			}
			
			
			if (allFiles.isEmpty()) {
			    System.err.println("El mensaje FILELIST no contiene lista de ficheros");
			    dmRespuesta = new DirMessage(DirMessageOps.OPERATION_FILELISTERROR); 
			    break;
			}
			
			
			//Mensaje de respuesta con los ficheros
			DirMessage dmRespuestaFileList = new DirMessage(DirMessageOps.OPERATION_FILELISTOK);
			dmRespuestaFileList.setFileList(allFiles);
			
			dmRespuesta = dmRespuestaFileList;
			System.out.println("Enviando listado con " + allFiles.size() + "ficheros publicados");
			
			//checkkkkkk
			System.out.println("Recibido OPERATION_FILELIST");
			System.out.println("Actualmente hay " + serverToFileList.size() + " servidores registrados");

			for (InetSocketAddress addr : serverToFileList.keySet()) {
			    ArrayList<FileInfo> list = serverToFileList.get(addr);
			    System.out.println("- " + addr + " publica " + list.size() + " ficheros");
			    for (FileInfo f : list) {
			        System.out.println("  - " + f.fileName + " (" + f.fileSize + " bytes)");
			    }
			}

			
			break;
		}
		
		
		
		case DirMessageOps.OPERATION_DOWNLOAD: {
			String requestedSubstring = dmDatos.getFileNameSubstring(); //subcadena
			ArrayList<InetSocketAddress> matchingServers = new ArrayList<>(); //servidores que tengan fichero con esa subcadena

			System.out.println("SERVIDOR: Procesando DOWNLOAD para subcadena: " + requestedSubstring);
			
			//con el primer bucle recorremos los servidores, y con el segundo, los ficheros de ese servidor, 
			//si hay uno cuya subcadena coincide, lo tomamos, vamos al break, y pasamos al siguiente servidor
			
			// Buscar servidores con al menos un fichero que contenga la subcadena
			for (Map.Entry<InetSocketAddress, ArrayList<FileInfo>> entry : serverToFileList.entrySet()) { //recorre servidores
				for (FileInfo f : entry.getValue()) {		// recorre ficheros en el servidoe
					if (f.fileName.contains(requestedSubstring)) {
						matchingServers.add(entry.getKey());
						break; // Ya sabemos que este servidor contiene el fichero con la subcadena, pasamos al siguiente
					}
				}
			}

			if (matchingServers.isEmpty()) {
				dmRespuesta = new DirMessage(DirMessageOps.OPERATION_DOWNLOADERROR);
				System.out.println("SERVIDOR: No hay servidores que compartan ficheros con la subcadena '" + requestedSubstring + "'");
			} else {
				dmRespuesta = new DirMessage(DirMessageOps.OPERATION_DOWNLOADOK);
				dmRespuesta.setServerList(matchingServers);
				System.out.println("SERVIDOR: Encontrados " + matchingServers.size() + " servidores con ficheros que coinciden.");
			}
			break;
		}
		

		default:
			System.err.println("Unexpected message operation: \"" + operation + "\"");
			System.exit(-1);
		}

		/*
		 * (Boletín MensajesASCII) Convertir a String el objeto DirMessage
		 * (msgToSend) con el mensaje de respuesta a enviar, extraer los bytes en que se
		 * codifica el string y finalmente enviarlos en un datagrama
		 */

			String mRespuesta = dmRespuesta.toString();
			
			byte[] bRespuesta = mRespuesta.getBytes();
			
			DatagramPacket dpRespuesta = new DatagramPacket(bRespuesta, bRespuesta.length, pkt.getSocketAddress());
			socket.send(dpRespuesta);
		} catch (Exception e)
		{
			System.err.println("Error procesando datagrama");
			e.printStackTrace();
		}
	}
}
