/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.provisioning.test.impl;

import static org.testng.AssertJUnit.assertFalse;
import static com.evolveum.midpoint.test.IntegrationTestTools.display;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.AssertJUnit;
import org.w3c.dom.Element;

import com.evolveum.icf.dummy.resource.DummyAccount;
import com.evolveum.icf.dummy.resource.DummyAttributeDefinition;
import com.evolveum.icf.dummy.resource.DummyGroup;
import com.evolveum.icf.dummy.resource.DummyObject;
import com.evolveum.icf.dummy.resource.DummyObjectClass;
import com.evolveum.icf.dummy.resource.DummyResource;
import com.evolveum.midpoint.common.InternalsConfig;
import com.evolveum.midpoint.common.monitor.CachingStatistics;
import com.evolveum.midpoint.common.monitor.InternalMonitor;
import com.evolveum.midpoint.common.refinery.RefinedResourceSchema;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.EqualsFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.util.PrismAsserts;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.provisioning.ProvisioningTestUtil;
import com.evolveum.midpoint.provisioning.api.ProvisioningService;
import com.evolveum.midpoint.provisioning.impl.ConnectorManager;
import com.evolveum.midpoint.provisioning.test.mock.SynchornizationServiceMock;
import com.evolveum.midpoint.provisioning.ucf.api.ConnectorInstance;
import com.evolveum.midpoint.provisioning.ucf.impl.ConnectorFactoryIcfImpl;
import com.evolveum.midpoint.schema.processor.ObjectClassComplexTypeDefinition;
import com.evolveum.midpoint.schema.processor.ResourceAttributeDefinition;
import com.evolveum.midpoint.schema.processor.ResourceSchema;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.schema.util.ShadowUtil;
import com.evolveum.midpoint.schema.util.ResourceTypeUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.test.AbstractIntegrationTest;
import com.evolveum.midpoint.test.DummyResourceContoller;
import com.evolveum.midpoint.test.IntegrationTestTools;
import com.evolveum.midpoint.test.util.TestUtil;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.CachingMetadataType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ShadowAssociationType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ShadowKindType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.XmlSchemaType;

/**
 * @author semancik
 *
 */
public abstract class AbstractDummyTest extends AbstractIntegrationTest {
	
	protected static final String TEST_DIR = "src/test/resources/impl/dummy/";
	
	public static final String RESOURCE_DUMMY_FILENAME = ProvisioningTestUtil.COMMON_TEST_DIR_FILENAME + "resource-dummy.xml";
	public static final String RESOURCE_DUMMY_OID = "ef2bc95b-76e0-59e2-86d6-9999dddddddd";
	
	protected static final String RESOURCE_DUMMY_NONEXISTENT_OID = "ef2bc95b-000-000-000-009900dddddd";

	protected static final String ACCOUNT_WILL_FILENAME = TEST_DIR + "account-will.xml";
	protected static final String ACCOUNT_WILL_OID = "c0c010c0-d34d-b44f-f11d-33322212dddd";
	protected static final String ACCOUNT_WILL_USERNAME = "Will";
	protected static final XMLGregorianCalendar ACCOUNT_WILL_ENABLE_TIMESTAMP = XmlTypeConverter.createXMLGregorianCalendar(2013, 5, 30, 12, 30, 42);

	protected static final String ACCOUNT_DAEMON_USERNAME = "daemon";
	protected static final String ACCOUNT_DAEMON_OID = "c0c010c0-dddd-dddd-dddd-dddddddae604";
	protected static final String ACCOUNT_DAEMON_FILENAME = TEST_DIR + "account-daemon.xml";

	protected static final String ACCOUNT_DAVIEJONES_USERNAME = "daviejones";

	protected static final String ACCOUNT_MORGAN_FILENAME = TEST_DIR + "account-morgan.xml";
	protected static final String ACCOUNT_MORGAN_OID = "c0c010c0-d34d-b44f-f11d-444400008888";
	protected static final String ACCOUNT_MORGAN_NAME = "morgan";
	
