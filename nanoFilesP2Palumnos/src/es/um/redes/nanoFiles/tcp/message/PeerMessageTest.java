package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
		int chunkSize1 = 512;
		msgGetChunk.setFileName(archivo1);
		msgGetChunk.setChunkNumber(chunkNumber1);
		msgGetChunk.setChunkSize(chunkSize1);
		
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
		System.out.println("Mensaje GET_CHUNK leído con éxito");
		System.out.println("Opcode: " + PeerMessageOps.opcodeToOperation(msgGetChunkLeido.getOpcode()));
		System.out.println("File name: " + msgGetChunkLeido.getFileName());
		System.out.println("Chunk number: " + msgGetChunkLeido.getChunkNumber());
		System.out.println("Chunk size: " + msgGetChunkLeido.getChunkSize());
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
		byte[] data = {'A', ' ', 'B', ' ', 'C', ' ', 'D'};   
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
		dis2.close();
		
		// ====== //
		// CHEECK //
		// ====== //
		
		if (msgSendChunk.getOpcode() == msgSendChunkLeido.getOpcode()) {
			System.out.println("Mensaje SEND_CHUNK leído con éxito");
			System.out.println("Opcode: " + PeerMessageOps.opcodeToOperation(msgSendChunkLeido.getOpcode()));
			System.out.println("File name: " + msgSendChunkLeido.getFileName());
			System.out.println("Chunk number: " + msgSendChunkLeido.getChunkNumber());
			System.out.println("Data (como bytes): " + Arrays.toString(msgSendChunkLeido.getChunkData()) + " (Código ASCII de cada carácter)"); //imprime los ascii de las letras
			System.out.println("Data (como texto): " + new String(msgSendChunkLeido.getChunkData()));
		} else {
			System.err.println("Opcode doesn't match");
		}
		
		// =================== //
		// TEST FILE_NOT_FOUND //
		// =================== //
		
		System.out.println();
		System.out.println("*** Test FILE_NOT_FOUND ***");
		System.out.println();
		
		// Crear PeerMessage con opcode FILE_NOT_FOUND
		PeerMessage msgFileNotFound = new PeerMessage(PeerMessageOps.FILE_NOT_FOUND);
		
		//Crear archivo para escribir en él
		File f3 = new File("file_not_found.bin");
		
		//Preparamos el fichero para escribir bytes en él
		FileOutputStream fos3 = new FileOutputStream(f3);
		
		// Encapsulamiento para escribir poder escribir datos primitivos en él
		DataOutputStream dos3 = new DataOutputStream(fos3);
		
		//Coodificamos el objeto PeerMessage campo a campo y lo escribimos al archivo
		msgFileNotFound.writeMessageToOutputStream(dos3);
		
		//cerramos el stream para guardar correctamente
		dos3.close();
		
		// ===================== //
		// Reconstruimos mensaje //
		// ===================== //
		
		//Abrimos el archivo recibido
		FileInputStream fis3 = new FileInputStream(f3);

		// Encapsulamos para poder leer datos primitivos (int, string...) y no bytes directamente
		DataInputStream dis3 = new DataInputStream(fis3);
		
		// Leemos el mensaje binario y lo decodificamos -> reconstruir en PeerMessage
		PeerMessage msgFileNotFoundLeido = PeerMessage.readMessageFromInputStream(dis3);
		
		//cerrar stream
		dis3.close();
		
		if (msgFileNotFound.getOpcode() == msgFileNotFoundLeido.getOpcode()) {
			System.out.println("Mensaje FILE_NOT_FOUND leído con éxito");
			System.out.println("Opcode: " + PeerMessageOps.opcodeToOperation(msgFileNotFoundLeido.getOpcode()));
		} else {
			System.err.println("Opcode doesn't match");
		}
		
		// ===================== //
		// TEST FILE_INFO_REQUEST//
		// ===================== //
		System.out.println();
		System.out.println("*** Test FILE_INFO_REQUEST ***");
		System.out.println();

		//Crear PeerMessage con el mensaje FILE_INFO_REQUEST
		PeerMessage msgInfoReq = new PeerMessage(PeerMessageOps.FILE_INFO_REQUEST);
		
		//Nombre del fichero del que queremos informacion
		msgInfoReq.setFileName("miFichero.txt");
		
		//Creamos archivo para escrbir en él
		File f4 = new File("file_info_request.bin");
		
		//Preparamos el fichero para escribir bytes en él
		FileOutputStream fos4 = new FileOutputStream(f4);
		
		//Encapsulamos para escribir datos primitivos
		DataOutputStream dos4 = new DataOutputStream(fos4);
		
		//coodificamos a binario y enviamos
		msgInfoReq.writeMessageToOutputStream(dos4);
		
		//cerramos el stream
		dos4.close();
		
		// ===================== //
		// Reconstruimos mensaje //
		// ===================== //
		
		//Abrimos el archivo recibido
		FileInputStream fis4 = new FileInputStream(f4);
		
		//Encapsulamos para poder leer datos primitivos y no bytes
		DataInputStream dis4 = new DataInputStream(fis4);
		
		//Leemos y reconstruimos el mensaje, decodificamos
		PeerMessage msgInfoReqLeido = PeerMessage.readMessageFromInputStream(dis4);
		
		//cerar stream
		dis4.close();

		// ======== //
		// CHEEEECK //
		// ======== //
		
		if (msgInfoReq.getOpcode() == msgInfoReqLeido.getOpcode()) {
			System.out.println("Mensaje FILE_INFO_REQUEST leído con éxito");
			System.out.println("Opcode: " + PeerMessageOps.opcodeToOperation(msgInfoReqLeido.getOpcode()));
			System.out.println("File name: " + msgInfoReqLeido.getFileName());
		} else {
			System.err.println("Opcode doesn't match");
		}

		
		// ====================== //
		// TEST FILE_INFO_RESPONSE//
		// ====================== //
		System.out.println();
		System.out.println("*** Test FILE_INFO_RESPONSE ***");
		System.out.println();
		
		//Crear PeerMessage con mensaje FILE_INFO_RESPONSE
		PeerMessage msgInfoResp = new PeerMessage(PeerMessageOps.FILE_INFO_RESPONSE);
		
		// Datos a enviar, nombre, tamaño, hash -> ejemplo
		msgInfoResp.setFileName("miFichero.txt");
		msgInfoResp.setFileSize(2048);
		msgInfoResp.setFileHash("aassddff");
		
		//Creamos fichero para escribir en él
		File f5 = new File("file_info_response.bin");
		
		//Preparamos fichero para escribir bytes
		FileOutputStream fos5 = new FileOutputStream(f5);
		
		//Encapsulamos para poder escribir tipos primitivos en él y no bytes
		DataOutputStream dos5 = new DataOutputStream(fos5);
		
		//Codificamos y enviamos
		msgInfoResp.writeMessageToOutputStream(dos5);
		
		//Cerrar stream
		dos5.close();
		
		// ===================== //
		// Reconstruimos mensaje //
		// ===================== //
		
		//Abrimos el archivo recibido
		FileInputStream fis5 = new FileInputStream(f5);
		
		//Encapsulamos para poder leer datos primitivos y no bytes
		DataInputStream dis5 = new DataInputStream(fis5);
		
		//Leemos y reconstruimos el mensaje, decodificamos
		PeerMessage msgInfoRespLeido = PeerMessage.readMessageFromInputStream(dis5);
		dis5.close();

		// ====== //
		// CHEECK //
		// ====== //
		
		if (msgInfoResp.getOpcode() == msgInfoRespLeido.getOpcode()) {
			System.out.println("Mensaje FILE_INFO_RESPONSE leído con éxito");
			System.out.println("Opcode: " + PeerMessageOps.opcodeToOperation(msgInfoRespLeido.getOpcode()));
			System.out.println("File name: " + msgInfoRespLeido.getFileName());
			System.out.println("File size: " + msgInfoRespLeido.getFileSize());
			System.out.println("File hash: " + msgInfoRespLeido.getFileHash());
		} else {
			System.err.println("Opcode doesn't match");
		}

		
	}
		
}


