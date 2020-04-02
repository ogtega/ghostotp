object Versions {
    val kotlin = "1.3.61"
    val lifecycle = "2.2.0"
    val navigation = "2.2.1"
    val room = "2.2.4"
    val work = "2.3.3"
    val build_tools = "29.0.3"
}

object Dependencies {
    val androidx = "androidx.core:core-ktx:1.2.0"

    val fragment = "androidx.fragment:fragment-ktx:1.2.2"
    val preference =  "androidx.preference:preference-ktx:1.1.0"
    val work = "androidx.work:work-runtime-ktx:${Versions.work}"
    val appcompat = "androidx.appcompat:appcompat:1.1.0"
    val constraint_layout = "androidx.constraintlayout:constraintlayout:1.1.3"
    val material = "com.google.android.material:material:1.1.0"

    val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"

    val lifecycle_common = "androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}"
    val lifecycle_ext = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"
    val lifecycle_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"

    val room_ktx = "androidx.room:room-ktx:${Versions.room}"
    val room_runtime = "androidx.room:room-runtime:${Versions.room}"

    val navigation_fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    val navigation_ui = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"


    val apache_codec = "commons-codec:commons-codec:1.14"
    val picasso = "com.squareup.picasso:picasso:2.71828"

    val coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0"
    val coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.0"
    val okhttp = "com.squareup.okhttp3:okhttp:4.3.1"
}