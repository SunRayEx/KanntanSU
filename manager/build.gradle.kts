plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose.compiler) apply false
}

extra["androidMinSdkVersion"] = 31
extra["androidTargetSdkVersion"] = 37
extra["androidCompileSdkVersion"] = 37
extra["androidCompileSdkVersionMinor"] = 0
extra["androidBuildToolsVersion"] = "37.0.0"
extra["androidCompileNdkVersion"] = libs.versions.ndk.get()
extra["androidSourceCompatibility"] = JavaVersion.VERSION_21
extra["androidTargetCompatibility"] = JavaVersion.VERSION_21
extra["managerVersionCode"] = getVersionCode()
extra["managerVersionName"] = getVersionName()

fun getGitCommitCount(): Int {
    val process = Runtime.getRuntime().exec(arrayOf("git", "rev-list", "--count", "HEAD"))
    return process.inputStream.bufferedReader().use { it.readText().trim().toInt() }
}

fun getGitDescribe(): String {
    val process = Runtime.getRuntime().exec(arrayOf("git", "describe", "--tags", "--always"))
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}

fun runGit(vararg args: String): String {
    val process = Runtime.getRuntime().exec(arrayOf("git", *args))
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}

/**
 * Newest release tag reachable from HEAD.
 *
 * Plain `git describe` minimizes commit *distance*, so when release tags live on
 * side branches it under-reports the level: a main build that already contains v3.3.0
 * still describes itself as "v3.2.4-83-..." because v3.2.4 happens to sit closer on
 * the first-parent line. Sorting merged tags by version instead reports the level we
 * actually shipped.
 */
fun getNewestMergedTag(): String? {
    val tags = runGit("tag", "--merged", "HEAD", "--sort=-v:refname")
    return tags.lineSequence().firstOrNull { it.isNotBlank() }?.trim()?.takeIf { it.isNotEmpty() }
}

fun getShortHash(): String {
    return runGit("rev-parse", "--short", "HEAD")
}

fun getVersionCode(): Int {
    val commitCount = getGitCommitCount()
    return 30000 + commitCount
}

fun getVersionName(): String {
    // Fall back to describe when no tag is reachable (shallow clone, pre-tag repo).
    val newest = getNewestMergedTag() ?: return getGitDescribe()
    val ahead = runGit("rev-list", "--count", "$newest..HEAD").trim()
    return if (ahead.isEmpty() || ahead == "0") {
        newest
    } else {
        "$newest-$ahead-g${getShortHash()}"
    }
}
