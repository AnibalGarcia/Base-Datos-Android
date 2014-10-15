/**
 * @class	utilidades.Internet
 * @brief	Proporciona utilidades de acceso a Internet.
 * @author	Aníbal García García
 * @date	15/09/2014
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

package utilidades;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public final class Internet {

	/**
	 * @brief	Verifica si el dispositivo dispone de conexión a Internet.
	 * @return	true si el dispositivo dispone de conexión; false en caso contrario.
	 */

	public static boolean verificarConexionInternet (Context contexto){
		boolean conectado = false;  // Indica si el dispositivo tiene conexión a Internet
		ConnectivityManager connectivity_manager = (ConnectivityManager) contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] redes = connectivity_manager.getAllNetworkInfo();  // Información del estado de conexión de todas las redes disponibles (Wi-Fi, GPRS, UMTS, etc)

		for (NetworkInfo red : redes){  // Para cada red
			if (red.getState() == NetworkInfo.State.CONNECTED){  // Si la red tiene conexión
				conectado = true;
				break;
			}
		}

		return conectado;
	}

}
