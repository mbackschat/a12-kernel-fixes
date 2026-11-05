package com.mgmtp.a12.kernel.gradle

import groovy.xml.XmlParser
import org.gradle.api.GradleException

/**
 * Static build helpers used from task actions.
 *
 * These live in a real (buildSrc) class on purpose: methods defined directly in a build script or in a
 * precompiled convention script are members of the script object, which is NOT available to a task action
 * (doFirst/doLast) when the configuration cache is enabled. Static methods of a compiled class, by contrast,
 * are reachable from such actions, so report/transform logic that runs at execution time belongs here.
 */
class BuildReportUtils {

    /**
     * Converts a single Checkstyle XML report into a JUnit-style XML report.
     */
    static void convertCheckstyleToJUnit(File checkstyleFile, File outputFile) {
        if (!checkstyleFile.exists()) {
            throw new GradleException("Checkstyle report not found at: ${checkstyleFile.path}")
        }

        final Node xml = new XmlParser().parse(checkstyleFile)

        int totalErrors = 0
        final int totalFilesChecked = xml.file.size()

        final String testcases = xml.file.stream().map(fileNode -> {
            def fileName = fileNode.@name
            def errors = fileNode.error
            totalErrors += errors.size()

            return """<testcase classname="check.Checkstyle" name="$fileName">
          |    ${errors.size() > 0 ? "<failure message=\"Wrong Copyright Header\" />" : ""}
          |    <system-out>${errors.stream().map { it.@line + ": " + it.@message }.collect().join('\n')}</system-out>
          |  </testcase>""".stripMargin()
        }).collect().join('\n')

        outputFile.text =
                """<?xml version="1.0" encoding="UTF-8"?>
                      |<testsuite failures="${totalErrors}" package="kernel" id="0" name="checkstyle" tests="$totalFilesChecked">
                      |  $testcases
                      |</testsuite>""".stripMargin()
    }

    /**
     * Deep-copies a parsed JSON tree (maps/lists/scalars) and drops any leaf String that matches the given
     * regex. Used to strip internal hostnames from generated SBOMs.
     */
    static Object filterJsonLeaves(Object node, String filterRegex) {
        if (node instanceof Map) {
            return node.collectEntries { k, v -> [k, filterJsonLeaves(v, filterRegex)] }
        } else if (node instanceof List) {
            return node.collect { filterJsonLeaves(it, filterRegex) }
        } else {
            def matches = (node instanceof String) && (node as String).matches(filterRegex)
            if (matches) {
                return null
            }
            return node
        }
    }
}
