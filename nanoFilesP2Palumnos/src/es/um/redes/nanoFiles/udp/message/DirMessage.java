package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.util.FileInfo;

/*
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	private static final String FIELDNAME_PROTOCOLID = "protocol";
	private static final String FIELDNAME_FILECOUNT = "filecount";
	private static final String FIELDNAME_FILENAME= "filename";
	private static final String FIELDNAME_SIZE= "size";
	private static final String FIELDNAME_HASH= "hash";
	private static final String FIELDNAME_PORT= "port";
	private static final String FIELDNAME_SUBSTRING = "name";
	private static final String FIELDNAME_SERVERCOUNT = "servercount";
	private static final String FIELDNAME_ADDRESS = "address";
	

	private String operation = DirMessageOps.OPERATION_INVALID;	//Tipo de mensaje, entre los tipos definidos en DirMessageOps
	
	private String protocolId; 	//Id protocolo, para comprobar compatibilidad del directorio -- ping	

	private List<FileInfo> fileList; 	//Lista de FileInfo para manejar la lista de archivos del comando filelist y serve
	
	private int port;	// Puerto del servidor de ficheros para manejar la subida de ficheros con el comando serve
	
	private String fileNameSubstring;	//string para indicar subcadena del nombre del fichero a buscar con download
	
	private List<InetSocketAddress> serverList;  //lista de direcciones IP:puerto donde el fihcero solicitado esta disponible (download)
	

	public DirMessage(String op) {
		operation = op;
	}

	/* Crear constructores para mensajes de diferentes tipos con 
	 * sus correspondientes argumentos (campos del mensaje)
	 * Crear setter/getter para los atributos del mensaje
	 */

	//getter y setter de fileList
	
	public List<FileInfo> getFileList() {
		return fileList;
	}
	
	public void setFileList(List<FileInfo> files) {
		if (!(operation.equals(DirMessageOps.OPERATION_FILELISTOK) || operation.equals(DirMessageOps.OPERATION_SERVE))) {
			throw new RuntimeException("Invalid operation for fileList or serve");
		}
		this.fileList = files;
	}
	
	//getter y setter de port (serve)
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int puerto) {
		if (!operation.equals(DirMessageOps.OPERATION_SERVE)) {
			throw new RuntimeException("Invalid operation for port");
		}
		port = puerto;
	}
	
	//getter y setter de fileNameSubstring y serverList (download)
	
	public String getFileNameSubstring() {
		return fileNameSubstring;
	}
	
	public void setFileNameSubstring(String descarga) {
		if (!operation.equals(DirMessageOps.OPERATION_DOWNLOAD)) {
			throw new RuntimeException("Invalid operation for fileNameSubstring");
		}
		fileNameSubstring = descarga;
	}
	
	public List<InetSocketAddress> getServerList() {
		return serverList;
	}
	
	
	public void setServerList(List<InetSocketAddress> list) {
	    if (!operation.equals(DirMessageOps.OPERATION_DOWNLOADOK)) {
	        throw new RuntimeException("Invalid operation for serverList");
	    }
	    this.serverList = list;
	}
	
	
	// operation GENERAL
	public String getOperation() {
		return operation;
	}

	// getter y setter de protocolId (ping)
	public void setProtocolID(String protocolIdent) {
		if (!operation.equals(DirMessageOps.OPERATION_PING)) {
			throw new RuntimeException("DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
		}
		protocolId = protocolIdent;
	}

	public String getProtocolId() {
		return protocolId;
	}




	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
		 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
		 * delimitador DELIMITER, y guardarlo en variables locales.
		 */

		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;
		FileInfo currentFile = null;	//fichero actual (filelist)
		int expectedFileCount = -1;
		



		for (String line : lines) {
			if (line.trim().isEmpty()) continue;
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			
			case FIELDNAME_PROTOCOLID: {
				assert(m!=null);
				m.setProtocolID(value);
				break;
			}
			
			case FIELDNAME_FILECOUNT: {
				expectedFileCount = Integer.parseInt(value);
				break;
			}
			
			case FIELDNAME_FILENAME: {
				currentFile = new FileInfo();
				currentFile.fileName = value;
				break;
			}
			
			case FIELDNAME_SIZE: {
				if (currentFile != null) {
					currentFile.fileSize = Long.parseLong(value);
				}
				break;
			}
			
			case FIELDNAME_HASH: {
				if (currentFile != null) {
					currentFile.fileHash = value;
					//Comprobamos que el ArrayList este creado
					if(m.fileList == null) {
						m.fileList = new ArrayList<>();
					}
					// Añadimos fichero actual y dejamos fichero actual en null
					// ya que filename creará un nuevo FileInfo
					m.fileList.add(currentFile);
					currentFile = null;
				}
				break;
			}
			
			case FIELDNAME_PORT: {
				m.port = Integer.parseInt(value);
				break;
			}
			
			//download -> para buscar ficheros cuyo nombre contenga la subcadena
			case FIELDNAME_SUBSTRING: {
				m.fileNameSubstring = value;
				break;
			}
			
			//Respuesta al download. Directorio dice cuantos servidores tienen el fichero
			// Inicializa el ArrayList que luego se rellenará en el case "adrress"
			case FIELDNAME_SERVERCOUNT: {
				m.serverList = new ArrayList<>();
				break;
			}
			
			//lo usa downloadok para saber desde que peers descargar los ficheros
			case FIELDNAME_ADDRESS: {
			    try {
			        String[] parts = value.split(":");
			        InetAddress ip = InetAddress.getByName(parts[0]);
			        int port = Integer.parseInt(parts[1]);
			        if (m.serverList == null) {
			            m.serverList = new ArrayList<>();
			        }
			        m.serverList.add(new InetSocketAddress(ip, port));
			    } catch (Exception e) {
			        System.err.println("Error al parsear dirección IP:puerto en mensaje ASCII: " + value);
			        e.printStackTrace();
			    }
			    break;
			}
			
			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}

		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo obligatorio

		//Caso mensaje ping
		
		if(getOperation().equals(DirMessageOps.OPERATION_PING)) {
			sb.append(FIELDNAME_PROTOCOLID+DELIMITER+NanoFiles.PROTOCOL_ID+END_LINE);
		}
		
		//caso mensaje filelist
		else if(getOperation().equals(DirMessageOps.OPERATION_FILELISTOK)) {
			if(fileList != null && !fileList.isEmpty()) {
				sb.append("filecount" + DELIMITER + fileList.size() + END_LINE);
				for (FileInfo file : fileList) {
					sb.append("filename" + DELIMITER + file.fileName + END_LINE);
					sb.append("size" + DELIMITER + file.fileSize + END_LINE);
					sb.append("hash" + DELIMITER + file.fileHash + END_LINE);
				}
			} else {
				sb.append("filecount" + DELIMITER + "0" + END_LINE);
			}
		}
		
		//caso mensaje serve
		else if (getOperation().equals(DirMessageOps.OPERATION_SERVE)) {
			sb.append("port" + DELIMITER + port + END_LINE);
			if (fileList != null && !fileList.isEmpty()) {
				sb.append("filecount" + DELIMITER + fileList.size() + END_LINE);
				for (FileInfo file : fileList) {
					sb.append("filename" + DELIMITER + file.fileName + END_LINE);
					sb.append("size" + DELIMITER + file.fileSize + END_LINE);
					sb.append("hash" + DELIMITER + file.fileHash + END_LINE);
				}
			} else {
				sb.append("filecount" + DELIMITER + "0" + END_LINE);
			}
		}
		
		//caso comando download
		else if (getOperation().equals(DirMessageOps.OPERATION_DOWNLOAD)) {
			sb.append("name" + DELIMITER + fileNameSubstring + END_LINE);
		}
		
		else if (getOperation().equals(DirMessageOps.OPERATION_DOWNLOADOK)) {
			sb.append("servercount" + DELIMITER + serverList.size() + END_LINE);
			for (InetSocketAddress addr : serverList) {
				sb.append("address" + DELIMITER + addr.getAddress().getHostAddress() + ":" + addr.getPort() + END_LINE);
			}												// los : separan ip de puerto, no confundir con DELIMITER
		}
		
		
		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}

}



