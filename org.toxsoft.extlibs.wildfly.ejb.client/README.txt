===========================================================================================================
Источник: https://github.com/jbossas/jboss-ejb-client/releases
Текущий : jboss-ejb-client-4.0.44.Final (on 9 Nov 2021)

===========================================================================================================
Примечание: номер версии наследуется от соответствующего пакета:
  $JBOSS_HOME/modules/system/layers/base/org/jboss/ejb-client/main/jboss-ejb-client-4.0.44.Final.jar 
  на основе которого создается патч библиотеки ts-jboss-ejb-client-4.0.44.Final

Назначение:
Встроенный код toxsoft реализующий разблокирование (с помощью runtime error: S5RemoteInvocationException) remote-EJB вызовов при обрыве связи с сервером.
Реализация обеспечивается классами S5Xxx и инъекцией кода в jboss-ejb-client

===========================================================================================================
Описание сборки новых ru.toxsoft.wildfly.client-xxx.jar:

Перед первой сборкой новых исходников ejb-client не забываем:

1. Изменить идентификатор артифакта собираемого проекта (файл: javax/pom.xml):
   Старое имя:
   <artifactId>jboss-ejb-client</artifactId>
   Новое имя:
   <artifactId>org.toxsoft.extlib.wildfly.ejb.client</artifactId>

2. Убедиться что установлен правильный номер версии
    <version>4.0.44.Final</version>

(*)3. Удалить конфигурацию модулей вызывающие конфликты проверки стиля. Файл ...\jboss-ejb-client-Xxx.Final\src\config\checkstyle.xml:
   ...
    <!-- mvk -->
    <!-- Suppress certain checkstyle rules for certain files 
    <module name="SuppressionFilter">
        <property name="file" value="src/config/checkstyle-suppressions.xml"/>
    </module> -->
   ...
    <!-- mvk -->
    <module name="TreeWalker">
    </module> -->
    ...
   Возможно еще другие (смотри сообщения диалога Eclipse)

...


    <properties>
    <!-- mvk -->
    <!-- <version.checkstyle.plugin>2.9.1</version.checkstyle.plugin> -->
...
4. JAVA_HOME должен стоять на JDK(!). 

5. Сборка проекта из терминала:
   mvn clean
   mvn install

6. Если в ходе выполнения тестов появляются непонятные ошибки, то можно собрать без тестов
   mvn install -Dmaven.test.skip=true

7. При переносе кода в новый библиотеку рекомендуется:
   - сохранить старый проект *.bak (переименовать и сам проект в .project)
   - заменить pom.xml, src. Добавить новые файлы/каталоги
   - в eclipse, с помощью "compare earch" отредактировать новый pom.xml (брать s5-изменения из старого).
   - произвести тестовую сборку
   - перенести s5-классы
   - отредактировать классы библиотеки на использование добавленных s5-классов
   - произвести окончательную сборку.

8. Сделать импорт maven-проекта в eclipse. Отключить(uncheck) в свойствах проекта "Builders/Checkstyle Builder"

9. Заменить в tswildfly-26.0.1.Final/modules/system/layers/base/org/jboss/ejb-client/main/jboss-ejb-client-4.0.44.Final.jar 
   на собранный org.toxsoft.extlib.wildfly.ejb.client-4.0.44.Final.jar.

Примечания:
(*) - в новой версии (jboss-ejb-client-4.0.44.Final) выполнение пункта инструкции не потребовалось.


2022-01-30 mvk

