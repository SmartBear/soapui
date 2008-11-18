;--------------------------------
;INFO
;--------------------------------

; Script written by Niclas Reimertz
; I used HM NIS Edit Wizard
; soapui installer
; March 16 2006
; version 1.0 :-D

;--------------------------------
; Before compiling
;--------------------------------
; make sure !define PRODUCT_VERSION  is set
;--------------------------------
; Prereqs
;--------------------------------
; NSIS installed (http://nsis.sourceforge.net/)
; MUI Installed (comes with nsis)
;
;--------------------------------
; TO DO:s
;--------------------------------
; fix all options for running with a jre
;HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Runtime Environment\<version number>
;HKEY_LOCAL_MACHINE\Software\JavaSoft\Java Development Kit\<version number>
; good splash!!!
; better icons!!!
;--------------------------------

;--------------------------------
; Version History
;--------------------------------

; -----
; 1.00
; April 3 2006
; improved for release 1.5

; -----
; 1.00
; March 18 2006
; for release 1.5

; -----
; 0.93
; November 26 2005
; got rid of all the files and used directories (file /r *.*) instead
; -----
; 0.92
; November 16 2005
; components page
; -----
; 0.91
; November 08 2005
; installs source
; uninstalls source
; fixed bug where desktop icon wasn't unistalled
; ----
; 0.9
; November 07 2005
; installs jre
; uninstalls jre
; ----
; 0.85
; November 06 2005
; got the unistaller to work correct, it leaves the docs folder.
; added first icon, not beutiful, but it works
; added link to documentation in start menu
;---
; 0.8 original
; October 30 2005

; 
; 
;--------------------------------

SetCompressor lzma

;--------------------------------
; Defines
;--------------------------------
!define PRODUCT_NAME "soapUI"
!define PRODUCT_VERSION "@SOAPUIVERSION@"
!define PRODUCT_PUBLISHER "eviware"
!define PRODUCT_WEB_SITE "http://www.soapui.org"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}-${PRODUCT_VERSION}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"
!define PRODUCT_STARTMENU_REGVAL "NSIS:StartMenuDir"
!define INSTALLER_SOURCE "..\..\target\standalone"

;--------------------------------
; MUI 1.67 compatible ------
;--------------------------------

!include "MUI.nsh"
!include "Sections.nsh"

;--------------------------------
; MUI Settings
;--------------------------------

!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "resources\images\eviware.bmp" ; optional
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

InstType "Complete"
InstType "Light"

;--------------------------------
; Pages
;--------------------------------

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!insertmacro MUI_PAGE_LICENSE "${INSTALLER_SOURCE}\LICENSE.txt"


;components page
;!define MUI_COMPONENTSPAGE_TEXT_TOP ""
!define MUI_COMPONENTSPAGE_TEXT_DESCRIPTION_TITLE "Description"
!define MUI_COMPONENTSPAGE_TEXT_DESCRIPTION_INFO "Description info"
!define MUI_COMPONENTSPAGE_SMALLDESC

!insertmacro MUI_PAGE_COMPONENTS

; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Start menu page
var ICONS_GROUP
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "${PRODUCT_NAME} ${PRODUCT_VERSION}"
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "${PRODUCT_UNINST_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "${PRODUCT_STARTMENU_REGVAL}"
!insertmacro MUI_PAGE_STARTMENU Application $ICONS_GROUP
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_FUNCTION "LaunchSoapUI"
!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\readme.txt"
!insertmacro MUI_PAGE_FINISH

  ;strings
    LangString DESC_SEC01 ${LANG_ENGLISH} "Installs ${PRODUCT_NAME} ${PRODUCT_VERSION}."
    LangString DESC_SEC04 ${LANG_ENGLISH} "Installs JRE to be used by ${PRODUCT_NAME}. Uncheck if you already have a JRE installed and in path"
    LangString info_text ${LANG_ENGLISH} "${PRODUCT_NAME} install"

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "English"

;--------------------------------
; include zip funtionality ------
;--------------------------------

;!include "${NSISDIR}\plugins\ZipDLL.nsh"

; MUI end ------

;--------------------------------
;SCRIPT
;--------------------------------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "..\..\target\distributions\${PRODUCT_NAME}-${PRODUCT_VERSION}-installer.exe"
InstallDir "$PROGRAMFILES\${PRODUCT_PUBLISHER}\${PRODUCT_NAME}-${PRODUCT_VERSION}"
ShowInstDetails show
ShowUnInstDetails show


;--------------------------------
;splash
;--------------------------------

Function .onInit
	; the plugins dir is automatically deleted when the installer exits
	InitPluginsDir
	File /oname=$PLUGINSDIR\splash.bmp "resources\images\soapui-splash.bmp"
    advsplash::show 3000 1000 500 -1 "$PLUGINSDIR\splash"
        Pop $0 ; $0 has '1' if the user closed the splash screen early,	'0' if everything closed normally, and '-1' if some error occurred.
FunctionEnd


;section soapui

