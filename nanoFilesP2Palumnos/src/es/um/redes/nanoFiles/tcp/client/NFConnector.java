package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;


//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	
	// Estos atributos servirán para enviar y recibir mensajes desde el socket TCP de forma cómoda
	// En vez de enviar/recibir bytes a mano, podremos usar métodos como writeInt() o readInt()
	private DataOutputStream dos;
	private DataInputStream dis;
	
	public DataOutputStream  getDos() {
		return dos;
	}
	
	public DataInputStream getDis() {
		return dis;
	}

	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		
		serverAddr = fserverAddr;
		
		/*
		 * (Boletín SocketsTCP) Se crea el socket a partir de la dirección del
		 * servidor (IP, puerto). La creación exitosa del socket significa que la
		 * conexión TCP ha sido establecida.
		 */
		
		//Establecer conexión TCP con el servidor
		socket = new Socket(serverAddr.getAddress(), serverAddr.getPort());
		System.out.println("[NFConnector] conectado a " + serverAddr);
		
		
		/*
		 * (Boletín SocketsTCP) Se crean los DataInputStream/DataOutputStream a
		 * partir de los streams de entrada/salida del socket creado. Se usarán para
		 * enviar (dos) y recibir (dis) datos del servidor.
		 */
		
		//Crear los streams de entrada y salida a partir del socket
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());


	}
	
	
	public PeerMessage requestFileInfo(String fileNameSubstring) throws IOException {
	    System.out.println("[NFConnector] Enviando FILE_INFO_REQUEST para: " + fileNameSubstring);

		
		//Crear mensaje para solicitar info
		PeerMessage request = new PeerMessage(PeerMessageOps.FILE_INFO_REQUEST);
		request.setFileName(fileNameSubstring);
		
		//Enviamos mensaje
		request.writeMessageToOutputStream(dos);
		dos.flush();
		
		 System.out.println("[NFConnector] Esperando FILE_INFO_RESPONSE...");
		
		//Respuesta
		PeerMessage response = PeerMessage.readMessageFromInputStream(dis);
		

	    System.out.println("[NFConnector] Respuesta recibida: opcode = " + response.getOpcode());

		
		//Comprobamos validez de la respuesta
		if (response.getOpcode() == PeerMessageOps.FILE_INFO_RESPONSE) {
			System.out.println("[NFConnector]: File info receive: " );
			System.out.println("Name: " + response.getFileName());
			System.out.println("Size: " + response.getFileSize() + " bytes");
			System.out.println("Hash: " + response.getFileHash());
			return response;
		} else {
			System.err.println("[NFConnector] Error: not valid response: " + response.getOpcode());
			return null;
		}
	}
	
	
	
	
	
	
	// Método para cerrar conexiones
	public void close() {
		try {
			if(dis != null && dos != null && socket != null) {
				dis.close();
				dos.close();
				socket.close();
				System.out.println("[NFConnector] Conexión cerrada correctamente");
		} 
	} catch (IOException e) {
		System.err.println("[NFConnector] Error cerrando conexión: " + e.getMessage());
	}
}
	
	
	
	

	public void test() {
		/*
		 * TODO: (Boletín SocketsTCP) Enviar entero cualquiera a través del socket y
		 * después recibir otro entero, comprobando que se trata del mismo valor.
		 */
		try {
			
			//=======================//
			//TEST CON NUMERTO ENTERO//
			//======================//
			
			int numero = 42;
			System.out.println("[NFConnector] Enviando número: " + numero);
			
			//enviamos el entero
			dos.writeInt(numero);
			dos.flush();  // fuerza envío 
			
			//=====================//
			//TEST CON PEER MESSAGE//
			//=====================//
			
			PeerMessage mensaje = new PeerMessage(PeerMessageOps.GET_CHUNK);
			mensaje.setFileName("test.txt");
			mensaje.setChunkNumber(7);
			mensaje.setChunkSize(1024);
			
			//enviamos el PeerMessage
			mensaje.writeMessageToOutputStream(dos);
			dos.flush();
			
			//=========================//
			//LEER RESPUESTA DEL ENTERO//
			//========================//
			
			//leemos respuesta
			int recibido = dis.readInt();
			System.out.println("Respuesta para el entero: ");
			System.out.println("[NFConnector] Número recibido del servidor: " + recibido);
			
			if(numero == recibido) {
				System.out.println("[NFConnector] Test OK: valores coinciden");
			} else {
				System.out.println("[NFConnector] Test FAIL: valores no coinciden");
			}
			
			//===============================//
			//LEER RESPUESTA DEL PEERMESSAGE//
			//=============================//
			
			PeerMessage respuesta = PeerMessage.readMessageFromInputStream(dis);
			System.out.println("Respuesta para el PeerMessage: ");
			System.out.println("[NFConnector] Respuesta del servidor:");
			System.out.println("- Opcode: " + PeerMessageOps.opcodeToOperation(respuesta.getOpcode()));
			System.out.println("- Fichero: " + respuesta.getFileName());
			System.out.println("- Chunk: " + respuesta.getChunkNumber());
			System.out.println("- Datos: " + new String(respuesta.getChunkData()));

			
		} catch (IOException e) {
			System.err.println("[NFConnector] Error durante test de conexión");
		}
	}





	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}



/*
El cliente va a mandar una solicud con el dos.writeInt(), a lo que toma las riendas el servirdor,
que acepta la peticion desde el metodo de test(), luego llama a serveFilesToClient,
que se encarga de interpretar el numero recibido y de reenviarlo,
a lo que toma las riendas el cliente de nuevo, que comprobará si el número recibido es el ha mandado
   */



/*
Para lanzar este test, hay que poner el testUDP de NanoFiles a true, lanzar el directorio, lanzar un cliente ejcutado un serve,
y, en otra consola, lanzar otro cliente ejcutando un download de alguno de los ficheros subidos, al estar a true el test, se 
realizará automáticamente este intercambio del entero definido en test(), concretamente, la variable numero.
*/