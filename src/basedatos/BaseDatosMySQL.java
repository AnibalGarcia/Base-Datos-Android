/**
 * @class	basedatos.BaseDatosMySQL
 * @brief	Accede a una base de datos MySQL.
 * @author	Aníbal García García
 * @date	01/06/14
 * @note	Utiliza JDBC para el acceso a la base de datos.
 * @see		BaseDatos
 * @note	Copyright 2014 Aníbal García García
 * @note	This program is free software: you can redistribute it and/or modify
			it under the terms of the GNU General Public License as published by
			the Free Software Foundation, either version 3 of the License, or
			(at your option) any later version.
 * @note	This program is distributed in the hope that it will be useful,
			but WITHOUT ANY WARRANTY; without even the implied warranty of
			MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
			GNU General Public License for more details.
 * @note	You should have received a copy of the GNU General Public License
			along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package basedatos;


import android.content.Context;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import utilidades.Internet;


public class BaseDatosMySQL extends BaseDatos {

	/// Contexto de la actividad o servicio que accede a la base de datos.
	private final Context contexto;

	/// Nombre del sistema de gestión de base de datos MySQL.
	private static final String SGBD = "mysql";

	/// Driver de JDBC para MySQL.
	private static final String DRIVER = "com.mysql.jdbc.Driver";

	/// Conexión con la base de datos.
	private Connection conexion = null;

	/// Indica si se debe bloquear la ejecución de un método esperando el término de la ejecución de una hebra lanzada.
	private boolean bloquear;

	// Resultado de la consulta a la base de datos.
	private ArrayList <String[]> resultado = null;  // Cada elemento del ArrayList (un array de String) representa un registro devuelto por la consulta, y cada elemento de este un campo del registro



	/**
	 * @brief	Constructor.
	 * @param 	in	contexto Contexto de la actividad o servicio que accede a la base de datos.
	 * @param	in	ip Dirección ip del servidor.
	 * @param	in	puerto Puerto de acceso al servidor.
	 * @param	in	base_datos Nombre de la base de datos.
	 * @param	in	usuario Nombre de usuario de acceso a la base de datos.
	 * @param	in	contrasena Contraseña de acceso a la base de datos.
	 * @note	La conexión a la base de datos se realiza en una hebra auxiliar.
	 * @see		registrarDriver()
	 * @see		establecerConexion()
	 */

	public BaseDatosMySQL (Context contexto, String ip, String puerto, String base_datos, String usuario, String contrasena){
		super(ip, puerto, base_datos, usuario, contrasena);  // Constructor de BaseDatos

		urlConexion = "jdbc:" + SGBD + "://" + ip + ":" + puerto + "/" + base_datos;
		this.contexto = contexto;
		bloquear = true;  // Bloqueo la ejecución del método hasta que se realice la conexión con la base de datos


		// Registro el driver de MySQL
		registrarDriver();

		// Conexión a la base de datos
		if (Internet.verificarConexionInternet(contexto)){  // Si el dispositivo dispone de conexión a Internet
			// Creo la hebra de conexión a la base de datos
			Thread hebra_conexion = new Thread ("Hebra de conexión a la base de datos"){
				@Override
				public void run(){
					// Establezco la conexión con la base de datos
					establecerConexion();

					bloquear = false;  // Ya se ha realizado la conexión
				}
			};

			hebra_conexion.start();  // Comienzo la ejecución de la hebra


			// Bloqueo la ejecución del método hasta que se realice la conexión con la base de datos (termine la ejecución de la hebra lanzada)
			while(bloquear);
		}
	}


	/// Se invoca antes de que el objeto sea recolectado por el recolector de basura.

	@Override
	protected void finalize() throws Throwable{
		cerrarConexion();  // Cierro la conexión con la base de datos

		super.finalize();
	}


	/**
	 * @brief	Registra el driver de JDBC para MySQL.
	 * @note	Este método debe ser llamado sin falta en el constructor de la clase.
	 */

	private void registrarDriver(){
		try{
			// Registro el driver
			Class.forName(DRIVER);
		}

		catch (ClassNotFoundException e){  // Capturo la excepción
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * @brief	Establece la conexión con la base de datos.
	 * @see		consultar(String)
	 * @see		cerrarConexion()
	 */

	protected void establecerConexion(){
		if (conexion == null){  // Si la conexión todavía no se ha establecido
			try{
				// Obtengo una conexión con la base de datos
				conexion = DriverManager.getConnection(urlConexion, usuario, contrasena);
			}

			catch (SQLException e){  // Capturo la excepción
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
	}


	/**
	 * @brief	Cierra la conexión con la base de datos.
	 * @post	El atributo "conexion" será null.
	 * @see		establecerConexion()
	 * @see		consultar(String)
	 */

	protected void cerrarConexion(){
		if (conexion != null){  // Si la conexión está establecida
			try{
				conexion.close();  // Cierro la conexión
				conexion = null;
			}

			catch (SQLException e){  // Capturo la excepción
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
	}


	/**
	 * @brief	Realiza una consulta a la base de datos reestableciendo la conexión a la base de datos si esta falla.
	 * @param	in	consulta Consulta a realizar.
	 * @return	Un ArrayList de arrays de String con el resultado de la consulta; null en caso de que no produzca resultado.
	 * @post	La conexión a la base de datos puede ser reestablecida modificando el atributo "conexion".
	 * @note	Detalles del valor devuelto por el método: cada elemento del ArrayList (un array de String) representa un registro devuelto por la consulta, y cada elemento de este un campo del registro.
	 * @note	La conexión a la base de datos se realiza en una hebra auxiliar.
	 * @see		realizarConsulta(String)
	 * @see		establecerConexion()
	 * @see		cerrarConexion()
	 */

	public ArrayList <String[]> consultar (final String consulta){
		if (Internet.verificarConexionInternet(contexto)){  // Si el dispositivo dispone de conexión a Internet
			bloquear = true;  // Bloqueo la ejecución del método hasta que se obtenga el resultado de la consulta

			// Creo la hebra de consulta a la base de datos
			Thread hebra_consulta = new Thread ("Hebra de consulta a la base de datos"){
				@Override
				public void run(){
					// Realizo la consulta a la base de datos
					try{
						resultado = realizarConsulta(consulta);  // Realizo la consulta
					}

					catch (SQLException unused){  // Capturo la excepción
						// Al desconectarse la conexión a Internet del dispositivo se pierde la conexión actual con la base de datos
						// Intento reestablecer la conexión con la base de datos y realizar la consulta de nuevo

						cerrarConexion();  // Cierro la conexión con la base de datos
						establecerConexion();  // Vuelvo a establecer la conexión con la base de datos


						// Realizo la consulta a la base de datos
						try{
							resultado = realizarConsulta(consulta);  // Realizo la consulta
						}

						catch (SQLException e){  // Capturo la excepción
							Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage());
							e.printStackTrace();
						}
					}

					bloquear = false;  // Ya se ha obtenido el resultado de la consulta
				}
			};

			hebra_consulta.start();  // Comienzo la ejecución de la hebra


			// Bloqueo la ejecución del método hasta que no se obtenga el resultado de la consulta (termine la ejecución de la hebra lanzada)
			while(bloquear);
		}


		return resultado;
	}


	/**
	 * @brief	Realiza la consulta a la base de datos.
	 * @param	in	consulta Consulta a realizar.
	 * @return	Un ArrayList de arrays de String con el resultado de la consulta; null en caso de que no produzca resultado.
	 * @throws	SQLException
	 * @note	Detalles del valor devuelto por el método: cada elemento del ArrayList (un array de String) representa un registro devuelto por la consulta, y cada elemento de este un campo del registro.
	 * @see		obtenerValorRegistro(ResultSet, ResultSetMetaData, int)
	 */

	private ArrayList <String[]> realizarConsulta (String consulta) throws SQLException{
		ArrayList <String[]> resultado = new ArrayList <String[]>();  // Resultado global de la consulta (todos los registros obtenidos)
		String resultado_array[];  // Cada registro obtenido en la consulta (cada elemento del array es un campo del registro)


		if (conexion != null){  // Si la conexión se ha establecido correctamente
			// Realizo la consulta
			Statement s = conexion.createStatement();  // Para realizar la consulta
			ResultSet rs = s.executeQuery(consulta);  // Realizo la consulta a la base de datos

			// Obtengo la información de los campos de los registros obtenidos en la consulta
			ResultSetMetaData rsmd = rs.getMetaData();


			// Obtengo los valores de los campos de cada registro
			while (rs.next()){  // Mientras queden registros por recorrer
				resultado_array = new String[rsmd.getColumnCount()];  // Reservo memoria para el número de campos en el registro

				for (int i = 1; i <= resultado_array.length; i++)  // Para cada campo del registro (comienzan por el valor 1)
					resultado_array[i - 1] = obtenerValorRegistro(rs, rsmd, i);  // Obtengo su valor

				resultado.add(resultado_array);  // Añado los valores del registro al resultado global
			}
		}


		return resultado;
	}


	/**
	 * @brief	Obtiene el valor de un campo de un registro.
	 * @param	in	rs Registros obtenidos al realizar la consulta.
	 * @param	in	rsmd Información de los campos de los registros obtenidos en la consulta.
	 * @param	in	campo Índice del campo a obtener su valor.
	 * @return	Un String con el valor del campo; null en caso de que el campo no exista.
	 * @throws	SQLException
	 * @note	El argumento "rs" mantiene un cursor que apunta al registro actual del que se desea obtener el valor de uno de sus campos.
	 */

	private String obtenerValorRegistro (ResultSet rs, ResultSetMetaData rsmd, int campo) throws SQLException{
		String valor = null;  // Valor del campo del registro
		String tipo = rsmd.getColumnTypeName(campo);  // Tipo de dato del campo del registro


		if (tipo.equals("INT"))
			valor = Integer.toString(rs.getInt(campo));

		else if (tipo.equals("DATETIME") || tipo.equals("TIMESTAMP"))
			valor = rs.getTimestamp(campo).toString();

		else if (tipo.equals("CHAR") || tipo.equals("VARCHAR"))  // Nota: "el tipo text equivale al valor VARCHAR"
			valor = rs.getString(campo);


		return valor;
	}

}