/**
 * @class	basedatos.BaseDatos
 * @brief	Accede a una base de datos.
 * @author	Aníbal García García
 * @date	28/05/2014
 * @see		BaseDatosMySQL
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


import java.util.ArrayList;


public abstract class BaseDatos {

	/// Dirección ip del servidor.
	protected final String ip;

	/// Puerto del servidor.
	protected final String puerto;

	/// Nombre de la base de datos.
	protected final String baseDatos;

	/// url para la conexión a la base de datos.
	protected String urlConexion;

	/// Nombre de usuario de acceso a la base de datos.
	protected final String usuario;

	/// Contraseña de acceso a la base de datos.
	protected final String contrasena;



	/**
	 * @brief	Constructor.
	 * @param	in	ip Dirección ip del servidor.
	 * @param	in	puerto Puerto de acceso al servidor.
	 * @param	in	base_datos Nombre de la base de datos.
	 * @param	in	usuario Nombre de usuario de acceso a la base de datos.
	 * @param	in	contrasena Contraseña de acceso a la base de datos.
	 */

	protected BaseDatos (String ip, String puerto, String base_datos, String usuario, String contrasena){
		this.ip = ip;
		this.puerto = puerto;
		this.baseDatos = base_datos;
		this.usuario = usuario;
		this.contrasena = contrasena;
	}


	/**
	 * @brief	Establece la conexión con la base de datos.
	 */

	protected abstract void establecerConexion();


	/**
	 * @brief	Cierra la conexión con la base de datos.
	 */

	protected abstract void cerrarConexion();


	/**
	 * @brief	Realiza una consulta a la base de datos.
	 * @param	in	consulta Consulta a realizar.
	 * @return	Un ArrayList de arrays de String con el resultado de la consulta; null en caso de que no produzca resultado.
	 * @note	Detalles del valor devuelto por él método: cada elemento del ArrayList representa un resultado de la consulta (array de String), y cada elemento de este un campo del resultado.
	 */

	protected abstract ArrayList <String[]> consultar (String consulta);

}