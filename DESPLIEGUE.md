# DESPLIEGUE DE LA APLICACIÓN
Autores: **Omar Marcos Julián** y **Rafael Hidalgo Otero**

##Consideraciones previas y justificación
A la hora de desplegar nuestra aplicación, vamos a utilizar AWS (Amazon Web Services), que es un sistema de Infraestructura
como servicio (IaaS) ya que nos permite un nivel más bajo de abstracción y nos ofrece todos los elementos que podamos necesitar
para el despliegue de nuestra aplicación. A diferencia de la Plataforma como Servicio (PaaS), somos nosotros los que debemos 
configurar todo lo relativo al hardware que va a ejecutar nuestra aplicación y de preparar el entorno en nuestras máquinas.
Esto nos da más transparencia y control sobre las instancias que ejecutarán nuestra aplicación (a cambio de una mayor dificultad
ya que somos nosotros los que lo debemos configurar todo: instancias, bases de datos, imágenes, escalado, entorno, etc).
Nos permite no depender del proveedor y saber cómo funciona todo, haciendo más fácil la resolución de errores en caso de
que los haya. 

Lo primero que vamos a hacer es crear una primera instancia donde instalaremos las dependencias necesarias y desplegaremos
nuestra aplicación. Haremos además que la aplicación se ponga en marcha sola en caso de que se detenga por algún motivo. 

Después crearemos una base de datos a la que conectaremos nuestra aplicación, dejándola así sin estado (uno de los 
12 factores para la construcción de aplicaciones SaaS). Además permite la persistencia de datos ya que se puede reiniciar 
la instancia o parar la aplicación sin la pérdida de éstos. 

Una vez hecho esto y comprobado que todo funciona correctamente, crearemos una imagen de la instancia del equipo virtual para que podamos replicarlo.
Mediante un grupo de autoescalado podremos aumentar o disminuir replicas de la instancia en función de la carga de cada una 
de ellas. Además utilizaremos un balanceador de carga que dirigirá el tráfico a las instancias de nuestras máquinas virtuales
en función de la carga que tengan. El balanceador nos sirve también para comprobar si las instancias están operativas o si alguna
ha dejado de funcionar. En ese caso, se creará otra instancia para que siempre estén operativas el número de instancias necesarias
para el buen fucionamiento de la aplicación, siempre dentro del rango que hayamos configurado (en nuestro caso entre 2 y 10 instancias).
Con esto conseguimos que nuestra aplicación sea escalable horizontalmente en función de las necesidades y disponer de una
cierta tolerancia a fallos, ya que si cae una instancia, se creará otra para mantener siempre el mínimo que hayamos configurado.

## Crear una instancia  
Lo primero que hacemos para desplegar nuestra aplicación es crear la instancia donde se ejecutará. Para ello entramos en la 
consola de AWS y seleccionamos EC2 (Elastic Computing). Lo primero que hacemos es cambiar la región a París, ya que en
principio es la más próxima a nosotros. Dependiendo de dónde acceda la mayoría de usuarios se podría ubicar en otro sitio 
más cercano a ellos. A la hora de crear la instancia elegimos el sistema operativo (Ubuntu en nuestro caso) y las 
características del equipo. De momento lanzamos sólo una instancia. Añadimos un disco duro y le ponemos un tag para 
diferenciarla de otras instancias si las tuviéramos. Creamos un grupo de seguridad nuevo y habilitamos el puerto 80 (utilizado 
en las peticiones Http) además del 22 para conectarnos por ssh. Con esto ya podemos crear nuestra instancia. Al crear la 
instancia nos generará una clave que deberemos descargar y guardar ya que es la única forma que tenemos de poder acceder 
a la instancia. En caso de perderla tendríamos que eliminar la instancia y crear una de nuevo. Una vez que hemos añadido 
la clave a nuestro ssh, ya podemos loguearnos en nuestra instancia a través de la consola mediante el DNS Público de la instancia ssh 
ubuntu@PUBLIC_DNS. Con el DNS Público podríamos acceder también desde cualquier navegador, ya que hemos habilitado el 
puerto 80. El problema es que cambia al reiniciar, pero esto se solucionará más adelante.

## Desplegar el software
Una vez logueados en nuestra máquina virtual a través de la consola, instalamos java en ella.
En nuestro equipo local, generamos el archivo jar de nuestra aplicación y la subimos a nuestra instancia. Lo instalamos en 
una carpeta temporal y una vez que comprobamos que funciona lo movemos a una carpeta que no se borre al apagar la instancia, 
en la ruta
```
/opt/recipes-api
```


