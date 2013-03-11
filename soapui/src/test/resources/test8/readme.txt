This setup splits the binding and service into 2 files; the later imports the former, which includes
the first xsd which imports the second, now all are in different directories. This differs from test7 by using
upward relative imports for the second xsd