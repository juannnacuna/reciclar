import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "edu.unlp.reciclar"
    compileSdk {
        version = release(36)
    }

    // 1. Cargamos el archivo local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }
    // 2. Habilitamos la generación de la clase BuildConfig
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "edu.unlp.reciclar"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 3. Extraemos la URL y la inyectamos.
        // ¡CUIDADO ACÁ! Necesitas escapar las comillas dobles ("\"...\"")
        // porque el valor final en Kotlin tiene que ser un String.
        val baseUrl = localProperties.getProperty("BASE_URL")
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Arquitectura y ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.androidx.activity.ktx)

    // Navegación
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Retrofit para Networking (Capa de Datos)
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson) // Para convertir JSON
    implementation(libs.gson)

    // Corrutinas (para manejo asíncrono en ViewModel y Repositorio)
    implementation(libs.kotlinx.coroutines.android)

    // Manejo de cámara
    implementation(libs.play.services.code.scanner)

    // dependencias Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
}