/**
 * @author Sistemas Operativos - DTE
 * @version 2020-a
 * 
 * agosto 2020: versión inicial, jl
 * 
 */
package servidor;

import comunicaciones.RecursoWeb;

/**
 * Representa un nodo del cache web. Cada nodo almacena un único RecursoWeb.
 * Mantiene la información necesaria para saber cuántos hilos de atención
 * de peticiones están usando este RecursoWeb, el estado del recurso y
 * cualquier otra información necesaria para aplicar el algoritmo de liberación
 * de espacio en disco.
 */
public class NodoCache
{

	private RecursoWeb recursoWeb;
	private int contadorUso;
	private boolean usadoRecientemente;
	private boolean debeDescargarse;
	private static final String NOUSADO = " El recurso no está en uso";
	private static final String NOCARGADO = "No tiene asignado el recurso";

	/**
	 * Crea un nodo del cache web. Al crearlo, el recurso aún no estará
	 * disponible; lo estará cuando sea descargado. Una vez descargado,
	 * se anotará el RecursoWeb correspondiente. En el momento de la
	 * creación estará siendo usado por el nodo que lo ha creado, por lo
	 * que su contador de uso será 1.
	 */
	public NodoCache ()
	{
		recursoWeb = null;
		contadorUso = 1;
		debeDescargarse = true;
	}

	/**
	 * Indica si el hilo llamante debe iniciar la descarga del RecursoWeb.
	 * @return true la primera vez que se invoca y false el resto de
	 * las veces.
	 */
	public boolean debeDescargarse ()
	{
		try
		{
			return debeDescargarse;
		}
		finally
		{
			debeDescargarse = false;
		}
	}

	/**
	 * Anota el RecursoWeb correspondiente.
	 * @param recurso RecursoWeb descargado que será anotado en este nodo.
	 */
	public void anotarRecursoWeb ( RecursoWeb recurso )
	{
		recursoWeb = recurso;
	}

	/**
	 * Obtiene acceso al recurso. Anota que hay un hilo más usándolo.
	 */
	public void usarRecurso ()
	{
		contadorUso++;
	}

	/**
	 * Anota que hay un hilo menos usando este recurso. Tiene que estar
	 * siendo usado para poder decrementar su contador de uso.
	 * Antes de liberarlo, se anota que acaba de ser usado.
	 * @throws IllegalStateException cuando el contador de uso es cero.
	 */
	public void liberarRecurso ()
	{
		if ( contadorUso == 0 )
			debug ( 0, " Error: " + NOUSADO );
		usadoRecientemente = true;
		contadorUso--;
	}

	/**
	 * Permite obtener el recurso web almacenado. Tiene que tener
	 * asignado un RecursoWeb
	 * @return el RecursoWeb correcpondiente. Puede ser null si aún no se
	 * le ha anotado ningún RecursoWeb a este nodo.
	 */
	public RecursoWeb recursoWeb ()
	{
		return recursoWeb;
	}

	/**
	 * Indica si el elemento ha sido usado recientemente.
	 * @return true si el elemento ha sido usado desde la última vez que
	 * fue considerado como candidato a ser eliminado y false en caso
	 * contrario.
	 */
	public boolean usadoRecientemente ()
	{
		return usadoRecientemente;
	}

	/**
	 * Marca el elemento como no recientemente utilizado.
	 */
	public void marcarNoUsadoRecientemente ()
	{
		usadoRecientemente = false;
	}
	
	/**
	 * Muestra el mensaje de depuración. Si el nivel de depuración es
	 * mayor o igual que el indicado, se muestra el mensaje precedido por
	 * el nombre del hilo para facilitar el seguimiento.
	 * @param nivel El nivel de depuración mínimo para que se muestre el
	 * mensaje.
	 * @param mensaje El mensaje de depuración a visualizar.
	 */
	private void debug ( int nivel, String mensaje )
	{
		if ( Main.param.debug >= nivel )
			System.err.println ( Thread.currentThread().getName() + mensaje );
	}
}
