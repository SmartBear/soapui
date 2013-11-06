import static groovy.io.FileType.FILES

if(args.length !=2){
println "You need to pass two parameters. First one is path, and second one is file extension."
println "example of calling this script:"
println ">> copyrightInXML.groovy ./src/xsd/soapui .xsd"
System.exit(0)
}

def currentYear = new Date().getYear()+1900
def copyright = """<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ SoapUI, copyright (C) 2004-${currentYear} eviware.com
  ~
  ~ SoapUI is free software; you can redistribute it and/or modify it under the
  ~ terms of version 2.1 of the GNU Lesser General Public License as published by
  ~ the Free Software Foundation.
  ~
  ~ SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  ~ even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  ~ See the GNU Lesser General Public License for more details at gnu.org.
  -->
"""


def updatedfiles=[]
def notUpdatedfiles=[]

new File(args[0]).eachFileRecurse(FILES){file-> 
  
    if(file.absolutePath.contains(File.separator+".svn"+File.separator)) return 
     println  file.absolutePath
    
    if(file.isFile() && file.name.endsWith(args[1])){
       def temp = copyright
       def schemaPasssed = false
      
       file.eachLine{ line->
             if ( line =~'schema *' ){
						schemaPasssed = true;
						
				  }
				  
				  if(schemaPasssed){
					  temp += line + "\n"
				 }
       }
       
    if(schemaPasssed){
        file.write(temp)
        updatedfiles <<file.absolutePath
        }
    else{
        notUpdatedfiles <<file.absolutePath
        } 
    
    }
}

updatedfiles.each{ println " UPDATED: ${it}"}
notUpdatedfiles.each{ println "NOT UPDATED: ${it}"}