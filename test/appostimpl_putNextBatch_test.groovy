
def testData = [
    1:[//each record will be keyed off of invoice id
        MatterManagementIntegration:[//used to feed data to putNextBatch
            1,//integrationId
            1,//invoiceId
            'InvoiceHeader',//baseEntity
        ],
        InvoiceHeader:[//InvoiceHeader object mock
            id:1,
            invoiceNumber:'invNum1',
            invoiceStartDate:new Date().format("MM/dd/YYYY"),
            invoiceEndDate:new Date().format("MM/dd/YYYY"),
            matter:[
                matterNumber:'12345',
            ],
            organization:[
                name:'orgName',
            ],
            totalAmount:[
                amount: new BigDecimal(3.44).setScale(2, BigDecimal.ROUND_HALF_EVEN),
            ],
        ],
    ],
    2:[//each record will be keyed off of invoice id
        MatterManagementIntegration:[//used to feed data to putNextBatch
            2,//integrationId
            2,//invoiceId
            'InvoiceHeader',//baseEntity
        ],
        InvoiceHeader:[//InvoiceHeader object mock
            id:2,
            invoiceNumber:'invNum2',
            invoiceStartDate:new Date().format("MM/dd/YYYY"),
            invoiceEndDate:new Date().format("MM/dd/YYYY"),
            matter:[
                matterNumber:'22345',
            ],
            organization:[
                name:'orgName2',
            ],
            totalAmount:[
                amount: new BigDecimal(32.44).setScale(2, BigDecimal.ROUND_HALF_EVEN),
            ],
        ],
    ],
]
def postData = testData.collectEntries{
    k,v ->
    [(k):['MatterManagementIntegration':v.get('MatterManagementIntegration')]]
}
/*
    class under test contains the following lines

   	@Autowired
	private Services services;
	private ApplicationContext applicationContext;

    These are normally injected or autowired, so we will provide them to the class under test
*/
def classUnderTest = new com.datacert.apps.mattermanagement.integration.ApPostImpl()
def result //we will populate this variable and validate content
def currentFileConsumer = [//simple map coerced into a provider
    initialize:{return true},//validator for testing will always return true
    putNextBatch:{data -> result = data; return}//capture results passed to provider from implementation class, so we can validate results
] as com.datacert.apps.mattermanagement.integration.ApPostFileDataConsumer

classUnderTest.services = services // since this is a command being run in Passport, services are always available. Assign to the class we are testing manually
classUnderTest.applicationContext = applicationContext
def currentIntegration = services.search.query("IntegrationInstance", services.search.property("name").eq('Ap Post')).first();
classUnderTest.currentIntegration = currentIntegration
classUnderTest.currentFileConsumer = currentFileConsumer
classUnderTest.logger = logger
classUnderTest.getEntityById = {// in real class, this closure will lookup record using services.query
    entityName,entityId ->
    return testData.get(entityId).get(entityName) // implementation class will be calling this closure during execution, and testData will be returned to it
}
assert classUnderTest.initialize()
test 'putNextBatch ideal',{
    classUnderTest.putNextBatch(postData) // invoking the putNextBatch method in isolation after everything is setup for testing
    assert result != null
    assert result.size() == 2 //there are 2 records is test data, so expect 2 rows processed in 
}
