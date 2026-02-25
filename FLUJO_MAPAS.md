# 🗺️ Resumen del Flujo de Mapas en ReciclApp

## Arquitectura General

La funcionalidad de mapas sigue el patrón **MVVM** del resto de la app, usando **Jetpack Compose** con un `AndroidView` para integrar el `MapView` de osmdroid (que no tiene soporte nativo de Compose).

---

## 📁 Archivos Involucrados

| Archivo | Responsabilidad |
|---------|----------------|
| `MyApplication.kt` | Inicializa OSMDroid al arrancar la app |
| `OsmdroidInitializer.kt` | Configura osmdroid (user agent, SharedPreferences) |
| `ApiService.kt` | Define el endpoint `GET api/estaciones/` |
| `EstacionesDtos.kt` | DTO `Estacion` (id, nombre, latitud, longitud) |
| `RecyclingPoint.kt` | Modelo de dominio del mapa (name, lat, lng → `GeoPoint`) |
| `RouteInfo.kt` | Modelo de ruta (distancia, duración, modo transporte) + enum `TransportMode` |
| `EstacionesRepository.kt` | Llama a la API y devuelve `Result<List<Estacion>>` |
| `RecyclingMapViewModel.kt` | ViewModel con Hilt, carga estaciones, maneja estado del mapa |
| `RecyclingMapScreen.kt` | Composable principal: mapa, markers, rutas, permisos, UI |
| `AppDestination.kt` | Define la ruta `"map"` como destino de navegación |
| `MainApp.kt` | Registra el destino en el `NavHost` y lo muestra en el bottom nav |

---

## 🔄 Flujo Completo (paso a paso)

### 1. Inicialización (arranque de la app)
```
MyApplication.onCreate()
  └─► OsmdroidInitializer.init(context)
        ├─ Carga config desde SharedPreferences("osmdroid")
        ├─ Setea userAgentValue = packageName (requerido por OpenStreetMap)
        └─ Desactiva debug de tile providers
```

### 2. Navegación al Mapa
```
MainApp.kt (Scaffold)
  ├─ NavigationBar con BottomNavItem(AppDestination.Map, icon=Place, "Mapa")
  ├─ Al tocar "Mapa" → navController.navigate("map")
  └─ NavHost → composable("map") {
       val viewModel: RecyclingMapViewModel = hiltViewModel()
       RecyclingMapScreen(viewModel)
     }
```
La `AppTopBar` (username, puntos, logout) y la `NavigationBar` se renderizan **a nivel de Scaffold en MainApp.kt**, no dentro de `RecyclingMapScreen`. Esto evita problemas de z-ordering con `AndroidView`.

### 3. Carga de Datos (ViewModel)
```
RecyclingMapViewModel (inyectado por Hilt)
  │
  ├─ init { loadEstaciones() }
  │     └─ viewModelScope.launch {
  │          estacionesRepository.fetchEstaciones()
  │            └─ apiService.getEstaciones()  // GET api/estaciones/
  │                 └─ Response<List<Estacion>>
  │          }
  │          Mapea cada Estacion → RecyclingPoint (name, lat, lng)
  │          Actualiza _points: StateFlow<List<RecyclingPoint>>
  │
  ├─ StateFlows expuestos:
  │   ├─ points         → Lista de puntos de reciclaje
  │   ├─ selectedPoint  → Punto seleccionado actualmente
  │   ├─ routeInfo      → Info de la ruta (distancia, tiempo, modo)
  │   ├─ originPoint    → Ubicación GPS del usuario
  │   └─ transportMode  → WALKING o DRIVING
```

