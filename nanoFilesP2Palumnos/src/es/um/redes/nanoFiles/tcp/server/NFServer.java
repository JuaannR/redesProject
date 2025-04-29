package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;




public class NFServer implements Runnable {

	public static final int PORT = 0;



	private ServerSocket serverSocket = null;


	public NFServer() throws IOException {

		// crea un server socket y lo liga a un puerto libre del sistema
		this.serverSocket = new ServerSocket(PORT);
		System.out.println("[NFServer] Servidor creado y escuchando en puerto " + serverSocket.getLocalPort());
	}
	
	// método nuevo para escuhar el puerto
	public int getListeningPort() {
		if (serverSocket != null && serverSocket.isBound()) {
			return serverSocket.getLocalPort();
		}
		return 0;
	}
	

	public void stop() throws IOException {
	    if (serverSocket != null && !serverSocket.isClosed()) {
	        serverSocket.close();  // Esto lanza IOException en el accept(), provocando que el run() termine
	    }
	}

	
	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	
	
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out.println("[fileServerTestMode] NFServer running on " + serverSocket.getLocalSocketAddress() + ".");
		}

		while (true) {
			/*
			 * (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
			 * otros peers que soliciten descargar ficheros.
			 */
			try {
				Socket cliente = serverSocket.accept();
				System.out.println("[NFServer] Conexión aceptada de: " + cliente.getRemoteSocketAddress());

				//Creamos flujo de entrada y salida para comunicar con el cliente
				/*
				//En el servidor, se crean dentro del método, a diferencia que en el cliente
				//que son atributos de la clase. Esto se debe a que el cliente se mantiene
				//conectado y necesita enviar o recibir mensajes a lo largo del tiempo, mientras
				// que el servidor que acepta la comunicación, es solo para ese cliente. Se abre
				//el socket, se gestionan los mensajes, y se cierra al acabar. Es por ello que 
				// se crean en el método, se usan, y se descartan al acabar
				*/
				DataInputStream dis = new DataInputStream(cliente.getInputStream());
				DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());

				//===========//
				//LEER ENTERO//
				//==========//

				//Leemos entero enviado por el cliente
				int intRecibido = dis.readInt();
				System.out.println("[NFServer] Número recibido del cliente: " + intRecibido );

				//=============//
				//ENVIAR ENTERO//
				//============//

				//Enviar de vuelta el mismo número como respuesta al cliente
				dos.writeInt(intRecibido );
				dos.flush(); //forzar envio

				System.out.println("[NFServer] Número reenviado al cliente: " + intRecibido );


				//=================//
				//LEER PEER MESSAGE//
				//=================//

				//Leemos Peer Message enviado por el cliente (GET_CHUNK)
				PeerMessage recibido = PeerMessage.readMessageFromInputStream(dis);
				System.out.println("[NFServer] Mensaje PeerMessage recibido del cliente:");
				System.out.println(" - Opcode: " + PeerMessageOps.opcodeToOperation(recibido.getOpcode()));
				System.out.println(" - Fichero: " + recibido.getFileName());
				System.out.println(" - Chunk #: " + recibido.getChunkNumber());
				System.out.println(" - Chunk Size: " + recibido.getChunkSize());

				//Ahora tenemos que crear la respuesta SEND_CHUNK
				PeerMessage respuesta = new PeerMessage(PeerMessageOps.SEND_CHUNK);
				respuesta.setFileName(recibido.getFileName());
				respuesta.setChunkNumber(recibido.getChunkNumber());
				respuesta.setChunkData("HolaCliente".getBytes());  // Simulamos datos


				//===================//
				//ENVIAR PEER MESSAGE//
				//===================//
				respuesta.writeMessageToOutputStream(dos);
				dos.flush();

				//Cerrar conexión
				cliente.close();

			} catch (IOException e) {
				System.err.println("[NFServer] Error al aceptar la conexión");
			}
			/*
			 * TODO: (Boletín SocketsTCP) Tras aceptar la conexión con un peer cliente, la
			 * comunicación con dicho cliente para servir los ficheros solicitados se debe
			 * implementar en el método serveFilesToClient, al cual hay que pasarle el
			 * socket devuelto por accept.
			 */
		}
	}


	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	
	
	
	/**
	 * Método run del hilo principal del servidor.
	 * Este método queda a la escucha de conexiones entrantes.
	 * 
	 * Por cada conexión aceptada (con accept()), se lanza un nuevo hilo (NFServerThread)
	 * que se encarga de gestionar la comunicación con ese cliente de forma independiente.
	 */

	@Override
	public void run() {
		System.out.println("[NFServer] run(), esperando conexiones");
		
		while(true) {
			try {
			//Esperar conexión del cliente
			Socket cliente = serverSocket.accept();
            System.out.println("[NFServer] Cliente conectado desde " + cliente.getInetAddress());
            
            //Crear hilo para manejar al cliente conectado
            NFServerThread t = new NFServerThread(cliente);
            t.start();
            
			} catch (IOException e) {
	            System.err.println("[NFServer] Error al aceptar conexión: " + e.getMessage());
			}
			
		}



	}


	public static void serveFilesToClient(Socket socket) {

		try {
			System.out.println("[NFServer] Cliente conectado desde " + socket.getInetAddress());
			
			//Flujos de entrada y salida TCP
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
			// Mientras el cliente este conectado
			while(true) {
				// leer mensaje entrante
				PeerMessage request = PeerMessage.readMessageFromInputStream(dis);
				byte opcode = request.getOpcode();
				
				// Buscamos el 1º fichero de la lista que coincida con la subcadena
				// Busqueda insensible a mayúsculas/minúsculas con toLowerCase()
				
				FileInfo[] ficheros = NanoFiles.db.getFiles();
				FileInfo encontrado = null;
				for(FileInfo f : ficheros) {
					if(f.getFileName().toLowerCase().contains(request.getFileName().toLowerCase())) {
						encontrado = f;
						break;			//rompemos for para quedarnos con el 1º fichero en "encontrado"
					}
				}
				
				//recorremos la lista y no coincide ninguna subcadena -> FILE_NOT_FOUND
				if(encontrado == null) {
					System.out.println("[NFServer] Fichero no encontrado");
					PeerMessage notFound = new PeerMessage(PeerMessageOps.FILE_NOT_FOUND);
					notFound.writeMessageToOutputStream(dos);
					dos.flush();
					break;
				}
				
				
				if (opcode == PeerMessageOps.FILE_INFO_REQUEST) {
					System.out.println("[NFServer] Petición FILE_INFO_REQUEST recibida:");
					System.out.println("[NFServer] Nombre archivo: " + encontrado.getFileName());
					System.out.println("[NFServer] El archivo ocupa " + encontrado.getFileSize() + " bytes");
					
					PeerMessage response = new PeerMessage(PeerMessageOps.FILE_INFO_RESPONSE);
					response.setFileName(encontrado.getFileName());
					response.setFileHash(encontrado.getFileHash());
					response.setFileSize(encontrado.getFileSize());
					
					response.writeMessageToOutputStream(dos);
					dos.flush();
					break;
				}
				
				if(opcode == PeerMessageOps.GET_CHUNK) {
				
				System.out.println("[NFServer] Petición GET_CHUNK recibida:");
				System.out.println(" - Fichero: " + request.getFileName());
				System.out.println(" - Chunk #: " + request.getChunkNumber());
				System.out.println(" - Chunk Size: " + request.getChunkSize());
				

				
				// Calcular que data hay que leer (desde donde empezamos y acabamos)
				int chunkNumber = request.getChunkNumber();
				int chunkSize = request.getChunkSize();
				long offset = (long) chunkNumber * chunkSize;  //byte inical por el que empezaremos a leer
				
				// Leer datos del fichero
				File file = new File(encontrado.getFilePath());
				RandomAccessFile raf = new RandomAccessFile(file, "r");  //abrir archivo el cual hay que leer
				
				//offset caclualdo fuera del tamaño del archivo -> no hay chunk válido para leer
				if(offset >= raf.length()) {
					System.out.println("[NFServer] Chunk fuera de rango, enviando FILE_NOT_FOUND");
					PeerMessage notFound = new PeerMessage(PeerMessageOps.FILE_NOT_FOUND);
					notFound.writeMessageToOutputStream(dos);
					dos.flush();
					raf.close();
					break;
				}
				
				//colocar puntero lectura en el 1º byte a leer
				raf.seek(offset);
				
				
				//Leemos el chunk entero, o hasta que se acabe el archivo
				int bytesToRead = (int) Math.min(chunkSize, raf.length() - offset);
				byte[] chunkData = new byte[bytesToRead];
				raf.readFully(chunkData);
				raf.close();
				
				//Crear y enviar respuesta
				PeerMessage response = new PeerMessage(PeerMessageOps.SEND_CHUNK);
				response.setFileName(encontrado.getFileName());
				response.setChunkNumber(chunkNumber);
				response.setChunkData(chunkData);
				
				response.writeMessageToOutputStream(dos);
				dos.flush();
				
				System.out.println("[NFServer] Chunk enviado correctamente (" + bytesToRead + " bytes)");
				break;
				}
				
				}
				
			
			
		} catch (IOException e) {
			System.err.println("[NFServer] Error en la conexión con el cliente: " + e.getMessage());		
			
		} finally {
			try {
				socket.close();
				System.out.println("[NFServer] Conexión cerrada");	
			} catch (IOException e) {
				System.err.println("[NFServer] Error cerrando el socket");
			}
		}
		

	}




}

