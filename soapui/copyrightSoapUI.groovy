import static groovy.io.FileType.FILES

if(args.length !=2){
println "You need to pass two parameters. First one is path, and second one is file extension."
println "example of calling this script:"
println ">> copyrightSoapUI.groovy D:\\projects\\eviware\\soapui\\ .java"
System.exit(0)
}

def currentYear = new Date().getYear()+1900
def copyright = """/*\r
 * SoapUI, Copyright (C) 2004-${currentYear} SmartBear Software \r
 *\r
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent \r
 * versions of the EUPL (the "Licence"); \r
 * You may not use this work except in compliance with the Licence. \r
 * You may obtain a copy of the Licence at: \r
 * \r
 * http://ec.europa.eu/idabc/eupl \r
 * \r
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is \r
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either \r
 * express or implied. See the Licence for the specific language governing permissions and limitations \r
 * under the Licence. \r
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