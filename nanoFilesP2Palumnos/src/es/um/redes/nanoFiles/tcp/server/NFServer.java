package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;




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
	public void run() {
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
		
		

	}




}