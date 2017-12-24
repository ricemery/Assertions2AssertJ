# Assertions2AssertJ

IntelliJ Plugin to convert Junit and Hamcrest Assertions to AssertJ
Assertions. The converstion can be run on a single file, a module or
an entire project.


Note that AssertJ must be included in the IntelliJ project classpath for
the Plugin to successfully complete. 

Limitations
* The plugin does NOT convert Hamcrest assertions included in Mockito
verify statements.