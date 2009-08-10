@echo off

rem  Copyright (c) 2002-2004 by The Web Services-Interoperability Organization (WS-I) and 
rem  Certain of its Members. All Rights Reserved.
rem	
rem  Notice
rem  The material contained herein is not a license, either expressly or impliedly, to any 
rem  intellectual property owned or controlled by any of the authors or developers of this 
rem  material or WS-I. The material contained herein is provided on an "AS IS" basis and to 
rem  the maximum extent permitted by applicable law, this material is provided AS IS AND WITH 
rem  ALL FAULTS, and the authors and developers of this material and WS-I hereby disclaim all 
rem  other warranties and conditions, either express, implied or statutory, including, but not 
rem  limited to, any (if any) implied warranties, duties or conditions of  merchantability, 
rem  of fitness for a particular purpose, of accuracy or completeness of responses, of results, 
rem  of workmanlike effort, of lack of viruses, and of lack of negligence. ALSO, THERE IS NO 
rem  WARRANTY OR CONDITION OF TITLE, QUIET ENJOYMENT, QUIET POSSESSION, CORRESPONDENCE TO 
rem  DESCRIPTION OR NON-INFRINGEMENT WITH REGARD TO THIS MATERIAL.
rem	
rem  IN NO EVENT WILL ANY AUTHOR OR DEVELOPER OF THIS MATERIAL OR WS-I BE LIABLE TO ANY OTHER 
rem  PARTY FOR THE COST OF PROCURING SUBSTITUTE GOODS OR SERVICES, LOST PROFITS, LOSS OF USE, 
rem  LOSS OF DATA, OR ANY INCIDENTAL, CONSEQUENTIAL, DIRECT, INDIRECT, OR SPECIAL DAMAGES 
rem  WHETHER UNDER CONTRACT, TORT, WARRANTY, OR OTHERWISE, ARISING IN ANY WAY OUT OF THIS OR 
rem  ANY OTHER AGREEMENT RELATING TO THIS MATERIAL, WHETHER OR NOT SUCH PARTY HAD ADVANCE 
rem  NOTICE OF THE POSSIBILITY OF SUCH DAMAGES.
rem	
rem
rem  WS-I License Information
rem  Use of this WS-I Material is governed by the WS-I Test License and other licenses.  Information on these 
rem  licenses are contained in the README.txt and ReleaseNotes.txt files.  By downloading this file, you agree 
rem  to the terms of these licenses.
rem	
rem  How To Provide Feedback
rem  The Web Services-Interoperability Organization (WS-I) would like to receive input, 
rem  suggestions and other feedback ("Feedback") on this work from a wide variety of 
rem  industry participants to improve its quality over time. 
rem	
rem  By sending email, or otherwise communicating with WS-I, you (on behalf of yourself if 
rem  you are an individual, and your company if you are providing Feedback on behalf of the 
rem  company) will be deemed to have granted to WS-I, the members of WS-I, and other parties 
rem  that have access to your Feedback, a non-exclusive, non-transferable, worldwide, perpetual, 
rem  irrevocable, royalty-free license to use, disclose, copy, license, modify, sublicense or 
rem  otherwise distribute and exploit in any manner whatsoever the Feedback you provide regarding 
rem  the work. You acknowledge that you have no expectation of confidentiality with respect to 
rem  any Feedback you provide. You represent and warrant that you have rights to provide this 
rem  Feedback, and if you are providing Feedback on behalf of a company, you represent and warrant 
rem  that you have the rights to provide Feedback on behalf of your company. You also acknowledge 
rem  that WS-I is not required to review, discuss, use, consider or in any way incorporate your 
rem  Feedback into future versions of its work. If WS-I does incorporate some or all of your 
rem  Feedback in a future version of the work, it may, but is not obligated to include your name 
rem  (or, if you are identified as acting on behalf of your company, the name of your company) on 
rem  a list of contributors to the work. If the foregoing is not acceptable to you and any company 
rem  on whose behalf you are acting, please do not provide any Feedback.
rem	
rem  WS-I members should direct feedback on this document to wsi_testing@lists.ws-i.org; 
rem  non-members should direct feedback to wsi-tools@ws-i.org. 

rem Verify that WSI home directory is set
if "%WSI_HOME%"=="" goto usage

setlocal

rem Set environment
call "%WSI_HOME%\java\bin\setenv"

rem Run command
"%JAVA%" %WSI_JAVA_OPTS% -Dwsi.home="%WSI_HOME%" -cp "%WSI_CP%" org.wsi.test.monitor.Monitor %*

goto end

:usage
echo.
echo Usage: Monitor -config ^<filename^>
echo.
echo NOTE:  The WSI_HOME environment variable must be set to the location 
echo        of the installed files [Example: c:\wsi-test-tools].
echo.

:end

endlocal




