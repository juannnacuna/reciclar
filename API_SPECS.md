# API Description

El sistema utiliza autenticación basada en tokens JWT. Al iniciar sesión, el usuario recibe dos tokens:
 - Access Token: válido por 15 minutos.
- Refresh Token: válido por 24 horas y usado para generar nuevos Access Token.
Se debe utilizar el token de acceso en el header para cada endpoint que se quiera llamar. Al cerrar sesión (logout), el Refresh Token se agrega a una blacklist para invalidarlo y evitar su reutilización.

## Endpoints

### Autenticación
- **POST /api/signup/** -> Registro de un nuevo usuario.
- **POST /api/login/** -> Inicio de sesión y obtención de tokens.
- **POST /api/logout/** -> Cierre de sesión.
- **POST /api/token/refresh/** -> Solicitud de un nuevo token de acceso. Recibe el Refresh Token.
- **GET /api/datos_usuario/** -> Devuelve el ID y el username del usuario actualmente logueado.

### Residuos
- **POST /api/residuos/reclamar/** -> Asocia un residuo existente a un usuario, recibiendo el ID del residuo generado por el QR.
- **GET /api/residuos/{id_usuario}** -> Devuelve la cantidad total de residuos de cada tipo asociados a un usuario.
- **GET /api/residuos/** -> Devuelve la cantidad total de residuos de cada tipo.

### Ranking
- **GET /api/ranking/?tipo_residuo={tipo_residuo}** -> Devuelve el top 10 de usuarios con mayor puntaje total, pudiendo recibir un parámetro de tipo query para filtrar por tipo de residuo.
- **GET /api/ranking/semanal/?tipo_residuo={tipo_residuo}** -> Devuelve el top 10 de usuarios con mayor puntaje total de la semana actual (comenzando en lunes), pudiendo recibir un parámetro de tipo query para filtrar por tipo de residuo.
- **GET /api/ranking/posicion/?id_usuario={id_usuario}&tipo_residuo={tipo_residuo}** -> Devuelve la posición del usuario recibido como parámetro en el ranking general. Opcionalmente se puede agregar un parámetro para filtrar por tipo de residuo.
