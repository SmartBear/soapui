Oh hi, I'm glad you decided to create a stepdef file!
-----------------------------------------------------

If you're new to the concept of steps please do some quick reading:

* https://github.com/cucumber/cucumber/wiki/Feature-Coupled-Step-Definitions-(Antipattern)
* https://github.com/cucumber/cucumber/wiki/Step-Organisation
* https://github.com/cucumber/cucumber/wiki/Feature-Introduction
* https://github.com/cucumber/cucumber/wiki/Conjunction-Steps-%28Antipattern%29

then organize it according to the following example structure based on domain concept:

    --src
    ----test
    ------java
    --------com
    ----------smartbear
    ------------soapui
    --------------stepdefs
    ----------------fest
    ------------------soap
    --------------------project
    ----------------------SoapProjectStepdefs.java
    --------------------refactoring
    ----------------------DefinitionRefactoringStepdefs.java
    ------------------rest
    --------------------project
    ----------------------RestProjectStepdefs.java
    --------------------authentication
    ----------------------OAuth2Stepdefs.java
    ----------------java
    ------------------licensing
    ----------------------LicensingStepdefs.java

**That's is!**