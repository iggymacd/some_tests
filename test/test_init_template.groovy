import groovy.xml.*
//def entityName = "VoidEntity";
//entity = queryService.query(entityName, queryService.property("foo").eq("bar")).first();
//println 'test transform template!'
//version 1.2.3

def savedMetaClasses = [:]

/**
 * Getting logger instance
 * @param intInstance
 * @return newLogger
 */
def getLoggerWithWriter(def className, def writer, def lLevel = 'INFO'){
  def newLogger = services.util.getLogger(className);
  newLogger.logger?.removeAllAppenders();
  def app = new org.apache.log4j.WriterAppender( new com.datacert.core.logging.pattern.UsagePatternLayout('[%d{MMM dd HH:mm:ss}] %-5p | %c | %u | %m%n'), writer );
  newLogger.logger.addAppender(app);
  newLogger.logger.setLevel(org.apache.log4j.Level.toLevel(lLevel, org.apache.log4j.Level.ALL));
  return newLogger;
}
//if logger begins with com.datacert, it will be included as part of normal log files, otherwise, they only show in results window

def logger = getLoggerWithWriter("com.datacert.command.test.Test", out, 'DEBUG')


def benchmark = { name="",clsr={} ->  
 logger.info("begin benchmark ${name}")
 start = System.currentTimeMillis()  
 clsr.call()  
 now = System.currentTimeMillis()  
 def bTime = now - start
 logger.info("benchmark took ${bTime} ms")
}  
  
def testSuite = [:]
def scriptClassName = this.getClass()

def skip = {
    skipMessage = 'test was skipped' ->
    return [
        'skipped':['message':(skipMessage?:'')],
    ]
}

def test = { def testName, def clsr -> 
  def testcase = [
    // classname:delegate.class?.name,
    name:testName,
  ]
  def start = System.currentTimeMillis()
logger.info('=' * 30)
logger.info('=' * 30)
logger.info("begin test ${testName}")
  try{
    def res = clsr.call()
    if(res instanceof Map && res.containsKey('skipped')){
        def skipped = testcase.get('result',[:]).get('skipped',[:])
        skipped.put('message',res.skipped?.message)
        // logger.info("skip...${testName}".toString())
        // testcase.put('result', 'skipped')
    }else{
        logger.info("pass...${testName}".toString())
        testcase.put('result', 'success')
    }
   }catch(Exception e){
     def failure = testcase.get('result',[:]).get('failure',[:])
     failure.put('message', e.message)
     failure.put('type','')
     logger.error("fail...${testName}".toString(), e)
     failure.put('body',t.stackTrace?.join('\n'))
     println(e.message)
    //  logger.error(t.message)
   }catch(Throwable t){
     def failure = testcase.get('result',[:]).get('failure',[:])
     failure.put('message', t.message)
     failure.put('type','')
     logger.error("fail...${testName}".toString())
    //  failure.put('body',t.stackTrace?.join('\n'))
     println(t.message)
   }
logger.info("end test ${testName}")
logger.info('=' * 60)
     savedMetaClasses.each { clazz, metaClass ->
        GroovySystem.metaClassRegistry.removeMetaClass(clazz)
        GroovySystem.metaClassRegistry.setMetaClass(clazz, metaClass)
    }
  now = System.currentTimeMillis()  
  def bTime = (now - start)/1000
  testcase.put('time', bTime)
  logger.info("benchmark took ${bTime} ms")    
  testSuite.get((delegate.class?.name),[:])?.put(testName,testcase)
}

