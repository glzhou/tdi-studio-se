// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.generic.model.migration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.avro.Schema;
import org.eclipse.core.runtime.Path;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.ReflectionUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;
import orgomg.cwm.objectmodel.core.CoreFactory;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * created by hcyi on Apr 11, 2016 Detailled comment
 *
 */
public class NewSalesforceWizardMigrationTask extends NewGenericWizardMigrationTask {

    public static final String SCHEMA_SCHEMA = "schema.schema"; //$NON-NLS-1$

    public static final String REFLECTION_SALESFORCE_MODULE_PROPERTIES = "org.talend.components.salesforce.SalesforceModuleProperties"; //$NON-NLS-1$

    public static final String TYPE_NAME = "salesforce"; //$NON-NLS-1$

    public static final String CONNECTION_COMPONENT_NAME = "tSalesforceConnection"; //$NON-NLS-1$

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<>();
        toReturn.add(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        ComponentService service = ComponentsUtils.getComponentService();
        Properties props = getPropertiesFromFile();
        if (item instanceof ConnectionItem) {
            boolean modify = false;
            GenericConnectionItem genericConnectionItem = null;
            ConnectionItem connectionItem = (ConnectionItem) item;
            Connection connection = connectionItem.getConnection();
            // Init
            genericConnectionItem = initGenericConnectionItem(connectionItem);
            genericConnectionItem.setTypeName(TYPE_NAME);
            GenericConnection genericConnection = initGenericConnection(connection);
            initProperty(connectionItem, genericConnectionItem);

            ComponentProperties componentProperties = service.getComponentProperties(CONNECTION_COMPONENT_NAME);
            // Update
            modify = updateComponentProperties(connection, componentProperties, props);
            genericConnection.setCompProperties(componentProperties.toSerialized());
            genericConnectionItem.setConnection(genericConnection);
            updateMetadataTable(connection, genericConnection, componentProperties);
            if (modify) {
                try {
                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    if (genericConnectionItem != null && connectionItem != null) {
                        factory.create(genericConnectionItem, new Path(connectionItem.getState().getPath()), true);
                    }
                    return ExecutionResult.SUCCESS_WITH_ALERT;
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    protected Properties getPropertiesFromFile() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("NewSalesforceWizardMigrationTask.properties");//$NON-NLS-1$
        try {
            props.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    @Override
    protected boolean updateMetadataTable(Connection oldConnection, Connection genericConnection,
            ComponentProperties componentProperties) {
        boolean modified = false;
        if (oldConnection == null || genericConnection == null || componentProperties == null) {
            return modified;
        }
        Set<MetadataTable> tables = ConnectionHelper.getTables(oldConnection);
        Set<MetadataTable> newTables = new HashSet<>();
        newTables.addAll(tables);
        for (MetadataTable metaTable : newTables) {
            try {
                Object object = ReflectionUtils.newInstance(REFLECTION_SALESFORCE_MODULE_PROPERTIES, componentProperties
                        .getClass().getClassLoader(), new Object[] { metaTable.getName() });
                if (object != null && object instanceof ComponentProperties) {
                    ComponentProperties salesforceModuleProperties = (ComponentProperties) object;
                    TaggedValue serializedPropsTV = CoreFactory.eINSTANCE.createTaggedValue();
                    serializedPropsTV.setTag(IComponentConstants.COMPONENT_PROPERTIES_TAG);
                    serializedPropsTV.setValue(salesforceModuleProperties.toSerialized());
                    metaTable.getTaggedValue().add(serializedPropsTV);
                    TaggedValue schemaPropertyTV = CoreFactory.eINSTANCE.createTaggedValue();
                    schemaPropertyTV.setTag(IComponentConstants.COMPONENT_SCHEMA_TAG);
                    schemaPropertyTV.setValue(SCHEMA_SCHEMA);
                    metaTable.getTaggedValue().add(schemaPropertyTV);
                    Schema schema = SchemaUtils.convertTalendSchemaIntoComponentSchema(metaTable);
                    salesforceModuleProperties.setValue(SCHEMA_SCHEMA, schema);
                    ((orgomg.cwm.objectmodel.core.Package) genericConnection).getOwnedElement().add(metaTable);
                    modified = true;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return modified;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 4, 11, 12, 0, 0);
        return gc.getTime();
    }
}
