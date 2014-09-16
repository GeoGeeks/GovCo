# _GovCo_

Esta aplicación fue creada por el grupo Semillero de Innovación Geográfica (SIG) de Esri Colombia, para mayor información consulte www.esri.co o visite @geo_geeks en twitter.

En esta aplicación usted puede conocer la ubicación y datos de contacto de varias entidades estatales en Bogotá, actualmente encontrara entidades como el IGAC, Ministerio de Salud, Servicio Geológico Colombiano y Federación Nacional de Cafeteros.
Esta aplicación cuenta con la funcionalidad de GeoTrigger que usa la tecnología de Esri, por medio del cual si se activa le permite una vez iniciada la aplicación cargar una notificación de tipo Push una vez usted haya ingresado a la entidad o haya salido de la misma, cada notificación lo lleva la página web de dicha entidad.

## Configuración

Para que la aplicación sea funcional es necesario contar con un los Sistemas de Información Geográfica de ESRI Colombia por medio de la plataforma ArcGis, de ahí se toman lo datos a mostrar

Se debe generar el codigo de Google Cloud Messaging para habilitar la recepcion de notificaciones en el dispositivo Android

   ```java
// Create a new application at https://developers.arcgis.com/en/applications
private static final String AGO_CLIENT_ID = "...";

// The project number from https://code.google.com/apis/console
private static final String GCM_SENDER_ID = "...";
```

## Implementación

- Se requiere que los servicios de Arcgis se encuentren habilitados y públicos
- Agregue el proyecto en su entorno de desarrollo para aplicaciones moviles

## Licenciamiento

Copyright 2014 Esri

Licenciado bajo la licencia Apache, Versión 2.0; usted no puede utilizar este archivo excepto en conformidad con la Licencia. Usted puede obtener una copia de la licencia en

http://www.apache.org/licenses/LICENSE-2.0

A menos que lo requiera la ley o se acuerde por escrito, el software distribuido bajo la licencia se distribuye "TAL CUAL", SIN GARANTÍAS NI CONDICIONES DE NINGÚN TIPO, ya sea expresa o implícita. Consulte la licencia para los permisos específicos de gobierno de idiomas y limitaciones en la licencia. 

Una copia de la licencia está disponible en el archivo license.txt del repositorio.