## Creación de una base de datos
Para que nuestra aplicación funcione correctamente, la conectaremos a una base de datos. Para ello buscamos el servicio 
RDS en la consola de AWS. Elegimos el tipo de base de datos. En nuestro caso elegimos PostgreSQL, ya que en nuestra 
aplicación hacemos uso de una funcionalidad de Play válida sólo para bases de datos PostgreSQL (validador @DbArray). 
Comenzamos a configurar nuestra base de datos. En caso de sacar la aplicación a producción elegiríamos esa opción, para 
hacer pruebas elegimos Dev/Test. Elegimos el tipo de máquina sobre la que correrá nuestra base de datos, le damos un nombre
a la base de datos para identificarla en caso de que hubiera más de una y creamos un nombre de usuario y una contraseña. 
El nombre de la base de datos, el usuario y la contraseña lo necesaritaremos para que nuestra aplicación pueda conectarse
a ella. En seguridad, hacemos que no sea accesible públicamente ya que en principio, solo nos conectaremos a ella a través 
de la app. Creamos una base de datos inicial y le damos nombre. Por último lanzamos la instancia de nuestra base de datos.
Modificamos el grupo de seguridad para que acepte tráfico desde cualquier red. 

## Configuramos el autoarranque de la aplicación y su acceso a la base de datos que hemos creado
Una vez creada la instancia de la base de datos, vamos a crear un archivo que arrancará sola la aplicación y en él 
configuraremos tanto el acceso por el puerto 80 como la conexión a la base de datos que acabamos de crear.
Primero creamos un archivo Systemd recipes-api.service en la carpeta /etc/systemd/system/ de nuestra máquina virtual:
```
[Unit]
Description="Recipes Api"

[Service]
WorkingDirectory=/opt/recipes-api
ExecStart=/opt/recipes-api/start.sh
ExecStop=/bin/kill -TERM $MAINPID
Type=simple
Restart=always

[Install]
WantedBy=multi-user.target
```

Creamos un script al que llamaremos start.sh a través del que arrancaremos la aplicación. Como queremos que se acceda por el puerto 80, 
hacemos que ésta escuche por el puerto 80 añadiendo -Dserver.port=80 a la instrucción del script. Además añadimos los datos 
de la base de datos para conectar la aplicación con ella: 
```
#!/bin/sh

export JDBC_DATABASE_URL=jdbc:postgresql://ENDPOINT/DB_NAME
 export JDBC_DATABASE_USERNAME= USERNAME 
export JDBC_DATABASE_PASSWORD=PASSWORD


java -Dserver.port=80 -jar recipes-api.jar
```
Una vez creados los archivos, recargamos:
```
sudo systemctl daemon-reload
sudo systemctl enable recipes-api.service
```
Ahora cada vez que se pare la aplicación la instancia intentará arrancarla.    

## Escalado horizontal
El escalado horizontal consiste en aumentar el número de instancias en caso de que la demanda sea alta para evitar que la 
instancia que tenemos se sature. Además si la instancia tiene un problema, todavía dispondremos de otra instancia para que
la aplicación esté operativa. Las instancias tendrán direcciones distintas por lo que nos surge el problema de cuál dar a los
usuarios para que puedan acceder a la aplicación, pero esto lo solucionaremos más adelante con un balanceador de carga.
Para crear una nueva instancia creamos primero un snapshot (imagen) de la instancia que tenemos. A partir de esta imagen, 
llamada AMI, podremos crear todas las instancias que queramos idénticas a la primera.
Para crearla, seleccionamos la instancia, presionamos botón derecho y seleccionamos crear imagen. Le damos un nombre y una
descripción y cuando la creemos aparecerá en la sección AMIs.
A continuación creamos una instancia a partir de esa imagen. Seleccionamos la imagen cuando esté lista y le damos a lanzar.
Seguimos los pasos que nos van indicando: seleccionamos el tipo de instancia, el almacenamiento, le damos un tag para diferenciarla 
de otras instancias, seleccionamos el grupo de seguridad de la instancia de la que hemos creado la imagen y por último 
seleccionamos la clave ya existente de la instancia original. Con esto ya tenemos creada otra instancia idéntica a la primera,
pero con direcciones de acceso distintas.