	protected static final String ACCOUNT_LECHUCK_FILENAME = TEST_DIR + "account-lechuck.xml";
	protected static final String ACCOUNT_LECHUCK_OID = "c0c010c0-d34d-b44f-f11d-444400009aa9";
	protected static final String ACCOUNT_LECHUCK_NAME = "lechuck";
	
	protected static final String GROUP_PIRATES_FILENAME = TEST_DIR + "group-pirates.xml";
	protected static final String GROUP_PIRATES_OID = "c0c010c0-d34d-b44f-f11d-3332eeee0000";
	protected static final String GROUP_PIRATES_NAME = "pirates";
	
	protected static final String PRIVILEGE_PILLAGE_FILENAME = TEST_DIR + "privilege-pillage.xml";
	protected static final String PRIVILEGE_PILLAGE_OID = "c0c010c0-d34d-b44f-f11d-3332eeff0000";
	protected static final String PRIVILEGE_PILLAGE_NAME = "pillage";

	protected static final String FILENAME_ACCOUNT_SCRIPT = TEST_DIR + "account-script.xml";
	protected static final String ACCOUNT_NEW_SCRIPT_OID = "c0c010c0-d34d-b44f-f11d-33322212abcd";
	protected static final String FILENAME_ENABLE_ACCOUNT = TEST_DIR + "modify-will-enable.xml";
	protected static final String FILENAME_DISABLE_ACCOUNT = TEST_DIR + "modify-will-disable.xml";
	protected static final String FILENAME_MODIFY_ACCOUNT = TEST_DIR + "modify-will-fullname.xml";
	protected static final File FILE_SCRIPTS = new File(TEST_DIR, "scripts.xml");
	
	protected static final String NOT_PRESENT_OID = "deaddead-dead-dead-dead-deaddeaddead";
	
	protected static final String OBJECTCLAS_PRIVILEGE_LOCAL_NAME = "CustomprivilegeObjectClass";
	
	private static final Trace LOGGER = TraceManager.getTrace(AbstractDummyTest.class);
	
	protected PrismObject<ResourceType> resource;
	protected ResourceType resourceType;
	protected static DummyResource dummyResource;
	protected static DummyResourceContoller dummyResourceCtl;
	
	@Autowired(required = true)
	protected ProvisioningService provisioningService;

	// Used to make sure that the connector is cached
	@Autowired(required = true)
	protected ConnectorManager connectorManager;

	@Autowired(required = true)
	protected SynchornizationServiceMock syncServiceMock;
	
	@Autowired(required = true) 
	protected TaskManager taskManager;
	
	// Values used to check if something is unchanged or changed properly
	private CachingMetadataType lastCachingMetadata;
	private long lastConnectorSchemaFetchCount = 0;
	private long lastConnectorInitializationCount = 0;
	private long lastResourceSchemaParseCount = 0;
	private CachingStatistics lastResourceCacheStats;
	private ResourceSchema lastResourceSchema = null;
	private RefinedResourceSchema lastRefinedResourceSchema;
	private Long lastResourceVersion = null;
	private ConnectorInstance lastConfiguredConnectorInstance;

	
	@Override
	public void initSystem(Task initTask, OperationResult initResult) throws Exception {
		// We need to switch off the encryption checks. Some values cannot be encrypted as we do
		// not have a definition here
		InternalsConfig.encryptionChecks = false;
		provisioningService.postInit(initResult);
		resource = addResourceFromFile(getResourceDummyFilename(), IntegrationTestTools.DUMMY_CONNECTOR_TYPE, initResult);
		resourceType = resource.asObjectable();

		dummyResourceCtl = DummyResourceContoller.create(null);
		dummyResourceCtl.setResource(resource);
		dummyResourceCtl.extendDummySchema();
		dummyResource = dummyResourceCtl.getDummyResource();

		DummyAccount dummyAccountDaemon = new DummyAccount(ACCOUNT_DAEMON_USERNAME);
		dummyAccountDaemon.setEnabled(true);
		dummyAccountDaemon.addAttributeValues("fullname", "Evil Daemon");
		dummyResource.addAccount(dummyAccountDaemon);

		repoAddObjectFromFile(ACCOUNT_DAEMON_FILENAME, ShadowType.class, initResult);
	}
	
