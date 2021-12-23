# SoapUI Docker TestRunner

## Building an Image

Perform the following steps to build an image:

* In the file *Dockerfile* change *SOAPUI_VERSION* to the appropriate value
* Download the installer *SoapUI-x64-$SOAPUI_VERSION.sh* and place it to the folder *Files* of the repository
* Update the line endings in the files _EntryPoint.sh_ and _RunProject.sh_ from Windows format to Unix (from CRLF to LF).
* Open *cmd.exe* in the repository root folder and execute the following command:
  ```
  docker build --build-arg SOAPUI_VERSION=5.6.1 -t smartbear/soapuios-testrunner:5.6.1 .
  ```

## Running Containers

Use the following command line to run a new container:

```
docker run -v="Local_Project_Folder":/project -v="Local_Reports_Folder":/reports -v="Local_Extensions_Folder":/ext -e COMMAND_LINE="SoapUI_TestRunner_Command_Line" -it smartbear/soapuios-testrunner:latest
```

For example:

```
docker run -v="C:\Users\avdee\Documents\soapui-docker-testrunner\Sample":/project -v="C:\Users\avdee\Documents\soapui-docker-testrunner\Sample\Reports":/reports -v="C:\Users\avdee\Documents\soapui-docker-testrunner\Sample\Extensions":/ext -e COMMAND_LINE="-f/%reports% -a -j '/%project%/sample-soapui-project.xml'" -it smartbear/soapuios-testrunner:latest
```

**Note:** on some systems the paths that are specified for _-v_ must be changed in the following way: _"C:\TestRunner\Project"_ to _"/host_mnt/C/TestRunner/Project"_.

**Note:** if Docker exits with the error _"Drive has not been shared"_ then it is necesary [to share the drive](https://scottseely.com/2017/12/29/copying-files-from-a-docker-container-onto-local-machine/).

### Parameters:

* [required] *-v="Local_Project_Folder":/project* - the folder with a project and all the project dependencies. All the files will be copied to a container so a parallel execution is possible
* [required] -e COMMAND_LINE="SoapUI_TestRunner_Command_Line". Example:
  ```
  -f/%reports% -a -j '/%project%/soapui-project.xml'
  ```
  There are two special variable here: *%project%* refers to a folder on a container that contains a SoapUI project with all the dependencies, *%reports%* refers to a folder that will contain SoapUI reports
* [optional] *-v="Local_Reports_Folder":/reports* - the folder that will contain the SoapUI reports
* [optional] *-v="Local_Extensions_Folder":/ext* - the content of this folder wil lbe copied to SoapUI's ext directory. It can be used to pass the necessary JDBC drivers, etc.

### The exit codes are:

* **Docker** exit codes correspond to [chroot standard](http://tldp.org/LDP/abs/html/exitcodes.html)
* **TestRunner** exit codes:
	* *102*: a SoapUI project was not found
	* *103*: project execution related errors