## Balanceador de carga
Para solucionar este problema, vamos a configurar un balanceador de carga, que será el encargado de recibir las peticiones
de los usuarios y derivarlas a las instancias que tenemos creadas en función de la carga que tenga cada una. Además hará 
comprobaciones periódicas para comprobar que el funcionamiento de las instancias es el correcto.
Para ello seleccionamos Load Balancers y clicamos en create load balancer, le damos un nombre para identificarlo por si tenemos
alguno más y creamos un grupo de seguridad nuevo, en el que permitimos, como en los otros, el tráfico a través del puerto 80
(ya viene por defecto) y desde cualquier sitio. A continuación configuramos los check healths, para que el balanceador compruebe
periódicamente que las instancias están operativas y la aplicación está funcionando. Si una instancia no funciona la quita y no
deriva a ella ninguna petición. En el Ping Path ponemos una llamada a la ruta /users?page=0 que devolverá un 200 si la 
instancia está operativa ya que la base de datos siempre tendrá como mínimo un usuario (el administrador). En un principio, 
dejamos los valores por defecto ya que si ponemos un intervalo menor, estamos generando más tráfico a nuestras instancias
por lo que incurriría en más gasto. Sin embargo, si el tráfico fuera alto, un intervalo de 30 segundos haría que el balanceador
tardara más de un minuto en comprobar el estado (ya que haría dos comprobaciones con dos intervalos de 30 segundos) y 
todas esas peticiones las estaría dirigiendo a una instancia que no está operativa. En caso de un tráfico alto bajaríamos
el intervalo para minimizar esas peticiones perdidas.
A continuación añadimos las instancias a las que redirigirá el tráfico el balanceador, que son las que hemos creado inicialmente.
Le asignamos un tag al balanceador para identificarlo y finalmente comprobamos datos y creamos el balanceador.
Hasta que no haga las health cheacks de las instancias no estarán operativas (InService). Finalmente, la dirección única 
a través de la cual conectarse a la aplicación será el DNS name del balanceador. 

## Grupos de autoescalado
A continuación vamos a configurar un grupo de autoescalado que nos permite, en función de la carga, aumentar o disminuir
el número de instancias automáticamente. Para ello seleccionamos Create Auto Scaling group de la sección Auto Scaling y 
configuramos la instancia que creará a partir de la imagen (AMI) que hicimos. Seleccionamos la imagen, elegimos el equipo
(el mismo que la instancia original), le damos un nombre, elegimos el almacenamiento, configuramos como grupo de seguridad 
el que hemos utilizado en la instancia original y al lanzar la instancia, seleccionamos la clave de la instancia. Con esto
ya hemos configurado la instancia que se creará si aumenta la carga. Ahora pasamos a configurar el grupo de autoescalado. Le
damos un nombre y elegimos con cuantas instancias vamos a empezar (en principio seleccionamos 2). Indicándole esto, siempre 
tendrá activas al menos 2. A continuación elegimos una de las subredes que nos salen y por último y más importante, configuramos
las políticas de escalado. Indicamos que escale entre 2 y 10 instancias. Seleccionamos **Use scaling policies to adjust 
the capacity of this group** y **Scale the Auto Scaling group using step or simple scaling policies**. Ahora podemos configurar 
el incremento y disminución de instancias. Para el incremento añadimos una alarma para que nos avise cuando el uso de la 
CPU esté por encima del 90% durante al menos 5 minutos. Si salta la alarma se deberá añadir una instancia. Para la disminución, 
configuramos una alarma para que salte cuando el uso de la CPU sea inferior al 10% durante un periodo de al menos 5 minutos.
Si esto ocurre deberá eliminarnos una instancia. Con esto podemos minimizar gastos ya que si una instancia no tiene casi carga, 
el sistema se encargará de apagarla.

## Elasticache
A continuación conectamos nuestra aplicación a un Redis con Amazon Elasticache para Redis. Para ello creamos una instancia de
Elasticache, elegimos cluster engine de tipo Redis, desmarcamos la casilla multi AZ with Auto Fail-Over, el número de réplicas
lo ponemos a 1, el tipo de nodo caché t2 micro, elegimos la subred donde están corriendo las instancias (Subnet ID) y el 
security group de la instancia, en el que tenemos que habilitar el puerto por el que escucha la instancia de Elasticache, y 
decirle que escuche desde cualquier lugar. 
Para no tener que rehacer la imagen de nuevo, configuramos las variables de entorno necesarias para que la aplicación
acceda al Redis, desde un archivo externo. Una vez creado el Redis, la aplicación puede acceder a él a través de su Endpoint.  