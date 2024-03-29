package ferreteria;

import java.io.File;
import java.util.List;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;
import com.db4o.*;
import com.db4o.query.Query;

public class DB4o {

	static String DBferreteria = "DBferreteria.db4o";
	static ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), DBferreteria);
	static Scanner teclado=new Scanner(System.in);

	public static void main(String[] args) {
		int opcion=-1;
		do
		{
			System.out.println("Elige la opcion:");
			System.out.println("================");
			System.out.println(" 1.- Añadir Cliente");
			System.out.println(" 2.- Mostrar Clientes");
			System.out.println(" 3.- Modificar Cliente");
			System.out.println();
			System.out.println(" 4.- Añadir Artículo nuevo");
			System.out.println(" 5.- Mostrar Artículos");
			System.out.println(" 6.- Modificar Artículo");
			System.out.println(" 7.- Reponer Artículo");
			System.out.println();
			System.out.println(" 8.- Hacer Venta");
			System.out.println(" 9.- Anular Venta");
			System.out.println("10.- Mostrar todos los artículos de los que haya que pedir unidades al almacen por tener pocas unidades en la ferretería");
			System.out.println("11.- Mostrar los nombres de los artículos vendidos entre 2 fechas que se piden por teclado");
			System.out.println("12.- Localiza a todos los clientes que han hecho una compra en los tres últimos meses");
			System.out.println("13.- Muestra las localidades de los clientes y los nombres de los clientes que han realizado una factura por un importe superior a 50€");
			System.out.println();
			System.out.println("0.- Salir");
			System.out.println("Opcion= ??");
			try {
				opcion=Integer.parseInt(teclado.next());
			} catch (Exception e) {
				System.out.println("Debes introducir un numero");
			}
			String basura = teclado.nextLine();

			switch (opcion) {
			case 0: System.out.println("HASTA LUEGO");break;
			case 1: anadirCliente(); break; //Hecho
			case 2: mostrarClientes(); break; //Hecho
			case 3: modificarCliente(); break; //Hecho
			case 4: anadirArticulo(); break; //Hecho
			case 5: mostrarArticulos(); break; //Hecho
			case 6: modificarArticulo(); break;//"HECHO"
			case 7: reponerArticulo(); break; //Hecho
			case 8: hacerVenta(); break; //Hecho
			case 9: anularVenta(); break;
			case 10: articulosAreponer(); break; //"HECHO"
			case 11: ventasRealizadas(); break;
			case 12: ultimosClientes(); break;
			case 13: ventasMasde50(); break;

			default: ; break;

			} // fin switch
		} while (opcion!=0);// fin while

		db.close(); // cerrar base de datos




	} //final del MAIN()

	private static void ventasMasde50() {
		// Mostrará localidades de los clientes y los nombres de los clientes que han realizado una factura por un importe superior a 50€.
		Query q = db.query();
		q.constrain(Venta.class);
		q.descend("unidades_vendidas*pvp_unidad").constrain(50).greater();
		ObjectSet<Venta> result = q.execute();
		while (result.hasNext()) {
			System.out.println("Localidad: " + result.next().getCliente().getLocalidad() + " Nombre: " + result.next().getCliente().getNombre());
		}
	}

	private static void ultimosClientes() {
		// TODO Auto-generated method stub
		// Mostrará los nombres de clientes que han comprado en los 3 últimos meses a partir de la fecha de hoy.
	}

	private static void ventasRealizadas() {
		// Mostrará las ventas realizadas
		Query q = db.query();
        q.constrain(Venta.class);
        q.descend("venta").orderAscending();
        ObjectSet result = q.execute();
	}

	private static void articulosAreponer() {
		Object stock_minimo = 5;
		Object stock_actual = 0;
		// Mostrará todos los artículos cuyo stock_actual es inferior al stock_minmo
			System.out.println("Estos articulos necesitan ser repuestos: ");
			Query q = db.query();
			q.constrain(Articulo.class);
			q.descend("stock_actual").constrain(stock_minimo).smaller();
			ObjectSet result = q.execute();
	}

	private static void anularVenta() {
		// TODO Auto-generated method stub
		// Pedirá un dni de cliente y una fecha_venta, mostrará esa venta si la hay y procederá a borrarla. Habrá que volver a sumar las unidades devueltas en el stock_actual
	}

	private static void hacerVenta() {
		// A partir de la fecha de hoy, pide dni de cliente, pide artículo y nº unidades quese venden. Hay que descontar el numero de unidades del stock.
		String dni,cod_articulo;
		int unidadesVenta,pvp;
		System.out.println("Dime el DNI del cliente: ");
		dni = teclado.next().toUpperCase();
		ObjectSet<Cliente> resultadoCliente = db.queryByExample(new Cliente(null, dni, null));
		if (resultadoCliente.isEmpty()) {
			System.out.println("No existe el cliente con el DNI " + dni);
		} else {
			System.out.println("Dime el codigo del articulo ");
			cod_articulo = teclado.next().toUpperCase();
			ObjectSet<Articulo> resultadoArticulo = db.queryByExample(new Articulo(null, cod_articulo, 0));
			if (resultadoArticulo.isEmpty()) {
				System.out.println("No existe el articulo con el codigo " + cod_articulo);
			} else {
				System.out.println("Dime las unidades que quieres comprar ");
				unidadesVenta = Integer.parseInt(teclado.next());
				System.out.println("Dime el PvP del artículo ");
				pvp = Integer.parseInt(teclado.next());
				Articulo articulo = resultadoArticulo.next();
				articulo.setStock_actual(articulo.getStock_actual() - unidadesVenta);
				Venta venta = new Venta(resultadoCliente.next(),articulo, unidadesVenta,pvp);
				db.store(articulo);
				db.store(venta);
			}
		}
	}

	private static void reponerArticulo() {
		// Pedirá cod_articulo a reponer y unidades adquiridas. Hay que actualizar stock_actual
		String cod_articulo;
		int unidades;
		System.out.println("Dime el codigo del articulo: ");
		cod_articulo = teclado.next().toUpperCase();
		if (existeArticulo(cod_articulo)) {
			try {
				System.out.println("Dime cuanto stock quieres añadir: ");
				unidades = Integer.parseInt(teclado.next());
				ObjectSet<Articulo> result = db.queryByExample(new Articulo(null, cod_articulo, 0));
				Articulo articulo = result.next();
				articulo.setStock_actual(articulo.getStock_actual() + unidades);
				db.store(articulo);
			} catch (Exception e) {
				System.out.println("Debes introducir un numero.");
			}
		} else {
			System.out.println("No existe el artículo con el codigo " + cod_articulo);
		}
	}

	private static void modificarArticulo() {
		// Pedirá un codigo y pedirá nuevos datos para ese articulo.
		String decripcion, cod_articulo;
		System.out.println("Dime el codigo del artículo: ");
		cod_articulo = teclado.next().toUpperCase();
		if (existeArticulo(cod_articulo)) {
			ObjectSet<Articulo> result = db.queryByExample(new Articulo());
			Articulo articulo = result.next();
			System.out.println("Dime la nueva descripcion: ");
			articulo.setDescripcion(teclado.next());
		} else {
			System.out.println("No existe un producto con el código " + cod_articulo);
		}
	}

	private static void mostrarArticulos() {
		Articulo articuloNull = new Articulo(null, null, 0);
		ObjectSet<Articulo> articulos = db.queryByExample(articuloNull);
		while(articulos.hasNext()) {
			Articulo articulo = articulos.next();
			articulo.mostrarDatos();
		}
	}

	private static void anadirArticulo() {
		// No se podrá añadir a ningún articulo si el cod_articulo está repetido.
		String descripcion,cod_articulo;
		int stock_actual;
		System.out.println("Dime el codigo del articulo: ");
		cod_articulo = teclado.next().toUpperCase();
		if (existeArticulo(cod_articulo)) {
			System.out.println("Ya existe un articulo con el codigo " + cod_articulo);
		} else {
			Boolean salir = true;
			Boolean escribiendo;
			String descripcionAux;
			do {
				try {
					System.out.println("Dime el stock actual: ");
					stock_actual = Integer.parseInt(teclado.next());
					System.out.println("Escribe la descipción\nEscribe \":\" para dejar de escribir\no al final del texto, separado por un espacio");
					escribiendo = true;
					descripcion = "";
					while(escribiendo) {
						descripcionAux = teclado.next();
						if (!descripcionAux.equals(":")) {
							descripcion += " " + descripcionAux;
						} else if (descripcionAux.equals(":")) {
							escribiendo = false;
						}
					}
					//Creo un objeto para guardar en la base de datos
					Articulo articulo = new Articulo(descripcion,cod_articulo,stock_actual);
					db.store(articulo);
					salir = false;
				} catch (Exception e) {}
			} while (salir);
		}
	}

	private static void modificarCliente() {
		// Pedirá un dni y pedirá nuevos datos para ese cliente.
		String nombre,dni,localidad;
		System.out.println("Dime el DNI de la persona: ");
		dni = teclado.next().toUpperCase();
		if (existeCliente(dni)) {
			ObjectSet<Cliente> result = db.queryByExample(new Cliente(null, dni, null));
			Cliente cliente = result.next();
			System.out.println("Dime el nuevo nombre: ");
			cliente.setNombre(teclado.next());
			System.out.println("Dime la nueva localidad: ");
			cliente.setLocalidad(teclado.next());
		} else {
			System.out.println("No existe un cliente con el DNI " + dni);
		}
	}

	private static void mostrarClientes() {
		// Mostrará todos los clientes almacenados con todos sus datos.
		Cliente clienteNull = new Cliente(null, null, null);
		ObjectSet<Cliente> clientes = db.queryByExample(clienteNull);
		while(clientes.hasNext()) {
			Cliente cliente = clientes.next();
			cliente.mostrarDatos();
		}
	}

	private static void anadirCliente() {
		// No se podrá añadir a ningún cliente si el dni está repetido.
		String nombre,dni,localidad;
		System.out.println("Dime el DNI de la persona: ");
		dni = teclado.next().toUpperCase();
		if (existeCliente(dni)) {
			System.out.println("Ya existe un cliente con el DNI " + dni);
		} else {
			System.out.println("Dime el nombre de la persona: ");
			nombre = teclado.next().toLowerCase();
			System.out.println("Dime la localidad de la persona: ");
			localidad = teclado.next().toLowerCase();
			//Creo un objeto para guardar en la base de datos
			Cliente cliente = new Cliente(nombre,dni,localidad);
			db.store(cliente);
		}
	}
	
	/**
	 * Devuelve True o False si existe o no un cliente con el DNI indicado
	 * @param dni
	 * @return boolean
	 */
	private static boolean existeCliente(String dni) {
		Cliente cliente = new Cliente(null, dni.toUpperCase(), null);
		ObjectSet<Cliente> resultado = db.queryByExample(cliente);
		if (resultado.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Devuelve True o False si existe o no un articulo con el codigo indicado
	 * @param codigo del articulo
	 * @return boolean
	 */
	private static boolean existeArticulo(String cod_articulo) {
		Articulo articulo = new Articulo(null, cod_articulo, 0);
		ObjectSet<Articulo> resultado = db.queryByExample(articulo);
		if (resultado.isEmpty()) {
			return false;
		}
		return true;
	}


} // final de la clase
