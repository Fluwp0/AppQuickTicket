plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.quickticket.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.quickticket.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Requerido por AGP moderno y Compose
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Para usar Kotest (JUnit 5) en tests de unidad
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    // Compose BOM para alinear versiones de UI
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)

    // Jetpack Compose UI + Material3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity y Lifecycle (ViewModel en Compose)
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // Navigation Compose (para navegar entre Login, Home, Perfil, Carrito)
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Room (SQLite) con KSP
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore (guardar nombre + URI de foto temporalmente)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Permisos en Compose (galería)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Coil para mostrar imagen elegida desde la galería
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Corrutinas (StateFlow/Flows)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Retrofit para hablar con tu backend
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ---------- 🔍 TESTS DE LA GUÍA 15 ----------

    // Kotest (motor de tests + matchers) - JUnit 5
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")

    // MockK para mocks en tests de unidad
    testImplementation("io.mockk:mockk:1.13.12")

    // Tests de corrutinas
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // JUnit clásico (por si quieres usarlo también)
    testImplementation("junit:junit:4.13.2")

    // Instrumented tests (UI / integración)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Tests de Compose UI
    // androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // debugImplementation("androidx.compose.ui:ui-test-manifest")

}