	protected String getResourceDummyFilename() {
		return RESOURCE_DUMMY_FILENAME;
	}
	
	protected File getAccountWillFile() {
		return new File(ACCOUNT_WILL_FILENAME);
	}
	
	protected boolean supportsActivation() {
		return true;
	}

	protected <T extends ShadowType> void checkConsistency(Collection<PrismObject<T>> shadows) throws SchemaException {
		for (PrismObject<T> shadow: shadows) {
			checkConsistency(shadow);
		}
	}
	
	protected void checkConsistency(PrismObject<? extends ShadowType> object) throws SchemaException {

		OperationResult result = new OperationResult(TestDummyNegative.class.getName()
				+ ".checkConsistency");
		
		ItemDefinition itemDef = ShadowUtil.getAttributesContainer(object).getDefinition().findAttributeDefinition(ConnectorFactoryIcfImpl.ICFS_UID);
		
		LOGGER.info("item definition: {}", itemDef.dump());
		//TODO: matching rule
		EqualsFilter equal = EqualsFilter.createEqual(new ItemPath(ShadowType.F_ATTRIBUTES), itemDef, null, getWillRepoIcfUid());
		ObjectQuery query = ObjectQuery.createObjectQuery(equal);
		
		System.out.println("Looking for shadows of \"" + getWillRepoIcfUid() + "\" with filter "
				+ query.dump());
		display("Looking for shadows of \"" + getWillRepoIcfUid() + "\" with filter "
				+ query.dump());
		
		List<PrismObject<ShadowType>> objects = repositoryService.searchObjects(ShadowType.class, query,
				result);

		
		assertEquals("Wrong number of shadows for ICF UID \"" + getWillRepoIcfUid() + "\"", 1, objects.size());

	}
	
	protected <T> void assertAttribute(ShadowType shadow, String attrName, T... expectedValues) {
		ProvisioningTestUtil.assertAttribute(resource, shadow, attrName, expectedValues);
	}
	
	protected <T> void assertAttribute(ShadowType shadow, QName attrName, T... expectedValues) {
		ProvisioningTestUtil.assertAttribute(resource, shadow, attrName, expectedValues);
	}
	
	protected <T> void assertNoAttribute(ShadowType shadow, String attrName) {
		ProvisioningTestUtil.assertNoAttribute(resource, shadow, attrName);
	}
	
	protected <T> void assertNoAttribute(PrismObject<ShadowType> shadow, String attrName) {
		ProvisioningTestUtil.assertNoAttribute(resource, shadow.asObjectable(), attrName);
	}
	
	protected void assertSchemaSanity(ResourceSchema resourceSchema, ResourceType resourceType) {
		dummyResourceCtl.assertDummyResourceSchemaSanityExtended(resourceSchema, resourceType);
	}
	
	protected <T> void assertDummyAccountAttributeValues(String accountId, String attributeName, T... expectedValues) throws ConnectException, FileNotFoundException {
		DummyAccount dummyAccount = dummyResource.getAccountByUsername(accountId);
		assertNotNull("No account '"+accountId+"'", dummyAccount);
		assertDummyAttributeValues(dummyAccount, attributeName, expectedValues);
	}
	
	protected <T> void assertDummyAttributeValues(DummyObject object, String attributeName, T... expectedValues) {
		Set<T> attributeValues = (Set<T>) object.getAttributeValues(attributeName, expectedValues[0].getClass());
		assertNotNull("No attribute "+attributeName+" in "+object.getShortTypeName()+" "+object, attributeValues);
		TestUtil.assertSetEquals("Wroung values of attribute "+attributeName+" in "+object.getShortTypeName()+" "+object, attributeValues, expectedValues);
	}
	