def htmlReport = {
      print('~' * 3)
      def xmlFile = new XmlParser().parse(new File("C:\\Users\\allan.macdonald\\Documents\\clients\\ups\\dev\\gts-ups-dev2\\report\\quick_check_test.groovy"))
      def xmlWriter = new java.io.StringWriter();
      // new FileWriter(file("C:\\Users\\allan.macdonald\\Documents\\clients\\ups\\dev\\gts-ups-dev2\\report\\index.html"))
      def xmlMarkup = new MarkupBuilder(xmlWriter)
      xmlWriter.write("<!DOCTYPE html>\n")
      xmlMarkup.html() {
          head() {
              meta(charset: "utf-8")
              meta(name: "viewport", content:"width=device-width, initial-scale=1, maximum-scale=1")
              title("Test Report")
              link(rel: "stylesheet", href: "./css/main.css") { mkp.yield("") }
              link(rel: "stylesheet", href: "https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0-beta/css/materialize.min.css", media: "screen,projection") { mkp.yield("") }
              link(rel: "stylesheet", href: "https://fonts.googleapis.com/icon?family=Material+Icons") { mkp.yield("") }
          }
          body(){
              header(){
                  div("class":"nav-wrapper"){
                      nav(class: "blue darken-3") {
                          a(class: "brand-logo", href: "#",id: "logo-container") {
                              img(class: "logo-img",src: "") {
                                  mkp.yield("")
                              }
                          }
                          a("href":"index.html") {
                              ul(class: "center flow-text", "TEST RESULTS")
                          }
                      }
                  }
              }
              main() {
                  div(class: "container") {
                      div(style: "margin-bottom: 50px;") {
                          table(class: "striped responsive-table", id: "example") {
                              thead() {
                                  tr() {
                                      th(class: "center","Test Suite")
                                      th(class: "center","Passed")
                                      th(class: "center","Failed")
                                      th(class: "center","Total")
                                  }
                              }
                              tbody() {
                                  xmlFile.testsuite.each { testsuite ->
                                      tr() {
                                          td(class: "center",) {
                                              a(href: "report.html",testsuite['@name'])
                                          }
                                          td(class: "center","${Integer.valueOf(testsuite['@tests']) - Integer.valueOf(testsuite['@failure'])}")
                                          td(class: "center",testsuite['@failure'])
                                          td(class: "center",testsuite['@tests'])
                                      }
                                  }
                              }
                          }
                      }
                  }
              }
          }
          footer(class: "page-footer blue darken-3") {
              div(class: "footer-copyright") {
                  div(class: "container", "Demo By - rookieInTraining") {
                      a(class: "grey-text text-lighten-4 right", "")
                  }
              }
          }
          script(type: "text/javascript", src: "https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0-beta/js/materialize.min.js") { mkp.yield("") }
      }

       print xmlWriter.toString();


      print('~' * 3)
      // def xmlFile = new XmlParser().parse(new File("C:\\Users\\allan.macdonald\\Documents\\clients\\ups\\dev\\gts-ups-dev2\\report\\quick_check_test.groovy"))
      xmlWriter = new java.io.StringWriter();
      // new FileWriter(file("C:\\Users\\allan.macdonald\\Documents\\clients\\ups\\dev\\gts-ups-dev2\\report\\index.html"))
      xmlMarkup = new MarkupBuilder(xmlWriter)
      xmlWriter.write("<!DOCTYPE html>\n")
      xmlMarkup.html() {
        head() {
                meta(charset: "utf-8")
                meta(name: "viewport", content:"width=device-width, initial-scale=1, maximum-scale=1")
                title("Test Report")
                link(rel: "stylesheet", href: "./css/main.css") { mkp.yield("") }
                link(rel: "stylesheet", href: "https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0-beta/css/materialize.min.css", media: "screen,projection") { mkp.yield("") }
                link(rel: "stylesheet", href: "https://fonts.googleapis.com/icon?family=Material+Icons") { mkp.yield("") }
            }
            body(){
                header(){
                    div("class":"nav-wrapper"){
                        nav(class: "blue darken-3") {
                            a(class: "brand-logo", href: "#",id: "logo-container") {
                                img(class: "logo-img",src: "") {
                                    mkp.yield("")
                                }
                            }
                            a("href":"index.html") {
                                ul(class: "center flow-text", "TEST RESULTS")
                            }
                        }
                    }
                }
                main() {
                    div("class":"container") {
                        xmlFile.testsuite.each { testsuite ->
                            h2("Suite Name : " + testsuite['@name'])
                            h5("Total Tests Executed : " + testsuite['@tests'])
                            h5("Total Tests Failed : " + testsuite['@failure'])
                            h5("Date : " + testsuite['@timestamp'])
                            div(style: "margin-bottom: 50px;") {
                                table(class: "striped highlight responsive-table") {
                                    thead() {
                                        tr() {
                                            th(class: "center","Test Name")
                                            th(class: "center","Result")
                                            th(class: "center","Failure Reason")
                                            th(class: "center","Test URL")
                                        }
                                    }
                                    tbody() {
                                        testsuite.testcase.each { testcase ->
                                            if(testcase.failure.size() <= 0) {
                                                tr(class: "green lighten-5") {
                                                    td(class: "center",testcase['@name'])
                                                    td(class: "center","Passed")
                                                    td("")
                                                    td(class: "center") {
                                                        a(href: "#", testcase.'system-out'.text())
                                                    }
                                                }
                                            } else {
                                                tr(class: "red lighten-5") {
                                                    td(class: "center",testcase['@name'])
                                                    td(class: "center", "Failed")
                                                    td(class: "center", testcase.failure['@message'].toString().replace("[","").replace("]","").split("More info")[0])
                                                    td(class: "center") {
                                                        a(href: "#", testcase.'system-out'.text())
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            footer(class: "page-footer blue darken-3") {
                div(class: "footer-copyright") {
                    div(class: "container", "Demo By - rookieInTraining") {
                        a(class: "grey-text text-lighten-4 right", "")
                    }
                }
            }
            script(type: "text/javascript", src: "https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0-beta/js/materialize.min.js") { mkp.yield("") }
      }

       print xmlWriter.toString();
  }  

def report = {
    def testCaseSize
    def testSuiteTests = 0
    def testSuiteTime = 0.0
    def testSuiteFailures = 0
    testSuite.each{
        // println groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(it))
        testCaseSize = it.value.size()
    }
    for(ts in testSuite){
        for(testcase in ts.value){
            testSuiteTests++
            testSuiteTime = testSuiteTime + testcase?.value?.time?.toDouble()
            // println "testcase is ${testcase.key}"
            if(testcase.value?.result != 'success'){
                if( testcase.value?.result instanceof Map && !testcase.value?.result.containsKey('skipped')){
                    testSuiteFailures++
                }
            }
            // println "result is ${testcase.value?.result}"
        }
    }
    print('~' * 3)

        def xmlWriter = new java.io.StringWriter();
        def xmlBuilder = new groovy.xml.MarkupBuilder(xmlWriter);
        xmlBuilder.mkp.xmlDeclaration(version: "1.0", encoding: "UTF-8")
    /*
    testsuites
    Child elements: <testsuite>
    Attributes:
    id: The ID of the scan.
    name: The label of the scan.
    tests: The total number of rules that were applied.
    failures: The total number of rule violations.
    time: The time that was required to process all the rules.
    Text: None
    */
    for(ts in testSuite){
        xmlBuilder.'testsuites'(){
    /*
    testsuite
    Child elements: <testcase>
    Attributes
    id: The ID of the provider.
    name: The label of the provider.
    tests: The number of rules in the provider that were applied.
    failures: The number of rule violations in the provider.
    time The time that was required to process the rules in the provider.
    Text: None.
    */
          'testsuite'(name:'suite-1', tests:"${testSuiteTests}",failures:"${testSuiteFailures}",time:"${testSuiteTime?.round(3)}"){
    /*
    <testcase> 
    Child elements: <failure>
    Attributes
    id: The ID of the rule.
    name: The label of the rule.
    time The time that was required to process all the applications of this rule.
    Text: None.
    */
            for(testcase in ts.value){
                // testSuiteTests = 2
              xmlBuilder.'testcase'(classname:'name',name:testcase?.key,time:testcase?.value?.time){
    /*
    <failure> 
    Child elements: None.
    Attributes
    message: The source code file, the line number, and the rule that is violated.
    type: The severity of the rule.
    Text:
    The text of the rule and the severity.
    The analysis provider and the analysis category.
    The source code file.
    The line number
    */
                
                if(testcase?.value?.result instanceof Map && testcase?.value?.result?.containsKey('failure')){
                  xmlBuilder.'failure'(message:testcase?.value?.result?.failure?.message,type:testcase?.value?.result?.failure?.type,testcase?.value?.result?.failure?.body)
                }
                if(testcase?.value?.result instanceof Map && testcase?.value?.result?.containsKey('skipped')){
                  xmlBuilder.'skipped'(message:testcase?.value?.result?.skipped?.message)
                }
              }
              //   Sample()
              // }
            }
          }

    // mkp.xmlDeclaration()
        }
    }
  print xmlWriter.toString();
  // def ant = new AntBuilder()          
  // ant.echo('hello from Ant!')

}

def testCustomDataFilter = { entityName, closure ->
  def query
  def searchGroups = []
                // System.out.print( "testing linter")
  try{
    closure(searchGroups)//filter definition passed in as closure is evaluated here
    query = services.util.searchBuilder.query(entityName){
      searchGroups.each {
        def tempQGroup = services.search.and()
        tempQGroup.searchGroup = it
        stackGroup.add(tempQGroup)
      }
    }
  }catch(Exception e){
    logger.error("Exception applying filter",e)
  }
  return query
}

def registerMetaClass =  { clazz ->
  // If the class has already been registered, then there's nothing to do.
  if (savedMetaClasses.containsKey(clazz)) return

  // Save the class's current meta class.
  savedMetaClasses[clazz] = clazz.metaClass

  // Create a new EMC for the class and attach it.
  def emc = new ExpandoMetaClass(clazz, true, true)
  emc.initialize()
  GroovySystem.metaClassRegistry.setMetaClass(clazz, emc)
}

def withReadOnlyEntity = { entityName, property, value, clsr ->
  def ent = services.search.query(entityName, services.search.property(property).eq(value)).first();
  if(ent == null){
      return null
  }
  def session = sessionFactory.getCurrentSession()
  session.setReadOnly(ent,true)
  clsr.call(ent)
  session.setReadOnly(ent,false)
}

def withEntity = { entityName, property, value, clsr ->
  def ent = services.search.query(entityName, services.search.property(property).eq(value)).first();
  if(ent == null){
      return null
  }
  clsr.call(ent)
}

def asUser = { String username, Closure c ->
  //the authorization service defines a method that will run a function as a specific username
  //we have adapted that to groovy
  services.entity.applicationContext
  .getBean("authorizationService")
  .runAsUser(username, {
    call : c()//call method is required to satisfy the Callable interface
  } as java.util.concurrent.Callable);
}
def withDataFilterResults = { filterDescription, filterMetadata, closure ->
    def ss =  applicationContext.getBean("searchService");
    def fs =  applicationContext.getBean("filterService");
    def filter = fs.findFilterByDescription(filterDescription)
    def filterMd = services.entity.findMetadata(filterMetadata)
    def resultList = ss.findResults(filterMd, null, filter[0]);
    return resultList.getSearchResults()
}
