package es.um.redes.nanoFiles.logic;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.application.NanoFiles;



import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicP2P {
	/*
	 * Se necesita un atributo NFServer que actuará como servidor de ficheros
	 * de este peer
	 */
	private NFServer fileServer = null;




	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean startFileServer() {
		boolean serverRunning = false;
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		if (fileServer != null) {
			System.err.println("File server is already running");
		} else {

			/*
			 * (Boletín Servidor TCP concurrente) Arrancar servidor en segundo plano
			 * creando un nuevo hilo, comprobar que el servidor está escuchando en un puerto
			 * válido (>0), imprimir mensaje informando sobre el puerto de escucha, y
			 * devolver verdadero. Las excepciones que puedan lanzarse deben ser capturadas
			 * y tratadas en este método. Si se produce una excepción de entrada/salida
			 * (error del que no es posible recuperarse), se debe informar sin abortar el
			 * programa
			 * 
			 */
			
			//Arrancamos servidor TCP en segundo plano, en otro hilo, para que el hilo
			//principal pueda seguir aceptando comandos en el shell
			
			try {
				System.out.println("Iniciando servidor de ficheros...");
				fileServer = new NFServer();  // Instancia el servidor (NFerver)

				int port = fileServer.getListeningPort(); // 10000
				if (port > 0) {
					//Creamos un hilo (Thread) que ejecuta el metodo run() de NFServer
					// El run() contiene un accept() dentro de un bucle para que el 
					//servidor quede escuchando conexiones
					Thread serverThread = new Thread(() -> {
						fileServer.run();  // Corre el servidor en segundo plano
					});
					serverThread.start();  //lanzar hilo en 2º plano
					System.out.println("Servidor de ficheros en marcha en el puerto TCP " + port);
					serverRunning = true;
				} else {
					System.err.println("Puerto inválido al iniciar NFServer");
				}
			} catch (IOException e) {
				System.err.println("No se pudo iniciar el servidor de ficheros: " + e.getMessage());
				fileServer = null;
			}


		}
		return serverRunning;

	}

	protected void testTCPServer() {
		assert (NanoFiles.testModeTCP);
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		assert (fileServer == null);
		try {

			fileServer = new NFServer();
			/*
			 * (Boletín SocketsTCP) Inicialmente, se creará un NFServer y se ejecutará su
			 * método "test" (servidor minimalista en primer plano, que sólo puede atender a
			 * un cliente conectado). Posteriormente, se desactivará "testModeTCP" para
			 * implementar un servidor en segundo plano, que se ejecute en un hilo
			 * secundario para permitir que este hilo (principal) siga procesando comandos
			 * introducidos mediante el shell.
			 */
			fileServer.test();
			// Este código es inalcanzable: el método 'test' nunca retorna...
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Cannot start the file server");
			fileServer = null;
		}
	}

	public void testTCPClient() {

		assert (NanoFiles.testModeTCP);
		/*
		 * (Boletín SocketsTCP) Inicialmente, se creará un NFConnector (cliente TCP)
		 * para conectarse a un servidor que esté escuchando en la misma máquina y un
		 * puerto fijo. Después, se ejecutará el método "test" para comprobar la
		 * comunicación mediante el socket TCP. Posteriormente, se desactivará
		 * "testModeTCP" para implementar la descarga de un fichero desde múltiples
		 * servidores.
		 */

		try {
			NFConnector nfConnector = new NFConnector(new InetSocketAddress(NFServer.PORT));
			nfConnector.test();
		} catch (IOException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList       La lista de direcciones de los servidores a
	 *                                los que se conectará
	 * @param targetFileNameSubstring Subcadena del nombre del fichero a descargar
	 * @param localFileName           Nombre con el que se guardará el fichero
	 *                                descargado
	 */
	
	
	
	
	protected boolean downloadFileFromServers(InetSocketAddress[] serverAddressList, String targetFileNameSubstring,
			String localFileName) {
		System.out.println("Entrando en el método downloadFileFromServe");
		boolean downloaded = false; 
		
		try {
			// lista de peers vacios, nadie tiene el fichero buscado
			if (serverAddressList.length == 0) {
				System.err.println("* Cannot start download - No list of server addresses provided");
				return false;
			}

			// fichero buscado ya existe. Evitamos sobreescribir el archivo
			File localFile = new File(localFileName);
			if (localFile.exists()) {
				System.err.println("[P2P] File already exists: " + localFileName);
				return false;
			}

			// Conectar al 1º peer y pedir FILE_INFO
			NFConnector infoConnector = new NFConnector(serverAddressList[0]);
			PeerMessage info = infoConnector.requestFileInfo(targetFileNameSubstring);
			
			//mini debug
			
			if (info != null) {
			    System.out.println("[P2P] FILE_INFO recibido:");
			    System.out.println(" - Nombre: " + info.getFileName());
			    System.out.println(" - Tamaño: " + info.getFileSize());
			    System.out.println(" - Hash: " + info.getFileHash());
			} else {
			    System.err.println("[P2P] FILE_INFO no recibido o inválido");
			}
			
			
			infoConnector.close();

			if (info == null || info.getOpcode() != PeerMessageOps.FILE_INFO_RESPONSE) {
				System.err.println("[P2P] No se pudo obtener información del archivo del primer servidor");
				return false;
			}

			String fileName = info.getFileName();
			String expectedHash = info.getFileHash();
			long fileSize = info.getFileSize();

			int numPeers = serverAddressList.length;
			int chunkSize = (int) Math.ceil((double) fileSize / numPeers);

			System.out.println("[P2P] Iniciando descarga de: " + fileName);
			System.out.println("[P2P] Tamaño total: " + fileSize + " bytes");
			System.out.println("[P2P] Hash esperado: " + expectedHash);
			System.out.println("[P2P] Chunks: " + numPeers + " (chunkSize: " + chunkSize + ")");

			System.out.println("[DEBUG] Guardando fichero en: " + localFile.getAbsolutePath());

			
			// Preparamos archivo de salida
			RandomAccessFile raf = new RandomAccessFile(localFile, "rw");

			// Mapa resumen de descargas por peer
			Map<InetSocketAddress, Integer> resumenChunks = new HashMap<>();

			// pedir chunks
			for (int i = 0; i < numPeers; i++) {
				try {
					InetSocketAddress peer = serverAddressList[i];
					NFConnector connector = new NFConnector(peer);

					// Crear petición GET_CHUNK
					PeerMessage request = new PeerMessage(PeerMessageOps.GET_CHUNK);
					request.setFileName(fileName);
					request.setChunkNumber(i);
					request.setChunkSize(chunkSize);

					// Enviar petición
					request.writeMessageToOutputStream(connector.getDos());
					connector.getDos().flush();

					// Leer respuesta
					PeerMessage response = PeerMessage.readMessageFromInputStream(connector.getDis());

					if (response.getOpcode() == PeerMessageOps.SEND_CHUNK) {
						byte[] chunkData = response.getChunkData();
						long offset = (long) i * chunkSize;

						raf.seek(offset);
						raf.write(chunkData);

						System.out.println("[P2P] Chunk #" + i + " recibido desde " + peer);
						resumenChunks.put(peer, chunkData.length); // Resumen
					} else {
						System.err.println("[P2P] Peer " + peer + " devolvió FILE_NOT_FOUND o error");
					}

					connector.close();

				} catch (IOException e) {
					System.err.println("[P2P] Error con el peer " + serverAddressList[i] + ": " + e.getMessage());
				}
			}

			raf.close();

			// Verificar integridad
			String downloadedHash = FileDigest.computeFileChecksumString(localFile.getPath());

			if (downloadedHash.equals(expectedHash)) {
				System.out.println("[P2P] Descarga completada correctamente y verificada");
				downloaded = true;

				System.out.println("[P2P] Resumen de descarga:");
				for (Map.Entry<InetSocketAddress, Integer> entry : resumenChunks.entrySet()) {
					System.out.println(" - " + entry.getKey() + " -> " + entry.getValue() + " bytes");
				}

			} else {
				System.err.println("[P2P] ERROR: Hash no coincide. Fichero corrupto o incompleto");
				localFile.delete();
			}

		} catch (IOException e) {
			System.err.println("[P2P] Error general durante la descarga: " + e.getMessage());
		}

		return downloaded;
	}



		
		/*
		 * Crear un objeto NFConnector distinto para establecer una conexión TCP
		 * con cada servidor de ficheros proporcionado, y usar dicho objeto para
		 * descargar trozos (chunks) del fichero. Se debe comprobar previamente si ya
		 * existe un fichero con el mismo nombre (localFileName) en esta máquina, en
		 * cuyo caso se informa y no se realiza la descarga. Se debe asegurar que el
		 * fichero cuyos datos se solicitan es el mismo para todos los servidores
		 * involucrados (el fichero está identificado por su hash). Una vez descargado,
		 * se debe comprobar la integridad del mismo calculando el hash mediante
		 * FileDigest.computeFileChecksumString. Si todo va bien, imprimir resumen de la
		 * descarga informando de los trozos obtenidos de cada servidor involucrado. Las
		 * excepciones que puedan lanzarse deben ser capturadas y tratadas en este
		 * método. Si se produce una excepción de entrada/salida (error del que no es
		 * posible recuperarse), se debe informar sin abortar el programa
		 */



	
	
	
	
	



	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	protected int getServerPort() {
		int port = 0;
		if (fileServer != null) {
			return fileServer.getListeningPort();
		}



		return port;
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	
	protected void stopFileServer() {
	    if (fileServer != null) {
	        try {
	            fileServer.stop();  // <-- ahora te explico cómo hacer el stop() en NFServer
	            fileServer = null;
	            System.out.println("[P2P] Servidor de ficheros detenido correctamente.");
	        } catch (IOException e) {
	            System.err.println("[P2P] Error al detener el servidor: " + e.getMessage());
	        }
	    } else {
	        System.err.println("[P2P] No hay servidor de ficheros en ejecución.");
	    }
	}

	protected boolean serving() {
		boolean result = false;



		return result;

	}

	protected boolean uploadFileToServer(FileInfo matchingFile, String uploadToServer) {
		boolean result = false;



		return result;
	}

}