### 4. Renderizado del Mapa (RecyclingMapScreen)
```
RecyclingMapScreen(viewModel)
  │
  ├─ Observa StateFlows con collectAsStateWithLifecycle()
  │
  ├─ Pide permiso de ubicación (LaunchedEffect + permissionLauncher)
  │   ├─ Si concedido → enableMyLocation() + runOnFirstFix → setOriginPoint()
  │   └─ Si denegado → Toast "Activá la ubicación..."
  │
  ├─ AndroidView (factory, una sola vez):
  │   └─ Crea MapView de osmdroid
  │       ├─ TileSource: MAPNIK (tiles de OpenStreetMap)
  │       ├─ Zoom: 15.0, Centro: La Plata (-34.9214, -57.9545)
  │       ├─ MyLocationNewOverlay (ícono verde personalizado)
  │       └─ MapEventsOverlay (tap para cerrar info windows y limpiar rutas)
  │
  ├─ LaunchedEffect(points, mapView):
  │   └─ Cuando llegan los puntos de la API:
  │       ├─ Crea un Marker por cada RecyclingPoint
  │       ├─ Configura click en cada marker:
  │       │   ├─ Cierra info windows anteriores
  │       │   ├─ Muestra info window del marker tocado
  │       │   ├─ Centra el mapa en ese punto
  │       │   └─ Si hay ubicación → drawRoute(origin, destino, modo)
  │       └─ Centra el mapa en el primer punto
  │
  ├─ DisposableEffect(lifecycleOwner):
  │   └─ Maneja onResume/onPause del MapView
  │
  ├─ Selector de transporte (Surface + Row, esquina superior derecha):
  │   ├─ Botón "🚶 A pie" → WALKING → recalcula ruta si hay punto seleccionado
  │   └─ Botón "🚗 Auto" → DRIVING → recalcula ruta si hay punto seleccionado
  │
  └─ Panel de info de ruta (AnimatedVisibility, parte inferior):
      └─ Muestra: modo + distancia + tiempo estimado
```

### 5. Cálculo de Rutas (función drawRoute)
```
drawRoute(from: GeoPoint, to: GeoPoint, mode: TransportMode)
  │
  ├─ Limpia polylines anteriores
  │
  ├─ Determina medio y color:
  │   ├─ WALKING → MEAN_BY_FOOT, azul (#1976D2)
  │   └─ DRIVING → MEAN_BY_CAR, rojo (#D32F2F)
  │
  ├─ coroutineScope.launch(Dispatchers.IO):
  │   ├─ OSRMRoadManager (usa API de OSRM - OpenStreetMap Routing Machine)
  │   ├─ getRoad(arrayListOf(from, to))
  │   └─ Si road.mStatus == OK:
  │       ├─ Crea overlay (Polyline) con strokeWidth = 12f
  │       ├─ Agrega al mapa
  │       └─ setRouteInfo(RouteInfo(distanceKm, durationMinutes, mode))
  │
  └─ El panel inferior se muestra automáticamente (AnimatedVisibility observa routeInfo)
```

### 6. Interacción del Usuario
```
Tap en Marker:
  → Muestra info window con nombre
  → Centra mapa
  → Calcula y dibuja ruta desde ubicación actual
  → Muestra panel inferior con distancia y tiempo

Cambio de modo (pie/auto):
  → Recalcula ruta con nuevo medio
  → Cambia color de la línea
  → Actualiza panel inferior

Tap en zona vacía del mapa:
  → Cierra info windows
  → Limpia rutas
  → Oculta panel inferior
```

---

## 🧩 Diagrama de Dependencias

```
API (Django backend)
  └─► ApiService.getEstaciones()
        └─► EstacionesRepository.fetchEstaciones()
              └─► RecyclingMapViewModel (Hilt)
                    ├─ points: StateFlow<List<RecyclingPoint>>
                    ├─ routeInfo: StateFlow<RouteInfo?>
                    ├─ transportMode: StateFlow<TransportMode>
                    └─► RecyclingMapScreen (Compose)
                          ├─ AndroidView → MapView (osmdroid)
                          ├─ Markers (puntos de reciclaje)
                          ├─ Polyline (ruta calculada via OSRM)
                          ├─ MyLocationOverlay (ubicación GPS)
                          └─ UI Compose (botones transporte, panel ruta)
```

---

## 📦 Dependencias Externas

| Librería | Uso |
|----------|-----|
| `osmdroid-android:6.1.18` | MapView, tiles OpenStreetMap, overlays |
| `osmbonuspack:6.9.0` (JitPack) | Cálculo de rutas con OSRM |
| `taptargetview:1.13.3` | (Opcional, disponible pero no usada activamente) |

---

## 🔑 Puntos Clave

1. **Datos de la API**: Las estaciones vienen del backend Django (`GET api/estaciones/`), no están hardcodeadas.
2. **Mapa nativo en Compose**: Se usa `AndroidView` como puente porque osmdroid no tiene componentes Compose nativos.
3. **`clipToBounds()`**: Evita que el MapView nativo pinte sobre la TopBar de Compose.
4. **TopBar/BottomBar centralizadas**: Se renderizan en `MainApp.kt`, no dentro de la pantalla del mapa.
5. **Rutas via OSRM**: El cálculo se hace en `Dispatchers.IO` para no bloquear el hilo principal.
6. **Marcador de ubicación verde**: Se crea programáticamente un bitmap circular verde con borde blanco.

