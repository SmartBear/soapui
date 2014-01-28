Oh hi, I'm glad you decided to create a feature file!
-----------------------------------------------------

Please organize it according to the following example structure based on features:

    --src
    ----test
    ------resources
    --------features
    ----------soap
    ------------definition-refactoring
    --------------refactor-schema.feature
    --------------update-xpath-expression.feature
    ----------rest
    ------------request
    --------------resource-path-dropdown.feature
    --------------parameters.feature

... and there are a few more thing that you need to know

* If you are creating an automated test - please use the @Automated annotation in the file header
* If you are creating a manual test - please use the @Manual annotation in the file header
* If the test is for a upcoming version - please use the @Acceptance annotation
* If the test is for older versions - please use the @Regression annotation

#### NOTE!
Currenly you need to use the "Given SoapUI is started" and "Then/And close SoapUI" steps in all your feature files
to initialize and cleanup the FEST runner. We're looking in to a better way to do this.

**That's it!**