	protected String getWillRepoIcfUid() {
		return ACCOUNT_WILL_USERNAME;
	}
	
	protected void assertMember(DummyGroup group, String accountId) {
		Collection<String> members = group.getMembers();
		assertNotNull("No members in group "+group.getName()+", expected that "+accountId+" will be there", members);
		assertTrue("Account "+accountId+" is not member of group "+group.getName()+", members: "+members, members.contains(accountId));
	}

	protected void assertNoMember(DummyGroup group, String accountId) {
		Collection<String> members = group.getMembers();
		if (members == null) {
			return;
		}
		assertFalse("Account "+accountId+" IS member of group "+group.getName()+" while not expecting it, members: "+members, members.contains(accountId));
	}
	
	protected void assertEntitlement(PrismObject<ShadowType> account, String entitlementOid) {
		ShadowType accountType = account.asObjectable();
		List<ShadowAssociationType> associations = accountType.getAssociation();
		assertNotNull("Null associations in "+account, associations);
		assertFalse("Empty associations in "+account, associations.isEmpty());
		for (ShadowAssociationType association: associations) {
			if (entitlementOid.equals(association.getShadowRef().getOid())) {
				return;
			}
		}
		AssertJUnit.fail("No association for entitlement "+entitlementOid+" in "+account);
	}

	
	protected <T extends ObjectType> void assertVersion(PrismObject<T> object, String expectedVersion) {
		assertEquals("Wrong version of "+object, expectedVersion, object.asObjectable().getVersion());
	}

	protected void rememberResourceVersion(String version) {
		lastResourceVersion = parseVersion(version);
	}
	
	protected void assertResourceVersionIncrement(PrismObject<ResourceType> resource, int expectedIncrement) {
		assertResourceVersionIncrement(resource.getVersion(), expectedIncrement);
	}
	
	protected void assertResourceVersionIncrement(String currentVersion, int expectedIncrement) {
		long currentVersionLong = parseVersion(currentVersion);
		long actualIncrement = currentVersionLong - lastResourceVersion;
		assertEquals("Unexpected increment in resource version", (long)expectedIncrement, actualIncrement);
		lastResourceVersion = currentVersionLong;
	}
	
	private long parseVersion(String stringVersion) {
		if (stringVersion == null) {
			AssertJUnit.fail("Version is null");
		}
		if (stringVersion.isEmpty()) {
			AssertJUnit.fail("Version is empty");
		}
		return Long.parseLong(stringVersion);
	}

	protected CachingMetadataType getSchemaCachingMetadata(PrismObject<ResourceType> resource) {
		ResourceType resourceType = resource.asObjectable();
		XmlSchemaType xmlSchemaTypeAfter = resourceType.getSchema();
		assertNotNull("No schema", xmlSchemaTypeAfter);
		Element resourceXsdSchemaElementAfter = ResourceTypeUtil.getResourceXsdSchema(resourceType);
		assertNotNull("No schema XSD element", resourceXsdSchemaElementAfter);
		return xmlSchemaTypeAfter.getCachingMetadata();
	}
	
	protected void rememberSchemaMetadata(PrismObject<ResourceType> resource) {
		lastCachingMetadata = getSchemaCachingMetadata(resource);
	}
	
	protected void assertSchemaMetadataUnchanged(PrismObject<ResourceType> resource) {
		CachingMetadataType current = getSchemaCachingMetadata(resource);
		assertEquals("Schema caching metadata changed", lastCachingMetadata, current);
	}
	
	protected void rememberConnectorSchemaFetchCount() {
		lastConnectorSchemaFetchCount = InternalMonitor.getConnectorSchemaFetchCount();
	}

