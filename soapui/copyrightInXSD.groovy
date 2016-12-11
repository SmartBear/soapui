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
  ~ Copyright 2004-${currentYear} SmartBear Software
  ~
  ~ Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
  ~ versions of the EUPL (the "Licence");
  ~ You may not use this work except in compliance with the Licence.
  ~ You may obtain a copy of the Licence at:
  ~
  ~ http://ec.europa.eu/idabc/eupl
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed under the Licence is
  ~ distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  ~ express or implied. See the Licence for the specific language governing permissions and limitations
  ~ under the Licence.
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