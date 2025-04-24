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

	public static final int PORT = 10000;



	private ServerSocket serverSocket = null;

	///////////////////////////////////////////////////////////////////////
	public NFServer() throws IOException {
		/*
		 * TODO: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
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
/////////////////////////////////////////////////////////////////////////////
	

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
			 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
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
	
	@Override
	public void run() {
		System.out.println("[NFServer] run(), esperando conexiones");
		
		while(true) {
			try {
			//Esperar conexión del cliente
			Socket cliente = serverSocket.accept();
            System.out.println("[NFServer] Cliente conectado desde " + cliente.getInetAddress());
            
            //Crear hilo para manejar al cliente conectado
            Thread t = new Thread(() -> serveFilesToClient(cliente));
            t.start();
            
			} catch (IOException e) {
	            System.err.println("[NFServer] Error al aceptar conexión: " + e.getMessage());
			}
			
		}
		/*
		 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Al establecerse la conexión con un peer, la
		 * comunicación con dicho cliente se hace en el método
		 * serveFilesToClient(socket), al cual hay que pasarle el socket devuelto por
		 * accept
		 */
		/*
		 * TODO: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
		 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
		 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
		 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
		 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
		 * más de un cliente conectado a este servidor.
		 */




	}
	/*
	 * TODO: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */




	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */
	
	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: (Boletín SocketsTCP) Crear dis/dos a partir del socket
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Mientras el cliente esté conectado, leer mensajes
		 * de socket, convertirlo a un objeto PeerMessage y luego actuar en función del
		 * tipo de mensaje recibido, enviando los correspondientes mensajes de
		 * respuesta.
		 */
		/*
		 * TODO: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
		 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
		 * compartidos. Los ficheros compartidos se pueden obtener con
		 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
		 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
		 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
		 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
		 * de su hash completo.
		 */
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
				
				//procesar mensaje recibido, solo deberia ser GET_CHUNK
				//if(opcode != PeerMessageOps.GET_CHUNK) {
				//	System.err.println("[NFServer] Opcode no soportado: " + request.getOpcode());
				//	break;
				//}
				
				if(opcode == PeerMessageOps.GET_CHUNK) {
				
				System.out.println("[NFServer] Petición GET_CHUNK recibida:");
				System.out.println(" - Fichero: " + request.getFileName());
				System.out.println(" - Chunk #: " + request.getChunkNumber());
				System.out.println(" - Chunk Size: " + request.getChunkSize());
				}
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
				response.setFileName(encontrado.fileName);
				response.setChunkNumber(chunkNumber);
				response.setChunkData(chunkData);
				
				response.writeMessageToOutputStream(dos);
				dos.flush();
				
				System.out.println("[NFServer] Chunk enviado correctamente (" + bytesToRead + " bytes)");
				
				
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


/*

public static void serveFilesToClient(Socket socket) {
    try {
        System.out.println("[NFServer] Cliente conectado desde " + socket.getInetAddress());
		
		//Flujos de entrada y salida TCP
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
	
		// Mientras el cliente este conectado
        while (true) {
        	// leer mensaje entrante
            PeerMessage request = PeerMessage.readMessageFromInputStream(dis);
            byte opcode = request.getOpcode();
		
			//procesar mensaje recibido
			
            if (opcode == PeerMessageOps.FILE_INFO_REQUEST) {
                System.out.println("[NFServer] FILE_INFO_REQUEST recibido para: " + request.getFileName());

                FileInfo[] files = NanoFiles.db.getFiles();
                FileInfo match = null;
                for (FileInfo f : files) {
                    if (f.getFileName().toLowerCase().contains(request.getFileName().toLowerCase())) {
                        match = f;
                        break;
                    }
                }

                PeerMessage response;
                if (match != null) {
                    response = new PeerMessage(PeerMessageOps.FILE_INFO_RESPONSE);
                    response.setFileName(match.fileName);
                    response.setFileSize(match.fileSize);
                    response.setFileHash(match.fileHash);
                    System.out.println("[NFServer] Enviando FILE_INFO_RESPONSE");
                } else {
                    response = new PeerMessage(PeerMessageOps.FILE_NOT_FOUND);
                    System.out.println("[NFServer] Archivo no encontrado, enviando FILE_NOT_FOUND");
                }

                response.writeMessageToOutputStream(dos);
                dos.flush();
                break;

            } else if (opcode == PeerMessageOps.GET_CHUNK) {
                System.out.println("[NFServer] GET_CHUNK recibido para: " + request.getFileName());

                FileInfo[] files = NanoFiles.db.getFiles();
                FileInfo match = null;
                for (FileInfo f : files) {
                    if (f.getFileName().toLowerCase().contains(request.getFileName().toLowerCase())) {
                        match = f;
                        break;
                    }
                }

                if (match == null) {
                    PeerMessage notFound = new PeerMessage(PeerMessageOps.FILE_NOT_FOUND);
                    notFound.writeMessageToOutputStream(dos);
                    dos.flush();
                    System.out.println("[NFServer] Archivo no encontrado para GET_CHUNK");
                    break;
                }

                File file = new File(match.getFilePath());
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                    int chunkNumber = request.getChunkNumber();
                    int chunkSize = request.getChunkSize();
                    long offset = (long) chunkNumber * chunkSize;

                    if (offset >= raf.length()) {
                        PeerMessage notFound = new PeerMessage(PeerMessageOps.FILE_NOT_FOUND);
                        notFound.writeMessageToOutputStream(dos);
                        dos.flush();
                        System.out.println("[NFServer] Chunk fuera de rango");
                        break;
                    }

                    int bytesToRead = (int) Math.min(chunkSize, raf.length() - offset);
                    byte[] chunkData = new byte[bytesToRead];
                    raf.seek(offset);
                    raf.readFully(chunkData);

                    PeerMessage response = new PeerMessage(PeerMessageOps.SEND_CHUNK);
                    response.setFileName(match.fileName);
                    response.setChunkNumber(chunkNumber);
                    response.setChunkData(chunkData);

                    response.writeMessageToOutputStream(dos);
                    dos.flush();

                    System.out.println("[NFServer] Chunk #" + chunkNumber + " enviado correctamente (" + bytesToRead + " bytes)");

                }
                break;

            } else {
                System.err.println("[NFServer] Opcode no soportado: " + opcode);
                break;
            }
        }

    } catch (IOException e) {
        System.err.println("[NFServer] Error en la conexión: " + e.getMessage());
    } finally {
        try {
            socket.close();
            System.out.println("[NFServer] Conexión cerrada");
        } catch (IOException e) {
            System.err.println("[NFServer] Error cerrando el socket");
        }
    }
}


*/