	protected void assertConnectorSchemaFetchIncrement(int expectedIncrement) {
		long currentConnectorSchemaFetchCount = InternalMonitor.getConnectorSchemaFetchCount();
		long actualIncrement = currentConnectorSchemaFetchCount - lastConnectorSchemaFetchCount;
		assertEquals("Unexpected increment in connector schema fetch count", (long)expectedIncrement, actualIncrement);
		lastConnectorSchemaFetchCount = currentConnectorSchemaFetchCount;
	}

	protected void rememberConnectorInitializationCount() {
		lastConnectorInitializationCount  = InternalMonitor.getConnectorInitializationCount();
	}

	protected void assertConnectorInitializationCountIncrement(int expectedIncrement) {
		long currentConnectorInitializationCount = InternalMonitor.getConnectorInitializationCount();
		long actualIncrement = currentConnectorInitializationCount - lastConnectorInitializationCount;
		assertEquals("Unexpected increment in connector initialization count", (long)expectedIncrement, actualIncrement);
		lastConnectorInitializationCount = currentConnectorInitializationCount;
	}
	
	protected void rememberResourceSchemaParseCount() {
		lastResourceSchemaParseCount  = InternalMonitor.getResourceSchemaParseCount();
	}

	protected void assertResourceSchemaParseCountIncrement(int expectedIncrement) {
		long currentResourceSchemaParseCount = InternalMonitor.getResourceSchemaParseCount();
		long actualIncrement = currentResourceSchemaParseCount - lastResourceSchemaParseCount;
		assertEquals("Unexpected increment in resource schema parse count", (long)expectedIncrement, actualIncrement);
		lastResourceSchemaParseCount = currentResourceSchemaParseCount;
	}
	
	protected void rememberResourceCacheStats() {
		lastResourceCacheStats  = InternalMonitor.getResourceCacheStats().clone();
	}
	
	protected void assertResourceCacheHitsIncrement(int expectedIncrement) {
		assertCacheHits(lastResourceCacheStats, InternalMonitor.getResourceCacheStats(), "resouce cache", expectedIncrement);
	}
	
	protected void assertResourceCacheMissesIncrement(int expectedIncrement) {
		assertCacheMisses(lastResourceCacheStats, InternalMonitor.getResourceCacheStats(), "resouce cache", expectedIncrement);
	}
	
	protected void assertCacheHits(CachingStatistics lastStats, CachingStatistics currentStats, String desc, int expectedIncrement) {
		long actualIncrement = currentStats.getHits() - lastStats.getHits();
		assertEquals("Unexpected increment in "+desc+" hit count", (long)expectedIncrement, actualIncrement);
		lastStats.setHits(currentStats.getHits());
	}

	protected void assertCacheMisses(CachingStatistics lastStats, CachingStatistics currentStats, String desc, int expectedIncrement) {
		long actualIncrement = currentStats.getMisses() - lastStats.getMisses();
		assertEquals("Unexpected increment in "+desc+" miss count", (long)expectedIncrement, actualIncrement);
		lastStats.setMisses(currentStats.getMisses());
	}
	
	protected void rememberResourceSchema(ResourceSchema resourceSchema) {
		lastResourceSchema = resourceSchema;
	}
	
	protected void assertResourceSchemaUnchanged(ResourceSchema currentResourceSchema) {
		// We really want == there. We want to make sure that this is actually the same instance and that
		// it was properly cached
		assertTrue("Resource schema has changed", lastResourceSchema == currentResourceSchema);
	}
	
	protected void rememberRefinedResourceSchema(RefinedResourceSchema rResourceSchema) {
		lastRefinedResourceSchema = rResourceSchema;
	}
	
	protected void assertRefinedResourceSchemaUnchanged(RefinedResourceSchema currentRefinedResourceSchema) {
		// We really want == there. We want to make sure that this is actually the same instance and that
		// it was properly cached
		assertTrue("Refined resource schema has changed", lastRefinedResourceSchema == currentRefinedResourceSchema);
	}

