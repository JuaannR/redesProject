package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {


	/*
	 * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */
	
	
	private byte opcode;	
	
	// get_chunk y send_chunk
	private String fileName;
	private int chunkNumber;
	
	// solo send_chuck
	private byte[] chunkData;
	
	//file_not_found solo necesita opcode

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}

	/*
	 * TODO: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	
	//getters/setters 
	public byte getOpcode() {
		return opcode;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String nombre) {
		if (opcode != PeerMessageOps.GET_CHUNK && opcode != PeerMessageOps.SEND_CHUNK) {
			throw new RuntimeException("Filename is not valid for this message type");
		}
		fileName = nombre;
	}
	
	public int getChunkNumber() {
		return chunkNumber;
	}
	
	public void setChunkNumber(int numero) {
		if (opcode != PeerMessageOps.GET_CHUNK && opcode != PeerMessageOps.SEND_CHUNK) {
			throw new RuntimeException("Chunknumber is not valid for this message type");
		}
		chunkNumber = numero;
	}
	
	public byte[] getChunkData() {
		return chunkData;
	}
	
	public void setChunkData(byte[] data) {
		if (opcode != PeerMessageOps.SEND_CHUNK) {
			throw new RuntimeException("Chunkdata is not valid for this message type");
		}
		chunkData = data;
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto DirMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
		
		// Lee mensaje binario recibido por socket TCP
		// Interpreta el contenido leido del mensaje  según el opcode 
		//  y reconstruye un objeto PeerMessage con los campos rellenos
		// usa DataInputStream para leer datos binarios
		
		
		byte opcode = dis.readByte();	// Lee 1º byte del mensaje (getchunk, seendchunk...)
		PeerMessage message = new PeerMessage(opcode); 
		
		switch (opcode) {
			
		case PeerMessageOps.GET_CHUNK: {
			
			//Leer longiutud del nombre del fichero y reservar array con esa longitud
			int fileNameLength = dis.readInt();
			byte[] fileNameBytes = new byte[fileNameLength];
			
			// se leen todos los bytes del nombre del fichero y se convierten a string
			dis.readFully(fileNameBytes);
			String fileName = new String(fileNameBytes);
			
			//Leer numero de chunk solicitado
			int chunkNum = dis.readInt();
			
			//Metemos los datos en el PeerMessage 
			message.setFileName(fileName);
			message.setChunkNumber(chunkNum);
			break;
		}
		
		case PeerMessageOps.SEND_CHUNK: {
			// de bytes a int, string
			
			//Leer longitud del nombre del fichero y reservar array con su longitud
			int fileNameLength = dis.readInt();
			byte[] fileNameBytes = new byte[fileNameLength];
			
			// se leen todos los bytes del nombre del fichero y se convierte a string
			dis.readFully(fileNameBytes);
			String fileName = new String(fileNameBytes);
			
			int chunkNum = dis.readInt();  //leer número de chunk
			int dataLength = dis.readInt();  //leer tamaño de los bytes del chunk
			byte[] data = new byte[dataLength];
			dis.readFully(data);  //leer contenido real del chunk
			
			// Lo metemos al PeerMessage
			message.setFileName(fileName);
			message.setChunkNumber(chunkNum);
			message.setChunkData(data);
			break;
		}



		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para leer un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */
		
		// Codifica y envia mensaje binario a través de socket TCP
		// Convierte un objeto PeerMessage en secuencia de bytes según su opcode 
		// y enviarlos 
		// DataOutputStream para escribir mensajes binarios

		dos.writeByte(opcode); //identifica tipo de mensaje
		switch (opcode) {
		
		//de int, string... a bytes para poder enviar
		case PeerMessageOps.GET_CHUNK: {
			//se codifica nombre del fichero a bytes
			byte[] fileNameBytes = fileName.getBytes();
			dos.writeInt(fileNameBytes.length);  //longitud nombre
			dos.write(fileNameBytes);  //bytes del nombre
			dos.writeInt(chunkNumber);  //byte del chunk
			break;
		}
		
		case PeerMessageOps.SEND_CHUNK: {
			//se codifica nombre del fichero a bytes
			byte[] fileNameBytes = fileName.getBytes();
			dos.writeInt(fileNameBytes.length); //longitud nombre
			dos.write(fileNameBytes);	//bytes nombre
			dos.writeInt(chunkNumber);	//bytes chunk
			dos.writeInt(chunkData.length);  //longitud datos
			dos.write(chunkData);	//bytes datos
			break;
		}

		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}
}





/*
El peer A manda un get_chunk a un peer B, entonces tiene que 
usar la función write en el caso de get_chunk.
El peer B lo recibe, entonces para entenderlo, debe usar la 
función read en el caso de get_chunk, y crea un mensaje send_chunk, 
para la cual debe usar la función write.
El peer A recibe este send_chunk, por lo que debe usar el método read para entenderlo

*/
