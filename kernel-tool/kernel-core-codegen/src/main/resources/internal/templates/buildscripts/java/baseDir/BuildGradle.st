BuildGradle(t) ::= <<
plugins {
    id("java")
}

dependencies {
    implementation("com.mgmtp.a12.kernel:kernel-core-runtime-<t.runtimeArtifactIdPart>:<t.kernelVersion>.+")
}
>>
