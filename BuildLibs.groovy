import groovy.util.AntBuilder
import groovy.util.XmlSlurper
import groovy.xml.*

if(args.length != 4)
{
	println "Got ${args.length} arguments: ${args}, expected 3"
	println "Usage: EclipseDist <project.xml> <libs plugin> <mavenRepo> <pom.currentVersion>"
	return
}
		
def projectXml = args[0]
def libsPlugin = args[1]
def libDir = libsPlugin + "/lib"
def mavenRepo = args[2]
def pomCurrentVersion = args[3]
println "EclipseDist: Copy libraries in ${projectXml} from ${mavenRepo} to ${libDir}"

def libs = []

def ant = new AntBuilder();

// Find the existing files in the libs plugins
oldFiles = [:]
new File(libDir).eachFile
{
   if(it.name != ".svn")
   {
      oldFiles[it.name] = it
   }
}


// Copy all dependencies with <jnlp.jar>true</jnlp.jar> from mavenRepo to libDir
def project = new XmlSlurper().parse(projectXml)
for(dependency in project.dependencies.dependency)
{
	def jnlp = dependency.properties['jnlp.jar'].text()
	def eclipse = dependency.properties['eclipse.jar'].text()
	if(jnlp == 'true' || eclipse == 'true')
	{
		// Special handling of soapui-xmlbeans <version>${pom.currentVersion}</version>
		def version = dependency.version
		if(version == '${pom.currentVersion}')
			version = pomCurrentVersion
				
		def jarName = "${dependency.artifactId}-${version}.${dependency.type}"
		def fileName = "${mavenRepo}/${dependency.groupId}/jars/${jarName}"
		libs.add(jarName)
		if(oldFiles[jarName] == null)
		{
			ant.copy(todir: libDir, file: fileName)
		}
		else
		{
		   oldFiles.remove(jarName)
		}
	}
}

// Don't delete the whole directory, because we don't want to delete .svn
for(oldFile in oldFiles.values)
{
	ant.delete(file: oldFile)
}

// Update libs/.classpath
def classpathFile= new FileWriter(libsPlugin + "/.classpath")
classpathFile.write '<?xml version="1.0" encoding="UTF-8"?>\n'
def classpathXML = new MarkupBuilder(classpathFile)
classpathXML.classpath {
   classpathentry(kind: "src", path: "src")
   new File(libDir).eachFile{ file ->
      classpathentry(kind: "lib", exported: "true", path: "lib/" + file.name)   
   }
   classpathentry(kind: "con", path:"org.eclipse.jdt.launching.JRE_CONTAINER")
   classpathentry(kind: "output", path:"bin")
}
classpathFile.close()



/* This does not work with plugin.xml ->

// Update Bundle-ClassPath in MANIFEST.MF
// Careful, this is extremely sensitive
def manifest = 'META-INF/MANIFEST.MF'
def backup = 'MANIFEST.MF.bak'
ant.move(tofile: backup, file: manifest)
def writer = new PrintStream(manifest)
def inBundleClasspath = false
new File(backup).eachLine
{
	if(it.startsWith('Bundle-ClassPath: '))
	{
		inBundleClasspath = true
		
		writer.print 'Bundle-ClassPath: soapui-plugin.jar,\n'
		int i = 0
		for(lib in libs)
		{
			writer.print " ${libDir}/${lib}"
			if(i < libs.size() - 1)
				writer.print ","
			writer.print "\n"
			i++
		}
	}
	else if(inBundleClasspath)
	{
		if(!it.startsWith(' '))
		{
			inBundleClasspath = false
		}
	}
	if(!inBundleClasspath)
	{
		writer.print it + "\n"
	}
}
*/
