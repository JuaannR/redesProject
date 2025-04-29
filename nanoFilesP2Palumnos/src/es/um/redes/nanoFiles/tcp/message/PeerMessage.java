package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class PeerMessage {

	
	private byte opcode;	
	
	// get_chunk y send_chunk
	private String fileName;  //file _info response
	private int chunkNumber;
	
	//solo get_chunk
	private int chunkSize;
	
	// solo send_chuck
	private byte[] chunkData;
	
	//file_not_found solo necesita opcode
	
	// file info request / response
	private long fileSize;
	private String fileHash;

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}

	
	//getters/setters 
	public byte getOpcode() {
		return opcode;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String nombre) {
		if (opcode != PeerMessageOps.GET_CHUNK && opcode != PeerMessageOps.SEND_CHUNK && opcode != PeerMessageOps.FILE_INFO_REQUEST && opcode != PeerMessageOps.FILE_INFO_RESPONSE) {
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
	
	public int getChunkSize() {
		return chunkSize;
	}
	
	public void setChunkSize(int size) {
		if (opcode != PeerMessageOps.GET_CHUNK) {
	        throw new RuntimeException("ChunkSize is not valid for this message type");
		}
		chunkSize = size;
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
	
	public long getFileSize() {
		return fileSize;
	}
	
	public void setFileSize(long tam) {
		if (opcode != PeerMessageOps.FILE_INFO_REQUEST && opcode != PeerMessageOps.FILE_INFO_RESPONSE) {
			throw new RuntimeException("fileSize not valid for this opetation");
		}
		fileSize = tam;
	}
	
	public String getFileHash() {
		return fileHash;
	}
	
	public void setFileHash(String hash) {
		if (opcode != PeerMessageOps.FILE_INFO_REQUEST && opcode != PeerMessageOps.FILE_INFO_RESPONSE) {
			throw new RuntimeException("fileHash not valid for this opetation");
	}
		fileHash = hash;
}

	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {

		
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
			
			//Leer tamaño del chunk solicitado
			int chunkSize = dis.readInt();
			
			//Metemos los datos en el PeerMessage 
			message.setFileName(fileName);
			message.setChunkNumber(chunkNum);
			message.setChunkSize(chunkSize);
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
		
		case PeerMessageOps.FILE_NOT_FOUND: {
			//No hay que hacer nada, solo necesitamos opcode que esta fuera del switch
			break;
		}
		
		case PeerMessageOps.FILE_INFO_REQUEST: {
			// leer longitud del nombre del fichero y reservar array con su longitus
			int fileNameLength = dis.readInt();
			byte[] fileNameBytes = new byte[fileNameLength];
			
			//se leen todos los bytes del nombre del fichero y se convierten a string
			dis.readFully(fileNameBytes);
			String fileName = new String(fileNameBytes);
			
			//Se mete al PeerMessage
			message.setFileName(fileName);
			break;
		}
		
		case PeerMessageOps.FILE_INFO_RESPONSE: {
			// leer longitud del nombre del fichero y reservar array con su longitus
			int fileNameLength = dis.readInt();
			byte[] fileNameBytes = new byte[fileNameLength];
			
			//se leen todos los bytes del nombre del fichero y se convierten a string
			dis.readFully(fileNameBytes);
			String fileName = new String(fileNameBytes);
			
			long fileSize = dis.readLong();  // leer tamFichero
			int hashLenght = dis.readInt();  //leer Longitud hash del fichero
			byte[] hashBytes = new byte[hashLenght];
			dis.readFully(hashBytes);  //leemos los bytes
			String fileHash = new String(hashBytes);  //y pasamos a string
			
			//Se mete al PeerMessage
			message.setFileName(fileName);
			message.setFileSize(fileSize);
			message.setFileHash(fileHash);
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
			dos.writeInt(fileNameBytes.length);  //longitud nombre en bytes
			dos.write(fileNameBytes);  //bytes del nombre
			dos.writeInt(chunkNumber);  //byte del chunk
			dos.writeInt(chunkSize);   //bytes tamaño chunk
			break;
		}
		
		case PeerMessageOps.SEND_CHUNK: {
			//se codifica nombre del fichero a bytes
			byte[] fileNameBytes = fileName.getBytes();
			dos.writeInt(fileNameBytes.length); //longitud nombre fichero en bytes
			dos.write(fileNameBytes);	//bytes nombre fichero
			dos.writeInt(chunkNumber);	//bytes chunk
			dos.writeInt(chunkData.length);  //longitud datos en bytes
			dos.write(chunkData);	//bytes datos
			break;
		}
		
		case PeerMessageOps.FILE_NOT_FOUND: {
			//No hay que hacer nada, solo necesitamos opcode que esta fuera del switch
			break;
		}
		
		case PeerMessageOps.FILE_INFO_REQUEST: {
			// codificamos nombre del fichero a bytes
			byte[] fileNameBytes = fileName.getBytes();
			dos.writeInt(fileNameBytes.length); //longitud nombre fichero en bytes
			dos.write(fileNameBytes);	//bytes nombre fichero
			break;
		}
		
		case PeerMessageOps.FILE_INFO_RESPONSE: {
			//coodificar nombre fichero y su hash a bytes
			byte[] fileNameBytes = fileName.getBytes();
			byte[] hashBytes = fileHash.getBytes();
			
			dos.writeInt(fileNameBytes.length);  //longitud nombre fichero en bytes
			dos.write(fileNameBytes);		// bytes nombre fichero
			dos.writeLong(fileSize);		//bytes tamaño fichero
			dos.writeInt(hashBytes.length); //longitud hash fichero en bytes
			dos.write(hashBytes);			//bytes hash fichero
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
para la cual debe usar la función write para enviarlo
El peer A recibe este send_chunk, por lo que debe usar el método read para 
decodificarlo y entenderlo
*/
