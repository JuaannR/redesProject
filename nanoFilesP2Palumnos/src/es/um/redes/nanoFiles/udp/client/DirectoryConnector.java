package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	
	private static final int DIRECTORY_PORT = 6868;  // Puerto de directorio
	private static final int TIMEOUT = 60000;  // Tiempo max espera para recibir respuesta, en ms
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;  //nº max intentos para obtener una respuesta a una solicitud enviada


	private DatagramSocket socket;	// Socket UDP usado para la comunicación con el directorio
	private InetSocketAddress directoryAddress;  // Dirección de socket del directorio
	private String directoryHostname;  // Nombre/IP del host donde se ejecuta el directorio


	public DirectoryConnector(String hostname) throws IOException {
		// Guardamos el string con el nombre/IP del host
		directoryHostname = hostname;
		/*
		 * (Boletín SocketsUDP) Convertir el string 'hostname' a InetAddress y
		 * guardar la dirección de socket (address:DIRECTORY_PORT) del directorio en el
		 * atributo directoryAddress, para poder enviar datagramas a dicho destino.
		 */  //hecho
		
		directoryAddress = new InetSocketAddress(InetAddress.getByName(hostname), DIRECTORY_PORT);
		
		/*
		 *  (Boletín SocketsUDP) Crea el socket UDP en cualquier puerto para enviar datagramas al directorio
		 */  //hecho
		this.socket = new DatagramSocket(0);

	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 */
	
	private byte[] sendAndReceiveDatagrams(byte[] requestData) {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		
		System.out.println("CLIENTE: Entrando en sendAndReceiveDatagrams()");
		System.out.println("CLIENTE: Socket local = " + socket.getLocalAddress() + ":" + socket.getLocalPort());

		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		/*
		 * (Boletín SocketsUDP) Enviar datos en un datagrama al directorio y
		 * recibir una respuesta. El array devuelto debe contener únicamente los datos
		 * recibidos, *NO* el búfer de recepción al completo.
		 */
	

	    // Intentamos enviar y recibir datagramas hasta que obtengamos una respuesta o agotemos los intentos
	    int attemptCount = 0;
	    boolean receivedResponse = false;
		
		 while (attemptCount < MAX_NUMBER_OF_ATTEMPTS && !receivedResponse) {
		        try {
		        	System.out.println("Cliente: Intento de envio + " + (attemptCount + 1));
		            // Enviar el datagrama al directorio
		            DatagramPacket paqueteEnvio = new DatagramPacket(requestData, requestData.length, directoryAddress);
		            System.out.println("Cliente: Enviando datagrama al directorio en " + directoryAddress);
		            System.out.println("Contenido enviado:\n" + new String(requestData));
		            socket.send(paqueteEnvio);

		            // Esperar una respuesta
		            DatagramPacket paqueteRespuesta = new DatagramPacket(responseData, responseData.length);
		            socket.setSoTimeout(TIMEOUT);  // Timeout para esperar la respuesta
		            
		            System.out.println("Esperando respuesta en puerto" + socket.getLocalPort());
		            socket.receive(paqueteRespuesta);  // Recibimos la respuesta
		            
		            
		            System.out.println("CLIENTE: Datagrama recibido desde " +
		                    paqueteRespuesta.getAddress() + ":" + paqueteRespuesta.getPort() +
		                    " con " + paqueteRespuesta.getLength() + " bytes");
		            
		            
		            // Procesamos la respuesta
		            response = new byte[paqueteRespuesta.getLength()];
		            System.arraycopy(paqueteRespuesta.getData(), paqueteRespuesta.getOffset(), response, 0, paqueteRespuesta.getLength());
		            System.out.println("CLIENTE: Contenido recibido:\n" + new String(response));

		            receivedResponse = true;  // Hemos recibido una respuesta correctamente

		        } catch (SocketTimeoutException e) {
		            // Si se agota el tiempo de espera, incrementamos el contador de intentos
		            System.err.println("Timeout reached, retrying... (" + (attemptCount + 1) + "/" + MAX_NUMBER_OF_ATTEMPTS + ")");
		            attemptCount++;  // Incrementamos el contador de intentos

		        } catch (IOException e) {
		            // Si ocurre un error de entrada/salida, mostramos el error y terminamos el programa
		            System.err.println("Error de entrada/salida: " + e.getMessage());
		            System.exit(-1);
		        }
		    }



			if (!receivedResponse) {
				System.err.println("CLIENTE: No se recibió ninguna respuesta tras " + MAX_NUMBER_OF_ATTEMPTS + " intentos.");
			} else {
				System.out.println("CLIENTE: Respuesta procesada correctamente");
			}
			
		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
		}
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 */
	public boolean testSendAndReceive() {
	    boolean success = false;

	    // Crear el mensaje "ping"
	    String miping = "ping";
	    byte[] requestData = miping.getBytes();
	    
	    // Enviar el datagrama y recibir la respuesta
	    byte[] bRespuesta = sendAndReceiveDatagrams(requestData);
	    System.out.println("Cliente: Enviando datagrama al directorio en " + directoryAddress);

	    
	    if (bRespuesta != null) {
	        // Convertir la respuesta en un String
	        String sRespuesta = new String(bRespuesta);
	        
	        // Comprobar que la respuesta empieza con "pingok"
	        if (sRespuesta.startsWith("pingok")) {
	            success = true;
	        }
	    }

	    return success;
	}




	public String getDirectoryHostname() {
		return directoryHostname;
	}


	public boolean pingDirectoryRaw() {
		//TEST 
		boolean success = false;
		
		//Se crea el mensaje
		String message = new String("ping&"+NanoFiles.PROTOCOL_ID);
		
		//Se convierte a bytes
		byte[] requestData = message.getBytes();
		
		//Se envia
		byte[] responseData = sendAndReceiveDatagrams(requestData);
		
		//Verificamos la respuesta y la procesamos
		if(responseData != null) {
			String responseMessage = new String(responseData);
				//Comprobamos si es welcome -> bien
				if(responseMessage.equals("welcome")) {
					System.out.println("Ping al direcotorio exitoso");
					success = true;
				} else {     //Si no es welcome mal
					System.err.println("Error: Respuesta inesperada del servidor" + responseMessage);
				}
		} else {			// Si no recibimos nada también mal
			System.err.println("Error: No se recibió respuesta del servidor");
		}
		return success;
	}

	
	public boolean pingDirectory() {
		boolean success = false;

		// mensaje ping con el protocol_id
		DirMessage dmPing = new DirMessage(DirMessageOps.OPERATION_PING);
		dmPing.setProtocolID(NanoFiles.PROTOCOL_ID);
		
		//pasamos a string y a bytes 
		String sPing = dmPing.toString();
		byte[] bPing = sPing.getBytes();
		
		System.out.println("Enviando mensaje...");
		System.out.println("Mensaje enviado:\n" + sPing);
		
		//envaimos y recibimos respuesta
		byte[] bRespuesta = sendAndReceiveDatagrams(bPing);
		
		String sRespuesta = new String(bRespuesta);
		DirMessage dmRespuesta = DirMessage.fromString(sRespuesta);
		
		if (bRespuesta == null) {
			System.err.println("No se recibió respuesta al mensaje serve");
			return false;
		}
		
		if (dmRespuesta.getOperation().equals(DirMessageOps.OPERATION_PINGOK)) {
			System.out.println("Conexión establecida correctamente");
			success = true;
		} else {
			System.err.println("Error al establecer conexión. Respuesta recibida: " + dmRespuesta.getOperation());
		}


		return success;
	}


	public boolean registerFileServer(int serverPort, FileInfo[] files) {
		boolean success = false;

		// Mensaje DirMessage de tipo serve
		
		DirMessage dmServe = new DirMessage(DirMessageOps.OPERATION_SERVE);
		
		List<FileInfo> fileList = new ArrayList<>();
		for (FileInfo f : files) {
			fileList.add(f);
		}
		
		dmServe.setFileList(fileList);
		dmServe.setPort(serverPort);
		
		// Convertir a string, y luego bytes para poder enviarlo
		String sServe = dmServe.toString();
		byte[] bServe = sServe.getBytes();
		
		//Enviar y recibir respuesta
		System.out.println("Enviando mensaje");
		System.out.println("Mensaje enviado:\n" + sServe);
		byte[] bRespuesta = sendAndReceiveDatagrams(bServe);
		if (bRespuesta == null) {
			System.err.println("No se recibió respuesta al mensaje serve");
			return false;
		}
		
		// Caso de si recibir respuesta, la procesamos

		String sRespuesta = new String(bRespuesta);
		DirMessage dmRespuesta = DirMessage.fromString(sRespuesta);

		
		if (dmRespuesta.getOperation().equals(DirMessageOps.OPERATION_SERVEOK)) {
			System.out.println("Ficheros de nf-shared compartidos exitosamente");
			success = true;
		} else {
			System.err.println("Error al compartir los ficheros de nf-shared. Carpeta vacía. Respuesta recibida: " + dmRespuesta.getOperation());
		}
		
		return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		FileInfo[] filelist = new FileInfo[0];
		
		//Se prepara el envio
		DirMessage request = new DirMessage(DirMessageOps.OPERATION_FILELIST);
		//Pasamos a string y bytes
		String requestString = request.toString();
		byte[] requestBytes = requestString.getBytes();
		
		System.out.println("CLIENTE: Enviando mensaje FILELIST:");
		System.out.println(requestString);
		
		//Y enviamos
		byte[] responseBytes = sendAndReceiveDatagrams(requestBytes);
		if (responseBytes == null) {
			System.err.println("No se recibió respuesta al mensaje filelist");
			return  null;
		}
		
		//Procesamos la respuesta
		//Se convierte a string y se parsea
		String responseString = new String(responseBytes);
		DirMessage response = DirMessage.fromString(responseString);
		
		System.out.println("CLIENTE: Recibida respuesta del servidor:");
		System.out.println(responseString);
		
		if (response.getOperation().equals(DirMessageOps.OPERATION_FILELISTOK)) {
			List<FileInfo> files = response.getFileList();
			filelist = files.toArray(new FileInfo[0]);
		} else {
			System.err.println("Error al obtener la lista de ficheros, no hay ficheros publicados. Respuesta: " + response.getOperation());
		}



		return filelist;
	}

	/**
	 * Método para obtener la lista de servidores que tienen un fichero cuyo nombre
	 * contenga la subcadena dada.
	 * 
	 * @filenameSubstring Subcadena del nombre del fichero a buscar
	 * 
	 * @return La lista de direcciones de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public InetSocketAddress[] getServersSharingThisFile(String filenameSubstring) {
		InetSocketAddress[] serversList = new InetSocketAddress[0];

		//Crear el mensaje DOWNLOAD con la subcadena
		DirMessage request = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD);
		request.setFileNameSubstring(filenameSubstring);

		//Se pasa a String y a bytes y se envia
		String requestString = request.toString();
		byte[] requestBytes = requestString.getBytes();
		System.out.println("CLIENTE: Enviando mensaje DOWNLOAD:\n" + requestString);
		
		byte[] responseBytes = sendAndReceiveDatagrams(requestBytes);
		
		
		if (responseBytes == null) {
			System.err.println("CLIENTE: No se recibió respuesta al mensaje DOWNLOAD");
			return serversList;
		}

		//Se procesa la respuesta
		//Pasamos de bytes a String y de String a DirMessage
		String responseString = new String(responseBytes);
		DirMessage response = DirMessage.fromString(responseString);
		System.out.println("CLIENTE: Recibida respuesta:\n" + responseString);

		if (response.getOperation().equals(DirMessageOps.OPERATION_DOWNLOADOK)) {
			List<InetSocketAddress> list = response.getServerList();
			serversList = list.toArray(new InetSocketAddress[0]);
			System.out.println("CLIENTE: Servidores encontrados: " + serversList.length);
		} else if (response.getOperation().equals(DirMessageOps.OPERATION_DOWNLOADERROR)) {
			System.err.println("CLIENTE: No se encontraron servidores que compartan ese fichero.");
		} else {
			System.err.println("CLIENTE: Respuesta inesperada: " + response.getOperation());
		}

		return serversList;
	}

	/**
	 * Método para darse de baja como servidor de ficheros.
	 * 
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y ha dado de baja sus ficheros.
	 */
	public boolean unregisterFileServer() {
		boolean success = false;




		return success;
	}




}
