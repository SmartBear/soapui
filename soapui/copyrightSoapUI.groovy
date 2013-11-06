import static groovy.io.FileType.FILES

if(args.length !=2){
println "You need to pass two parameters. First one is path, and second one is file extension."
println "example of calling this script:"
println ">> copyrightSoapUI.groovy D:\\projects\\eviware\\soapui\\ .java"
System.exit(0)
}

def currentYear = new Date().getYear()+1900
def copyright = """/*\r
 *  SoapUI, copyright (C) 2004-${currentYear} eviware.com \r
 *\r
 *  SoapUI is free software; you can redistribute it and/or modify it under the \r
 *  terms of version 2.1 of the GNU Lesser General Public License as published by \r
 *  the Free Software Foundation.\r
 *\r
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without \r
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. \r
 *  See the GNU Lesser General Public License for more details at gnu.org.\r
 */\r
\r
"""

def updatedfiles=[]
def notUpdatedfiles=[]

new File(args[0]).eachFileRecurse(FILES){file-> 
  
    if(file.absolutePath.contains(File.separator+".svn"+File.separator)) return 
     println  file.absolutePath
    
    if(file.isFile() && file.name.endsWith(args[1])){
       def temp = copyright
       def packagePasssed = false
      
       file.eachLine{ line->
      
               if ( line =~ '^package *' ){
                   packagePasssed = true;
               } 
      
               if(packagePasssed){
                   temp += line + "\r\n"
               }      
       }
       
    if(packagePasssed){
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