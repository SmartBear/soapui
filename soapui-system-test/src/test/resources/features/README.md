Oh hi, I'm glad you decided to create a feature file!
-----------------------------------------------------

Please organize it according to the following structure based on features:

    --src
    ----test
    ------resources
    --------features
    ----------Mocking
    ------------soap-mocking
    ------------rest-mocking 
    ----------REST
    ------------projects-creation (REST only)
    ------------top-URI-bar
    ------------parameters
    ------------request-editor
    ------------test-step-editor
    ------------oauth
    ------------rest_discovery (Pro)
    ----------project-creation (import, generic and SOAP. except REST)
    ----------generate-test-suites
    ------------insert-test-step (different test steps in os or Pro)
    ------------set-assertions
    ------------data-driven-testing (Pro,data source <grid,file,Excel,JDBC,XML> and data source loop)
    ------------property (property expansion-Pro only /property transfer)
    ----------operate-test-steps (create/edit/reorder/clone/delete/disable)
    ----------run-tests (in different levels)
    ----------get-reports ( how can we check the reports contents?)
    ----------environment (settings and switch)
    ----------command-line (functional,security,load,mockservice and reports --different options settings )
    ----------load-test
    ----------security-test

... and there are a few more thing that you need to know

* If you are creating an automated test - please use the @Automated annotation in the file header
* If you are creating a manual test - please use the @Manual annotation in the file header
* If the test is for a upcoming version - please use the @Acceptance annotation
* If the test is for older versions - please use the @Regression annotation


**That's it!**