	protected void assertHasSchema(PrismObject<ResourceType> resource, String desc) throws SchemaException {
		ResourceType resourceType = resource.asObjectable();
		display("Resource "+desc, resourceType);

		XmlSchemaType xmlSchemaTypeAfter = resourceType.getSchema();
		assertNotNull("No schema in "+desc, xmlSchemaTypeAfter);
		Element resourceXsdSchemaElementAfter = ResourceTypeUtil.getResourceXsdSchema(resourceType);
		assertNotNull("No schema XSD element in "+desc, resourceXsdSchemaElementAfter);

		String resourceXml = prismContext.getPrismDomProcessor().serializeObjectToString(resource);
//		display("Resource XML", resourceXml);                            

		CachingMetadataType cachingMetadata = xmlSchemaTypeAfter.getCachingMetadata();
		assertNotNull("No caching metadata in "+desc, cachingMetadata);
		assertNotNull("No retrievalTimestamp in "+desc, cachingMetadata.getRetrievalTimestamp());
		assertNotNull("No serialNumber in "+desc, cachingMetadata.getSerialNumber());

		Element xsdElement = ObjectTypeUtil.findXsdElement(xmlSchemaTypeAfter);
		ResourceSchema parsedSchema = ResourceSchema.parse(xsdElement, resource.toString(), prismContext);
		assertNotNull("No schema after parsing in "+desc, parsedSchema);
	}

	protected void rememberConnectorInstance(PrismObject<ResourceType> resource) throws ObjectNotFoundException, SchemaException, CommunicationException, ConfigurationException {
		OperationResult result = new OperationResult(TestDummyResourceAndSchemaCaching.class.getName()
				+ ".rememberConnectorInstance");
		rememberConnectorInstance(connectorManager.getConfiguredConnectorInstance(resource, false, result));
	}
	
	protected void rememberConnectorInstance(ConnectorInstance currentConnectorInstance) throws ObjectNotFoundException, SchemaException, CommunicationException, ConfigurationException {
		lastConfiguredConnectorInstance = currentConnectorInstance;
	}
	
	protected void assertConnectorInstanceUnchanged(PrismObject<ResourceType> resource) throws ObjectNotFoundException, SchemaException, CommunicationException, ConfigurationException {
		OperationResult result = new OperationResult(TestDummyResourceAndSchemaCaching.class.getName()
				+ ".rememberConnectorInstance");
		ConnectorInstance currentConfiguredConnectorInstance = connectorManager.getConfiguredConnectorInstance(
				resource, false, result);
		assertTrue("Connector instance has changed", lastConfiguredConnectorInstance == currentConfiguredConnectorInstance);
	}
	
	protected void assertConnectorInstanceChanged(PrismObject<ResourceType> resource) throws ObjectNotFoundException, SchemaException, CommunicationException, ConfigurationException {
		OperationResult result = new OperationResult(TestDummyResourceAndSchemaCaching.class.getName()
				+ ".rememberConnectorInstance");
		ConnectorInstance currentConfiguredConnectorInstance = connectorManager.getConfiguredConnectorInstance(
				resource, false, result);
		assertTrue("Connector instance has NOT changed", lastConfiguredConnectorInstance != currentConfiguredConnectorInstance);
		lastConfiguredConnectorInstance = currentConfiguredConnectorInstance;
	}
	
	protected void assertSteadyResource() throws ObjectNotFoundException, SchemaException, CommunicationException, ConfigurationException {
		assertConnectorSchemaFetchIncrement(0);
		assertConnectorInitializationCountIncrement(0);
		assertResourceSchemaParseCountIncrement(0);
		assertResourceVersionIncrement(resource, 0);
		assertSchemaMetadataUnchanged(resource);
		assertConnectorInstanceUnchanged(resource);
		
		display("Resource cache", InternalMonitor.getResourceCacheStats());
		// We do not assert hits, there may be a lot of them
		assertResourceCacheMissesIncrement(0);
	}

}
