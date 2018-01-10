# Assertions2AssertJ

IntelliJ Plugin to convert Junit and Hamcrest Assertions to AssertJ
Assertions. The conversion can be run on a single file, a module or
an entire project.


Note that AssertJ must be included in the IntelliJ project classpath for
the Plugin to successfully complete. 

Limitations
* The plugin does NOT convert Hamcrest assertions included in Mockito
verify statements.
* The plugin does NOT convert all assertions that utilize matchers embedded 
within other matchers (e.g. anyOf(equalTo(1.0), equalTo(2.0))). 
* When a file contains both assertions that are converted, and assertions that
are ignored the Hamcrest imports will still be deleted.

