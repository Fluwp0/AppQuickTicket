# QuickTicket 

Aplicación móvil académica para generar el ticket del comedor y usar catálogo + carrito.

---

## Cómo hacer funcionar la app

### 1) Iniciar backend (Render)
Abrir el siguiente link y esperar aprox. 2 minutos a que el backend inicie:

https://quickticket-backend.onrender.com/

---

### 2) Acceder a la base de datos (H2)
Para ver las tablas de la base de datos, abrir:

https://quickticket-backend.onrender.com/h2-console

JDBC URL:
jdbc:h2:file:./data/quickticket-db

Usuario: sa  
Contraseña: (vacía)

---

### 3) Ejecutar la app Android
1. Abrir el proyecto en Android Studio.
2. Verificar la URL del backend en:
   app/src/main/java/com/quickticket/app/network/RetrofitClient.kt

   BASE_URL = "https://quickticket-backend.onrender.com/"

3. Ejecutar la app en emulador o dispositivo físico.

---

## APK
Archivo incluido: app-debug.apk

---

## Nota
Si el backend estuvo inactivo, puede tardar 1–2 minutos en responder la primera vez.
