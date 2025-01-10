toxsoft extlibs for org.openjdk.nashorn 

useful links:  
https://mvnrepository.com/artifact/org.ow2.asm/asm/9.7.1
https://mvnrepository.com/artifact/org.ow2.asm/asm-commons/9.7.1
https://mvnrepository.com/artifact/org.ow2.asm/asm-util/9.7.1
https://mvnrepository.com/artifact/org.ow2.asm/asm-tree/9.7.1
https://mvnrepository.com/artifact/org.ow2.asm/asm-analysis/9.7.1
https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/15.6/
https://central.sonatype.com/artifact/org.openjdk.nashorn/nashorn-core/15.6/versions

https://github.com/openjdk/nashorn?tab=readme-ov-file
https://docs.oracle.com/en/java/javase/14/nashorn/#GUID-E409CC44-9A8F-4043-82C8-6B95CD939296
https://kmsquare.in/blog/running-javascript-using-nashorn-in-jvm/
https://community.developer.atlassian.com/t/issues-with-nashorn-script-engine-and-jdk17-in-confluence-8-8-0/78456

   // При инициализии движка под RCP (под консольным приложением такого нет) могут возникнуть вопросы доступа к библиотеке: factory.getEngineByName( "nashorn" ) возвращает null.
   // Гарантированным, универсальным методом использования библиотеки (и для консольного приложения и для RCP) ялвяется использование конструктора ScriptEngineManager 
   // с указанием classloader-а с помощью которого был загружен класс NashornScriptEngineFactory (фабрика движка nashorn). 
   // Например:
   ScriptEngineManager factory = new ScriptEngineManager( NashornScriptEngineFactory.class.getClassLoader() );
   ScriptEngine engine = factory.getEngineByName( "nashorn" );
   try {
      engine.eval( "print('Hello, World!');" );
   }
   catch( ScriptException ex ) {
      // TODO Auto-generated catch block
      LoggerUtils.errorLogger().error( ex );
   }

2025-01-08 mvk