Section "!${PRODUCT_NAME}" SEC01
SectionIn 1 2
  SetOutPath "$INSTDIR\bin"
  SetOverwrite try
        File /r "${INSTALLER_SOURCE}\bin_nojre\*.*"
  SetOutPath "$INSTDIR"
        File "${INSTALLER_SOURCE}\LICENSE.txt"
  SetOutPath "$INSTDIR\licenses"
        File /r "${INSTALLER_SOURCE}\licenses\*.*"
  SetOutPath "$INSTDIR"
        File "${INSTALLER_SOURCE}\readme.txt"
        File "resources\images\soapui.ico"
  SetOutPath "$INSTDIR\Tutorials"
  		File "${INSTALLER_SOURCE}\Tutorials\sample-soapui-project.xml"
  		File "${INSTALLER_SOURCE}\Tutorials\Sample REST Project-soapui-project.xml"
  SetOutPath "$INSTDIR\Tutorials\WSDL-WADL"	
  		File "${INSTALLER_SOURCE}\Tutorials\WSDL-WADL\*.*"
  SetOutPath "$INSTDIR\Tutorials\restexample\NewsSearchService\V1"	
  		File "${INSTALLER_SOURCE}\Tutorials\restexample\NewsSearchService\V1\*.*"
  SetOutPath "$INSTDIR\Tutorials\restexample\NewsSearchService\V1\madonna_html_files"	
  		File "${INSTALLER_SOURCE}\Tutorials\restexample\NewsSearchService\V1\madonna_html_files\*.*" 		
  SetOutPath "$INSTDIR\Tutorials\restexample\NewsSearchService\V1"	
  		File "${INSTALLER_SOURCE}\Tutorials\WSDL-WADL\*.*"
  		
  SetOutPath "$INSTDIR\lib"
        File /r "${INSTALLER_SOURCE}\lib\*.*"
        
SectionEnd

;jre

Section "JRE 1.6.0_10" SEC04
SectionIn 1
   SetOutPath "$INSTDIR\jre"
        File /r "${INSTALLER_SOURCE}\jre\*.*"
   SetOutPath "$INSTDIR\bin"
      SetOverwrite try
        File /r "${INSTALLER_SOURCE}\bin\*.*"
SectionEnd

;--------------------------------
;Macro for Descriptions
;--------------------------------

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC01} $(DESC_SEC01)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC04} $(DESC_SEC04)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;ShortCut creation
;--------------------------------

Section -Startmenu
; Shortcuts
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  ; CreateDirectory ""
  SetOutPath "$INSTDIR\bin\"
    CreateShortCut "$DESKTOP\${PRODUCT_NAME} ${PRODUCT_VERSION}.lnk" "$INSTDIR\bin\soapui.bat" "" "$INSTDIR\soapui.ico"
    CreateShortCut "$STARTMENU.lnk" "$INSTDIR\bin\soapui.bat" "" "$INSTDIR\soapui.ico"
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

Section -AdditionalIcons
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application

  WriteIniStr "$INSTDIR\${PRODUCT_NAME}-${PRODUCT_VERSION}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}-${PRODUCT_VERSION}-UserGuide.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}/userguide/index.html"
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}-${PRODUCT_VERSION}-JavaDoc.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}/apidocs/index.html"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"

  SetOutPath "$INSTDIR\bin\"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\${PRODUCT_NAME} ${PRODUCT_VERSION}.lnk" "$INSTDIR\bin\soapui.bat" "" "$INSTDIR\soapui.ico"
  SetOutPath "$INSTDIR\"
  
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\soapui.org website.lnk" "$INSTDIR\${PRODUCT_NAME}-${PRODUCT_VERSION}.url"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Online User Guide.lnk" "$INSTDIR\${PRODUCT_NAME}-${PRODUCT_VERSION}-UserGuide.url"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Online JavaDoc.lnk" "$INSTDIR\${PRODUCT_NAME}-${PRODUCT_VERSION}-JavaDoc.url"
  
  AddUninstallLinks:
    CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Uninstall.lnk" "$INSTDIR\uninst.exe"
  
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

;--------------------------------
;Uninstaller Creation
;--------------------------------

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd

Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
  Abort
FunctionEnd


;--------------------------------
;The Uninstaller
;--------------------------------

Section Uninstall
  !insertmacro MUI_STARTMENU_GETFOLDER "Application" $ICONS_GROUP
  
  Delete "$INSTDIR\${PRODUCT_NAME}-${PRODUCT_VERSION}.url"
  Delete "$INSTDIR\${PRODUCT_NAME}-${PRODUCT_VERSION}-JavaDoc.url"
  Delete "$INSTDIR\${PRODUCT_NAME}-${PRODUCT_VERSION}-UserGuide.url"
  
  Delete "$INSTDIR\uninst.exe"
  Delete "$INSTDIR\sample-soapui-project.xml"
  Delete "$INSTDIR\readme.txt"
  Delete "$INSTDIR\soapui.ico"
  Delete "$INSTDIR\LICENSE.txt"

;
;deleting from the menu
;

  Delete "$SMPROGRAMS\$ICONS_GROUP\Uninstall.lnk"
  Delete "$SMPROGRAMS\$ICONS_GROUP\soapui.org website.lnk"
  Delete "$SMPROGRAMS\$ICONS_GROUP\${PRODUCT_NAME} ${PRODUCT_VERSION}.lnk"
  Delete "$SMPROGRAMS\$ICONS_GROUP\Online JavaDoc.lnk"
  Delete "$SMPROGRAMS\$ICONS_GROUP\Online UserGuide.lnk"
  Delete "$STARTMENU.lnk"
  Delete "$DESKTOP\${PRODUCT_NAME} ${PRODUCT_VERSION}.lnk"

;
;deleting directories
;

  RMDir /r "$SMPROGRAMS\$ICONS_GROUP"
  RMDir /r "$INSTDIR\licenses"
  RMDir /r "$INSTDIR\lib"
  RMDir /r "$INSTDIR\bin"
  RMDir /r "$INSTDIR\jre"
  RMDir "$INSTDIR"
  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  SetAutoClose true
SectionEnd

; used for launching soapui from final page
Function LaunchSoapUI
  ExecShell "" "$SMPROGRAMS\$ICONS_GROUP\${PRODUCT_NAME} ${PRODUCT_VERSION}.lnk"
FunctionEnd


