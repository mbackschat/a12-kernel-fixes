BuildGradle(t) ::= <<
plugins {
    id("java")
}

dependencies {
    implementation("com.mgmtp.a12.kernel:kernel-core-runtime-<t.runtimeArtifactIdPart>:<t.kernelVersion>.+")
}

sourceSets {
    main {
        java {
            srcDirs = ['.']
            exclude 'build/**'
            exclude '.gradle/**'
        }
        resources {
            srcDirs = ['.']
            exclude '**/*.java'
            exclude 'build/**'
            exclude 'build.gradle'
            exclude 'settings.gradle'
            exclude '.gradle/**'
        }
    }
}
>>