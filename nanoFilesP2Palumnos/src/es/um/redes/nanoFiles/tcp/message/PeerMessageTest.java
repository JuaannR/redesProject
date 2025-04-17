package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PeerMessageTest {

	public static void main(String[] args) throws IOException {
		
		// ============== //
		// TEST GET_CHUNK //
		// ============== //
		System.out.println("*** Test GET_CHUNK ***");
		System.out.println();
		
		//Creamos PeerMessage con mensaje getChunk
		// Quiero el chunk 4 del archivo "ejemplo.txt"
		PeerMessage msgGetChunk = new PeerMessage(PeerMessageOps.GET_CHUNK);
		String archivo1 = "get_chunk.txt";
		int chunkNumber1 = 4;
		msgGetChunk.setFileName(archivo1);
		msgGetChunk.setChunkNumber(chunkNumber1);
		
		//Creamos un archivo para escribir en él
		File f1 = new File("get_chunk_test.bin");
		
		// preparamos el archivo para escribir bytes en él
		FileOutputStream fos = new FileOutputStream(f1);
		
		// "encápsulamos" el FileOutputStream para poder escribir 
		// de forma sencilla en él (int, string...) y no bytes del tirón
		DataOutputStream dos = new DataOutputStream(fos);
		
		// Se codifica el objeto PeerMessage en binario 
		//campo a campo y se escribe al archivo
		msgGetChunk.writeMessageToOutputStream(dos);
		
		// cerramos el encapsulamiento para asegurarnos de que se guardan
		//correctamente los campos
		dos.close();
		
		// =====================================================
		// ** Ahora tenemos que "leer" este archivo BINARIO ** //
		// =====================================================
		
		//Abrimos el archivo recibido donde escribimos el mensaje
		FileInputStream fis = new FileInputStream(f1);
		
		// lo encapsulamos para poder leer una estructura sencilla
		// y no los bytes que contiene
		DataInputStream dis = new DataInputStream(fis);
		
		// Leemos el mensaje binario y lo decodificamos -> reconstruimos en un PeerMessage
		PeerMessage msgGetChunkLeido = PeerMessage.readMessageFromInputStream(dis);
		
		//cerramos el encapsulamiento
		dis.close();
		
		//==========
		// CHEEEECK
		//==========
		
		if (msgGetChunk.getOpcode() ==  msgGetChunkLeido.getOpcode()) {
		System.out.println("Mensje GET_CHUNK leído con éxito");
		System.out.println("Opcode: " + PeerMessageOps.opcodeToOperation(msgGetChunkLeido.getOpcode()));
		System.out.println("File name: " + msgGetChunkLeido.getFileName());
		System.out.println("Chunk number: " + msgGetChunkLeido.getChunkNumber());
		} else {
			System.err.println("Opcode doesn't match");
		}
		
		
		
		
		
		// =============== //
		// TEST SEND_CHUNK //
		// =============== //
		System.out.println();
		System.out.println("*** Test SEND_CHUNK ***");
		System.out.println();
		
		//Creamos el PeerMessage con el mensaje SEND_CHUNK
		// Te envio los datos del fichero "send_chunk.txt" en el chunk 8
		PeerMessage msgSendChunk = new PeerMessage(PeerMessageOps.SEND_CHUNK);
		String archivo2 = "send_chunk.txt";
		int chunkNumber2 = 8;
		byte[] data = {'A', 'B', 'C', 'D'};   // byte[] data = {0x41, 0x42, 0x43, 0x44};
		msgSendChunk.setFileName(archivo2);
		msgSendChunk.setChunkNumber(chunkNumber2);
		msgSendChunk.setChunkData(data);
		
		//Creamos archivo para escribir en él
		File f2 = new File("send_chunk_test.bin");
		
		//Preparamos el fichero para escribir bytes en él
		FileOutputStream fos2 = new FileOutputStream(f2);
		
		// Encapsulamiento para escribir poder escribir datos primitivos en él
		DataOutputStream dos2 = new DataOutputStream(fos2);
		
		//Coodificamos el objeto PeerMessage campo a campo y lo escribimos al archivo
		msgSendChunk.writeMessageToOutputStream(dos2);
		
		//cerramos el stream para guardar correctamente
		dos2.close();
		
		
		// ===================== //
		// Reconstruimos mensaje //
		// ===================== //
		
		//Abrimos el archivo recibido
		FileInputStream fis2 = new FileInputStream(f2);

		// Encapsulamos para poder leer datos primitivos (int, string...) y no bytes directamente
		DataInputStream dis2 = new DataInputStream(fis2);
		
		// Leemos el mensaje binario y lo decodificamos -> reconstruir en PeerMessage
		PeerMessage msgSendChunkLeido = PeerMessage.readMessageFromInputStream(dis2);
		
		//cerrar stream
		
		// ====== //
		// CHEECK //
		// ====== //
		
		if (msgSendChunk.getOpcode() == msgSendChunkLeido.getOpcode()) {
			System.out.println("Mensje SEND_CHUNK leído con éxito");
			System.out.println("Opcode: " + PeerMessageOps.opcodeToOperation(msgSendChunkLeido.getOpcode()));
			System.out.println("File name: " + msgSendChunkLeido.getFileName());
			System.out.println("Chunk number: " + msgSendChunkLeido.getChunkNumber());
			System.out.println("Data: " + msgSendChunkLeido.getChunkData());
		} else {
			System.err.println("Opcode doesn't match");
		}
		
		
	}
		
}


