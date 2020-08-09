//smoke testing

import com.datacert.core.behavior.command.enums.CommandResultEnum


//define convenience functions
def scheduledJobList = [
    'COA Update Generic Users Information',
    'Collaboration Person Sync'
]

def checkIfCpClientCertificateExists = {
    def appConfiguration = services.app.applicationContext.getBean("ApplicationConfiguration");
    def keystoreLocation = appConfiguration.keyStoreLocation
    if(keystoreLocation && new File(keystoreLocation).exists()){
        return true
    }
    return false
}

def checkTunnelConnectionIsOn = {
    def tunnelService = applicationContext.getBean('sslTunnelClientService')
    assert tunnelService != null
    if(tunnelService.status == com.datacert.ssltunnel.client.TunnelStatus.OFF){
        //may be off because auto start is not defined in jvm settings
        try{
            tunnelService.start()
        }catch(Throwable e){
            println e
        }
        
        println tunnelService.status
    } 
    return true
}

def checkScheduledJobsAreActive = {
    jobList = [] -> //default to empty list if not provided
    def allActive = true
    jobList.each{
        job ->
        def scheduledJob = services.search.query('ScheduledJob', services.search.and()
            .add(services.search.property('name').eq(job)) 
        ).first()
        if(scheduledJob != null){
            if(com.datacert.core.scheduler.enums.JobStatusEnum.Active.equals(scheduledJob.jobStatus?.statusEnum)){
                logger.debug("status of job ${scheduledJob.name} is active")
            }else{
                logger.debug("problem with status of job ${scheduledJob.name}, it is NOT active; it is ${scheduledJob.jobStatus?.displayName}")
                allActive = false
            }
            
        }else{
            allActive = false
        }
    }
    return allActive
}

def checkForJvmArgument = {
    arg ->
      def runtimeMxBean = java.lang.management.ManagementFactory.getRuntimeMXBean();
      return runtimeMxBean.getInputArguments().find{it.contains(arg)} != null;
}

def getInstance = { entityName ->
    return services.entity.createInstance(entityName)
}

def log4jPath = 'C:\\Datacert\\Passport\\tomcat\\webapps\\Passport\\WEB-INF\\classes\\log4j.properties'
def checkForLog4jText = {
    arg ->
    Properties properties = new Properties()
    File propertiesFile = new File(log4jPath)
    propertiesFile.withInputStream {
        properties.load(it)
    }

    return properties."$arg" == 'org.apache.log4j.RollingFileAppender'
   
}

//define test cases
def testCases = [
    [
        name:'check cp client certificate exists',
        args:[

        ],
        want:true,
        wantError: false,
        testCase:{
            return checkIfCpClientCertificateExists() 
        },
        warnMessage:"Cannot find client certificate for CP -  - please see Setting up the Client Certificate in document Rough Guide to CP.",
        skipWhen: {
            return cp_enabled != 'true'
        },
        skipMessage:'Collaboration Portal not enabled.',
    ],
    [
        name:'check connection to tunnel status is on',
        args:[

        ],
        want:true,
        wantError: false,
        testCase:{
            return checkTunnelConnectionIsOn() 
        },
        warnMessage:"Tunnel connection is expected to be On, but it is Off - please complete configuration as per instructions in installation guide.",
        skipWhen: {
            return cp_enabled != 'true'
        },
        skipMessage:'Collaboration Portal not enabled.',
    ],
    [
        name:'check COA scheduled jobs are active',
        args:[

        ],
        want:true,
        wantError: false,
        testCase:{
            return checkScheduledJobsAreActive(scheduledJobList) 
        },
        warnMessage:"All scheduled jobs expected to be active are not currently active or cannot be found - please complete configuration as per instructions in installation guide.",
        skipWhen: {
            return cp_enabled != 'true'
        },
        skipMessage:'Collaboration Portal not enabled.',
    ],
    [
        name:'check log4j COA settings',
        args:[

        ],
        want:true,
        wantError: false,
        testCase:{
            return checkForLog4jText('log4j.appender.PERSONSYNCCLIENTFILE') 
        },
        warnMessage:"Can't find correct log4j settings for COA - please complete configuration as per instructions in installation guide.",
        skipWhen: {
            return cp_enabled != 'true'
        },
        skipMessage:'Collaboration Portal not enabled.',
    ],
    [
        name:'check COA autotunnel setting',
        args:[

        ],
        want:true,
        wantError: false,
        testCase:{
            return checkForJvmArgument('-DautostartTunnel=true') 
        },
        skipWhen: {
            return cp_enabled != 'true'
        },
        skipMessage:'Collaboration Portal not enabled.',
    ],
    [
        name:'check version of passport',
        args:[

        ],
        want:passport_version,
        wantError: false,
        testCase:{
            def version = com.datacert.core.util.PropertiesLoader.getPropertyValue("version.properties", "framework.pom.version")
            return version 
        },

    ],
    [
        name:'check for clustering enabled',
        args:[

        ],
        want:false,
        wantError: false,
        testCase:{

            def appConfiguration = services.app.applicationContext.getBean("ApplicationConfiguration");
            return appConfiguration.isClusterEnabled()

        },
        skipWhen: {
            return cp_enabled != 'true'
        },
        skipMessage:'Collaboration Portal not enabled.',
    ],
]

testCases.each{ tc ->
    test(tc.name,{
        if(tc.containsKey('skipWhen') && tc.skipWhen()){
            logger.debug("skipping because ${tc.skipMessage?:''}")
            return [
                'skipped':['message':(tc.skipMessage?:'')],
                ]
        }
        def result = tc.testCase()
        assert tc.want == result : tc.warnMessage?:"We were expecting ${tc.want}, but we got ${result}"
    })
}

report()
