![UML](http://i.imgur.com/nWZANfA.png)

# Team member contributions
## Joe
* Added functionality to Pattern.toGraphviz() so it recognizes fields and methods.
* Repackaged everything.
* Added Association Pattern functionality.
* Added bidirectional arrow functionality.
* Integrated dependency functionality for method return types and method bodies.
* Added cardinality labeling to single and bidirectional arrows.
* Fixed various ConfigSettings bugs.
* Added functionality for including or excluding Synthetic methods and fields.
* Added the ability to specify arguments for Patterns and PatternDecorators within the config file.
* Added the ability to blacklist classes when considering dependency inversion principle violations.
* Added a ColorPatternDecorator that accepts colors as arguments within the config file (and removed existing decorators such as PurplePatternDecorator.)
* The King of Swing.


## Kieran
* Initial class skeletons
* Added detection and display of "implements" relations
* Parse and display class names
* Improved parsing to capture and display types of generically typed classes
* Added detection of dependencies in return types and arguments
* Added drawing of dependency arrows
* Detection and rendering of Singleton patterns
* Bidirectional highlighting/project setup
* Adapter, Decorator, Bad Decorator detection implementatio


## Lewis
* Wrote extensive parser up to generating Graphviz code.
* Created base style for the graph generator decorators.
* Updated Javadoc comments.
* Implemented inheritance arrows and changed labels to HTML.
* Implemented access-level field/method hiding.
* Master UML maker/curator.
* Created ConfigSettings
* Created the Inheritance Pattern
* Updated README to describe the usage of the config file
