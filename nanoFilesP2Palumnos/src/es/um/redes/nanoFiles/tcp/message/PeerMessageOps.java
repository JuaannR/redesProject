package es.um.redes.nanoFiles.tcp.message;

import java.util.Map;
import java.util.TreeMap;

public class PeerMessageOps {

	public static final byte OPCODE_INVALID_CODE = 0;  //marcado de error
	public static final byte FILE_NOT_FOUND = 0X01;    // fichero no encontrado
	public static final byte GET_CHUNK = 0X02;			// solicita chunk de un fihcero
	public static final byte SEND_CHUNK = 0X03;			// envia chunk solicitado
	public static final byte FILE_INFO_REQUEST = 0X04;  // solicita info de un fichero (nombre, tamaño, hash)
	public static final byte FILE_INFO_RESPONSE = 0X05; // respuesta con info de un fichero
														

	private static final Byte[] _valid_opcodes = { 
			OPCODE_INVALID_CODE,
			FILE_NOT_FOUND,
			GET_CHUNK,
			SEND_CHUNK,
			FILE_INFO_REQUEST,
			FILE_INFO_RESPONSE
	};
	
	private static final String[] _valid_operations_str = { 
			"INVALID_OPCODE",
			"FILE_NOT_FOUND",
			"GET_CHUNK",
			"SEND_CHUNK",
			"FILE_INFO_REQUEST",
			"FILE_INFO_RESPONSE"
	};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}

	
	 // Transforma una cadena en el opcode correspondiente
	
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OPCODE_INVALID_CODE);
	}

	
	 // Transforma un opcode en la cadena correspondiente
	 
	public static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
}
